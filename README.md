# Test Smell Detection Pipeline

Pipeline per l'analisi automatica dei **Test Smell** in suite di test Java utilizzando il tool **TestSmellDetector**.
Il progetto consente di eseguire l'analisi su un dataset di classi di test, gestire eventuali errori durante l'esecuzione e aggregare automaticamente i risultati in un file CSV finale.

# Obiettivo del progetto
L'obiettivo è automatizzare l'identificazione dei **test smell** all'interno di classi di test Java, facilitando l'analisi di dataset di test e la raccolta dei risultati prodotti da TestSmellDetector.

La pipeline:
- legge un file CSV contenente le classi di test da analizzare
- esegue TestSmellDetector per ogni riga
- gestisce errori di parsing o analisi
- aggrega i risultati in un unico file finale

# Struttura del progetto

estraiTest
│
├── DATASET
│ Dataset contenente i file di test
│
├── ProductionFile
│ File di produzione associati alle classi di test
│
├── reports
│
│ ├── raw
│ │ File di input utilizzati dalla pipeline
│ │ - InputFile_TsDet.csv
│ │ - TestClass.csv
│ │ - tsdetect_output.csv
│ │
│ ├── csv
│ │ File CSV intermedi generati durante l'esecuzione
│ │
│ └── results
│ │ Risultati finali dell'analisi
│ │ - Output_TestSmellDetection_FINAL.csv
│
├── scripts
│ Script utilizzati per eseguire la pipeline
│ - run_tsdetect.ps1
│ - UnisciRisultati.ps1
│ - CSVconTest.py
│
├── tools
│ Tool utilizzato per rilevare i test smell
│ - TestSmellDetector.jar
│
└── README.md


# Pipeline di esecuzione

La pipeline segue il seguente flusso:

Input CSV
↓
run_tsdetect.ps1
↓
TestSmellDetector.jar
↓
Output CSV intermedi
↓
UnisciRisultati.ps1
↓
Output finale aggregato


# Requisiti

Per eseguire il progetto sono necessari:

- **Java (JDK 8 o superiore)**
- **Python 3**
- **PowerShell**
- **TestSmellDetector**

Il tool utilizzato è disponibile qui:

https://testsmells.org/


# Esecuzione dell'analisi

Per eseguire la pipeline:
powershell -File scripts/run_tsdetect.ps1

Questo script:
-legge il file CSV di input
-esegue TestSmellDetector su ogni riga
-gestisce eventuali errori
-produce file CSV di output
-Se una riga non può essere analizzata, viene marcata con fail.

#Unione dei risultati
Se il tool genera più file CSV, è possibile unirli con:
powershell -File scripts/UnisciRisultati.ps1
Lo script unisce tutti i CSV generati mantenendo un unico header.

#Formato del file di input
Il file InputFile_TsDet.csv deve avere il formato:
appName,pathToTestFile,pathToProductionFile
Ogni riga rappresenta una classe di test da analizzare.

#Output
Il risultato finale dell'analisi viene salvato in:
reports/results/Output_TestSmellDetection_FINAL.csv
Il file contiene i test smell rilevati per ogni classe di test.

#Gestione degli errori
La pipeline è progettata per essere robusta:
se una riga genera un errore
l'esecuzione continua
la riga viene marcata con fail
Questo permette di completare l'analisi anche su dataset molto grandi.

