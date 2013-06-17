import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Email {

	private static Map<String, Integer> mapSpamWords = new HashMap<String, Integer>();
	private static Map<String, Integer> mapHamWords = new HashMap<String, Integer>();
	private static Map<String, Double> spamProbabilities = new HashMap<String, Double>();
	private static Map<String, Double> hamProbabilities = new HashMap<String, Double>();
	/**
	 *  Liste mit stopwords, gefunden auf: http://norm.al/2009/04/14/list-of-english-stop-words/
	 */
	private static List<String> stopwords = new ArrayList<String>(
			Arrays.asList("a", "about", "above", "above", "across", "after",
					"afterwards", "again", "against", "all", "almost", "alone",
					"along", "already", "also", "although", "always", "am",
					"among", "amongst", "amoungst", "amount", "an", "and",
					"another", "any", "anyhow", "anyone", "anything", "anyway",
					"anywhere", "are", "around", "as", "at", "back", "be",
					"became", "because", "become", "becomes", "becoming",
					"been", "before", "beforehand", "behind", "being", "below",
					"beside", "besides", "between", "beyond", "bill", "both",
					"bottom", "but", "by", "call", "can", "cannot", "cant",
					"co", "con", "could", "couldnt", "cry", "de", "describe",
					"detail", "do", "done", "down", "due", "during", "each",
					"eg", "eight", "either", "eleven", "else", "elsewhere",
					"empty", "enough", "etc", "even", "ever", "every",
					"everyone", "everything", "everywhere", "except", "few",
					"fifteen", "fify", "fill", "find", "fire", "first", "five",
					"for", "former", "formerly", "forty", "found", "four",
					"from", "front", "full", "further", "get", "give", "go",
					"had", "has", "hasnt", "have", "he", "hence", "her",
					"here", "hereafter", "hereby", "herein", "hereupon",
					"hers", "herself", "him", "himself", "his", "how",
					"however", "hundred", "ie", "if", "in", "inc", "indeed",
					"interest", "into", "is", "it", "its", "itself", "keep",
					"last", "latter", "latterly", "least", "less", "ltd",
					"made", "many", "may", "me", "meanwhile", "might", "mill",
					"mine", "more", "moreover", "most", "mostly", "move",
					"much", "must", "my", "myself", "name", "namely",
					"neither", "never", "nevertheless", "next", "nine", "no",
					"nobody", "none", "noone", "nor", "not", "nothing", "now",
					"nowhere", "of", "off", "often", "on", "once", "one",
					"only", "onto", "or", "other", "others", "otherwise",
					"our", "ours", "ourselves", "out", "over", "own", "part",
					"per", "perhaps", "please", "put", "rather", "re", "same",
					"see", "seem", "seemed", "seeming", "seems", "serious",
					"several", "she", "should", "show", "side", "since",
					"sincere", "six", "sixty", "so", "some", "somehow",
					"someone", "something", "sometime", "sometimes",
					"somewhere", "still", "such", "system", "take", "ten",
					"than", "that", "the", "their", "them", "themselves",
					"then", "thence", "there", "thereafter", "thereby",
					"therefore", "therein", "thereupon", "these", "they",
					"thickv", "thin", "third", "this", "those", "though",
					"three", "through", "throughout", "thru", "thus", "to",
					"together", "too", "top", "toward", "towards", "twelve",
					"twenty", "two", "un", "under", "until", "up", "upon",
					"us", "very", "via", "was", "we", "well", "were", "what",
					"whatever", "when", "whence", "whenever", "where",
					"whereafter", "whereas", "whereby", "wherein", "whereupon",
					"wherever", "whether", "which", "while", "whither", "who",
					"whoever", "whole", "whom", "whose", "why", "will", "with",
					"within", "without", "would", "yet", "you", "your",
					"yours", "yourself", "yourselves"));
	private static int spamWords = 0;
	private static int spamMails = 0;
	private static int hamWords = 0;
	private static int hamMails = 0;
	private static double spamMailProbability;
	
	private String emailText = new String();
	private EmailType type;
	private EmailType classifiedType;

	/**
	 * Konstruktor, um aus einer Textdatei eine für das Programm brauchbare Email zu erstellen
	 * @param input Textdatei
	 * @throws IOException
	 */
	public Email(File input) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line = reader.readLine();

		/* Header und Body sind durch eine leere Zeile getrennt der Header ist für uns uninteressant */
		while (line != null && !line.isEmpty()) {
			line = reader.readLine(); 
		}

		while (line != null) {
			emailText += clean(line).toLowerCase() + " "; /* Leerzeichen zum splitten der Worte auch am Zeilenumbruch */
			line = reader.readLine();
		}
		
		setType(input);
	}

	/**
	 * Zerlegt den Text der Mail in einzelne Worte und hält die Häufigkeit ihrer Vorkommen in Abhängigkeit des Mail-Typen in HashMaps fest
	 */
	public void tokenize() {
		String[] words = emailText.split("\\s");
		for (String word : words) {
			if (type == EmailType.Spam) {
				if (word.length() != 0 && !stopwords.contains(word)) {
					spamWords++;
					if (mapSpamWords.containsKey(word)) {
						int count = mapSpamWords.get(word);
						mapSpamWords.put(word, ++count);
					} else {
						mapSpamWords.put(word, 1);
					}
				}
			} else {
				if (word.length() != 0 && !stopwords.contains(word)) {
					hamWords++;
					if (mapHamWords.containsKey(word)) {
						int count = mapHamWords.get(word);
						mapHamWords.put(word, ++count);
					} else {
						mapHamWords.put(word, 1);
					}
				}
			}
		}
	}

	/**
	 * Methode, die anhand der zuvor klassifizierten Worte versucht zu errechnen, ob es sich um Spam oder Ham handelt
	 */
	public void classify() {
		if (spamProbabilities.isEmpty() || hamProbabilities.isEmpty()) {
			classifyWords();
		}
		double spamProbability;
		double spamProbabilityEmail = 1;
		double spamProbabilityWord;
		double hamProbability;
		double hamProbabilityEmail = 1;
		double hamProbabilityWord;
		double probabilityWord;
		String[] words = emailText.split("\\s");
		for (String word : words) {
			if (!stopwords.contains(word) && !word.isEmpty()) {

				try{
					spamProbability = spamProbabilities.get(word);
				} catch (NullPointerException e) {
					spamProbability = 0;
				}
				try{
					hamProbability = hamProbabilities.get(word);
				} catch (NullPointerException e) {
					hamProbability = 0;
				}
				probabilityWord = (spamMailProbability * spamProbability) + ((1 - spamMailProbability) * hamProbability);
				spamProbabilityWord = ((spamMailProbability * spamProbability) / probabilityWord);
				spamProbabilityEmail *= spamProbabilityWord;
				hamProbabilityWord = (((1 - spamMailProbability) * hamProbability) / probabilityWord);
				hamProbabilityEmail *= hamProbabilityWord;
			}
		}
		if (spamProbabilityEmail >= hamProbabilityEmail) {
			classifiedType = EmailType.Spam;
		} else {
			classifiedType = EmailType.Ham;
		}
	}
	
	/**
	 * Methode zur Berechnung der Wahrscheinlichkeiten, dass ein Wort in einer Ham / Spam auftaut
	 */
	private static void classifyWords() {
		spamProbabilities.clear();
		hamProbabilities.clear();
		spamMailProbability = ((double) spamMails / (spamMails + hamMails));
		for (Entry<String, Integer> e : mapSpamWords.entrySet()) {
			spamProbabilities.put(e.getKey(),
					Math.log((double) e.getValue() / spamWords));
		}

		for (Entry<String, Integer> e : mapHamWords.entrySet()) {
			hamProbabilities.put(e.getKey(),
					Math.log((double) e.getValue() / hamWords));
		}
	}
	
	/**
	 * Methode zum Entfernen Sonderzeichen, Zahlen und HTML-Tags
	 * @param line zu bearbeitende Zeile aus der Datei, die gerade eingelesen wird
	 * @return "gesäuberte" Zeile
	 */
	private String clean(String line) {
		// HTML Tags
		String clean = new String();
		if (line.contains("<")) {
			String[] split = line.split("<(?=[\\w]*)>");
			for (String s : split) {
				if (!s.contains("<") || !s.contains(">")) {
					clean += s;
				}
			}
		} else {
			clean = line;
		}
		// Zeichen die keine Buchstaben sind
		clean = clean.replaceAll("[^a-zöäüßA-ZÖÄÜ ]", "");
		return clean;
	}

	private void setType(File input) {
		type = input.getPath().startsWith("HAM") ? EmailType.Ham
				: EmailType.Spam;
		if (type == EmailType.Ham) {
			hamMails++;
		} else {
			spamMails++;
		}
	}
	
	public EmailType getType() {
		return type;
	}

	public EmailType getClassifiedType() {
		return classifiedType;
	}
}
