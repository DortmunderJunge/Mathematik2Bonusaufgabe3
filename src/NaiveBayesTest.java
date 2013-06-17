import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class NaiveBayesTest {

	/**
	 * @param args
	 * @throws IOException
	 */

	private static List<Email> inbox = new LinkedList<Email>();
	private static int spamMails = 0;
	private static int hamMails = 0;

	public static void main(String[] args) throws IOException {
		learn();
		test();
	}

	private static void learn() throws IOException {
		File spamdir = new File("SPAM");
		for (File file : spamdir.listFiles()) {
			spamMails++;
			Email email = new Email(file);
			email.tokenize();
			inbox.add(email);
		}
		File hamdir = new File("HAM");
		for (File file : hamdir.listFiles()) {
			hamMails++;
			Email email = new Email(file);
			email.tokenize();
			inbox.add(email);
		}
	}

	private static void test() throws IOException {
		int messages = 0;
		int correctlyClassified = 0;
		int correctlyClassifiedHam = 0;
		int correctlyClassifiedSpam = 0;
		int falseClassifiedSpam = 0;
		int falseClassifiedHam = 0;
		for (Email email : inbox) {
			messages++;
			email.classify();
			if (email.getType() == email.getClassifiedType()) {
				correctlyClassified++;
				if (email.getType() == EmailType.Ham) {
					correctlyClassifiedHam++;
				} else {
					correctlyClassifiedSpam++;
				}
			} else {
				if (email.getType() == EmailType.Spam) {
					falseClassifiedSpam++;
				} else {
					falseClassifiedHam++;
				}
			}
		}
		System.out.println(correctlyClassified
				+ " von " + (hamMails + spamMails) + " = " + (double)(100 * correctlyClassified / (hamMails + spamMails)) + "% der Emails korrekt klassifiziert.");
		System.out.println("Trefferquote Spam: " + correctlyClassifiedSpam
				+ " von " + spamMails + " = "
				+ (double) 100 * correctlyClassifiedSpam / spamMails + "%");
		System.out.println("Trefferquote Ham:  " + correctlyClassifiedHam
				+ " von " + hamMails + " = " + (double) 100 * correctlyClassifiedHam
				/ hamMails + "%");
		System.out.println("Falsch identifizierte Spams: " + falseClassifiedSpam + " = " + (double) 100 * falseClassifiedSpam / spamMails + "%");
		System.out.println("Falsch identifizierte Hams:  " + falseClassifiedHam + " = " + (double) 100 * falseClassifiedHam / hamMails + "%");

	}
}
