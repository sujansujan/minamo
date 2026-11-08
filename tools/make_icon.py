#!/usr/bin/env python3
import argparse
import os
from PIL import Image, ImageDraw

def parse_grid(path):
    with open(path, "r", encoding="utf-8") as f:
        lines = [line.strip() for line in f if line.strip() and not line.strip().startswith(";")]
    return lines

def build_vector_xml(lines, fg_hex, accent_hex, monochrome=False):
    rows = len(lines)
    cols = len(lines[0]) if rows > 0 else 11
    # Adaptive icon safe zone is centered at (54, 54) with diameter 66dp -> use 55x55 box from 26.5 to 81.5
    cell = 55.0 / max(rows, cols)
    ox = 54.0 - (cols * cell) / 2.0
    oy = 54.0 - (rows * cell) / 2.0

    fg_paths = []
    acc_paths = []
    for r, row in enumerate(lines):
        for c, ch in enumerate(row):
            if ch not in ("#", "+"):
                continue
            x0 = ox + c * cell
            y0 = oy + r * cell
            x1 = x0 + cell
            y1 = y0 + cell
            cmd = f"M{x0:.2f},{y0:.2f}H{x1:.2f}V{y1:.2f}H{x0:.2f}Z"
            if ch == "#" or monochrome:
                fg_paths.append(cmd)
            else:
                acc_paths.append(cmd)

    xml = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="108dp"',
        '    android:height="108dp"',
        '    android:viewportWidth="108"',
        '    android:viewportHeight="108">'
    ]
    if fg_paths:
        color = "#FFFFFF" if monochrome else f"#{fg_hex}"
        xml.append(f'    <path android:fillColor="{color}" android:pathData="{"".join(fg_paths)}" />')
    if acc_paths and not monochrome:
        xml.append(f'    <path android:fillColor="#{accent_hex}" android:pathData="{"".join(acc_paths)}" />')
    xml.append('</vector>')
    return "\n".join(xml) + "\n"

def generate_icons(grid_path, bg_hex, fg_hex, accent_hex, res_dir, png_out):
    lines = parse_grid(grid_path)
    drawable_dir = os.path.join(res_dir, "drawable")
    values_dir = os.path.join(res_dir, "values")
    mipmap_dir = os.path.join(res_dir, "mipmap-anydpi-v26")
    os.makedirs(drawable_dir, exist_ok=True)
    os.makedirs(values_dir, exist_ok=True)
    os.makedirs(mipmap_dir, exist_ok=True)

    fg_xml = build_vector_xml(lines, fg_hex, accent_hex, monochrome=False)
    mono_xml = build_vector_xml(lines, fg_hex, accent_hex, monochrome=True)

    with open(os.path.join(drawable_dir, "ic_launcher_foreground.xml"), "w", encoding="utf-8") as f:
        f.write(fg_xml)
    with open(os.path.join(drawable_dir, "ic_wallpaper_thumb.xml"), "w", encoding="utf-8") as f:
        f.write(fg_xml)
    with open(os.path.join(drawable_dir, "ic_launcher_monochrome.xml"), "w", encoding="utf-8") as f:
        f.write(mono_xml)

    icon_colors_xml = f'''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#{bg_hex}</color>
</resources>
'''
    with open(os.path.join(values_dir, "icon_colors.xml"), "w", encoding="utf-8") as f:
        f.write(icon_colors_xml)

    adaptive_xml = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
'''
    with open(os.path.join(mipmap_dir, "ic_launcher.xml"), "w", encoding="utf-8") as f:
        f.write(adaptive_xml)
    with open(os.path.join(mipmap_dir, "ic_launcher_round.xml"), "w", encoding="utf-8") as f:
        f.write(adaptive_xml)

    # Render crisp 512x512 Play Store PNG
    if png_out:
        os.makedirs(os.path.dirname(png_out), exist_ok=True)
        size = 512
        img = Image.new("RGB", (size, size), tuple(int(bg_hex[i:i+2], 16) for i in (0, 2, 4)))
        draw = ImageDraw.Draw(img)
        rows = len(lines)
        cols = len(lines[0])
        cell = 352.0 / max(rows, cols)
        ox = (size - cols * cell) / 2.0
        oy = (size - rows * cell) / 2.0
        fg_rgb = tuple(int(fg_hex[i:i+2], 16) for i in (0, 2, 4))
        acc_rgb = tuple(int(accent_hex[i:i+2], 16) for i in (0, 2, 4))
        for r, row in enumerate(lines):
            for c, ch in enumerate(row):
                if ch not in ("#", "+"):
                    continue
                x0 = int(round(ox + c * cell))
                y0 = int(round(oy + r * cell))
                x1 = int(round(ox + (c + 1) * cell)) - 4
                y1 = int(round(oy + (r + 1) * cell)) - 4
                draw.rectangle([x0, y0, x1, y1], fill=(fg_rgb if ch == "#" else acc_rgb))
        img.save(png_out)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("grid")
    parser.add_argument("--bg", default="0A1D26")
    parser.add_argument("--fg", default="5EEAD4")
    parser.add_argument("--accent", default="FF7A59")
    parser.add_argument("--res", default="app/src/main/res")
    parser.add_argument("--png-out", default="store/icon_512.png")
    args = parser.parse_args()
    generate_icons(args.grid, args.bg, args.fg, args.accent, args.res, args.png_out)
