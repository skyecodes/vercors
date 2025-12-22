package app.vercors.meta.backend.project

import app.vercors.meta.lib.project.IconData
import app.vercors.meta.lib.project.IconNameData
import app.vercors.meta.lib.project.IconSvgData

private const val CREEPER_SVG =
    """<svg xml:space="preserve" fill-rule="evenodd" stroke-linejoin="round" stroke-miterlimit="1.5" clip-rule="evenodd" viewBox="0 0 24 24"><path fill="none" d="M0 0h24v24H0z"/><path fill="none" stroke="currentColor" stroke-width="2" d="M3 3h18v18H3z"/><path stroke="currentColor" fill="currentColor" d="M6 6h4v4H6zm8 0h4v4h-4zm-4 4h4v2h2v6h-2v-2h-4v2H8v-6h2v-2Z"/></svg>"""

private const val SINK_SVG =
    """<svg viewBox="0 0 24 24" xml:space="preserve"><g fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m19.9 14-1.4 4.9c-.3 1-1.1 1.7-2.1 1.7H7.6c-.9 0-1.8-.7-2.1-1.7L4.1 14h15.8zM12 10V4.5M12 4.5c0-1.2.9-2.1 2.1-2.1M14.1 2.4c1.2 0 2.1.9 2.1 2.1M22.2 12c0 .6-.2 1.1-.6 1.4-.4.4-.9.6-1.4.6H3.8c-1.1 0-2-.9-2-2 0-.6.2-1.1.6-1.4.4-.4.9-.6 1.4-.6h16.4c1.1 0 2 .9 2 2z"/></g><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" d="M16.2 7.2h0"/></svg>"""

private const val PATH_TRACING_SVG =
    """<svg viewBox="0 0 24 24" style="" fill="none" stroke="currentColor" stroke-width="2"><path d="M2.977 19.17h16.222" style="" transform="translate(-.189 -.328) scale(1.09932)"/><path d="M3.889 3.259 12 19.17l5.749-11.277" style="" transform="translate(-1.192 -.328) scale(1.09932)"/><path d="M9.865 6.192h4.623v4.623" style="" transform="scale(1.09931) rotate(-18 20.008 .02)"/></svg>"""

private const val POTATO_SVG =
    """<svg viewBox="0 0 512 512" fill="currentColor" stroke="currentColor"><g><g><path d="M218.913,116.8c-6.4-6.4-16-6.4-22.4,0c-3.2,3.2-4.8,6.4-4.8,11.2s1.6,8,4.8,11.2c3.2,3.2,8,4.8,11.2,4.8    c4.8,0,8-1.6,11.2-4.8c3.2-3.2,4.8-6.4,4.8-11.2S222.113,120,218.913,116.8z"/></g></g><g><g><path d="M170.913,372.8c-6.4-6.4-16-6.4-22.4,0c-3.2,3.2-4.8,6.4-4.8,11.2s1.6,8,4.8,11.2c3.2,3.2,8,4.8,11.2,4.8    c4.8,0,8-1.6,11.2-4.8c3.2-3.2,4.8-8,4.8-11.2C175.713,379.2,174.113,376,170.913,372.8z"/></g></g><g><g><path d="M250.913,228.8c-4.8-6.4-16-6.4-22.4,0c-3.2,3.2-4.8,6.4-4.8,11.2s1.6,8,4.8,11.2c3.2,3.2,8,4.8,11.2,4.8    c4.8,0,8-1.6,11.2-4.8c3.2-3.2,4.8-8,4.8-11.2C255.713,235.2,254.113,232,250.913,228.8z"/></g></g><g><g><path d="M410.913,212.8c-4.8-6.4-16-6.4-22.4,0c-3.2,3.2-4.8,6.4-4.8,11.2s1.6,8,4.8,11.2c3.2,3.2,8,4.8,11.2,4.8    c4.8,0,8-1.6,11.2-4.8c3.2-3.2,4.8-8,4.8-11.2C415.713,219.2,414.113,216,410.913,212.8z"/></g></g><g><g><path d="M346.913,308.8c-4.8-6.4-16-6.4-22.4,0c-3.2,3.2-4.8,6.4-4.8,11.2s1.6,8,4.8,11.2c3.2,3.2,8,4.8,11.2,4.8    c4.8,0,8-1.6,11.2-4.8c3.2-3.2,4.8-8,4.8-11.2C351.713,315.2,350.113,312,346.913,308.8z"/></g></g><g><g><path d="M346.913,100.8c-6.4-6.4-16-6.4-22.4,0c-3.2,3.2-4.8,6.4-4.8,11.2s1.6,8,4.8,11.2c3.2,3.2,8,4.8,11.2,4.8    c4.8,0,8-1.6,11.2-4.8s4.8-6.4,4.8-11.2S350.113,104,346.913,100.8z"/></g></g><g><g><path d="M503.713,142.4c-28.8-136-179.2-142.4-208-142.4c-4.8,0-9.6,0-16,0c-67.2,1.6-132.8,36.8-187.2,97.6    c-60.8,67.2-96,155.2-91.2,227.2c8,126.4,70.4,187.2,192,187.2c115.2,0,201.6-33.6,256-100.8    C513.313,331.2,519.713,219.2,503.713,142.4z M423.713,392c-48,59.2-126.4,89.6-230.4,89.6s-152-48-160-158.4    c-4.8-64,28.8-144,83.2-203.2c48-54.4,107.2-84.8,164.8-88c4.8,0,9.6,0,14.4,0c140.8,0,171.2,89.6,176,116.8    C486.113,219.2,481.313,320,423.713,392z"/></g></g></svg>"""

private const val FTB_SVG =
    """<svg fill="none" viewBox="0 0 487 300"><path fill="#fff" d="M48.602 0 0 49.167V271.83l15.259 27.692H89.29l16.389-27.692v-69.512h40.69l16.389-28.256v-58.774h14.693V271.83l14.694 27.692h74.598l16.954-28.822V115.288h6.216v118.678l11.303 25.996 39.56 39.56h129.415l16.389-27.692V148.065l-12.433-8.477 4.521-9.042v-83.64L428.938 0H48.602Z"/><path fill="#322A2A" d="m463.271 141.829 5.685-13.238v-78.04L424.423 8.786H54.007l-43.38 43.63v216.221l12.71 23.016h63.01l12.709-23.016V193.29h43.564l12.71-23.017v-63.579h31.103v161.943l12.709 23.016h63.298l12.709-23.016V106.694h23.543v125.253l9.331 22.416 37.098 37.29h119.675l12.709-23.016V152.105l-14.234-10.276Z"/><path fill="#65ADB9" d="m78.647 180.157 7.333 85.084-7.333 13.279H31.045l-7.333-13.279 7.333-183.675 31.432-31.614h77.161l7.333 30.309-7.333 13.28h-60.99v43.877h56.273l7.333 29.46-7.333 13.279H78.647Z"/><path fill="#9FC41E" d="M297.661 93.56h-42.906l7.333 171.7-7.333 13.28h-47.89l-7.333-13.28 7.333-171.7h-44.492l-7.333-13.28 7.333-30.309h135.288l7.333 30.309-7.333 13.28Z"/><path fill="#E65014" d="m457.095 183.303 7.333 81.956-7.333 13.28H350.521l-31.433-31.614-7.333-17.589 7.333-179.345h97.285l32.174 30.163 7.333 45.764-7.333 17.112-22.328 18.003 30.876 22.291v-.021Zm-55.594-42.718V93.558h-34.79v47.027h34.79Zm7.704 94.635v-51.896h-42.473v51.896h42.473Z"/><path fill="#0787C1" d="M23.707 57.858v207.375h62.268V166.87h56.274v-57.468H85.975V80.253h60.991V21.935H59.424L23.707 57.858Z"/><path fill="#798B2F" d="M155.021 80.253h44.492v184.98h62.556V80.253h42.927V21.935H155.021v58.318Z"/><path fill="#B11917" d="m438.345 140 17.529-14.149V56.263l-36.603-34.328H311.77V229.29l35.737 35.923h116.936V158.832l-26.077-18.852-.021.02Zm-64.307 67.164v-37.166h27.828v37.166h-27.828Zm0-94.634V80.253h20.124v32.277h-20.124Z"/><path fill="#fff" d="M78.647 159.511v98.363H31.045V60.919l31.432-31.614h77.161v43.589H78.647v43.878h56.274v42.718H78.647v.021ZM297.666 72.913H254.76v184.959h-47.891V72.912h-44.492V29.326h135.289v43.588ZM457.095 162.658v95.214H350.52l-31.432-31.613V29.325h97.285l32.174 30.163v62.855l-22.328 18.003 30.876 22.291v.021Zm-55.594-42.718V72.913h-34.79v47.027h34.79Zm7.704 94.634v-51.895h-42.474v51.895h42.474Z"/></svg>"""

val iconMap: Map<String, IconData> = mapOf(
    // MODRINTH
    "adventure" to IconNameData("compass"),
    "atmosphere" to IconNameData("cloud-sun-rain"),
    "audio" to IconNameData("headphones"),
    "blocks" to IconNameData("box"),
    "bloom" to IconNameData("lamp"),
    "cartoon" to IconNameData("brush"),
    "challenging" to IconNameData("chart-no-axes-column-increasing"),
    "colored-lighting" to IconNameData("blend"),
    "combat" to IconNameData("swords"),
    "core-shaders" to IconNameData("cpu"),
    "cursed" to IconNameData("bug"),
    "decoration" to IconNameData("house"),
    "economy" to IconNameData("dollar-sign"),
    "entities" to IconSvgData(CREEPER_SVG),
    "environment" to IconNameData("sun"),
    "equipment" to IconNameData("wrench"),
    "fantasy" to IconNameData("wand-sparkles"),
    "foliage" to IconNameData("tree-deciduous"),
    "fonts" to IconNameData("type"),
    "food" to IconNameData("carrot"),
    "game-mechanics" to IconNameData("sliders-vertical"),
    "gui" to IconNameData("panels-top-left"),
    "high" to IconNameData("signal-high"),
    "items" to IconNameData("pickaxe"),
    "kitchen-sink" to IconSvgData(SINK_SVG),
    "library" to IconNameData("book"),
    "lightweight" to IconNameData("feather"),
    "locale" to IconNameData("globe"),
    "low" to IconNameData("signal-low"),
    "magic" to IconNameData("wand"),
    "management" to IconNameData("server"),
    "medium" to IconNameData("signal-medium"),
    "minigame" to IconNameData("award"),
    "mobs" to IconSvgData(CREEPER_SVG),
    "modded" to IconNameData("square-plus"),
    "models" to IconNameData("layers"),
    "multiplayer" to IconNameData("users"),
    "optimization" to IconNameData("zap"),
    "path-tracing" to IconSvgData(PATH_TRACING_SVG),
    "pbr" to IconNameData("lightbulb"),
    "potato" to IconSvgData(POTATO_SVG),
    "quests" to IconNameData("network"),
    "realistic" to IconNameData("image"),
    "reflections" to IconNameData("flip-horizontal-2"),
    "screenshot" to IconNameData("image"),
    "semi-realistic" to IconNameData("film"),
    "shadows" to IconNameData("mountain"),
    "simplistic" to IconNameData("hexagon"),
    "social" to IconNameData("message-circle"),
    "storage" to IconNameData("archive"),
    "technology" to IconNameData("hard-drive"),
    "themed" to IconNameData("pen-tool"),
    "transportation" to IconNameData("truck"),
    "tweaks" to IconNameData("square-pen"),
    "utility" to IconNameData("toolbox"),
    "vanilla-like" to IconNameData("ice-cream-cone"),
    "worldgen" to IconNameData("earth"),
    // CURSEFORGE
    "adventure-and-rpg" to IconNameData("shield"),
    "combat-pvp" to IconNameData("swords"),
    "exploration" to IconNameData("compass"),
    "extra-large" to IconNameData("boxes"),
    "ftb-official-pack" to IconSvgData(FTB_SVG),
    "hardcore" to IconNameData("heart-crack"),
    "horror" to IconNameData("bug"),
    "map-based" to IconNameData("map"),
    "mini-game" to IconNameData("award"),
    "sci-fi" to IconNameData("drone"),
    "skyblock" to IconNameData("box"),
    "small-light" to IconNameData("feather"),
    "tech" to IconNameData("hard-drive"),
    "vanilla" to IconNameData("ice-cream-cone"),
    "adventure-rpg" to IconNameData("shield"),
    "armor-weapons-tools" to IconNameData("swords"),
    "bug-fixes" to IconNameData("bug-off"),
    "cosmetic" to IconNameData("house"),
    "creativemode" to IconNameData("blocks"),
    "education" to IconNameData("graduation-cap"),
    "library-api" to IconNameData("book"),
    "map-information" to IconNameData("map"),
    "mc-addons" to IconNameData("square-plus"),
    "mc-food" to IconNameData("carrot"),
    "mc-miscellaneous" to IconNameData("briefcase"),
    "performance" to IconNameData("zap"),
    "redstone" to IconNameData("cable"),
    "server-utility" to IconNameData("server"),
    "utility-qol" to IconNameData("toolbox"),
    "world-gen" to IconNameData("earth"),
    "creation" to IconNameData("brush"),
    "game-map" to IconNameData("map"),
    "modded-world" to IconNameData("square-plus"),
    "parkour" to IconNameData("hand-fist"),
    "puzzle" to IconNameData("puzzle"),
    "survival" to IconNameData("heart"),
    "animated" to IconNameData("video"),
    "data-packs" to IconNameData("braces"),
    "font-packs" to IconNameData("type"),
    "medieval" to IconNameData("castle"),
    "miscellaneous" to IconNameData("briefcase"),
    "mod-support" to IconNameData("square-plus"),
    "modern" to IconNameData("hard-drive"),
    "photo-realistic" to IconNameData("image"),
    "steampunk" to IconNameData("cog"),
    "modjam-2025" to IconNameData("trophy"),
    "traditional" to IconNameData("hexagon"),
)
