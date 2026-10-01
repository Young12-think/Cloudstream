# NOTICE — Atribusi

Repo ini (Young12-think/cloudstream) adalah karya gabungan dari dua proyek
sumber. Logika scraping tiap provider dipertahankan identik dengan sumbernya;
yang diubah hanya mekanisme resolusi domain (override pengaturan → JSON remote
di repo ini → domain bawaan).

## Sumber provider anime (17) — `sites-anime.json`, `plugin/*Provider`
- Proyek: **AnimeX** oleh HatsuneMikuUwU
- Repo: https://github.com/HatsuneMikuUwU/AnimeX
- Lisensi: **GNU General Public License v3.0 (GPLv3)**
- Karena kode GPLv3 digabungkan ke dalam karya ini, keseluruhan karya
  (termasuk plugin di `plugin/`) didistribusikan di bawah **GPLv3**.
  Lihat file `LICENSE`.

## Sumber provider film (16 situs / 15 provider) — `sites-movie.json`, `plugin/*`
- Proyek: **CloudX-V2** oleh Asm0d3usX
- Repo: https://github.com/Asm0d3usX/CloudX-V2
- Lisensi: **MIT License**
- Kode MIT boleh digabung ke dalam karya GPLv3; atribusi penulis asli
  dipertahankan di file `build.gradle.kts` tiap modul (`authors`).

## Perubahan oleh Young12-think/cloudstream
- Konfigurasi domain terpusat: `sites-anime.json`, `sites-movie.json`.
- Tiap provider kini me-resolve domain dengan urutan:
  1. Override manual pengguna (pengaturan ekstensi di aplikasi CloudStream),
  2. JSON remote di repo ini (cache memori 6 jam),
  3. Domain bawaan (fallback, sama seperti kode sumber).
- `mainPage` diubah menjadi getter agar URL memakai domain hasil resolusi.
- Plugin `AnimexinProvider` dan `PencurimoviePlugin` diubah dari `BasePlugin`
  menjadi `Plugin` agar mendukung tombol pengaturan domain.

Catatan: situs `nontonfilm` (surgafilm21) hanya ada di `sites-movie.json`
sebagai data domain; tidak ada kode provider sumbernya di CloudX-V2,
sehingga belum ada modul plugin untuknya.
