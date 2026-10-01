#!/usr/bin/env python3
"""Cek kesehatan domain di sites-anime.json & sites-movie.json.

Pakai: python3 scripts/check_links.py
Bisa juga dipasang sebagai GitHub Action (jalan terjadwal tiap 6 jam).
Hanya pakai stdlib — tanpa install apa-apa.

Strategi: coba HEAD dulu (cepat), kalau gagal / 4xx coba GET.
Hasil dikelompokkan: OK, BLOCKED (403/anti-bot — kemungkinan hidup),
API (root API 404 — wajar), TIMEOUT, DNS/ERROR.
"""
import json
import socket
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FILES = [ROOT / "sites-anime.json", ROOT / "sites-movie.json"]
TIMEOUT = 15
UA = {"User-Agent": "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36"}


def fetch(url: str, method: str) -> tuple[int | None, str]:
    req = urllib.request.Request(url, method=method, headers=UA)
    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
            return r.status, ""
    except urllib.error.HTTPError as e:
        return e.code, ""
    except (urllib.error.URLError, socket.timeout, TimeoutError, OSError) as e:
        reason = str(getattr(e, "reason", e)) or type(e).__name__
        return None, reason
    except Exception as e:  # noqa: BLE001
        return None, f"{type(e).__name__}"


def check(url: str) -> tuple[str, str]:
    """-> (kategori, info). Kategori: OK, BLOCKED, API, TIMEOUT, ERROR."""
    status, err = fetch(url, "HEAD")
    if status is None or status in (403, 405, 429):
        # HEAD diblokir / tidak diizinkan -> coba GET sungguhan
        status, err = fetch(url, "GET")

    if status is not None and 200 <= status < 400:
        return "OK", f"HTTP {status}"
    if status == 403:
        return "BLOCKED", "HTTP 403 (anti-bot? coba buka manual)"
    if status == 404 and ("api." in url or "-api." in url):
        return "API", "HTTP 404 di root API (wajar, bukan halaman web)"
    if status is not None:
        return "ERROR", f"HTTP {status}"
    low = err.lower()
    if "timed out" in low or "timeout" in low:
        return "TIMEOUT", "timeout 15 dtk"
    if "name or service not known" in low or "nodename" in low or "dns" in low:
        return "ERROR", f"DNS gagal: {err[:60]}"
    return "ERROR", err[:80]


def main() -> int:
    counts: dict[str, int] = {}
    problems = []
    total = 0
    for path in FILES:
        data = json.loads(path.read_text())
        print(f"\n== {path.name} ==")
        for site in data["sites"]:
            for domain in site["domains"]:
                total += 1
                cat, info = check(domain)
                counts[cat] = counts.get(cat, 0) + 1
                print(f"  [{cat:7}] {site['id']:15} {domain}  ({info})")
                if cat not in ("OK", "API"):
                    problems.append((site["id"], domain, cat, info))

    print(f"\nRingkasan {total} domain: " + ", ".join(
        f"{k}={v}" for k, v in sorted(counts.items())))
    if problems:
        print("\nPerlu cek manual (terutama dari HP/Indonesia):")
        for sid, domain, cat, info in problems:
            print(f"  - [{cat}] {sid}: {domain} ({info})")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
