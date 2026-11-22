"""Draws the MP4 Looper icon (same design as the Android icon) and saves windows/mp4looper.ico."""
from pathlib import Path
from PIL import Image, ImageDraw

S = 1024                      # draw big, then shrink for smooth edges
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# Dark rounded square
d.rounded_rectangle([40, 40, S - 40, S - 40], radius=200, fill=(23, 23, 29, 255))

# Cyan loop ring, open at the top right
c, r, w = S / 2, 300, 64
d.arc([c - r, c - r, c + r, c + r], start=-30, end=290, fill=(37, 244, 238, 255), width=w)
# Arrow head at the open end (about 290 degrees = top, slightly right)
d.polygon([(600, 150), (790, 250), (610, 360)], fill=(37, 244, 238, 255))

# Pink play triangle
d.polygon([(430, 360), (660, 512), (430, 664)], fill=(254, 44, 85, 255))

out = Path(__file__).with_name("mp4looper.ico")
img.save(out, sizes=[(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
img.resize((256, 256), Image.LANCZOS).save(Path(__file__).with_name("icon-preview.png"))
print("saved", out)
