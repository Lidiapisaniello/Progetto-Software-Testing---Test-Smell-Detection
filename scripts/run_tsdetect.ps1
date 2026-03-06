$ErrorActionPreference = "Stop"

$ProjectRoot = "C:\Users\lidia\PycharmProjects\estraiTest"
$Jar         = Join-Path $ProjectRoot "tools\TestSmellDetector.jar"
$InputCsv    = Join-Path $ProjectRoot "reports\raw\inputFile_TsDet.csv"
$OutputCsv   = Join-Path $ProjectRoot "reports\raw\tsdetect_output.csv"
$TempCsv     = Join-Path $ProjectRoot "reports\raw\temp_tsdetect.csv"
$Log         = Join-Path $ProjectRoot "reports\raw\tsdetect_error.log"

function Assert-FileExists($Path, $Label) {
    if (-not (Test-Path $Path)) {
        throw "$Label not found: $Path"
    }
}

Assert-FileExists $Jar "JAR"
Assert-FileExists $InputCsv "Input CSV"

Write-Host "Starting safe TestSmellDetector run..."

if (Test-Path $OutputCsv) {
    Remove-Item $OutputCsv -Force
}

if (Test-Path $TempCsv) {
    Remove-Item $TempCsv -Force
}

$lines = Get-Content $InputCsv

if ($lines.Count -lt 1) {
    throw "Input CSV is empty: $InputCsv"
}

$header = $lines[0]
Add-Content -Path $OutputCsv -Value $header

for ($i = 1; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]

    if ([string]::IsNullOrWhiteSpace($line)) {
        continue
    }

    Write-Host "Processing row $i..."

    Set-Content -Path $TempCsv -Value $header
    Add-Content -Path $TempCsv -Value $line

    try {
        & java -jar $Jar $TempCsv 2>> $Log | Out-Null
        $exitCode = $LASTEXITCODE

        if ($exitCode -ne 0) {
            throw "TestSmellDetector failed on row $i with exit code $exitCode"
        }

        # Qui assumo che il tool modifichi/generi il CSV temporaneo
        # e che la riga di risultato sia la seconda riga del file.
        $tempLines = Get-Content $TempCsv

        if ($tempLines.Count -ge 2) {
            $resultLine = $tempLines[1]
            Add-Content -Path $OutputCsv -Value $resultLine
        }
        else {
            throw "No result row produced for row $i"
        }
    }
   catch {
    Write-Warning "Row $i failed. Inserting fail row."

    $cols = $line -split ","
    if ($cols.Length -eq 0) {
        Add-Content -Path $OutputCsv -Value $line
        continue
    }

    $failRow = $cols[0]
    for ($k = 1; $k -lt $cols.Length; $k++) {
        $failRow += ",fail"
    }

    Add-Content -Path $OutputCsv -Value $failRow
}
}

if (Test-Path $TempCsv) {
    Remove-Item $TempCsv -Force
}

Write-Host "Done. Output saved in $OutputCsv"

