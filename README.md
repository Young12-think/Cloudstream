# CloudstreamID

Koleksi ekstensi CloudStream untuk **anime/donghua** dan **film/series subtitle Indonesia**,
dengan **domain terpusat** — ganti domain cukup edit JSON, tanpa rebuild plugin.

Sumber provider:
- Anime (17): [HatsuneMikuUwU/AnimeX](https://github.com/HatsuneMikuUwU/AnimeX) — GPLv3
- Film (15 provider / 16 situs): [Asm0d3usX/CloudX-V2](https://github.com/Asm0d3usX/CloudX-V2) — MIT

Atribusi lengkap: [NOTICE.md](NOTICE.md). Lisensi keseluruhan: **GPLv3** ([LICENSE](LICENSE)).

## Struktur

```
├── sites-anime.json        # daftar situs anime + domain (dibaca plugin saat runtime)
├── sites-movie.json        # daftar situs film + domain
├── scripts/
│   └── check_links.py      # health-check domain (python3 scripts/check_links.py)
├── plugin/                 # proyek Gradle CloudStream (cs3 API)
│   ├── <Provider>/         # 32 modul plugin, satu .cs3 per provider
│   ├── build.gradle.kts    # root: AGP 9.1.1, Kotlin 2.4.20
│   ├── settings.gradle.kts
│   └── repo.json           # metadata repo plugin
├── LICENSE                 # GPLv3
└── NOTICE.md               # atribusi sumber
```

## Cara kerja domain (3 lapis)

Setiap provider me-resolve domain dengan urutan ini, setiap kali dipanggil:

1. **Override pengguna** — tombol pengaturan di tiap ekstensi (dalam aplikasi
   CloudStream → Extensions → ⚙) membuka dialog input domain manual.
   Disimpan di SharedPreferences per provider.
2. **JSON remote** — `sites-anime.json` / `sites-movie.json` dari branch `main`
   repo ini (`raw.githubusercontent.com`), dicocokkan lewat field `id`,
   diambil `domains[0]`. Hasilnya di-cache di memori selama 6 jam.
   Entri berstatus `"dead"` dilewati.
3. **Fallback bawaan** — domain yang tertulis di kode sumber asli.

Implementasi: tiap modul punya `<Nama>Domain.kt` (`resolveDomain()` /
`resolveDomains()`), dipanggil di awal `getMainPage` / `search` / `load` /
`loadLinks` (provider CloudX memakai `loadMainUrlIfNeeded()` seperti aslinya).
`mainPage` dijadikan getter agar URL-nya memakai domain hasil resolusi.

## Update domain tanpa rebuild

Edit `sites-anime.json` / `sites-movie.json`, commit, push. Plugin yang
terinstal otomatis memakai domain baru (maks. 6 jam setelah cache habis,
atau segera jika pengguna memakai override manual).

Cek kesehatan domain:

```bash
python3 scripts/check_links.py
```

## Build plugin (.cs3)

Kebutuhan: **JDK 17**, **Android SDK** (platform 35, build-tools), internet
untuk dependensi Gradle.

```bash
cd plugin
./gradlew assemble
# hasil: <Provider>/build/outputs/...
```

Untuk menerbitkan ke repo plugin (dipakai aplikasi CloudStream):

```bash
./gradlew makePluginsJson   # generate plugins.json ke branch builds (via CI)
```

> Catatan: repo ini belum punya workflow CI untuk branch `builds`.
> Tambahkan GitHub Action yang menjalankan build + push `.cs3` dan
> `plugins.json` ke branch `builds` bila ingin distribusi otomatis.

## Daftar provider

### Anime (17) — dari AnimeX

| id | Nama | Domain |
|----|------|--------|
| alqanime | Alqanime | https://alqanime.net |
| anichin | Anichin | https://anichin.moe |
| animasu | Animasu | https://animasu.love |
| animesail | AnimeSail | https://anisail.com |
| animein | Animein (API) | https://xyz-api.animein.net, https://api.animein.net |
| animexin | Animexin | https://animexin.dev |
| anoboy | AnoBoy | https://anoboy.quest |
| donghub | Donghub | https://donghive.vip |
| dubbindo | Dubbindo | https://www.dubbindo.site |
| kuramanime | Kuramanime | https://v20.kuramanime.ing |
| kuronime | Kuronime | https://kuronime.sbs |
| nekopoi | NekoPoi | https://nekopoi.care |
| nimegami | Nimegami | https://nimegami.id |
| nontonanimeid | NontonAnimeID | https://s13.nontonanimeid.boats |
| otakudesu | OtakuDesu | https://otakudesu.blog |
| samehadaku | Samehadaku | https://v2.samehadaku.how |
| winbu | Winbu | https://winbu.org |

### Film (16 situs / 15 provider) — dari CloudX-V2

| id | Nama | Domain |
|----|------|--------|
| dutamovie | Dutamovie | https://itoshii-movie.com |
| filmkita | Filmkita | https://s9.iix.llc |
| filmlokal | Filmlokal | https://tv1.filmlokal.me |
| indomax | IndoMax | https://idmxl.ink |
| kawanfilm | Kawanfilm | https://tv2.kawanfilm21.co |
| klikxxi | KlikXXi | https://klikxxi.shop |
| layarwarna | LayarWarna | https://hisgloryco.com |
| ngefilm | Ngefilm | https://new39.ngefilm.site |
| nomat | Nomat | https://nomat.shop |
| nontonfilm | NontonFilm | https://surgafilm21.homes |
| pencurimovie | PencuriMovie | https://ww11.pencurimovie.sbs |
| pusatfilm | PusatFilm | https://pf21.net |
| pusatmovie | PusatMovie | https://refugepdx.com |
| sarangfilm | SarangFilm | https://sarangfilm.diy |
| savefilm | SaveFilm | https://new13.savefilm21info.com |
| wgfilm21 | WGFilm21 | https://alternatif3.wgfilm21.net |

> `nontonfilm` hanya ada sebagai data domain di `sites-movie.json`;
> tidak ada kode provider sumbernya di CloudX-V2 sehingga belum ada modul plugin.

## Catatan adaptasi dari kode sumber

- Logika scraping (selector, extractor, API) **tidak diubah**.
- Provider CloudX: `loadMainUrlIfNeeded()` kini memakai resolver 3 lapis;
  `directUrl` tetap di-set (dipakai `loadExtractor` sebagai referer).
- Provider Animein (API): `apiBase` ikut di-set dari hasil resolusi; mekanisme
  failover antar-host bawaannya tetap jalan.
- `AnimexinProvider` & `PencurimoviePlugin`: diubah dari `BasePlugin` ke
  `Plugin` agar mendapat `Context` dan tombol pengaturan domain.
