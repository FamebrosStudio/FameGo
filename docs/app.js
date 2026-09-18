// FameGo site — minimal progressive enhancement. No framework, no build.
(function () {
  "use strict";

  // Failsafe first: if anything below throws, never trap the visitor
  // behind the loader or leave headlines hidden.
  window.addEventListener("error", function () {
    try {
      document.body.classList.remove("loading");
      var l = document.getElementById("loader");
      if (l) l.style.display = "none";
      document.querySelectorAll(".rv").forEach(function (e) { e.classList.add("in"); });
    } catch (_) { /* last resort: stay visible */ }
  });

  // Sticky nav toggle (mobile)
  var btn = document.getElementById("menuBtn");
  var nav = document.getElementById("siteNav");
  if (btn && nav) {
    btn.addEventListener("click", function () { nav.classList.toggle("open"); });
    nav.addEventListener("click", function (e) {
      if (e.target.tagName === "A") nav.classList.remove("open");
    });
  }

  // Footer year
  var y = document.getElementById("year");
  if (y) y.textContent = String(new Date().getFullYear());

  // Rise-on-scroll — mirrors FameGoMotion RISE (+16px, fade)
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

  var fine = window.matchMedia("(pointer: fine)").matches;
  var calm = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var isIndex = !!document.getElementById("loader");

  // Preloader (index only) — counter + curtain lift, failsafe included
  (function loader() {
    var l = document.getElementById("loader");
    if (!l) { document.body.classList.remove("loading"); return; }
    if (calm) { l.style.display = "none"; document.body.classList.remove("loading"); return; }
    var n = document.getElementById("lcount"), bar = document.getElementById("lbar");
    var t0 = null, dur = 950, done = false;
    function finish() {
      if (done) return; done = true;
      l.classList.add("done");
      document.body.classList.remove("loading");
      setTimeout(function () { l.style.display = "none"; }, 900);
    }
    function frame(t) {
      if (done) return;
      if (!t0) t0 = t;
      var p = Math.min(1, (t - t0) / dur);
      if (n) n.textContent = String(Math.round(p * 100)).padStart(2, "0");
      if (bar) bar.style.width = (p * 100).toFixed(1) + "%";
      if (p < 1) requestAnimationFrame(frame); else setTimeout(finish, 120);
    }
    requestAnimationFrame(frame);
    setTimeout(finish, 2600); // failsafe
  })();

  // Word-by-word manifesto reveal
  (function words() {
    var m = document.getElementById("mani");
    if (!m) return;
    var text = m.textContent.trim().split(/\s+/);
    m.innerHTML = text.map(function (w) { return '<span class="w">' + w + "</span>"; }).join(" ");
    var spans = m.querySelectorAll(".w");
    if (calm || !("IntersectionObserver" in window)) { m.classList.add("in"); return; }
    spans.forEach(function (s, i) { s.style.transitionDelay = Math.min(i * 28, 900) + "ms"; });
    var o = new IntersectionObserver(function (es) {
      es.forEach(function (en) { if (en.isIntersecting) { m.classList.add("in"); o.disconnect(); } });
    }, { threshold: 0.35 });
    o.observe(m);
  })();

  // Count-up stats — ticker-style numerals on first view (instant if reduced motion)
  var counters = document.querySelectorAll("[data-count]");
  function setNum(el) {
    var target = parseInt(el.getAttribute("data-count"), 10) || 0;
    var pre = el.getAttribute("data-prefix") || "", suf = el.getAttribute("data-suffix") || "";
    el.textContent = pre + target.toLocaleString("en-IN") + suf;
  }
  if (counters.length) {
    if (calm || !("IntersectionObserver" in window)) {
      counters.forEach(setNum);
    } else {
      var cio = new IntersectionObserver(function (entries) {
        entries.forEach(function (en) {
          if (!en.isIntersecting) return;
          cio.unobserve(en.target);
          var el = en.target, target = parseInt(el.getAttribute("data-count"), 10) || 0;
          var pre = el.getAttribute("data-prefix") || "", suf = el.getAttribute("data-suffix") || "";
          var t0 = null, dur = 1100;
          function frame(t) {
            if (!t0) t0 = t;
            var p = Math.min(1, (t - t0) / dur), e = 1 - Math.pow(1 - p, 3);
            el.textContent = pre + Math.round(target * e).toLocaleString("en-IN") + suf;
            if (p < 1) requestAnimationFrame(frame);
          }
          requestAnimationFrame(frame);
        });
      }, { threshold: 0.6 });
      counters.forEach(function (el) { cio.observe(el); });
    }
  }

  if (!fine || calm) return; // touch users + reduced motion: stay still and minimal

  // Custom cursor (index only) — dot + trailing ring, grows on interactives
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
      if (!ring) return;
      ring.classList.toggle("big", !!(e.target.closest && e.target.closest("a,button,summary,.plan,.hcard")));
    });
  }

  // Cursor-following aura — one soft light trailing the pointer
  var glow = document.createElement("div");
  glow.className = "cursor-glow";
  glow.setAttribute("aria-hidden", "true");
  document.body.appendChild(glow);
  var tx = -600, ty = -600, gx = tx, gy = ty, lit = false;
  window.addEventListener("pointermove", function (e) {
    tx = e.clientX; ty = e.clientY;
    if (!lit) { lit = true; document.body.classList.add("glow-on"); }
  }, { passive: true });
  window.addEventListener("pointerleave", function () {
    lit = false; document.body.classList.remove("glow-on");
  });
  (function loop() {
    gx += (tx - gx) * 0.08; gy += (ty - gy) * 0.08;
    glow.style.transform = "translate(" + gx.toFixed(1) + "px," + gy.toFixed(1) + "px)";
    requestAnimationFrame(loop);
  })();

  // Card spotlight — light follows the cursor inside each card
  document.addEventListener("pointermove", function (e) {
    var t = e.target.closest ? e.target.closest(".card,.plan,.step,.stack-card,.hcard,.phone") : null;
    if (!t) return;
    var r = t.getBoundingClientRect();
    t.style.setProperty("--mx", (e.clientX - r.left).toFixed(1) + "px");
    t.style.setProperty("--my", (e.clientY - r.top).toFixed(1) + "px");
  }, { passive: true });

  // Gold-dust particle field — slow drifting motes, gently nudged by the cursor
  (function dust() {
    var cv = document.getElementById("dust");
    if (!cv || !cv.getContext) return;
    var ctx = cv.getContext("2d"), W = 0, H = 0, P = [];
    var mx = -9999, my = -9999;
    function size() {
      W = cv.width = window.innerWidth; H = cv.height = window.innerHeight;
      var n = Math.min(90, Math.floor(W * H / 22000));
      P = [];
      for (var i = 0; i < n; i++) P.push({
        x: Math.random() * W, y: Math.random() * H,
        r: 0.6 + Math.random() * 1.5, s: 0.08 + Math.random() * 0.28,
        a: 0.15 + Math.random() * 0.4, ph: Math.random() * Math.PI * 2
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

  // Hero stage tilt — barely-there 3D lean toward the cursor
  var stage = document.querySelector(".stage");
  if (stage) {
    stage.classList.add("tilt");
    var rx = 0, ry = 0, crx = 0, cry = 0;
    document.addEventListener("pointermove", function (e) {
      var r = stage.getBoundingClientRect();
      var px = (e.clientX - (r.left + r.width / 2)) / r.width;
      var py = (e.clientY - (r.top + r.height / 2)) / r.height;
      var near = Math.abs(px) < 1.4 && Math.abs(py) < 1.4;
      rx = near ? Math.max(-1, Math.min(1, -py)) * 4 : 0;
      ry = near ? Math.max(-1, Math.min(1, px)) * 5 : 0;
    }, { passive: true });
    (function tilt() {
      crx += (rx - crx) * 0.06; cry += (ry - cry) * 0.06;
      // scroll velocity adds a passing lean so the phone feels alive while reading
      var lean = Math.max(-3, Math.min(3, -sVel * 0.12));
      stage.style.transform = "rotateX(" + (crx + lean).toFixed(2) + "deg) rotateY(" + cry.toFixed(2) + "deg)";
      requestAnimationFrame(tilt);
    })();
  }

  // ——— Scroll parallax engine (translate property: composes with transform) ———
  var layers = Array.prototype.slice.call(document.querySelectorAll("[data-plx],[data-plx-x]"));
  var hwrap = document.getElementById("hwrap");
  var htrack = document.getElementById("htrack");
  var hprog = document.querySelector("#hprog i");
  var pinned = false;

  // Pin the horizontal strip on desktop only; elsewhere it stays a plain grid
  if (hwrap && htrack && wide) {
    hwrap.classList.add("pinned");
    document.getElementById("hprog").hidden = false;
    pinned = true;
  }

  var ticking = false;
  var lastY = window.scrollY, vel = 0, sVel = 0;
  var sfill = document.getElementById("scrollbarFill");
  var marquee = document.querySelector(".marquee");
  function render() {
    ticking = false;
    var vh = window.innerHeight;
    var y = window.scrollY;
    // scroll velocity (smoothed) — drives the alive-feel extras below
    vel = y - lastY; lastY = y;
    sVel += (vel - sVel) * 0.12;
    // gold reading progress
    if (sfill) {
      var max = document.documentElement.scrollHeight - vh;
      sfill.style.transform = "scaleX(" + (max > 0 ? Math.max(0, Math.min(1, y / max)).toFixed(3) : 0) + ")";
    }
    // marquee leans with scroll velocity
    if (marquee) marquee.style.transform = "skewX(" + Math.max(-6, Math.min(6, -sVel * 0.25)).toFixed(2) + "deg)";
    for (var i = 0; i < layers.length; i++) {
      var el = layers[i];
      var r = el.getBoundingClientRect();
      if (r.bottom < -200 || r.top > vh + 200) continue; // off-screen: skip
      var c = r.top + r.height / 2 - vh / 2;
      var sy = parseFloat(el.getAttribute("data-plx")) || 0;
      var sx = parseFloat(el.getAttribute("data-plx-x")) || 0;
      el.style.translate = (sx ? (-c * sx).toFixed(1) + "px" : "0px") + " " + (sy ? (-c * sy).toFixed(1) + "px" : "0px");
    }
    if (pinned) {
      var wr = hwrap.getBoundingClientRect();
      var total = hwrap.offsetHeight - vh;
      var p = Math.max(0, Math.min(1, -wr.top / total));
      var maxX = htrack.scrollWidth - window.innerWidth + parseInt(getComputedStyle(htrack).paddingRight, 10);
      htrack.style.transform = "translate3d(" + (-p * maxX).toFixed(1) + "px,0,0)";
      if (hprog) hprog.style.transform = "scaleX(" + p.toFixed(3) + ")";
    }
  }
  function onScroll() {
    if (!ticking) { ticking = true; requestAnimationFrame(render); }
  }
  window.addEventListener("scroll", onScroll, { passive: true });
  window.addEventListener("resize", onScroll);
  render();

  // Deep-link to app with graceful fallback
  window.openFameGoApp = function () {
    window.location.href = "famego://auth/callback";
  };
})();
