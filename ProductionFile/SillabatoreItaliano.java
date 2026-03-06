import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SillabatoreItaliano {

    private static final String VOCALI = "aeiouàèéìòóù";
    private static final String CONSONANTI_SEMPLICI = "bcdfghlmnpqrstv";
    private static final String CONSONANTI_SPECIALI = "jzkwxy"; // Caratteri non tipici ma da considerare
    private static final String GRUPPI_UNITI_BASE = "gli|gn|sc|ch|gh"; // Gruppi inseparabili fondamentali
                                                                       // (digrammi/trigrammi)
    private static final String GRUPPI_UNITI_LIQUIDI = "[bcdfghptv][lr]"; // Consonante + l/r (es. 'pr', 'cl', 'br',
                                                                          // 'tr')

    /**
     * Divide in sillabe una parola in italiano.
     * La sillabazione non è perfetta e si basa su regole fonetiche di base.
     * 
     * @param parola La parola da sillabare.
     * @return Una stringa con la parola sillabata, separata da trattini.
     */
    public String sillaba(String parola) {
        if (parola == null || parola.isEmpty()) {
            return "";
        }

        // 1. Prepara la parola: minuscolo e normalizzazione (opzionale: rimuovi accenti
        // non standard)
        parola = parola.toLowerCase().trim();

        // 2. Doppie e gruppi 'cq' si dividono:
        // Sostituisce la doppia con la doppia separata da un trattino.
        // Esempio: "palla" -> "pal-la", "acqua" -> "ac-qua"
        String sillabata = parola.replaceAll("([bcdfghlmnpqrstzv])\\1", "$1-$1");
        sillabata = sillabata.replaceAll("cq", "c-q");

        // 3. I gruppi consonantici che **non possono stare a inizio di parola** (l, r,
        // m, n seguite da consonante, etc.) si dividono:
        // Questa è una semplificazione complessa. Gestiamo alcuni casi comuni (l/m/n/r
        // + consonante).
        // Esempio: "altare" -> "al-tare", "ombra" -> "om-bra", "andare" -> "an-dare"
        // Attenzione: usiamo un lookahead negativo per evitare di dividere i gruppi
        // come 'pr', 'tr', 'cl', etc. già definiti come uniti.
        Pattern patternDivisioneCN = Pattern.compile("([" + CONSONANTI_SEMPLICI + "])([" + CONSONANTI_SEMPLICI + "])");
        Matcher matcherDivisioneCN = patternDivisioneCN.matcher(sillabata);
        StringBuffer sb = new StringBuffer();
        while (matcherDivisioneCN.find()) {
            String c1 = matcherDivisioneCN.group(1);
            String c2 = matcherDivisioneCN.group(2);

            // Check semplificato se il gruppo è indivisibile (es. Consonante + l/r, o
            // gruppi speciali)
            if (Pattern.compile(GRUPPI_UNITI_LIQUIDI).matcher(c1 + c2).matches() ||
                    Pattern.compile(GRUPPI_UNITI_BASE).matcher(c1 + c2).matches()) {
                // Lascia il gruppo unito se rientra nei gruppi noti che non si dividono
                matcherDivisioneCN.appendReplacement(sb, c1 + c2);
            } else if (("lmrnc".contains(c1) && CONSONANTI_SEMPLICI.contains(String.valueOf(c2)))
                    || (c1.equals("s") && CONSONANTI_SEMPLICI.contains(String.valueOf(c2)))) {
                // Divisione delle consonanti diverse, escluse le 's impura' (gestite dopo).
                // Qui: l/m/r/n/c + altra cons (non liquide) si dividono.
                matcherDivisioneCN.appendReplacement(sb, c1 + "-" + c2);
            } else {
                // Lascia invariato se non si applica una regola di divisione specifica
                matcherDivisioneCN.appendReplacement(sb, c1 + c2);
            }
        }
        matcherDivisioneCN.appendTail(sb);
        sillabata = sb.toString();

        // 4. Vocale + Consonante + Vocale (V-CV): la consonante va con la vocale
        // successiva.
        // Non divide se c'è già un trattino (es. doppie). Ignora i gruppi inseparabili.
        // Utilizziamo le regex per trovare VCV dove C è una singola consonante non
        // preceduta da trattino
        // Questa è la parte più complessa da implementare bene con regex, quindi
        // procediamo con un approccio più iterativo.

        // 5. Rimuovi i trattini per l'elaborazione VCV/CVV e reinseriamo le sillabe
        sillabata = sillabata.replaceAll("-", "");

        List<String> sillabe = new ArrayList<>();
        int i = 0;
        while (i < sillabata.length()) {
            // Caso 1: Vocale iniziale seguita da una singola consonante. Es: 'a-mo' -> 'a',
            // 'mo'
            if (i == 0 && VOCALI.contains(String.valueOf(sillabata.charAt(i))) &&
                    i + 1 < sillabata.length() && CONSONANTI_SEMPLICI.contains(String.valueOf(sillabata.charAt(i + 1)))
                    &&
                    (i + 2 >= sillabata.length() || VOCALI.contains(String.valueOf(sillabata.charAt(i + 2))))) {

                sillabe.add(String.valueOf(sillabata.charAt(i)));
                i++;
                continue;
            }

            // Caso 2: Cerca il gruppo più grande che forma una sillaba (tentativo di
            // Massimo Attacco)
            // Cerca sequenze V...
            int start = i;
            int j = i;

            // Trova la prima vocale (il nucleo della sillaba)
            while (j < sillabata.length() && !VOCALI.contains(String.valueOf(sillabata.charAt(j)))) {
                j++; // Salta le consonanti all'inizio di parola (se non è il primo carattere)
            }
            if (j >= sillabata.length()) { // Fine della parola dopo le consonanti iniziali
                sillabe.add(sillabata.substring(start));
                break;
            }

            int nucleo = j;
            j++;

            // Cerca la prossima vocale. Il confine di sillaba è generalmente prima della
            // consonante che precede la vocale successiva.
            while (j < sillabata.length() && !VOCALI.contains(String.valueOf(sillabata.charAt(j)))) {
                j++; // Salta le consonanti dopo la vocale nucleo
            }

            if (j >= sillabata.length()) { // Non ci sono più vocali
                sillabe.add(sillabata.substring(start));
                i = sillabata.length();
                continue;
            }

            // C'è almeno un'altra vocale a 'j'. Dobbiamo dividere le consonanti tra
            // 'nucleo' e 'j'.
            int nextVocale = j;
            int consLength = nextVocale - (nucleo + 1);

            if (consLength == 0) { // V-V (Iato/Dittongo)
                // Caso Iato: V-V. Semplificazione: consideriamo separati i gruppi di vocali non
                // i-u-e/o-a.
                // Lo iato è complesso, qui semplifichiamo. I dittonghi (es. 'io', 'ua') restano
                // uniti, gli iati (es. 'po-e-ta') si dividono.
                // Se sono vocali forti (a,e,o) non contigue, si dividono.
                if (nucleo + 1 == nextVocale) {
                    char v1 = sillabata.charAt(nucleo);
                    char v2 = sillabata.charAt(nextVocale);

                    // Divisione V-V solo se entrambe sono vocali forti (a, e, o) o è una sequenza
                    // 'iu', 'ui' (iatizzate)
                    if ("aeo".contains(String.valueOf(v1)) && "aeo".contains(String.valueOf(v2))) {
                        sillabe.add(sillabata.substring(start, nucleo + 1));
                        i = nucleo + 1;
                        continue;
                    }
                    // Altrimenti (dittongo o vocale debole non accentata), le tiene unite.
                    // Il codice successivo (V-CV) le gestirà come parte della sillaba.
                }

                // V-V semplice (Dittongo o Iato non separato): include la vocale successiva
                // nella sillaba corrente e continua.
                // Semplificazione: la V successiva fa parte della sillaba, a meno di una regola
                // di iato.

            } else if (consLength == 1) { // V-C-V. La consonante C va con la V successiva.
                // Es. 'ca-sa'. La sillaba finisce prima di 'C'.
                // Verifica se C è già stata marcata per divisione (doppie, gruppi l/m/n/r).

                // La sillaba attuale è V. La prossima sillaba è CV.
                sillabe.add(sillabata.substring(start, nucleo + 1)); // V-
                i = nucleo + 1;
                continue;
            } else { // V-CCV, V-CCCV, ecc.
                // Massima attribuzione di consonanti alla sillaba successiva (Massimo Attacco)
                // Trova il punto di divisione in base alle regole sui gruppi inseparabili.

                String gruppoCons = sillabata.substring(nucleo + 1, nextVocale);
                int splitIndex = -1;

                // Gruppi di 2 consonanti (CC): Se il gruppo può stare all'inizio di parola (es.
                // 'pr', 'tr', 'st'), resta unito: V-CCV. Altrimenti si divide: VC-CV.
                if (gruppoCons.length() == 2) {
                    if (Pattern.compile(GRUPPI_UNITI_LIQUIDI).matcher(gruppoCons).matches() ||
                            Pattern.compile(GRUPPI_UNITI_BASE).matcher(gruppoCons).matches() ||
                            gruppoCons.startsWith("s")) {
                        // Gruppo unito: V-CCV
                        splitIndex = nucleo + 1;
                    } else {
                        // Gruppo diviso (es. 'mp', 'lt'): VC-CV
                        splitIndex = nucleo + 2;
                    }
                } else if (gruppoCons.length() >= 3) {
                    // Gruppi di 3 o più: si divide prima della seconda consonante (C-CCV, CC-CCV)
                    // Es: 'con-trol-lo' (qui abbiamo solo la parte 'ntr'), si divide n-tr, quindi
                    // dopo la prima consonante del gruppo.
                    // Si divide prima della seconda consonante: V C - C C V
                    splitIndex = nucleo + 2;
                }

                if (splitIndex != -1 && splitIndex > start) {
                    sillabe.add(sillabata.substring(start, splitIndex));
                    i = splitIndex;
                    continue;
                }
            }

            // Se non è stata trovata una regola specifica, avanza al prossimo carattere per
            // evitare loop infiniti
            i++;
        }

        // Finalizza: unisce le sillabe separate da trattini
        return String.join("-", sillabe);
    }
}