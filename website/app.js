// FameGo site — minimal progressive enhancement. No framework, no build.
(function () {
  "use strict";

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
  if (!fine || calm) return; // touch users + reduced motion: stay still

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
    var t = e.target.closest ? e.target.closest(".card,.plan,.step") : null;
    if (!t) return;
    var r = t.getBoundingClientRect();
    t.style.setProperty("--mx", (e.clientX - r.left).toFixed(1) + "px");
    t.style.setProperty("--my", (e.clientY - r.top).toFixed(1) + "px");
  }, { passive: true });

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
      stage.style.transform = "rotateX(" + crx.toFixed(2) + "deg) rotateY(" + cry.toFixed(2) + "deg)";
      requestAnimationFrame(tilt);
    })();
  }

  // Deep-link to app with graceful fallback
  window.openFameGoApp = function () {
    window.location.href = "famego://auth/callback";
  };
})();
