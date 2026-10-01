#!/usr/bin/env python3
"""Cek kesehatan domain di sites-anime.json & sites-movie.json.

Pakai: python3 scripts/check_links.py
Bisa juga dipasang sebagai GitHub Action (jalan terjadwal tiap 6 jam).
Hanya pakai stdlib — tanpa install apa-apa.
"""
import json
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FILES = [ROOT / "sites-anime.json", ROOT / "sites-movie.json"]
TIMEOUT = 15


def check(url: str) -> tuple[bool, str]:
    req = urllib.request.Request(
        url, method="HEAD", headers={"User-Agent": "Mozilla/5.0"}
    )
    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
            return (200 <= r.status < 400, f"HTTP {r.status}")
    except Exception as e:  # noqa: BLE001 - laporkan apa pun penyebabnya
        return (False, f"{type(e).__name__}: {e}")


def main() -> int:
    dead = []
    total = 0
    for path in FILES:
        data = json.loads(path.read_text())
        print(f"\n== {path.name} ==")
        for site in data["sites"]:
            for domain in site["domains"]:
                total += 1
                ok, info = check(domain)
                mark = "OK  " if ok else "MATI"
                print(f"  [{mark}] {site['id']:15} {domain}  ({info})")
                if not ok:
                    dead.append((site["id"], domain, info))

    print(f"\n{total - len(dead)}/{total} domain hidup.")
    if dead:
        print("\nDomain mati / bermasalah:")
        for sid, domain, info in dead:
            print(f"  - {sid}: {domain} ({info})")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
