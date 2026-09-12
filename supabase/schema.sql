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
  plan_code text not null default 'BRONZE_3H' check (plan_code in ('BRONZE_90', 'BRONZE_3H', 'BRONZE_6H')),
  plan_name text not null default 'Bronze 3 Hour',
  plan_price_paise integer not null default 299900 check (plan_price_paise >= 0),
  payment_status text not null default 'PENDING' check (payment_status in ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
  payment_reference text not null default '',
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

-- Upgrade existing projects without requiring a destructive rebuild.
alter table public.bookings add column if not exists plan_code text not null default 'BRONZE_3H';
alter table public.bookings add column if not exists plan_name text not null default 'Bronze 3 Hour';
alter table public.bookings add column if not exists plan_price_paise integer not null default 299900;
alter table public.bookings add column if not exists payment_status text not null default 'PENDING';
alter table public.bookings add column if not exists payment_reference text not null default '';

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
  read_at timestamptz,
  created_at timestamptz not null default now()
);

create table if not exists public.favorite_crew (
  client_id uuid not null references public.profiles(id) on delete cascade,
  crew_id uuid not null references public.crew_profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (client_id, crew_id)
);

create table if not exists public.crew_ratings (
  id uuid primary key default gen_random_uuid(),
  booking_id uuid not null references public.bookings(id) on delete cascade,
  client_id uuid not null references public.profiles(id) on delete cascade,
  crew_id uuid not null references public.crew_profiles(id) on delete cascade,
  stars smallint not null check (stars between 1 and 5),
  review text not null default '',
  created_at timestamptz not null default now(),
  unique (booking_id, client_id, crew_id)
);

create table if not exists public.crew_live_locations (
  crew_id uuid primary key references public.crew_profiles(id) on delete cascade,
  booking_id uuid references public.bookings(id) on delete cascade,
  latitude double precision not null check (latitude between -90 and 90),
  longitude double precision not null check (longitude between -180 and 180),
  sharing_enabled boolean not null default false,
  recorded_at timestamptz not null default now()
);

create table if not exists public.device_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  token text not null unique,
  platform text not null default 'android' check (platform = 'android'),
  updated_at timestamptz not null default now()
);

create table if not exists public.support_messages (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  message text not null check (length(trim(message)) > 0),
  is_from_support boolean not null default false,
  created_at timestamptz not null default now()
);

alter table public.chat_messages add column if not exists read_at timestamptz;

create index if not exists bookings_client_id_idx on public.bookings(client_id);
create index if not exists bookings_status_idx on public.bookings(status);
create index if not exists bookings_date_idx on public.bookings(shoot_date);
create index if not exists assignments_crew_id_idx on public.booking_assignments(crew_id);
create index if not exists notifications_target_idx on public.notifications(target_user_id, is_read);
create index if not exists chat_booking_idx on public.chat_messages(booking_id, created_at);
create index if not exists ratings_crew_idx on public.crew_ratings(crew_id, created_at desc);
create index if not exists live_locations_booking_idx on public.crew_live_locations(booking_id) where sharing_enabled;
create index if not exists device_tokens_user_idx on public.device_tokens(user_id);

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
  requested_role text := 'CLIENT';
begin
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
alter table public.favorite_crew enable row level security;
alter table public.crew_ratings enable row level security;
alter table public.crew_live_locations enable row level security;
alter table public.device_tokens enable row level security;

create policy profiles_select on public.profiles for select to authenticated using (id = auth.uid() or public.is_admin());
create policy profiles_insert on public.profiles for insert to authenticated with check (id = auth.uid());
create policy profiles_update on public.profiles for update to authenticated using (id = auth.uid() or public.is_admin()) with check (id = auth.uid() or public.is_admin());

create policy crew_profiles_select on public.crew_profiles for select to authenticated using (true);
create policy crew_profiles_insert on public.crew_profiles for insert to authenticated with check (user_id = auth.uid() and public.is_crew());
create policy crew_profiles_update on public.crew_profiles for update to authenticated using (user_id = auth.uid() or public.is_admin()) with check (user_id = auth.uid() or public.is_admin());

create policy saved_locations_owner on public.saved_locations for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy bookings_select on public.bookings for select to authenticated
using (
  client_id = (select auth.uid()) or public.is_admin() or
  (public.is_crew() and payment_status = 'PAID' and status = 'SEARCHING_CREW') or
  exists (
    select 1 from public.booking_assignments a
    join public.crew_profiles c on c.id = a.crew_id
    where a.booking_id = bookings.id and c.user_id = (select auth.uid())
  )
);
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
create policy chat_insert on public.chat_messages for insert to authenticated with check (
  sender_id = (select auth.uid()) and (
    exists (select 1 from public.bookings b where b.id = booking_id and b.client_id = (select auth.uid())) or
    exists (
      select 1 from public.booking_assignments a
      join public.crew_profiles c on c.id = a.crew_id
      where a.booking_id = chat_messages.booking_id and c.user_id = (select auth.uid())
    )
  )
);

create policy support_owner on public.support_messages for select to authenticated using (user_id = auth.uid() or public.is_admin());
create policy support_insert on public.support_messages for insert to authenticated with check (user_id = auth.uid());
create policy support_admin_update on public.support_messages for update to authenticated using (public.is_admin()) with check (public.is_admin());

create policy favorites_owner on public.favorite_crew for all to authenticated
using (client_id = (select auth.uid())) with check (client_id = (select auth.uid()) and public.is_client());

create policy ratings_participants_select on public.crew_ratings for select to authenticated using (
  client_id = (select auth.uid()) or public.is_admin() or
  exists (select 1 from public.crew_profiles c where c.id = crew_id and c.user_id = (select auth.uid()))
);
create policy ratings_client_insert on public.crew_ratings for insert to authenticated with check (
  client_id = (select auth.uid()) and exists (
    select 1 from public.bookings b
    join public.booking_assignments a on a.booking_id = b.id
    where b.id = booking_id and b.client_id = (select auth.uid()) and b.status = 'COMPLETED' and a.crew_id = crew_id
  )
);

create policy live_location_participants_select on public.crew_live_locations for select to authenticated using (
  public.is_admin() or exists (
    select 1 from public.booking_assignments a
    join public.bookings b on b.id = a.booking_id
    join public.crew_profiles c on c.id = a.crew_id
    where a.booking_id = crew_live_locations.booking_id
      and (b.client_id = (select auth.uid()) or c.user_id = (select auth.uid()))
  )
);
create policy live_location_crew_write on public.crew_live_locations for all to authenticated using (
  exists (select 1 from public.crew_profiles c where c.id = crew_id and c.user_id = (select auth.uid()))
) with check (
  exists (select 1 from public.crew_profiles c where c.id = crew_id and c.user_id = (select auth.uid()))
);

create policy device_tokens_owner on public.device_tokens for all to authenticated
using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

create or replace function public.accept_booking(p_booking_id uuid)
returns public.booking_assignments
language plpgsql security definer set search_path = '' as $$
declare
  v_crew public.crew_profiles;
  v_booking public.bookings;
  v_profile public.profiles;
  v_assignment public.booking_assignments;
begin
  select * into v_crew from public.crew_profiles where user_id = auth.uid() and is_available for update;
  if v_crew.id is null then raise exception 'crew_not_available'; end if;
  select * into v_booking from public.bookings where id = p_booking_id for update;
  if v_booking.id is null or v_booking.status <> 'SEARCHING_CREW' or v_booking.payment_status <> 'PAID' then
    raise exception 'booking_not_available';
  end if;
  select * into v_profile from public.profiles where id = auth.uid();
  insert into public.booking_assignments (
    booking_id, crew_id, role, name_snapshot, phone_snapshot, gear_snapshot, rating_snapshot, is_verified_snapshot
  ) values (
    v_booking.id, v_crew.id, v_crew.primary_role, v_profile.full_name, v_profile.phone,
    v_crew.gear_summary, v_crew.rating, v_crew.verification_status = 'VERIFIED'
  ) returning * into v_assignment;
  update public.bookings set status = 'CONFIRMED' where id = v_booking.id;
  update public.crew_profiles set is_available = false where id = v_crew.id;
  return v_assignment;
end;
$$;

revoke all on function public.is_admin() from public, anon;
revoke all on function public.is_crew() from public, anon;
revoke all on function public.is_client() from public, anon;
revoke all on function public.accept_booking(uuid) from public, anon;
grant execute on function public.is_admin(), public.is_crew(), public.is_client(), public.accept_booking(uuid) to authenticated;

grant usage on schema public to authenticated;
grant select, insert, update, delete on all tables in schema public to authenticated;
