from pathlib import Path

APPNAME = "dataset"
#SCRIPT PER ASSOCIARE IL PRODUCTION FILE AD OGNI CLASSE DI TEST
def main():
    SCRIPT_DIR = Path(__file__).resolve().parent
    # .../estraiTest
    PROJECT_ROOT = SCRIPT_DIR.parent

    DATASET_DIR = PROJECT_ROOT / "DATASET"
    OUT_DIR = PROJECT_ROOT / "reports" / "raw"
    OUT = OUT_DIR / "TestClass.csv"


    if not DATASET_DIR.exists():
        raise FileNotFoundError(f"DATASET_DIR non esiste: {DATASET_DIR}")

    # usa rglob se i test sono anche in sottocartelle
    files = sorted(DATASET_DIR.rglob("*.java"))

    # Debug utile
    print("PROJECT_ROOT:", PROJECT_ROOT)
    print("DATASET_DIR:", DATASET_DIR)
    print("Trovati .java:", len(files))

    lines = []
    for f in files:
        # path RELATIVO al progetto
        test_path = f.relative_to(PROJECT_ROOT).as_posix()

        # terza colonna vuota = niente production associata
        lines.append(f"{APPNAME},{test_path},")

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {len(files)} rows to {OUT}")

if __name__ == "__main__":
    main()
