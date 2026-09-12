-- FameGo initial Supabase schema
-- Run this entire file once in Supabase Dashboard > SQL Editor.
-- Never put the database password or a service-role key in the Android app.

create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  email text not null default '',
  phone text not null default '',
  company_name text not null default '',
  role text not null default 'CLIENT' check (role in ('CLIENT', 'CREW', 'ADMIN')),
  avatar_initials text not null default 'FG',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.crew_profiles (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null unique references public.profiles(id) on delete cascade,
  city text not null default '',
  primary_role text not null check (primary_role in ('CINEMATOGRAPHER', 'VIDEOGRAPHER', 'PHOTOGRAPHER', 'DRONE_OPERATOR', 'EDITOR', 'ASSISTANT')),
  secondary_roles text[] not null default '{}',
  experience_years integer not null default 0 check (experience_years >= 0),
  bio text not null default '',
  gear_summary text not null default '',
  portfolio_url text not null default '',
  instagram_handle text not null default '',
  verification_status text not null default 'PENDING_VERIFICATION' check (verification_status in ('VERIFIED', 'PENDING_VERIFICATION', 'SUSPENDED')),
  is_available boolean not null default false,
  rating numeric(2,1) not null default 0 check (rating between 0 and 5),
  total_shoots_completed integer not null default 0 check (total_shoots_completed >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.saved_locations (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  label text not null,
  venue_name text not null,
  address text not null,
  created_at timestamptz not null default now()
);

create table if not exists public.bookings (
  id uuid primary key default gen_random_uuid(),
  booking_code text not null unique default ('FG-' || lpad((floor(random() * 10000))::int::text, 4, '0')),
  client_id uuid not null references public.profiles(id) on delete restrict,
  shoot_title text not null,
  category text not null check (category in ('VIDEO', 'FASHION', 'FOOD', 'PRODUCT', 'EVENT', 'PHOTOGRAPHY', 'CORPORATE')),
  shoot_date date not null,
  shoot_time time not null,
  duration_hours integer not null check (duration_hours between 1 and 24),
  venue_name text not null,
  full_address text not null,
  location_instructions text not null default '',
  crew_requirements jsonb not null default '[]'::jsonb,
  shoot_description text not null default '',
  special_instructions text not null default '',
  brand_name text not null default '',
  reference_link text not null default '',
  status text not null default 'SEARCHING_CREW' check (status in ('DRAFT', 'SEARCHING_CREW', 'CREW_RESPONDED', 'CONFIRMED', 'UPCOMING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.booking_assignments (
  id uuid primary key default gen_random_uuid(),
  booking_id uuid not null references public.bookings(id) on delete cascade,
  crew_id uuid not null references public.crew_profiles(id) on delete restrict,
  role text not null,
  name_snapshot text not null default '',
  phone_snapshot text not null default '',
  gear_snapshot text not null default '',
  rating_snapshot numeric(2,1) not null default 0,
  is_verified_snapshot boolean not null default false,
  created_at timestamptz not null default now(),
  unique (booking_id, crew_id)
);

create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  target_user_id uuid not null references public.profiles(id) on delete cascade,
  title text not null,
  message text not null,
  booking_id uuid references public.bookings(id) on delete cascade,
  is_read boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists public.chat_messages (
  id uuid primary key default gen_random_uuid(),
  booking_id uuid not null references public.bookings(id) on delete cascade,
  sender_id uuid not null references public.profiles(id) on delete cascade,
  message text not null check (length(trim(message)) > 0),
  created_at timestamptz not null default now()
);

create table if not exists public.support_messages (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  message text not null check (length(trim(message)) > 0),
  is_from_support boolean not null default false,
  created_at timestamptz not null default now()
);

create index if not exists bookings_client_id_idx on public.bookings(client_id);
create index if not exists bookings_status_idx on public.bookings(status);
create index if not exists bookings_date_idx on public.bookings(shoot_date);
create index if not exists assignments_crew_id_idx on public.booking_assignments(crew_id);
create index if not exists notifications_target_idx on public.notifications(target_user_id, is_read);
create index if not exists chat_booking_idx on public.chat_messages(booking_id, created_at);

drop trigger if exists profiles_updated_at on public.profiles;
create trigger profiles_updated_at before update on public.profiles
for each row execute function public.set_updated_at();
drop trigger if exists crew_profiles_updated_at on public.crew_profiles;
create trigger crew_profiles_updated_at before update on public.crew_profiles
for each row execute function public.set_updated_at();
drop trigger if exists bookings_updated_at on public.bookings;
create trigger bookings_updated_at before update on public.bookings
for each row execute function public.set_updated_at();

create or replace function public.is_admin()
returns boolean language sql stable security definer set search_path = public as $$
  select exists(select 1 from public.profiles where id = auth.uid() and role = 'ADMIN');
$$;

create or replace function public.is_crew()
returns boolean language sql stable security definer set search_path = public as $$
  select exists(select 1 from public.profiles where id = auth.uid() and role = 'CREW');
$$;

create or replace function public.is_client()
returns boolean language sql stable security definer set search_path = public as $$
  select exists(select 1 from public.profiles where id = auth.uid() and role = 'CLIENT');
$$;

create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
declare
  requested_role text := upper(coalesce(new.raw_user_meta_data ->> 'role', 'CLIENT'));
begin
  if lower(new.email) = 'famebros.studio@gmail.com' then requested_role := 'ADMIN'; end if;
  if requested_role not in ('CLIENT', 'CREW', 'ADMIN') then requested_role := 'CLIENT'; end if;
  insert into public.profiles (id, full_name, email, phone, company_name, role, avatar_initials)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', ''),
    coalesce(new.email, ''),
    coalesce(new.raw_user_meta_data ->> 'phone', ''),
    coalesce(new.raw_user_meta_data ->> 'company_name', ''),
    requested_role,
    upper(left(regexp_replace(coalesce(new.raw_user_meta_data ->> 'full_name', 'FG'), '[^A-Za-z]', '', 'g'), 2))
  ) on conflict (id) do update set email = excluded.email;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created after insert on auth.users
for each row execute function public.handle_new_user();

alter table public.profiles enable row level security;
alter table public.crew_profiles enable row level security;
alter table public.saved_locations enable row level security;
alter table public.bookings enable row level security;
alter table public.booking_assignments enable row level security;
alter table public.notifications enable row level security;
alter table public.chat_messages enable row level security;
alter table public.support_messages enable row level security;

create policy profiles_select on public.profiles for select to authenticated using (id = auth.uid() or public.is_admin());
create policy profiles_insert on public.profiles for insert to authenticated with check (id = auth.uid());
create policy profiles_update on public.profiles for update to authenticated using (id = auth.uid() or public.is_admin()) with check (id = auth.uid() or public.is_admin());

create policy crew_profiles_select on public.crew_profiles for select to authenticated using (true);
create policy crew_profiles_insert on public.crew_profiles for insert to authenticated with check (user_id = auth.uid() and public.is_crew());
create policy crew_profiles_update on public.crew_profiles for update to authenticated using (user_id = auth.uid() or public.is_admin()) with check (user_id = auth.uid() or public.is_admin());

create policy saved_locations_owner on public.saved_locations for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy bookings_select on public.bookings for select to authenticated
using (client_id = auth.uid() or public.is_admin() or public.is_crew());
create policy bookings_client_insert on public.bookings for insert to authenticated
with check (client_id = auth.uid() and public.is_client());
create policy bookings_client_update on public.bookings for update to authenticated
using (client_id = auth.uid() or public.is_admin())
with check ((client_id = auth.uid() and public.is_client()) or public.is_admin());
create policy bookings_client_delete on public.bookings for delete to authenticated
using (client_id = auth.uid() or public.is_admin());

create policy assignments_select on public.booking_assignments for select to authenticated
using (public.is_admin() or exists (select 1 from public.bookings b where b.id = booking_id and b.client_id = auth.uid()) or exists (select 1 from public.crew_profiles c where c.id = crew_id and c.user_id = auth.uid()));
create policy assignments_admin_insert on public.booking_assignments for insert to authenticated with check (public.is_admin());
create policy assignments_crew_insert on public.booking_assignments for insert to authenticated
with check (public.is_crew() and exists (select 1 from public.crew_profiles c where c.id = crew_id and c.user_id = auth.uid()));
create policy assignments_admin_delete on public.booking_assignments for delete to authenticated using (public.is_admin());

create policy notifications_select on public.notifications for select to authenticated using (target_user_id = auth.uid() or public.is_admin());
create policy notifications_update on public.notifications for update to authenticated using (target_user_id = auth.uid()) with check (target_user_id = auth.uid());
create policy notifications_admin_insert on public.notifications for insert to authenticated with check (public.is_admin());

create policy chat_select on public.chat_messages for select to authenticated using (
  public.is_admin() or sender_id = auth.uid() or exists (select 1 from public.bookings b where b.id = booking_id and b.client_id = auth.uid()) or exists (select 1 from public.booking_assignments a join public.crew_profiles c on c.id = a.crew_id where a.booking_id = chat_messages.booking_id and c.user_id = auth.uid())
);
create policy chat_insert on public.chat_messages for insert to authenticated with check (sender_id = auth.uid());

create policy support_owner on public.support_messages for select to authenticated using (user_id = auth.uid() or public.is_admin());
create policy support_insert on public.support_messages for insert to authenticated with check (user_id = auth.uid());
create policy support_admin_update on public.support_messages for update to authenticated using (public.is_admin()) with check (public.is_admin());

grant usage on schema public to authenticated;
grant select, insert, update, delete on all tables in schema public to authenticated;
