package queryTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class AddTransaction {

	public static void addTransaction(String[] transactions) throws IllegalArgumentException {
		int i = 0;
		try {
			for (i = 0; i < transactions.length; i++) {
				addTransaction(transactions[i]);
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Transaction " + i + e.getMessage());
		}
	}

	public static void addTransactionFromJSON(String json) throws JSONException {
		JSONObject dataIn = new JSONObject(json);
		String transaction = dataIn.getString("transaction");
		addTransaction(transaction.trim());
	}

	public static void addTransaction(String transaction) throws IllegalArgumentException {
		// Create RegEx to match transactions
		// Integrity checks on database - make sure any funds referenced have already been created, must buy before
		// sell, etc.
		transaction = transaction.replaceAll("\\s{0,},\\s{0,}", ",");
		System.out.println("Processing transaction: " + transaction);

		Pattern fundCreationPattern = Pattern.compile("fund,\\S{1,},[0-9]{0,},[0-9]{4}-?[0-9]{2}-?[0-9]{2}");
		Pattern buyPattern = Pattern.compile("buy,\\S{1,},\\S{1,},[0-9]{0,},[0-9]{4}-?[0-9]{2}-?[0-9]{2}");
		Pattern sellPattern = Pattern.compile("sell,\\S{1,},\\S{1,},[0-9]{4}-?[0-9]{2}-?[0-9]{2}");
		Pattern sellbuyPattern = Pattern.compile("sellbuy,\\S{1,},\\S{1,},\\S{1,},[0-9]{4}-?[0-9]{2}-?[0-9]{2}");
		Matcher fundMatcher = fundCreationPattern.matcher(transaction);
		Matcher buyMatcher = buyPattern.matcher(transaction);
		Matcher sellMatcher = sellPattern.matcher(transaction);
		Matcher sellbuyMatcher = sellbuyPattern.matcher(transaction);
		if (fundMatcher.matches()) {
			System.out.println("Transaction matched fund type!");
		} else if (buyMatcher.matches()) {
			System.out.println("Transaction matched buy!");
		} else if (sellMatcher.matches()) {
			System.out.println("Transaction matched sell!");
		} else if (sellbuyMatcher.matches()) {
			System.out.println("Transaction matched sellbuy!");
		} else {
			System.out.println("Transaction did not match fund type.");
		}
	}
}
