#!/usr/bin/env node
// Regenerates public/screenshots/ from the website's live launcher demo (app:launcher-gui:demo), so the screenshots
// and the demo that fades in over them stay pixel-identical.
//
// Usage: node scripts/capture-screenshots.mjs [--skip-build] [--only home,instance,content]
// Needs Node 22+, Chromium/Chrome (override with $CHROME) and ImageMagick's `magick`.

import {spawn, spawnSync} from "node:child_process";
import {createServer} from "node:http";
import {mkdtempSync, readFileSync, rmSync, statSync, writeFileSync} from "node:fs";
import {tmpdir} from "node:os";
import {dirname, extname, join, normalize, resolve} from "node:path";
import {fileURLToPath} from "node:url";

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const DIST_DIR = join(ROOT, "app/launcher-gui/demo/build/dist/js/productionExecutable");
const OUTPUT_DIR = join(ROOT, "public/screenshots");
const SCREENS = ["home", "instance", "content"];
const MODES = [
    {htmlClass: "silk-dark", suffix: ""},
    {htmlClass: "silk-light", suffix: "_light"},
];
// Must match DEMO_WINDOW_WIDTH_PX in the demo's Main.kt and the 16:9 hosts on the website
const WIDTH = 1280;
const HEIGHT = 720;
const DEVICE_SCALE_FACTOR = 1.5;
const WEBP_QUALITY = 90;
const READY_TIMEOUT_MS = 60_000;
const STABLE_POLL_MS = 500;
const STABLE_TIMEOUT_MS = 20_000;

const MIME_TYPES = {
    ".html": "text/html",
    ".js": "text/javascript",
    ".mjs": "text/javascript",
    ".wasm": "application/wasm",
    ".json": "application/json",
    ".png": "image/png",
    ".webp": "image/webp",
    ".svg": "image/svg+xml",
    ".ttf": "font/ttf",
    ".xml": "application/xml",
};

const args = parseArgs(process.argv.slice(2));

if (!args.skipBuild) {
    run(join(ROOT, process.platform === "win32" ? "gradlew.bat" : "gradlew"), [
        ":app:launcher-gui:demo:jsBrowserDistribution",
    ]);
}
statSync(join(DIST_DIR, "vercors-demo.js"));
run("magick", ["-version"], {quiet: true});

const workDir = mkdtempSync(join(tmpdir(), "vercors-screenshots-"));
const server = await startServer();
const browser = await launchBrowser(join(workDir, "profile"));

try {
    for (const screen of args.screens) {
        for (const mode of MODES) {
            const name = `${screen}${mode.suffix}`;
            process.stdout.write(`Capturing ${name}... `);
            const png = await capture(browser, `${server.url}/capture.html?screen=${screen}&class=${mode.htmlClass}`);
            const pngPath = join(workDir, `${name}.png`);
            writeFileSync(pngPath, png);
            run("magick", [pngPath, "-quality", String(WEBP_QUALITY), join(OUTPUT_DIR, `${name}.webp`)], {quiet: true});
            console.log("done");
        }
    }
} finally {
    await browser.close();
    server.close();
    rmSync(workDir, {recursive: true, force: true});
}

function parseArgs(argv) {
    const result = {skipBuild: false, screens: SCREENS};
    for (let i = 0; i < argv.length; i++) {
        if (argv[i] === "--skip-build") {
            result.skipBuild = true;
        } else if (argv[i] === "--only") {
            result.screens = argv[++i].split(",");
            const unknown = result.screens.filter((s) => !SCREENS.includes(s));
            if (unknown.length) fail(`Unknown screen(s): ${unknown.join(", ")} (expected ${SCREENS.join(", ")})`);
        } else {
            fail(`Unknown argument: ${argv[i]}`);
        }
    }
    return result;
}

function run(command, commandArgs, {quiet = false} = {}) {
    const result = spawnSync(command, commandArgs, {cwd: ROOT, stdio: quiet ? "pipe" : "inherit"});
    if (result.error) fail(`Couldn't run ${command}: ${result.error.message}`);
    if (result.status !== 0) fail(`${command} ${commandArgs.join(" ")} failed\n${result.stderr ?? ""}`);
}

function fail(message) {
    console.error(message);
    process.exit(1);
}

// One bare host the size of the screenshot; the <html> class is what the demo reads the site's color mode from
function capturePage(screen, htmlClass) {
    return `<!doctype html>
<html class="${htmlClass}">
<head>
<meta charset="utf-8">
<style>
  html, body { margin: 0; overflow: hidden; }
  [data-demo] { position: relative; width: ${WIDTH}px; height: ${HEIGHT}px; }
</style>
</head>
<body>
<div data-demo="${screen}"></div>
<script src="vercors-demo.js"></script>
</body>
</html>`;
}

function startServer() {
    const server = createServer((request, response) => {
        const url = new URL(request.url, "http://localhost");
        if (url.pathname === "/capture.html") {
            response.writeHead(200, {"Content-Type": "text/html"});
            response.end(capturePage(url.searchParams.get("screen"), url.searchParams.get("class")));
            return;
        }
        const path = normalize(join(DIST_DIR, decodeURIComponent(url.pathname)));
        if (!path.startsWith(DIST_DIR) || !statSync(path, {throwIfNoEntry: false})?.isFile()) {
            response.writeHead(404);
            response.end();
            return;
        }
        response.writeHead(200, {"Content-Type": MIME_TYPES[extname(path)] ?? "application/octet-stream"});
        response.end(readFileSync(path));
    });
    return new Promise((resolvePromise) => {
        server.listen(0, "127.0.0.1", () => {
            resolvePromise({url: `http://127.0.0.1:${server.address().port}`, close: () => server.close()});
        });
    });
}

function findChrome() {
    const candidates = [
        process.env.CHROME,
        "chromium",
        "chromium-browser",
        "google-chrome-stable",
        "google-chrome",
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        "/Applications/Chromium.app/Contents/MacOS/Chromium",
    ].filter(Boolean);
    for (const candidate of candidates) {
        if (!spawnSync(candidate, ["--version"], {stdio: "ignore"}).error) return candidate;
    }
    fail("No Chromium/Chrome found; set $CHROME to its executable");
}

// Driven over the DevTools protocol: one-shot --screenshot mode stalls Compose Resources' and Coil's web reads
async function launchBrowser(profileDir) {
    const chrome = spawn(findChrome(), [
        "--headless=new",
        "--remote-debugging-port=0",
        `--user-data-dir=${profileDir}`,
        "--no-first-run",
        "--no-default-browser-check",
        "--hide-scrollbars",
        "--force-color-profile=srgb",
        "--enable-unsafe-swiftshader",
        "about:blank",
    ], {stdio: ["ignore", "ignore", "pipe"]});

    const wsUrl = await new Promise((resolvePromise, reject) => {
        let stderr = "";
        chrome.stderr.on("data", (chunk) => {
            stderr += chunk;
            const match = /DevTools listening on (ws:\/\/\S+)/.exec(stderr);
            if (match) resolvePromise(match[1]);
        });
        chrome.on("exit", (code) => reject(new Error(`Chromium exited (${code}):\n${stderr}`)));
    });

    const socket = new WebSocket(wsUrl);
    await new Promise((resolvePromise, reject) => {
        socket.addEventListener("open", resolvePromise, {once: true});
        socket.addEventListener("error", reject, {once: true});
    });

    let nextId = 0;
    const pending = new Map();
    socket.addEventListener("message", (event) => {
        const message = JSON.parse(event.data);
        const callbacks = pending.get(message.id);
        if (!callbacks) return;
        pending.delete(message.id);
        if (message.error) callbacks.reject(new Error(message.error.message));
        else callbacks.resolve(message.result);
    });

    const send = (method, params, sessionId) =>
        new Promise((resolvePromise, reject) => {
            const id = ++nextId;
            pending.set(id, {resolve: resolvePromise, reject});
            socket.send(JSON.stringify({id, method, params, sessionId}));
        });

    return {
        send,
        close: () => {
            socket.close();
            const exited = new Promise((resolvePromise) => chrome.once("exit", resolvePromise));
            chrome.kill();
            return exited;
        },
    };
}

async function capture(browser, url) {
    const {targetId} = await browser.send("Target.createTarget", {url: "about:blank"});
    const {sessionId} = await browser.send("Target.attachToTarget", {targetId, flatten: true});
    const send = (method, params) => browser.send(method, params, sessionId);
    try {
        await send("Emulation.setDeviceMetricsOverride", {
            width: WIDTH,
            height: HEIGHT,
            deviceScaleFactor: DEVICE_SCALE_FACTOR,
            mobile: false,
        });
        await send("Page.navigate", {url});
        await waitFor(send, "document.querySelector('.demo-viewport[data-demo-ready]') !== null", READY_TIMEOUT_MS);
        return await stableScreenshot(send);
    } finally {
        await browser.send("Target.closeTarget", {targetId});
    }
}

async function waitFor(send, expression, timeoutMs) {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
        const {result} = await send("Runtime.evaluate", {expression, returnByValue: true});
        if (result.value === true) return;
        await sleep(200);
    }
    throw new Error(`Timed out waiting for: ${expression}`);
}

// data-demo-ready only covers fonts and bundled icons; remote images (Coil) can land later, so wait for two
// identical frames in a row
async function stableScreenshot(send) {
    const deadline = Date.now() + STABLE_TIMEOUT_MS;
    let previous = null;
    while (true) {
        await sleep(STABLE_POLL_MS);
        const {data} = await send("Page.captureScreenshot", {format: "png"});
        if (data === previous) return Buffer.from(data, "base64");
        if (Date.now() > deadline) {
            process.stdout.write("(never settled, using the last frame) ");
            return Buffer.from(data, "base64");
        }
        previous = data;
    }
}

function sleep(ms) {
    return new Promise((resolvePromise) => setTimeout(resolvePromise, ms));
}
