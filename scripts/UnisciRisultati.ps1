$ErrorActionPreference = "Stop"

$ProjectRoot = "C:\Users\lidia\PycharmProjects\estraiTest"

# tutti i csv generati dal tool
$csvFiles = Get-ChildItem -Path $ProjectRoot -Filter "Output_TestSmellDetection_*.csv" |
            Sort-Object Name

# file finale
$MergedCsv = Join-Path $ProjectRoot "reports\results\Output_TestSmellDetection_FINAL.csv"

if ($csvFiles.Count -eq 0) {
    throw "No TestSmellDetection CSV files found."
}

# cancella il finale se esiste
if (Test-Path $MergedCsv) {
    Remove-Item $MergedCsv -Force
}

$headerWritten = $false

foreach ($file in $csvFiles) {

    Write-Host "Reading $($file.Name)..."

    $lines = Get-Content $file.FullName

    if ($lines.Count -eq 0) {
        continue
    }

    if (-not $headerWritten) {
        Set-Content -Path $MergedCsv -Value $lines[0]
        $headerWritten = $true
    }

    for ($i = 1; $i -lt $lines.Count; $i++) {
        if (-not [string]::IsNullOrWhiteSpace($lines[$i])) {
            Add-Content -Path $MergedCsv -Value $lines[$i]
        }
    }
}

Write-Host ""
Write-Host "Merge completato."
Write-Host "File finale creato:"
Write-Host $MergedCsv