// FameGo site — vanilla interaction engine. No framework, no build.
// Every module guards its own DOM: safe on all pages, calm on reduced-motion.
(function () {
  "use strict";

  // Failsafe first: never trap a visitor behind the loader or hidden content.
  window.addEventListener("error", function () {
    try {
      document.body.classList.remove("loading");
      var l = document.getElementById("loader");
      if (l) l.style.display = "none";
      document.querySelectorAll(".rv").forEach(function (e) { e.classList.add("in"); });
    } catch (_) { /* stay visible */ }
  });

  var fine = window.matchMedia("(pointer: fine)").matches;
  var calm = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var wide = window.matchMedia("(min-width: 1021px)").matches;
  var isIndex = !!document.getElementById("loader");

  // Slate preloader (index only) — snap, count, lift. Failsafe included.
  (function loader() {
    var l = document.getElementById("loader");
    if (!l) { document.body.classList.remove("loading"); return; }
    if (calm) { l.style.display = "none"; document.body.classList.remove("loading"); return; }
    var n = document.getElementById("lcount"), bar = document.getElementById("lbar"), done = false;
    function finish() {
      if (done) return; done = true;
      l.classList.add("done");
      document.body.classList.remove("loading");
      setTimeout(function () { l.style.display = "none"; }, 900);
    }
    var t0 = null;
    function frame(t) {
      if (done) return;
      if (!t0) t0 = t;
      var p = Math.min(1, (t - t0) / 900);
      if (n) n.textContent = "TAKE " + String(Math.round(1 + p * 2));
      if (bar) bar.style.width = (p * 100).toFixed(1) + "%";
      if (p < 1) requestAnimationFrame(frame); else setTimeout(finish, 150);
    }
    requestAnimationFrame(frame);
    setTimeout(finish, 2600);
  })();

  // Footer year
  var y = document.getElementById("year");
  if (y) y.textContent = String(new Date().getFullYear());

  // Nav: shrink on scroll + full-screen mobile menu
  var topbar = document.getElementById("topbar");
  var menuBtn = document.getElementById("menuBtn");
  var mmenu = document.getElementById("mmenu");
  if (menuBtn && mmenu) {
    var links = mmenu.querySelectorAll("a");
    links.forEach(function (a, i) { a.style.transitionDelay = (0.06 * i + 0.1) + "s"; });
    menuBtn.addEventListener("click", function () {
      var open = mmenu.classList.toggle("open");
      menuBtn.textContent = open ? "✕" : "☰";
      document.body.classList.toggle("menu-open", open);
    });
    links.forEach(function (a) {
      a.addEventListener("click", function () {
        mmenu.classList.remove("open");
        menuBtn.textContent = "☰";
        document.body.classList.remove("menu-open");
      });
    });
  }

  // Masked line reveals for display headings — the scroll-down signature
  (function linereveal() {
    var heads = document.querySelectorAll("h2.disp");
    if (!heads.length) return;
    heads.forEach(function (h) {
      var parts = h.innerHTML.split(/<br\s*\/?>/i);
      if (parts.length < 2) return; // single-line heads keep the plain rise
      h.classList.add("rl");
      h.innerHTML = parts.map(function (p) {
        return '<span class="rl-line"><span>' + p + "</span></span>";
      }).join("");
      h.querySelectorAll(".rl-line > span").forEach(function (s, i) {
        s.style.transitionDelay = (i * 0.1) + "s";
      });
    });
    var lines = document.querySelectorAll("h2.disp.rl");
    if (calm || !("IntersectionObserver" in window)) {
      lines.forEach(function (h) { h.classList.add("in"); });
      return;
    }
    var o = new IntersectionObserver(function (es) {
      es.forEach(function (en) {
        if (en.isIntersecting) { en.target.classList.add("in"); o.unobserve(en.target); }
      });
    }, { threshold: 0.4 });
    lines.forEach(function (h) { o.observe(h); });
  })();
  var els = document.querySelectorAll(".rv");
  if ("IntersectionObserver" in window && els.length) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) { en.target.classList.add("in"); io.unobserve(en.target); }
      });
    }, { threshold: 0.12, rootMargin: "0px 0px -6% 0px" });
    els.forEach(function (el, i) {
      el.style.transitionDelay = Math.min(i % 3, 2) * 70 + "ms";
      io.observe(el);
    });
  } else {
    els.forEach(function (el) { el.classList.add("in"); });
  }

  // Count-up stats (supports data-dec for decimals)
  (function counters() {
    var list = document.querySelectorAll("[data-count]");
    function render(el, v) {
      var pre = el.getAttribute("data-prefix") || "", suf = el.getAttribute("data-suffix") || "";
      var dec = parseInt(el.getAttribute("data-dec") || "0", 10);
      el.textContent = pre + (dec ? v.toFixed(dec) : Math.round(v).toLocaleString("en-IN")) + suf;
    }
    if (!list.length) return;
    if (calm || !("IntersectionObserver" in window)) {
      list.forEach(function (el) { render(el, parseFloat(el.getAttribute("data-count"))); });
      return;
    }
    var cio = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (!en.isIntersecting) return;
        cio.unobserve(en.target);
        var el = en.target, target = parseFloat(el.getAttribute("data-count")) || 0, t0 = null;
        (function frame(t) {
          if (!t0) t0 = t;
          var p = Math.min(1, (t - t0) / 1200), e = 1 - Math.pow(1 - p, 3);
          render(el, target * e);
          if (p < 1) requestAnimationFrame(frame);
        })(performance.now());
      });
    }, { threshold: 0.6 });
    list.forEach(function (el) { cio.observe(el); });
  })();

  // Hero clock (Mumbai time, HH:MM)
  (function clock() {
    var c = document.getElementById("heroClock");
    if (!c) return;
    try {
      c.textContent = new Intl.DateTimeFormat("en-IN", { hour: "2-digit", minute: "2-digit", hour12: false, timeZone: "Asia/Kolkata" }).format(new Date()) + " IST";
    } catch (_) { c.textContent = "MUMBAI"; }
  })();

  // HERO desk: looping live-booking playback
  (function desk() {
    var box = document.getElementById("deskStatus");
    if (!box || calm) { if (box) box.querySelectorAll("div").forEach(function (d) { d.classList.add("show"); }); return; }
    var lines = Array.prototype.slice.call(box.querySelectorAll("div"));
    var bar = document.getElementById("deskBar"), i = 0;
    function play() {
      lines.forEach(function (d) { d.classList.remove("show"); });
      if (bar) bar.style.width = "0";
      i = 0;
      (function next() {
        if (i >= lines.length) { setTimeout(play, 3600); return; }
        lines[i].classList.add("show");
        if (bar) bar.style.width = ((i + 1) / lines.length * 100) + "%";
        i++;
        setTimeout(next, i === lines.length ? 2200 : 950);
      })();
    }
    var o = new IntersectionObserver(function (es) {
      es.forEach(function (en) { if (en.isIntersecting) { o.disconnect(); play(); } });
    }, { threshold: 0.3 });
    o.observe(box);
  })();

  // Interactive demo: 4-question booking sim
  (function demo() {
    var body = document.getElementById("demoBody");
    if (!body) return;
    var stepEl = document.getElementById("demoStep"), dots = document.getElementById("demoDots"),
        back = document.getElementById("demoBack");
    var state = {}, order = ["what", "when", "where", "who"], at = 0;
    var QS = {
      what: { q: "What are you shooting?", opts: [["Fashion", "editorial · lookbook"], ["Food", "menu · café"], ["Product", "e-com · brand"], ["Reel", "90-sec vertical"], ["Event", "live · stage"], ["Corporate", "interview · office"]] },
      when: { q: "When's call time?", opts: [["Today", "crew within hours"], ["Tomorrow", "morning slots"], ["This weekend", "prime time"], ["Pick a date", "plan ahead"]] },
      where: { q: "Where are we shooting?", opts: [["Bandra West", "2.1 km · 14 crew"], ["Andheri", "3.8 km · 22 crew"], ["Colaba", "5.4 km · 9 crew"], ["Juhu", "6.0 km · 11 crew"]] },
      who: { q: "Who do you need?", opts: [["Videographer", "A7S III + gimbal"], ["Photographer", "R5 · lights"], ["Cinematographer", "FX6 · primes"], ["Drone Op.", "Mavic 3 Pro"], ["Editor", "Resolve · fast"], ["Assistant", "grip + logistics"]] }
    };
    function paintDots() {
      if (!dots) return;
      Array.prototype.forEach.call(dots.children, function (d, i) { d.classList.toggle("on", i <= Math.min(at, 3)); });
    }
    function ask() {
      var k = order[at], q = QS[k];
      if (stepEl) stepEl.textContent = "STEP " + (at + 1) + " OF 4";
      if (back) back.hidden = at === 0;
      paintDots();
      var h = '<div class="demo-q">' + q.q + "</div><div class='demo-opts'>";
      q.opts.forEach(function (o, i) {
        h += "<button class='opt' data-i='" + i + "'>" + o[0] + "<small>" + o[1] + "</small></button>";
      });
      body.innerHTML = h + "</div>";
      Array.prototype.forEach.call(body.querySelectorAll(".opt"), function (b) {
        b.addEventListener("click", function () {
          b.classList.add("picked");
          state[k] = QS[k].opts[parseInt(b.getAttribute("data-i"), 10)][0];
          setTimeout(function () { at++; at < 4 ? ask() : searching(); }, 220);
        });
      });
    }
    function searching() {
      if (stepEl) stepEl.textContent = "MATCHING · FG-" + (4800 + Math.floor(Math.random() * 99));
      if (back) back.hidden = true;
      paintDots();
      body.innerHTML = "<div class='demo-q'>Finding verified crew…</div><div class='demo-opts'><div class='desk-status' style='min-height:0'>" +
        "<div class='show'>Checking availability…</div><div class='show gold'>2 crew free within 5 km</div>" +
        "<div class='show ok'>Gear match ✓ · ratings ✓</div></div></div>";
      setTimeout(found, calm ? 100 : 1600);
    }
    function found() {
      body.innerHTML = "<div class='demo-result show'><div class='found'>Crew found <span>✓</span></div>" +
        "<p style='color:var(--muted);margin-top:10px'>" + state.what + " · " + state.when + " · " + state.where + " · " + state.who + "</p>" +
        "<div class='demo-crew'><div class='avatar'>H</div><div><h4>Huzaifa <small>· VERIFIED</small><span class='wave'><i></i><i></i><i></i><i></i><i></i></span></h4>" +
        "<p>Cinematographer · 4.9 ★ · 126 shoots · accepts in ~40 sec</p></div></div>" +
        "<div class='cta-row' style='margin-top:24px'><a class='btn' href='#download'>Get the app to book</a>" +
        "<button class='btn ghost' id='demoAgain'>Run it back</button></div></div>";
      var ag = document.getElementById("demoAgain");
      if (ag) ag.addEventListener("click", function () { at = 0; state = {}; ask(); });
    }
    if (back) back.addEventListener("click", function () { if (at > 0) { at--; ask(); } });
    ask();
  })();

  // Sticky phone journey: swap screens as steps pass
  (function journey() {
    var steps = document.getElementById("jSteps"), scr = document.getElementById("jScreen");
    if (!steps || !scr || !("IntersectionObserver" in window)) return;
    var panes = Array.prototype.slice.call(scr.querySelectorAll(".scr"));
    function show(n) {
      panes.forEach(function (p) { p.classList.toggle("on", p.getAttribute("data-s") === String(n)); });
    }
    var o = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) show(en.target.getAttribute("data-s"));
      });
    }, { threshold: 0.55 });
    steps.querySelectorAll(".j-step").forEach(function (s) { o.observe(s); });
  })();

  // Scroll-driven 3D phone — banks, straightens and rises as the journey scrolls
  (function phone3d() {
    var scene = document.getElementById("p3d");
    if (!scene || calm) return;
    var journey = scene.closest ? (scene.closest(".journey") || scene.parentElement) : scene.parentElement;
    function clamp(v, a, b) { return Math.max(a, Math.min(b, v)); }
    var crx = 10, cry = -26, cdy = 0, mrx = 0, mry = 0, hidden = false;
    document.addEventListener("visibilitychange", function () { hidden = document.hidden; });
    if (fine) {
      document.addEventListener("pointermove", function (e) {
        var r = scene.getBoundingClientRect();
        if (!r.width) return;
        mry = clamp((e.clientX - (r.left + r.width / 2)) / r.width, -1, 1) * 5;
        mrx = clamp(-(e.clientY - (r.top + r.height / 2)) / r.height, -1, 1) * 4;
      }, { passive: true });
    }
    (function frame() {
      requestAnimationFrame(frame);
      if (hidden) return;
      var r = journey.getBoundingClientRect(), vh = window.innerHeight;
      var p = clamp((vh * 0.72 - r.top) / (r.height || 1), 0, 1);
      var trx = 10 - 15 * p;     // tips back, then faces you
      var tryy = -26 + 46 * p;   // shows its edge, then turns front-on
      var tdy = (0.5 - p) * -70; // rises as you travel
      crx += (trx + mrx - crx) * 0.08;
      cry += (tryy + mry - cry) * 0.08;
      cdy += (tdy - cdy) * 0.08;
      scene.style.transform = "translate3d(0," + cdy.toFixed(1) + "px,0) rotateX(" +
        crx.toFixed(2) + "deg) rotateY(" + cry.toFixed(2) + "deg)";
    })();
  })();

  // Split converge: halves meet at MATCHED (desktop pin)
  var splitWrap = document.getElementById("split");
  var halfL = document.getElementById("halfL"), halfR = document.getElementById("halfR"),
      badge = document.getElementById("matchBadge");
  var splitPinned = false;
  if (splitWrap && halfL && wide && fine && !calm) {
    splitWrap.classList.add("pinned");
    splitPinned = true;
  } else if (badge) {
    badge.style.position = "static"; badge.style.transform = "none";
    badge.style.opacity = "1"; badge.style.margin = "0 auto"; badge.style.display = "block";
    badge.style.width = "max-content";
  }

  // Map matching sequence
  (function mapmatch() {
    var log = document.getElementById("mapLog");
    if (!log) return;
    var lines = Array.prototype.slice.call(log.querySelectorAll("div"));
    var dots = Array.prototype.slice.call(document.querySelectorAll("#mapSvg .dotc"));
    var card = document.getElementById("matchCard"), played = false;
    function play() {
      if (played) return; played = true;
      var i = 0;
      (function next() {
        if (i < lines.length) {
          lines[i].classList.add("show");
          if (dots[i - 1]) dots[i - 1].classList.add("here");
          i++;
          setTimeout(next, 700);
        } else if (card) card.classList.add("show");
      })();
    }
    if (calm) {
      lines.forEach(function (d) { d.classList.add("show"); });
      dots.forEach(function (d) { d.classList.add("here"); });
      if (card) card.classList.add("show");
      return;
    }
    if (!("IntersectionObserver" in window)) { play(); return; }
    var o = new IntersectionObserver(function (es) {
      es.forEach(function (en) { if (en.isIntersecting) { o.disconnect(); play(); } });
    }, { threshold: 0.35 });
    o.observe(log);
  })();

  // Verification stamps
  (function stamps() {
    var box = document.getElementById("stamps");
    if (!box) return;
    var items = Array.prototype.slice.call(box.querySelectorAll(".stamp"));
    if (calm || !("IntersectionObserver" in window)) {
      items.forEach(function (s) { s.classList.add("stamped"); });
      return;
    }
    var o = new IntersectionObserver(function (es) {
      es.forEach(function (en) {
        if (!en.isIntersecting) return;
        o.disconnect();
        items.forEach(function (s, i) { setTimeout(function () { s.classList.add("stamped"); }, i * 260); });
      });
    }, { threshold: 0.35 });
    o.observe(box);
  })();

  // Gallery shutter flash on first view
  (function shutter() {
    var sheet = document.getElementById("sheet"), flash = document.getElementById("flash");
    if (!sheet || !flash || calm || !("IntersectionObserver" in window)) return;
    var o = new IntersectionObserver(function (es) {
      es.forEach(function (en) {
        if (!en.isIntersecting) return;
        o.disconnect();
        flash.classList.remove("go");
        void flash.offsetWidth;
        flash.classList.add("go");
      });
    }, { threshold: 0.3 });
    o.observe(sheet);
  })();

  if (!fine || calm) return; // the rest is pointer-play for desktops

  // Cursor-following aura
  var glow = document.createElement("div");
  glow.className = "cursor-glow";
  glow.setAttribute("aria-hidden", "true");
  document.body.appendChild(glow);
  var tx = -600, ty = -600, gx = tx, gy = ty, lit = false;
  window.addEventListener("pointermove", function (e) {
    tx = e.clientX; ty = e.clientY;
    if (!lit) { lit = true; document.body.classList.add("glow-on"); }
  }, { passive: true });
  (function loop() {
    gx += (tx - gx) * 0.08; gy += (ty - gy) * 0.08;
    glow.style.transform = "translate(" + gx.toFixed(1) + "px," + gy.toFixed(1) + "px)";
    requestAnimationFrame(loop);
  })();

  // Card spotlight + custom cursor (index only)
  document.addEventListener("pointermove", function (e) {
    var t = e.target.closest ? e.target.closest(".card,.phone,.tier,.shot,.opt") : null;
    if (!t) return;
    var r = t.getBoundingClientRect();
    t.style.setProperty("--mx", (e.clientX - r.left).toFixed(1) + "px");
    t.style.setProperty("--my", (e.clientY - r.top).toFixed(1) + "px");
  }, { passive: true });

  if (isIndex) {
    var dot = document.getElementById("curDot"), ring = document.getElementById("curRing");
    var cx = -100, cy = -100, rx2 = cx, ry2 = cy, curOn = false;
    document.addEventListener("pointermove", function (e) {
      cx = e.clientX; cy = e.clientY;
      if (!curOn) { curOn = true; document.body.classList.add("cur-on"); }
      if (dot) dot.style.transform = "translate(" + cx + "px," + cy + "px)";
    }, { passive: true });
    (function cur() {
      rx2 += (cx - rx2) * 0.16; ry2 += (cy - ry2) * 0.16;
      if (ring) ring.style.transform = "translate(" + rx2.toFixed(1) + "px," + ry2.toFixed(1) + "px)";
      requestAnimationFrame(cur);
    })();
    document.addEventListener("mouseover", function (e) {
      if (!ring || !e.target.closest) return;
      var t = e.target.closest("a,button,summary,.tier,.shot,.opt");
      ring.classList.toggle("big", !!t);
      ring.classList.toggle("focus", !!(t && e.target.closest(".sheet,.shot")));
    });
  }

  // Magnetic CTAs + hero headline lean
  var heroH = document.getElementById("heroH");
  document.querySelectorAll(".mag").forEach(function (b) {
    b.addEventListener("pointermove", function (e) {
      var r = b.getBoundingClientRect();
      var x = (e.clientX - (r.left + r.width / 2)) / r.width;
      var y = (e.clientY - (r.top + r.height / 2)) / r.height;
      b.style.transform = "translate(" + (x * 10).toFixed(1) + "px," + (y * 8).toFixed(1) + "px)";
    });
    b.addEventListener("pointerleave", function () { b.style.transform = ""; });
  });
  if (heroH) {
    document.addEventListener("pointermove", function (e) {
      var x = (e.clientX / window.innerWidth - 0.5) * 10;
      var y = (e.clientY / window.innerHeight - 0.5) * 6;
      heroH.style.translate = x.toFixed(1) + "px " + y.toFixed(1) + "px";
    }, { passive: true });
  }

  // Gold-dust field
  (function dust() {
    var cv = document.getElementById("dust");
    if (!cv || !cv.getContext) return;
    var ctx = cv.getContext("2d"), W = 0, H = 0, P = [];
    var mx = -9999, my = -9999;
    function size() {
      W = cv.width = window.innerWidth; H = cv.height = window.innerHeight;
      var n = Math.min(80, Math.floor(W * H / 26000));
      P = [];
      for (var i = 0; i < n; i++) P.push({
        x: Math.random() * W, y: Math.random() * H,
        r: 0.6 + Math.random() * 1.4, s: 0.08 + Math.random() * 0.26,
        a: 0.12 + Math.random() * 0.35, ph: Math.random() * Math.PI * 2
      });
    }
    size();
    window.addEventListener("resize", size);
    window.addEventListener("pointermove", function (e) { mx = e.clientX; my = e.clientY; }, { passive: true });
    var t = 0, hidden = false;
    document.addEventListener("visibilitychange", function () { hidden = document.hidden; });
    (function frame() {
      requestAnimationFrame(frame);
      if (hidden) return;
      t += 0.008;
      ctx.clearRect(0, 0, W, H);
      for (var i = 0; i < P.length; i++) {
        var p = P[i];
        p.y -= p.s; p.x += Math.sin(t * 2 + p.ph) * 0.08;
        if (p.y < -6) { p.y = H + 6; p.x = Math.random() * W; }
        var dx = p.x - mx, dy = p.y - my, d = Math.sqrt(dx * dx + dy * dy);
        var push = d < 130 ? (130 - d) / 130 * 0.6 : 0;
        var tw = p.a * (0.65 + 0.35 * Math.sin(t * 3 + p.ph));
        ctx.beginPath();
        ctx.arc(p.x + (d ? dx / d * push * 8 : 0), p.y + (d ? dy / d * push * 8 : 0), p.r, 0, 6.283);
        ctx.fillStyle = "rgba(246,185,65," + tw.toFixed(3) + ")";
        ctx.fill();
      }
    })();
  })();

  // Scroll engine: progress, parallax layers, split converge
  var layers = Array.prototype.slice.call(document.querySelectorAll("[data-plx],[data-plx-x]"));
  var sfill = document.getElementById("scrollbarFill");
  var cue = document.getElementById("scrollCue");
  var ticking = false, lastY = window.scrollY, vel = 0, sVel = 0;
  function render() {
    ticking = false;
    var vh = window.innerHeight, y = window.scrollY;
    vel = y - lastY; lastY = y;
    sVel += (vel - sVel) * 0.12;
    if (topbar) topbar.classList.toggle("scrolled", y > 40);
    if (cue) cue.classList.toggle("hide", y > 120);
    if (sfill) {
      var max = document.documentElement.scrollHeight - vh;
      sfill.style.transform = "scaleX(" + (max > 0 ? Math.max(0, Math.min(1, y / max)).toFixed(3) : 0) + ")";
    }
    for (var i = 0; i < layers.length; i++) {
      var el = layers[i], r = el.getBoundingClientRect();
      if (r.bottom < -200 || r.top > vh + 200) continue;
      var c = r.top + r.height / 2 - vh / 2;
      var sy = parseFloat(el.getAttribute("data-plx")) || 0;
      var sx = parseFloat(el.getAttribute("data-plx-x")) || 0;
      el.style.translate = (sx ? (-c * sx).toFixed(1) + "px" : "0px") + " " + (sy ? (-c * sy).toFixed(1) + "px" : "0px");
    }
    if (splitPinned) {
      var wr = splitWrap.getBoundingClientRect();
      var total = splitWrap.offsetHeight - vh;
      var p = Math.max(0, Math.min(1, -wr.top / total));
      var shift = Math.min(1, p * 1.6);
      halfL.style.transform = "translateX(" + (shift * 22).toFixed(1) + "%)";
      halfR.style.transform = "translateX(" + (-shift * 22).toFixed(1) + "%)";
      var b = Math.max(0, Math.min(1, (p - 0.55) / 0.3));
      badge.style.setProperty("--mb", b.toFixed(3));
      badge.style.setProperty("--ms", (0.4 + b * 0.6).toFixed(3));
    }
  }
  function onScroll() {
    if (!ticking) { ticking = true; requestAnimationFrame(render); }
  }
  window.addEventListener("scroll", onScroll, { passive: true });
  window.addEventListener("resize", onScroll);
  render();

  // Deep-link to app
  window.openFameGoApp = function () {
    window.location.href = "famego://auth/callback";
  };
})();
