import pandas as pd
import re
from pathlib import Path

# SCRIPT PER CREARE IL PRIMO CSV CON LE CLASSI DI TEST
PROJECT_ROOT = Path(__file__).resolve().parents[1]   # estraiTest/
CSV_INPUT = PROJECT_ROOT / "reports" / "raw" / "TestClass.csv"
CSV_OUTPUT = PROJECT_ROOT / "reports" / "raw" / "InputFile_TsDet.csv"

PRODUCTION_DIR = Path("ProductionFile")


def extract_production_class_name(test_path: str):
    filename = Path(test_path).name
    m = re.search(r"(?:^|.*_)Test(?:Test)?([A-Za-z0-9_]+)\.java$", filename)
    return f"{m.group(1)}.java" if m else None


# === LOAD CSV (SENZA HEADER) ===
df = pd.read_csv(
    CSV_INPUT,
    header=None,          # 🔴 fondamentale
    names=["appName", "pathToTestFile", "pathToProductionFile"],
    dtype=str
)

# assicuriamoci che la terza colonna esista
df["pathToProductionFile"] = df["pathToProductionFile"].fillna("")

# === PROCESS ===
for idx, row in df.iterrows():
    test_path = row["pathToTestFile"]
    if not test_path:
        continue

    prod_class = extract_production_class_name(test_path)
    if not prod_class:
        continue

    # aggiungiamo SOLO la terza colonna
    df.at[idx, "pathToProductionFile"] = (PRODUCTION_DIR / prod_class).as_posix()

# === SAVE (ANCORA SENZA HEADER) ===
df.to_csv(CSV_OUTPUT, index=False, header=False)
print("✅ Terza colonna pathToProductionFile aggiunta")
print(f"📄 File salvato in {CSV_OUTPUT}")
