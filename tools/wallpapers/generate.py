#!/usr/bin/env python3
"""Mono's bundled wallpapers: soft gradients, large blurred organic blobs and a touch of grain.

Original art, fully reproducible: every image comes from the fixed recipes and seeds below.

    python3 tools/wallpapers/generate.py            # writes app/src/main/res/drawable-nodpi/
    python3 tools/wallpapers/generate.py --out DIR  # somewhere else, e.g. to compare

Needs Pillow (with WebP) and numpy. Each image is 1440x3200, lossy WebP at quality 80.
"""

import argparse
import math
from dataclasses import dataclass, field
from pathlib import Path

import numpy as np
from PIL import Image

WIDTH, HEIGHT = 1440, 3200
# The colour fields are smooth, so they are drawn at a quarter size and scaled up; only the grain
# is drawn at full size.
FIELD_SCALE = 4
QUALITY = 80
# Grain size in pixels.
GRAIN_SIZE = 2
DEFAULT_OUT = Path(__file__).resolve().parents[2] / "app/src/main/res/drawable-nodpi"


def hex_rgb(value: str) -> np.ndarray:
    value = value.lstrip("#")
    return np.array([int(value[i:i + 2], 16) for i in (0, 2, 4)], dtype=np.float64) / 255.0


def to_linear(c: np.ndarray) -> np.ndarray:
    return np.where(c <= 0.04045, c / 12.92, ((c + 0.055) / 1.055) ** 2.4)


def to_srgb(c: np.ndarray) -> np.ndarray:
    c = np.clip(c, 0.0, 1.0)
    return np.where(c <= 0.0031308, c * 12.92, 1.055 * c ** (1 / 2.4) - 0.055)


@dataclass
class Blob:
    """A soft ellipse of colour: centre and radii as fractions of the width, strength 0..1."""

    color: str
    x: float
    y: float
    rx: float
    ry: float
    strength: float = 0.85
    angle: float = 0.0


@dataclass
class Recipe:
    name: str
    # (position 0..1 from top, colour) stops of the vertical base gradient.
    stops: list
    blobs: list = field(default_factory=list)
    grain: float = 0.02
    warp: float = 0.08
    seed: int = 1
    # Darkens towards the edges, 0 for none.
    vignette: float = 0.0
    monochrome: bool = False


def gradient(stops, h: int, w: int) -> np.ndarray:
    ys = np.linspace(0.0, 1.0, h)
    positions = np.array([p for p, _ in stops])
    colors = np.array([to_linear(hex_rgb(c)) for _, c in stops])
    column = np.stack([np.interp(ys, positions, colors[:, ch]) for ch in range(3)], axis=-1)
    return np.repeat(column[:, None, :], w, axis=1)


def warped_grid(h: int, w: int, amount: float, rng: np.random.Generator):
    """Coordinates in width units, bent by a few slow sine waves so blobs read organic, not round."""
    aspect = h / w
    u, v = np.meshgrid(np.linspace(0.0, 1.0, w), np.linspace(0.0, aspect, h))
    for _ in range(3):
        fu, fv = rng.uniform(1.5, 4.0, 2)
        pu, pv = rng.uniform(0, 2 * math.pi, 2)
        u, v = u + amount * np.sin(v * fv * math.pi + pv), v + amount * np.sin(u * fu * math.pi + pu)
        amount *= 0.55
    return u, v


def paint(recipe: Recipe) -> Image.Image:
    rng = np.random.default_rng(recipe.seed)
    h, w = HEIGHT // FIELD_SCALE, WIDTH // FIELD_SCALE
    image = gradient(recipe.stops, h, w)
    u, v = warped_grid(h, w, recipe.warp, rng)
    for blob in recipe.blobs:
        cos, sin = math.cos(blob.angle), math.sin(blob.angle)
        du, dv = u - blob.x, v - blob.y
        a, b = (du * cos + dv * sin) / blob.rx, (-du * sin + dv * cos) / blob.ry
        alpha = (np.exp(-(a * a + b * b)) * blob.strength)[..., None]
        image = image * (1 - alpha) + to_linear(hex_rgb(blob.color)) * alpha
    if recipe.vignette:
        cu, cv = u - 0.5, v - (h / w) / 2
        falloff = np.clip(1 - recipe.vignette * (cu * cu + 0.35 * cv * cv) * 2.2, 0, 1)
        image *= falloff[..., None]
    if recipe.monochrome:
        luma = image @ np.array([0.2126, 0.7152, 0.0722])
        image = np.repeat(luma[..., None], 3, axis=-1)
    # Pillow has no float RGB mode, so the field is scaled up one float channel at a time.
    channels = [
        Image.fromarray(to_srgb(image[..., ch]).astype(np.float32), "F").resize((WIDTH, HEIGHT), Image.BICUBIC)
        for ch in range(3)
    ]
    full = np.stack([np.asarray(c, dtype=np.float64) for c in channels], axis=-1)
    # Grain: luminance noise, a little stronger in the shadows where banding shows most. It also
    # dithers the gradients, so they never band at 8 bits. Grains span two pixels, like film at
    # this density, which also keeps them from being smoothed away by the WebP encoder.
    coarse = rng.normal(0.0, 1.0, (HEIGHT // GRAIN_SIZE, WIDTH // GRAIN_SIZE)).astype(np.float32)
    noise = np.asarray(Image.fromarray(coarse, "F").resize((WIDTH, HEIGHT), Image.BILINEAR))[..., None]
    shade = 1.0 - 0.5 * full.mean(axis=-1, keepdims=True)
    full = full + noise * recipe.grain * shade
    return Image.fromarray((np.clip(full, 0, 1) * 255 + 0.5).astype(np.uint8), "RGB")


RECIPES = [
    # Dark, AMOLED-friendly: mostly true black, colour pooled low and to one side.
    Recipe(
        "ink",
        stops=[(0.0, "#000000"), (0.55, "#03040a"), (1.0, "#0b0d22")],
        blobs=[
            Blob("#2a2f8f", 0.15, 1.95, 0.75, 0.55, 0.75, 0.4),
            Blob("#5b3fa6", 0.95, 1.70, 0.45, 0.35, 0.55, -0.3),
            Blob("#0f6c8a", 0.55, 2.25, 0.6, 0.25, 0.45),
        ],
        seed=11, vignette=0.25,
    ),
    Recipe(
        "ember",
        stops=[(0.0, "#000000"), (0.6, "#070302"), (1.0, "#1a0905")],
        blobs=[
            Blob("#b8431c", 0.85, 1.85, 0.6, 0.45, 0.8, 0.5),
            Blob("#e08a3c", 0.95, 2.10, 0.32, 0.25, 0.6),
            Blob("#7a1f3d", 0.05, 2.05, 0.55, 0.4, 0.6, -0.2),
        ],
        seed=23, vignette=0.3,
    ),
    Recipe(
        "moss",
        stops=[(0.0, "#020403"), (0.5, "#06100c"), (1.0, "#0d1d17")],
        blobs=[
            Blob("#2f6b4f", 0.2, 0.45, 0.7, 0.5, 0.55, 0.7),
            Blob("#5e8c5a", 0.9, 1.6, 0.55, 0.6, 0.5, -0.5),
            Blob("#16443f", 0.4, 2.0, 0.8, 0.4, 0.6),
        ],
        seed=37, vignette=0.2,
    ),
    # Light pastels.
    Recipe(
        "dune",
        stops=[(0.0, "#f6ede2"), (0.5, "#efdcc7"), (1.0, "#e6c3a6")],
        blobs=[
            Blob("#f7c8a8", 0.85, 0.5, 0.6, 0.45, 0.7, 0.3),
            Blob("#fbf3e8", 0.15, 1.15, 0.7, 0.5, 0.75),
            Blob("#d99b84", 0.6, 1.95, 0.75, 0.45, 0.55, -0.4),
        ],
        seed=41,
    ),
    Recipe(
        "mist",
        stops=[(0.0, "#eef1fa"), (0.5, "#e2e4f6"), (1.0, "#d7dcf2")],
        blobs=[
            Blob("#c9d6fb", 0.1, 0.55, 0.7, 0.5, 0.75, 0.4),
            Blob("#e6d4f4", 0.95, 1.2, 0.6, 0.55, 0.7, -0.3),
            Blob("#bfe1ef", 0.35, 1.95, 0.8, 0.45, 0.6),
        ],
        seed=53,
    ),
    Recipe(
        "sage",
        stops=[(0.0, "#eef2ea"), (0.55, "#dfe7d8"), (1.0, "#cfdcc9")],
        blobs=[
            Blob("#b9cfb0", 0.9, 0.4, 0.65, 0.45, 0.7, -0.4),
            Blob("#f4f1e3", 0.2, 1.05, 0.7, 0.5, 0.65),
            Blob("#9fbfae", 0.55, 1.9, 0.8, 0.45, 0.6, 0.3),
        ],
        seed=67,
    ),
    # Mid tones: a plum dusk, and one monochrome.
    Recipe(
        "dusk",
        stops=[(0.0, "#241631"), (0.45, "#4a2a4f"), (0.8, "#8c4d5c"), (1.0, "#c7826c")],
        blobs=[
            Blob("#e3a07a", 0.75, 2.05, 0.7, 0.35, 0.6, 0.2),
            Blob("#5a2f6e", 0.1, 0.6, 0.7, 0.6, 0.6, 0.5),
            Blob("#b0607a", 0.95, 1.25, 0.5, 0.45, 0.45, -0.4),
        ],
        seed=79, warp=0.06,
    ),
    Recipe(
        "graphite",
        stops=[(0.0, "#1b1b1c"), (0.5, "#2a2a2c"), (1.0, "#3a3a3d")],
        blobs=[
            Blob("#5a5a5e", 0.85, 0.55, 0.6, 0.5, 0.6, 0.6),
            Blob("#0e0e0f", 0.1, 1.35, 0.7, 0.55, 0.7),
            Blob("#77777b", 0.45, 2.05, 0.75, 0.35, 0.45, -0.3),
        ],
        seed=97, vignette=0.15, monochrome=True,
    ),
]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)
    for recipe in RECIPES:
        path = args.out / f"wallpaper_{recipe.name}.webp"
        paint(recipe).save(path, "WEBP", quality=QUALITY, method=6)
        print(f"{path.name}: {path.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
