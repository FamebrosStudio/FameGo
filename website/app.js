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

  // Slate preloader — snap, count, lift. Failsafe included.
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
      if (n) n.textContent = p < 0.34 ? "ROLLING…" : p < 0.67 ? "CHECKING CREW…" : "CALL SHEET UP";
      if (bar) bar.style.width = (p * 100).toFixed(1) + "%";
      if (p < 1) requestAnimationFrame(frame); else setTimeout(finish, 150);
    }
    requestAnimationFrame(frame);
    setTimeout(finish, 2600);
  })();

  // Footer year
  var y = document.getElementById("year");
  if (y) y.textContent = String(new Date().getFullYear());

  // Nav: shrink on scroll + full-screen mobile menu (button stays reachable).
  // Sub-pages reuse the classic dropdown instead.
  var topbar = document.getElementById("topbar");
  var menuBtn = document.getElementById("menuBtn");
  var mmenu = document.getElementById("mmenu");
  var siteNav = document.getElementById("siteNav");
  if (menuBtn && !mmenu && siteNav) {
    menuBtn.addEventListener("click", function () {
      var open = siteNav.classList.toggle("open");
      menuBtn.textContent = open ? "✕" : "☰";
      menuBtn.setAttribute("aria-expanded", open ? "true" : "false");
    });
  }
  if (menuBtn && mmenu) {
    var links = mmenu.querySelectorAll("a");
    links.forEach(function (a, i) { a.style.transitionDelay = (0.06 * i + 0.1) + "s"; });
    menuBtn.addEventListener("click", function () {
      var open = mmenu.classList.toggle("open");
      menuBtn.textContent = open ? "✕" : "☰";
      menuBtn.setAttribute("aria-expanded", open ? "true" : "false");
      menuBtn.setAttribute("aria-label", open ? "Close menu" : "Open menu");
      document.body.classList.toggle("menu-open", open);
    });
    links.forEach(function (a) {
      a.addEventListener("click", function () {
        mmenu.classList.remove("open");
        menuBtn.textContent = "☰";
        menuBtn.setAttribute("aria-expanded", "false");
        document.body.classList.remove("menu-open");
      });
    });
  }

  // Rise-on-scroll
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

  // Masked line reveals for display headings
  (function linereveal() {
    var heads = document.querySelectorAll("h2.disp");
    if (!heads.length) return;
    heads.forEach(function (h) {
      var parts = h.innerHTML.split(/<br\s*\/?>/i);
      if (parts.length < 2) return;
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

  // Sample-booking playback (clearly a demo, loops while visible)
  (function desk() {
    var box = document.getElementById("deskStatus");
    if (!box) return;
    var lines = Array.prototype.slice.call(box.querySelectorAll("div"));
    var bar = document.getElementById("deskBar");
    if (calm || !("IntersectionObserver" in window)) {
      lines.forEach(function (d) { d.classList.add("show"); });
      if (bar) bar.style.width = "100%";
      return;
    }
    function play() {
      lines.forEach(function (d) { d.classList.remove("show"); });
      if (bar) bar.style.width = "0";
      var i = 0;
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

  // Interactive demo: 4-question booking sim (demo content, no real data)
  (function demo() {
    var body = document.getElementById("demoBody");
    if (!body) return;
    var stepEl = document.getElementById("demoStep"), dots = document.getElementById("demoDots"),
        back = document.getElementById("demoBack");
    var state = {}, order = ["what", "when", "where", "who"], at = 0;
    var QS = {
      what: { q: "What are you shooting?", opts: [["Fashion", "editorial · lookbook"], ["Food", "menu · café"], ["Product", "e-com · brand"], ["Reel", "vertical video"], ["Event", "live coverage"], ["Corporate", "interview · office"]] },
      when: { q: "When's call time?", opts: [["Today", "crew within hours"], ["Tomorrow", "morning slots"], ["This weekend", "prime time"], ["Pick a date", "plan ahead"]] },
      where: { q: "Where are we shooting?", opts: [["Bandra West"], ["Andheri"], ["Colaba"], ["Juhu"]] },
      who: { q: "Who do you need?", opts: [["Videographer", "video + gimbal"], ["Photographer", "stills + lights"], ["Cinematographer", "cinema kit"], ["Drone operator", "aerials"], ["Editor", "post-production"], ["Assistant", "set support"]] }
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
        h += "<button class='opt' data-i='" + i + "'>" + o[0] + (o[1] ? "<small>" + o[1] + "</small>" : "") + "</button>";
      });
      body.innerHTML = h + "</div>";
      Array.prototype.forEach.call(body.querySelectorAll(".opt"), function (b) {
        b.addEventListener("click", function () {
          b.classList.add("picked");
          state[k] = q.opts[parseInt(b.getAttribute("data-i"), 10)][0];
          setTimeout(function () { at++; at < 4 ? ask() : searching(); }, 220);
        });
      });
    }
    function searching() {
      if (stepEl) stepEl.textContent = "MATCHING · DEMO";
      if (back) back.hidden = true;
      paintDots();
      body.innerHTML = "<div class='demo-q'>Finding verified crew…</div><div class='demo-opts'><div class='desk-status' style='min-height:0'>" +
        "<div class='show'>Checking availability…</div><div class='show gold'>Matching the brief…</div>" +
        "<div class='show ok'>Verified profiles only</div></div></div>";
      setTimeout(found, calm ? 100 : 1600);
    }
    function found() {
      body.innerHTML = "<div class='demo-result show'><div class='found'>Crew found <span>✓</span></div>" +
        "<p style='color:var(--muted);margin-top:10px'>" + state.what + " · " + state.when + " · " + state.where + " · " + state.who + "</p>" +
        "<div class='demo-crew'><div class='avatar'>V</div><div><h4>" + state.who + " <small>· VERIFIED</small></h4>" +
        "<p>Verified profile · gear checked · in-app rates · chat unlocks on accept</p></div></div>" +
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
      var trx = 10 - 15 * p;
      var tryy = -26 + 46 * p;
      var tdy = (0.5 - p) * -70;
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
    badge.style.opacity = "1"; badge.style.margin = "24px auto 0"; badge.style.display = "block";
    badge.style.width = "max-content";
  }

  // Matching sequence: process steps, then the confirmed role
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

  if (!fine || calm) return; // hover-only feedback below is for desktop pointers

  // Rising motes — the water of this set. Slow, gold, endless.
  (function sea() {
    var cv = document.getElementById("sea");
    if (!cv || !cv.getContext) return;
    var ctx = cv.getContext("2d"), W = 0, H = 0, P = [], hidden = false;
    function size() {
      W = cv.width = window.innerWidth; H = cv.height = window.innerHeight;
      var n = Math.min(70, Math.floor(W * H / 30000));
      P = [];
      for (var i = 0; i < n; i++) P.push({
        x: Math.random() * W, y: Math.random() * H,
        r: 0.7 + Math.random() * 1.6, s: 0.15 + Math.random() * 0.4,
        a: 0.1 + Math.random() * 0.3, ph: Math.random() * Math.PI * 2, wob: 0.3 + Math.random() * 0.7
      });
    }
    size();
    window.addEventListener("resize", size);
    document.addEventListener("visibilitychange", function () { hidden = document.hidden; });
    var t = 0;
    (function frame() {
      requestAnimationFrame(frame);
      if (hidden) return;
      t += 0.008;
      ctx.clearRect(0, 0, W, H);
      for (var i = 0; i < P.length; i++) {
        var p = P[i];
        p.y -= p.s;
        p.x += Math.sin(t * 1.5 + p.ph) * 0.12 * p.wob;
        if (p.y < -8) { p.y = H + 8; p.x = Math.random() * W; }
        var tw = p.a * (0.6 + 0.4 * Math.sin(t * 2 + p.ph));
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, 6.283);
        ctx.fillStyle = "rgba(246,185,65," + tw.toFixed(3) + ")";
        ctx.fill();
      }
    })();
  })();

  // The fish: hero phone travels a bounded loop, drifts to cursor, dives on scroll — never clipped
  (function floatphone() {
    var fp = document.getElementById("floatphone");
    if (!fp || calm) return;
    fp.style.transition = "opacity .4s ease"; // transform is driven per-frame below
    var tx = 0, ty = 0, cx = 0, cy = 0, hidden = false, t = 0;
    document.addEventListener("visibilitychange", function () { hidden = document.hidden; });
    if (fine) {
      document.addEventListener("pointermove", function (e) {
        tx = (e.clientX / window.innerWidth - 0.5) * 22;
        ty = (e.clientY / window.innerHeight - 0.5) * 14;
      }, { passive: true });
    }
    function clamp(v, a, b) { return Math.max(a, Math.min(b, v)); }
    (function frame() {
      requestAnimationFrame(frame);
      if (hidden) return;
      t += 0.016;
      var hero = fp.closest ? fp.closest(".hero-desk") : null;
      var hr = hero ? hero.getBoundingClientRect() : null;
      if (hr && (hr.bottom < -80 || hr.top > window.innerHeight + 80)) { fp.style.opacity = "0"; return; }
      cx += (tx - cx) * 0.05;
      cy += (ty - cy) * 0.05;
      // gentle travel loop (stays near its anchor — amplitude small so it can't hit the edge)
      var loopX = Math.sin(t * 0.7) * 18 + Math.sin(t * 0.31) * 10;
      var loopY = Math.cos(t * 0.55) * 16 + Math.sin(t * 0.9) * 6;
      var dive = Math.min(220, window.scrollY * 0.18);
      var x = clamp(cx + loopX, -60, 60);
      var y = clamp(cy + loopY + dive, -30, 230);
      var rot = 7 + cx * 0.1 + Math.sin(t * 0.7) * 2.5;
      fp.style.transform = "translate3d(" + x.toFixed(1) + "px," + y.toFixed(1) + "px,0) rotate(" + rot.toFixed(2) + "deg)";
      // fade as hero scrolls away instead of sliding under content
      fp.style.opacity = String(clamp(1 - window.scrollY / (window.innerHeight * 0.85), 0, 1));
    })();
  })();

  // Card spotlight — soft light follows the cursor over interactive surfaces
  document.addEventListener("pointermove", function (e) {
    var t = e.target.closest ? e.target.closest(".card,.phone,.tier,.shot,.opt,.stamp") : null;
    if (!t) return;
    var r = t.getBoundingClientRect();
    t.style.setProperty("--mx", (e.clientX - r.left).toFixed(1) + "px");
    t.style.setProperty("--my", (e.clientY - r.top).toFixed(1) + "px");
  }, { passive: true });

  // Scroll engine: progress beam, gentle parallax, split converge, cue fade
  var layers = Array.prototype.slice.call(document.querySelectorAll("[data-plx],[data-plx-x]"));
  var sfill = document.getElementById("scrollbarFill");
  var cue = document.getElementById("scrollCue");
  var ticking = false;
  function render() {
    ticking = false;
    var vh = window.innerHeight, y = window.scrollY;
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

  // ═══ FAMEGO ALIVE — central motion bus (transform/opacity only) ═══
  (function alive() {
    if (calm) return;
    document.body.classList.add("alive");
    var px = 0, py = 0, tx = 0, ty = 0, cx = 0, cy = 0;
    var lastY = window.scrollY, vel = 0, skew = 0, hidden = false;
    var glow = document.getElementById("cursorGlow");
    var flare = document.getElementById("lensFlare");
    var drone = document.getElementById("heroDrone");
    var cube = document.getElementById("heroCube");
    var strip = document.querySelector(".filmstrip .strip");
    document.addEventListener("visibilitychange", function () { hidden = document.hidden; });

    // orbs: felt-not-seen gold dust
    var orbBox = document.getElementById("orbs"), orbs = [];
    if (orbBox) {
      var n = wide ? 10 : 5;
      for (var i = 0; i < n; i++) {
        var d = document.createElement("div");
        d.className = "orb";
        var s = 60 + Math.random() * 160;
        d.style.width = s + "px"; d.style.height = s + "px";
        d.style.left = (Math.random() * 100) + "vw";
        d.style.top = (Math.random() * 100) + "vh";
        d.style.animationDelay = (-Math.random() * 5) + "s";
        orbBox.appendChild(d);
        orbs.push({ el: d, x0: Math.random() * window.innerWidth, y0: Math.random() * window.innerHeight, ph: Math.random() * 6.28, amp: 30 + Math.random() * 70, sp: 0.0004 + Math.random() * 0.0008 });
      }
    }

    if (fine) {
      document.addEventListener("pointermove", function (e) {
        tx = e.clientX / window.innerWidth - 0.5;
        ty = e.clientY / window.innerHeight - 0.5;
        px = e.clientX; py = e.clientY;
      }, { passive: true });
    }

    // 3D tilt: auto-tag cards (adds glare node once)
    var tiltEls = Array.prototype.slice.call(document.querySelectorAll(".tier,.shot,.opt,.demo-crew,.mcard,.stamp"));
    tiltEls.forEach(function (el) {
      if (!el.hasAttribute("data-tilt")) el.setAttribute("data-tilt", "");
      if (!el.querySelector(".tilt-glare") && el.classList.contains("tier")) {
        var g = document.createElement("span"); g.className = "tilt-glare"; g.setAttribute("aria-hidden", "true"); el.appendChild(g);
      }
      if (fine) {
        el.addEventListener("pointermove", function (e) {
          var r = el.getBoundingClientRect();
          var dx = (e.clientX - r.left) / r.width - 0.5;
          var dy = (e.clientY - r.top) / r.height - 0.5;
          el.style.transform = "perspective(900px) rotateX(" + (-dy * 10).toFixed(2) + "deg) rotateY(" + (dx * 12).toFixed(2) + "deg) translateZ(6px)";
        });
        el.addEventListener("pointerleave", function () { el.style.transform = ""; });
      }
    });

    // magnetic buttons
    var mags = Array.prototype.slice.call(document.querySelectorAll(".btn.sheen,.btn"));
    if (fine) mags.forEach(function (b) {
      b.classList.add("magnet");
      b.addEventListener("pointermove", function (e) {
        var r = b.getBoundingClientRect();
        var dx = e.clientX - (r.left + r.width / 2), dy = e.clientY - (r.top + r.height / 2);
        b.style.transform = "translate(" + (dx * 0.12).toFixed(1) + "px," + (dy * 0.18).toFixed(1) + "px) scale(1.03)";
      });
      b.addEventListener("pointerleave", function () { b.style.transform = ""; });
    });

    // journey active step highlight
    var jsteps = Array.prototype.slice.call(document.querySelectorAll(".j-step"));
    if ("IntersectionObserver" in window && jsteps.length) {
      var jo = new IntersectionObserver(function (es) {
        es.forEach(function (en) {
          if (en.isIntersecting) {
            jsteps.forEach(function (s) { s.classList.remove("active"); });
            en.target.classList.add("active");
          }
        });
      }, { threshold: 0.6 });
      jsteps.forEach(function (s) { jo.observe(s); });
    }

    // finale pulse ring
    var finale = document.querySelector(".finale");
    if (finale && !finale.querySelector(".pulse-ring")) {
      var pr = document.createElement("div"); pr.className = "pulse-ring"; pr.setAttribute("aria-hidden", "true"); finale.appendChild(pr);
      if ("IntersectionObserver" in window) {
        var fo = new IntersectionObserver(function (es) {
          es.forEach(function (en) { if (en.isIntersecting) { pr.classList.add("go"); fo.disconnect(); } });
        }, { threshold: 0.3 });
        fo.observe(finale);
      } else pr.classList.add("go");
    }

    // Escape closes mobile menu (a11y)
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && mmenu && mmenu.classList.contains("open")) {
        mmenu.classList.remove("open"); document.body.classList.remove("menu-open");
        if (menuBtn) { menuBtn.textContent = "☰"; menuBtn.setAttribute("aria-expanded", "false"); menuBtn.focus(); }
      }
    });

    var t = 0;
    (function frame() {
      requestAnimationFrame(frame);
      if (hidden || calm) return;
      t += 0.016;
      cx += (tx - cx) * 0.06; cy += (ty - cy) * 0.06;
      var y = window.scrollY;
      vel += ((Math.abs(y - lastY) - vel) * 0.12);
      lastY = y;
      skew += ((Math.max(-8, Math.min(8, (y - (frame._py || y)) * 0.25)) - skew) * 0.1);
      frame._py = y;
      // headings skew
      if (Math.abs(skew) > 0.05) {
        document.documentElement.style.setProperty("--skew-live", skew.toFixed(2) + "deg");
        var hs = document.querySelectorAll("h1.hero-h,h2.disp");
        for (var i = 0; i < hs.length; i++) {
          var r = hs[i].getBoundingClientRect();
          if (r.top < window.innerHeight && r.bottom > 0) hs[i].style.setProperty("--skew", (skew * 0.12).toFixed(2) + "deg");
        }
      }
      // filmstrip reacts to scroll
      if (strip) strip.style.setProperty("--strip-speed", Math.max(12, 30 - vel * 0.6).toFixed(1) + "s");
      // glow + flare + drone + cube drift
      if (glow && fine) glow.style.transform = "translate3d(" + px.toFixed(0) + "px," + py.toFixed(0) + "px,0)";
      if (flare) flare.style.transform = "translate3d(" + (cx * 120).toFixed(1) + "px," + (y * 0.08).toFixed(1) + "px,0)";
      if (drone) {
        var dr = drone.getBoundingClientRect();
        if (dr.bottom > -100 && dr.top < window.innerHeight + 100)
          drone.style.transform = "translateX(-50%) translate(" + (cx * 90).toFixed(1) + "px," + (Math.sin(t * 1.4) * 16 + y * 0.04).toFixed(1) + "px) rotate(" + (cx * 6).toFixed(2) + "deg)";
      }
      if (cube) {
        var cr = cube.parentElement.getBoundingClientRect();
        if (cr.bottom > -100 && cr.top < window.innerHeight + 200)
          cube.parentElement.style.transform = "translate3d(" + (cx * -50).toFixed(1) + "px," + (cy * -40 + y * 0.05).toFixed(1) + "px,0)";
      }
      // orbs drift
      for (var k = 0; k < orbs.length; k++) {
        var o = orbs[k];
        var ox = (o.x0 + Math.sin(t * 0.4 + o.ph) * o.amp + cx * 60) % window.innerWidth;
        var oy = (o.y0 + Math.cos(t * 0.3 + o.ph) * o.amp * 0.7 + y * 0.03) % window.innerHeight;
        if (ox < 0) ox += window.innerWidth; if (oy < 0) oy += window.innerHeight;
        o.el.style.transform = "translate3d(" + ox.toFixed(0) + "px," + oy.toFixed(0) + "px,0)";
      }
    })();
  })();

  // shooting-star upgrade for existing sea motes (light touch)
  (function stars() {
    if (calm) return;
    var cv = document.getElementById("sea");
    if (!cv) return;
    var shoot = document.createElement("canvas");
    shoot.id = "shoot"; shoot.setAttribute("aria-hidden", "true");
    shoot.style.cssText = "position:fixed;inset:0;z-index:0;pointer-events:none;opacity:.9";
    document.body.appendChild(shoot);
    var ctx = shoot.getContext("2d"), W, H, sx = -100, sy = -100, life = 0;
    function size() { W = shoot.width = window.innerWidth; H = shoot.height = window.innerHeight; }
    size(); window.addEventListener("resize", size);
    setInterval(function () {
      if (document.hidden || calm) return;
      sx = Math.random() * W; sy = Math.random() * H * 0.4; life = 1;
    }, 5200);
    (function frame() {
      requestAnimationFrame(frame);
      if (document.hidden) return;
      ctx.clearRect(0, 0, W, H);
      if (life > 0) {
        life -= 0.02; sx += 9; sy += 3.5;
        var g = ctx.createLinearGradient(sx, sy, sx - 90, sy - 35);
        g.addColorStop(0, "rgba(246,185,65," + Math.max(0, life).toFixed(2) + ")");
        g.addColorStop(1, "rgba(246,185,65,0)");
        ctx.strokeStyle = g; ctx.lineWidth = 1.6;
        ctx.beginPath(); ctx.moveTo(sx, sy); ctx.lineTo(sx - 90, sy - 35); ctx.stroke();
        ctx.fillStyle = "rgba(255,255,255," + Math.max(0, life).toFixed(2) + ")";
        ctx.beginPath(); ctx.arc(sx, sy, 2, 0, 6.283); ctx.fill();
      }
    })();
  })();

  // Deep-link to app
  window.openFameGoApp = function () {
    window.location.href = "famego://auth/callback";
  };
})();
