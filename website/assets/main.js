import { signal, wifi, battery } from "./styles.js";

const REPO = "DHD2280/Oxygen-Customizer";
const CACHE_KEY = "oc-gh-stats-v2";
const TTL = 60 * 60 * 1000;
const EASE = "cubic-bezier(0.16, 1, 0.3, 1)";
const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

const packs = { signal, wifi, battery };
const LEVEL_NAMES = ["None", "Weak", "Fair", "Good", "Full"];

let uid = 0;

function glyph(kind, pack, level = 4) {
	if (kind === "battery") {
		const el = document.createElement("span");
		el.className = "batt";
		const url = `url(assets/batteries/${pack.id}.png)`;
		el.style.webkitMaskImage = url;
		el.style.maskImage = url;
		return el;
	}
	const entry = pack.levels[level];
	const vb = pack.vb ?? entry.vb;
	const body = pack.vb ? entry : entry.svg;
	const prefix = `u${++uid}`;
	const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	svg.setAttribute("viewBox", vb);
	svg.setAttribute("aria-hidden", "true");
	svg.innerHTML = body.replaceAll("{P}", prefix);
	return svg;
}

function place(slot, node, animate) {
	if (!animate || reduceMotion.matches) {
		slot.replaceChildren(node);
		return;
	}
	for (const stale of slot.querySelectorAll(".is-leaving")) stale.remove();
	const outgoing = slot.firstElementChild;
	slot.append(node);
	if (outgoing) {
		outgoing.classList.add("is-leaving");
		outgoing
			.animate(
				{
					transform: ["scale(1)", "scale(0.55)"],
					filter: ["blur(0)", "blur(3px)"],
					opacity: [1, 0],
				},
				{ duration: 300, easing: EASE, fill: "forwards" },
			)
			.finished.then(
				() => outgoing.remove(),
				() => {},
			);
	}
	node.animate(
		{
			transform: ["scale(1.35)", "scale(1)"],
			filter: ["blur(3px)", "blur(0)"],
			opacity: [0, 1],
		},
		{ duration: 520, delay: 60, easing: EASE, fill: "backwards" },
	);
}

function clockText(withSeconds) {
	const now = new Date();
	const parts = new Intl.DateTimeFormat(undefined, {
		hour: "numeric",
		minute: "2-digit",
	})
		.formatToParts(now)
		.filter((p) => p.type !== "dayPeriod")
		.map((p) => p.value)
		.join("")
		.trim();
	if (!withSeconds) return parts;
	return `${parts}<span class="sec">${String(now.getSeconds()).padStart(2, "0")}</span>`;
}

function startClock() {
	const clocks = document.querySelectorAll("[data-clock]");
	let seconds = false;
	let timer = null;
	const tick = () => {
		const html = clockText(seconds);
		clocks.forEach((el) => {
			if (el.innerHTML !== html) el.innerHTML = html;
		});
	};
	const schedule = () => {
		clearTimeout(timer);
		tick();
		const step = seconds ? 1000 : 60_000;
		timer = setTimeout(schedule, step - (Date.now() % step));
	};
	schedule();
	return {
		setSeconds(on) {
			seconds = on;
			schedule();
		},
	};
}

function moveClock(sb, pos) {
	const clock = sb.querySelector(".sb-clock");
	const slot = sb.querySelector(`[data-slot-pos='${pos}']`);
	if (!clock || !slot || sb.dataset.clockPos === pos) return;
	const before = clock.getBoundingClientRect();
	sb.dataset.clockPos = pos;
	slot.append(clock);
	if (reduceMotion.matches) return;
	const after = clock.getBoundingClientRect();
	clock.getAnimations().forEach((a) => a.cancel());
	clock.animate(
		{
			transform: [`translate(${before.left - after.left}px, ${before.top - after.top}px)`, "translate(0, 0)"],
		},
		{ duration: 720, easing: "cubic-bezier(0.22, 1.2, 0.36, 1)" },
	);
}

function setPressed(group, value) {
	for (const btn of group.querySelectorAll("button")) {
		btn.setAttribute("aria-pressed", String(btn.dataset.value === value));
	}
}

function startHero(clock) {
	const sb = document.querySelector("[data-sb='hero']");
	const panel = document.querySelector("[data-console]");
	if (!sb || !panel) return;

	const posGroup = panel.querySelector("[data-seg='clock-pos']");
	const colorGroup = panel.querySelector("[data-seg='clock-color']");
	const chipBtn = panel.querySelector("[data-toggle='chip']");
	const barBtn = panel.querySelector("[data-toggle='bar']");
	const pauseBtn = panel.querySelector("[data-pause]");

	const setPos = (pos) => {
		setPressed(posGroup, pos);
		moveClock(sb, pos);
	};
	const setColor = (color) => {
		setPressed(colorGroup, color);
		sb.style.setProperty("--clock-color", color);
	};
	const setChip = (on) => {
		chipBtn.setAttribute("aria-pressed", String(on));
		sb.classList.toggle("has-chip", on);
	};

	const demoSteps = [
		() => {
			setPos("center");
			setColor("#ffc24d");
		},
		() => {
			setPos("right");
			setColor("#b8a4ff");
			setChip(true);
		},
		() => {
			setPos("left");
			setColor("#54dbc8");
			setChip(false);
		},
	];
	let demoStep = 0;
	let demoTimer = null;
	let demoDone = reduceMotion.matches;
	let paused = false;

	const runDemo = () => {
		clearTimeout(demoTimer);
		if (demoDone || paused || demoStep >= demoSteps.length) return;
		demoTimer = setTimeout(() => {
			demoSteps[demoStep++]();
			runDemo();
		}, 2200);
	};
	const stopDemo = () => {
		demoDone = true;
		clearTimeout(demoTimer);
	};

	posGroup.addEventListener("click", (e) => {
		const btn = e.target.closest("button");
		if (!btn) return;
		stopDemo();
		setPos(btn.dataset.value);
	});
	colorGroup.addEventListener("click", (e) => {
		const btn = e.target.closest("button");
		if (!btn) return;
		stopDemo();
		setColor(btn.dataset.value);
	});
	barBtn?.addEventListener("click", () => {
		const on = barBtn.getAttribute("aria-pressed") !== "true";
		barBtn.setAttribute("aria-pressed", String(on));
		sb.classList.toggle("no-bar", !on);
	});
	chipBtn.addEventListener("click", () => {
		stopDemo();
		setChip(chipBtn.getAttribute("aria-pressed") !== "true");
	});

	const current = { signal: "Aquarium", wifi: "Aurora", battery: "Landscape iOS 16" };
	const levelFor = { signal: 3, wifi: 4, battery: 4 };
	const index = {};
	for (const kind of Object.keys(current)) {
		index[kind] = Math.max(
			0,
			packs[kind].findIndex((p) => p.name === current[kind]),
		);
		const slot = sb.querySelector(`[data-part='${kind}']`);
		place(slot, glyph(kind, packs[kind][index[kind]], levelFor[kind]), false);
	}

	runDemo();

	if (reduceMotion.matches) return;

	let visible = true;
	let turn = 0;
	const order = ["signal", "wifi", "battery"];

	setInterval(() => {
		if (paused || !visible || document.hidden) return;
		const kind = order[turn++ % order.length];
		index[kind] = (index[kind] + 1) % packs[kind].length;
		const pack = packs[kind][index[kind]];
		place(sb.querySelector(`[data-part='${kind}']`), glyph(kind, pack, levelFor[kind]), true);
	}, 1700);

	new IntersectionObserver(([entry]) => {
		visible = entry.isIntersecting;
	}).observe(sb);

	pauseBtn.hidden = false;
	pauseBtn.addEventListener("click", () => {
		paused = !paused;
		pauseBtn.setAttribute("aria-pressed", String(paused));
		pauseBtn.setAttribute("aria-label", paused ? "Resume preview" : "Pause preview");
		if (paused) clearTimeout(demoTimer);
		else runDemo();
	});
}

function startPicker() {
	const sb = document.querySelector("[data-sb='picker']");
	const range = document.querySelector("[data-level]");
	const out = document.querySelector("[data-level-out]");
	if (!sb || !range) return;

	const chosen = {
		signal: signal.find((p) => p.name === "Aquarium") ?? signal[0],
		wifi: wifi.find((p) => p.name === "Aurora") ?? wifi[0],
		battery: battery[0],
	};
	let level = Number(range.value);
	let sweep = [];

	const paint = (kind, lvl = level, animate = false) => {
		place(sb.querySelector(`[data-part='${kind}']`), glyph(kind, chosen[kind], kind === "battery" ? 4 : lvl), animate);
	};

	const renderTiles = (kind) => {
		const list = document.querySelector(`[data-tiles='${kind}']`);
		const frag = document.createDocumentFragment();
		for (const pack of packs[kind]) {
			const li = document.createElement("li");
			const btn = document.createElement("button");
			btn.type = "button";
			btn.className = "tile";
			btn.dataset.kind = kind;
			btn.dataset.id = pack.id;
			btn.setAttribute("aria-pressed", String(pack === chosen[kind]));
			const g = document.createElement("span");
			g.className = "tile-glyph";
			g.append(glyph(kind, pack, level));
			const n = document.createElement("span");
			n.className = "tile-name";
			n.textContent = pack.name;
			btn.append(g, n);
			li.append(btn);
			frag.append(li);
		}
		list.replaceChildren(frag);
		const count = document.querySelector(`[data-count='${kind}']`);
		if (count) count.textContent = packs[kind].length;
	};

	for (const kind of Object.keys(packs)) {
		renderTiles(kind);
		paint(kind);
	}

	document.querySelector(".picker").addEventListener("click", (e) => {
		const btn = e.target.closest(".tile");
		if (!btn) return;
		const kind = btn.dataset.kind;
		const pack = packs[kind].find((p) => p.id === btn.dataset.id);
		chosen[kind] = pack;
		for (const other of document.querySelectorAll(`.tile[data-kind='${kind}']`)) {
			other.setAttribute("aria-pressed", String(other === btn));
		}
		sweep.forEach(clearTimeout);
		sweep = [];
		if (kind === "battery" || reduceMotion.matches) {
			paint(kind, level, true);
			return;
		}
		paint(kind, 0, true);
		for (let l = 1; l <= level; l++) {
			sweep.push(setTimeout(() => paint(kind, l, false), 260 + l * 110));
		}
	});

	range.addEventListener("input", () => {
		level = Number(range.value);
		out.textContent = LEVEL_NAMES[level];
		sweep.forEach(clearTimeout);
		for (const kind of ["signal", "wifi"]) {
			paint(kind);
			for (const btn of document.querySelectorAll(`.tile[data-kind='${kind}'] .tile-glyph`)) {
				const pack = packs[kind].find((p) => p.id === btn.parentElement.dataset.id);
				btn.replaceChildren(glyph(kind, pack, level));
			}
		}
	});
}

function startTabs(selector, key) {
	const tabs = [...document.querySelectorAll(selector)];
	if (!tabs.length) return;
	const select = (tab, focus) => {
		for (const t of tabs) {
			const on = t === tab;
			t.setAttribute("aria-selected", String(on));
			t.tabIndex = on ? 0 : -1;
			document.getElementById(t.getAttribute("aria-controls")).hidden = !on;
		}
		if (focus) tab.focus();
	};
	tabs.forEach((tab, i) => {
		tab.addEventListener("click", () => select(tab, false));
		tab.addEventListener("keydown", (e) => {
			const step = { ArrowRight: 1, ArrowLeft: -1 }[e.key];
			if (!step) return;
			e.preventDefault();
			select(tabs[(i + step + tabs.length) % tabs.length], true);
		});
	});
}

const compact = new Intl.NumberFormat("en", { notation: "compact", maximumFractionDigits: 1 });

function renderStats({ tag, stars, downloads, contributors }) {
	if (tag) {
		const label = document.querySelector("[data-download-label]");
		if (label) label.textContent = `Download ${tag}`;
	}
	const values = { stars, downloads, contributors };
	let shown = 0;
	for (const [key, value] of Object.entries(values)) {
		const el = document.querySelector(`[data-stat='${key}']`);
		if (!el) continue;
		const ok = Number.isFinite(value) && value > 0;
		el.textContent = ok ? compact.format(value) : "";
		el.dataset.value = ok ? String(value) : "";
		el.parentElement.hidden = !ok;
		if (ok) shown++;
	}
	const row = document.querySelector("[data-stats]");
	if (!row) return;
	row.hidden = shown === 0;
	if (!row.hidden) countUp(row);
}

function countUp(row) {
	if (row.dataset.counted || reduceMotion.matches) return;
	row.dataset.counted = "true";
	const cells = [...row.querySelectorAll("dd[data-value]")].filter((el) => el.dataset.value);
	for (const el of cells) el.textContent = "0";
	new IntersectionObserver(
		([entry], observer) => {
			if (!entry.isIntersecting) return;
			observer.disconnect();
			const start = performance.now();
			const duration = 1400;
			const frame = (now) => {
				const t = Math.min(1, (now - start) / duration);
				const eased = 1 - Math.pow(1 - t, 4);
				for (const el of cells) {
					const target = Number(el.dataset.value);
					el.textContent = compact.format(t < 1 ? Math.round(target * eased) : target);
				}
				if (t < 1) requestAnimationFrame(frame);
			};
			requestAnimationFrame(frame);
		},
		{ threshold: 0.4 },
	).observe(row);
}

function lastPage(res) {
	const link = res.headers.get("link") ?? "";
	const match = link.match(/[?&]page=(\d+)>; rel="last"/);
	return match ? Number(match[1]) : null;
}

async function loadStats() {
	let cached = null;
	try {
		cached = JSON.parse(localStorage.getItem(CACHE_KEY));
	} catch {}
	if (cached && Date.now() - cached.at < TTL) {
		renderStats(cached.data);
		return;
	}
	try {
		const api = `https://api.github.com/repos/${REPO}`;
		const [repoRes, relRes, contribRes] = await Promise.all([
			fetch(api),
			fetch(`${api}/releases?per_page=100`),
			fetch(`${api}/contributors?per_page=1`),
		]);
		if (!repoRes.ok && !relRes.ok) throw new Error(String(repoRes.status));
		const repo = repoRes.ok ? await repoRes.json() : null;
		const releases = relRes.ok ? await relRes.json() : [];
		const latest = releases[0];
		let contributors = null;
		if (contribRes.ok) {
			contributors = lastPage(contribRes) ?? (await contribRes.json()).length;
		}
		const data = {
			tag: latest?.tag_name ?? null,
			stars: repo?.stargazers_count ?? null,
			downloads: releases.reduce(
				(sum, r) => sum + (r.assets ?? []).reduce((s, a) => s + (a.download_count ?? 0), 0),
				0,
			),
			contributors,
		};
		renderStats(data);
		try {
			localStorage.setItem(CACHE_KEY, JSON.stringify({ at: Date.now(), data }));
		} catch {}
	} catch {
		if (cached?.data) renderStats(cached.data);
	}
}

function startNav() {
	const links = new Map(
		[...document.querySelectorAll(".nav a")].map((a) => [a.getAttribute("href").slice(1), a]),
	);
	const observer = new IntersectionObserver(
		(entries) => {
			for (const entry of entries) {
				const link = links.get(entry.target.id);
				if (!link) continue;
				if (entry.isIntersecting) {
					links.forEach((l) => l.removeAttribute("aria-current"));
					link.setAttribute("aria-current", "true");
				}
			}
		},
		{ rootMargin: "-45% 0px -50% 0px" },
	);
	for (const id of links.keys()) {
		const section = document.getElementById(id);
		if (section) observer.observe(section);
	}
}

function startStrip() {
	const strip = document.querySelector("[data-strip]");
	const nav = document.querySelector("[data-strip-nav]");
	if (!strip || !nav) return;
	const [prev, next] = nav.querySelectorAll("button");

	const update = () => {
		const max = strip.scrollWidth - strip.clientWidth;
		nav.hidden = max <= 1;
		prev.disabled = strip.scrollLeft <= 1;
		next.disabled = strip.scrollLeft >= max - 1;
	};

	for (const btn of [prev, next]) {
		btn.addEventListener("click", () => {
			strip.scrollBy({
				left: Number(btn.dataset.step) * strip.clientWidth,
				behavior: reduceMotion.matches ? "auto" : "smooth",
			});
		});
	}

	strip.addEventListener("scroll", update, { passive: true });
	new ResizeObserver(update).observe(strip);
	update();
}

function startFaq() {
	for (const details of document.querySelectorAll(".faq details")) {
		const summary = details.querySelector("summary");
		const answer = details.querySelector(".faq-a");
		let running = null;
		summary.addEventListener("click", (e) => {
			if (reduceMotion.matches) return;
			e.preventDefault();
			running?.cancel();
			const closing = details.open && !details.classList.contains("is-closing");
			const from = `${answer.getBoundingClientRect().height}px`;
			if (closing) {
				details.classList.add("is-closing");
				running = answer.animate({ height: [from, "0px"], opacity: [1, 0] }, { duration: 240, easing: EASE });
				running.onfinish = () => {
					details.open = false;
					details.classList.remove("is-closing");
					running = null;
				};
			} else {
				details.classList.remove("is-closing");
				details.open = true;
				running = answer.animate(
					{ height: [from, `${answer.scrollHeight}px`], opacity: [0, 1] },
					{ duration: 320, easing: EASE },
				);
				running.onfinish = () => {
					running = null;
				};
			}
		});
	}
}

function startTheme() {
	const root = document.documentElement;
	const btn = document.querySelector("[data-theme-toggle]");
	const meta = document.querySelector("[data-theme-color]");
	const system = window.matchMedia("(prefers-color-scheme: light)");
	let stored = null;
	try {
		stored = localStorage.getItem("oc-theme");
	} catch {}

	const apply = (theme) => {
		root.dataset.theme = theme;
		const light = theme === "light";
		btn?.setAttribute("aria-label", light ? "Switch to dark theme" : "Switch to light theme");
		meta?.setAttribute("content", light ? "#f3f6f5" : "#000000");
	};

	apply(stored === "light" || stored === "dark" ? stored : system.matches ? "light" : "dark");

	system.addEventListener("change", (e) => {
		if (!stored) apply(e.matches ? "light" : "dark");
	});

	btn?.addEventListener("click", () => {
		stored = root.dataset.theme === "light" ? "dark" : "light";
		apply(stored);
		try {
			localStorage.setItem("oc-theme", stored);
		} catch {}
	});
}

startTheme();
const clock = startClock();
startHero(clock);
startPicker();
startTabs(".tabs [role='tab']");
startTabs(".setup [role='tab']");
startNav();
startStrip();
startFaq();
loadStats();
