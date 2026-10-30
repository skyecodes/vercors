#!/usr/bin/env bash
# Regenerates public/logo.{png,ico,icns} from public/logo.svg.
# Needs rsvg-convert (librsvg), ImageMagick 7 (magick) and python3.
set -euo pipefail

cd "$(dirname "$0")/.."
SVG=public/logo.svg
OUT=public

for tool in rsvg-convert magick python3; do
    command -v "$tool" >/dev/null || { echo "Missing required tool: $tool" >&2; exit 1; }
done

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

# Rendering each size from the SVG keeps small icons sharper than downscaling one big bitmap
for size in 16 24 32 48 64 128 256 512 1024; do
    rsvg-convert -w "$size" -h "$size" "$SVG" -o "$TMP/$size.png"
done

cp "$TMP/512.png" "$OUT/logo.png"

magick "$TMP"/{16,24,32,48,64,128,256}.png "$OUT/logo.ico"

# No iconutil outside macOS: an ICNS is just typed PNG chunks plus a table of contents
python3 -I - "$TMP" "$OUT/logo.icns" <<'EOF'
import struct, sys
from pathlib import Path

tmp, out = Path(sys.argv[1]), Path(sys.argv[2])
types = [("ic07", 128), ("ic08", 256), ("ic09", 512), ("ic10", 1024),
         ("ic11", 32), ("ic12", 64), ("ic13", 256), ("ic14", 512)]
chunks = [(t.encode(), (tmp / f"{size}.png").read_bytes()) for t, size in types]
toc = b"".join(t + struct.pack(">I", len(data) + 8) for t, data in chunks)
body = b"TOC " + struct.pack(">I", len(toc) + 8) + toc
body += b"".join(t + struct.pack(">I", len(data) + 8) + data for t, data in chunks)
out.write_bytes(b"icns" + struct.pack(">I", len(body) + 8) + body)
EOF

echo "Regenerated $OUT/logo.png, $OUT/logo.ico and $OUT/logo.icns from $SVG"
