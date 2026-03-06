import re
import shutil
from pathlib import Path
from typing import Optional, Dict, Tuple

#SCRIPT PER ESTRARRE CLASSI DAL SERVER 1
SOURCE_ROOT = Path(r"C:\Users\lidia\Downloads\Server1_VolumeT0_2025-10-26_17-08")  # root estratto
OUT_DIR = Path(r"/DATASET")
LOG_FILE = OUT_DIR / "_dataset_log.csv"

# Nel tuo dataset: PlayerXX e GameYYY
PLAYER_RE = re.compile(r"(?:^|[\\/])Player(?P<id>\d+)(?:[\\/]|$)", re.IGNORECASE)
GAME_RE   = re.compile(r"(?:^|[\\/])Game(?P<num>\d+)(?:[\\/]|$)", re.IGNORECASE)

# Username dentro al file: "Username: michele.perlotto@unina.it"
USERNAME_RE = re.compile(r"Username:\s*(?P<user>[^\s\r\n]+)", re.IGNORECASE)

# Test file: in base al dataset basta ".java" sotto src/test
# Se vuoi restringere a *Test.java / *Tests.java, dimmelo e lo stringiamo.
TEST_FILE_RE = re.compile(r".*\.java$", re.IGNORECASE)

# =========================

def is_test_java(p: Path) -> bool:
    s = str(p).replace("\\", "/").lower()
    if "/src/test/" not in s:
        return False
    return bool(TEST_FILE_RE.match(p.name))

def extract_player_id(p: Path) -> Optional[str]:
    m = PLAYER_RE.search(str(p))
    return m.group("id") if m else None

def extract_game_num(p: Path) -> Optional[int]:
    m = GAME_RE.search(str(p))
    return int(m.group("num")) if m else None

def extract_username_from_file(p: Path) -> Optional[str]:
    # Lettura robusta: molti dataset hanno encoding misto
    try:
        txt = p.read_text(encoding="utf-8", errors="ignore")
    except Exception:
        return None
    m = USERNAME_RE.search(txt)
    if not m:
        return None
    return m.group("user").strip()

def sanitize_for_filename(s: str) -> str:
    """
    Windows-safe filename:
    - sostituisce caratteri non validi con "_"
    - riduce sequenze di "_" ripetute
    """
    s = s.strip()
    # sostituisci separatori e caratteri proibiti
    s = re.sub(r'[<>:"/\\|?*\s]+', "_", s)
    # @ e . li puoi tenere? "." sì, "@" è ok su Windows, ma spesso è scomodo.
    # Per sicurezza trasformo '@' in '_'
    s = s.replace("@", "_")
    # comprimi underscore multipli
    s = re.sub(r"_+", "_", s)
    # evita underscore finali/iniziali
    s = s.strip("_")
    # fallback se vuoto
    return s if s else "unknown"

def unique_path(base: Path) -> Path:
    """Evita overwrite: se esiste già, aggiunge _2, _3, ..."""
    if not base.exists():
        return base
    stem = base.stem
    suffix = base.suffix
    i = 2
    while True:
        cand = base.with_name(f"{stem}_{i}{suffix}")
        if not cand.exists():
            return cand
        i += 1

def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    # best[(player_id, class_key)] = (game_num, path)
    best: Dict[Tuple[str, str], Tuple[int, Path]] = {}

    scanned = 0
    skipped = 0

    for f in SOURCE_ROOT.rglob("*.java"):
        if not is_test_java(f):
            continue

        scanned += 1
        player_id = extract_player_id(f)
        game_num = extract_game_num(f)

        if player_id is None or game_num is None:
            skipped += 1
            continue

        class_key = f.stem  # nome file senza .java
        key = (player_id, class_key)

        if key not in best or game_num > best[key][0]:
            best[key] = (game_num, f)

    log_lines = ["player_id,username_or_student,class_key,selected_game,original_path,stored_path"]
    copied = 0

    for (player_id, class_key), (game_num, src_path) in sorted(best.items(), key=lambda x: (int(x[0][0]), x[0][1])):
        username = extract_username_from_file(src_path)

        if username:
            prefix = sanitize_for_filename(username)
        else:
            prefix = f"student{player_id}"

        # nome richiesto: username_ClassediTest.java oppure studentNum_ClassediTest.java
        dst_name = f"{prefix}_{class_key}.java"
        dst_path = unique_path(OUT_DIR / dst_name)

        shutil.copy2(src_path, dst_path)
        copied += 1

        log_lines.append(
            f"{player_id},{prefix},{class_key},{game_num},"
            f"{str(src_path).replace(',', '_')},"
            f"{str(dst_path).replace(',', '_')}"
        )

    LOG_FILE.write_text("\n".join(log_lines), encoding="utf-8")

    print(f"Scanned test .java files: {scanned}")
    print(f"Skipped (missing Player/Game in path): {skipped}")
    print(f"Selected & copied to DATASET: {copied}")
    print(f"Log: {LOG_FILE}")

if __name__ == "__main__":
    main()
