package queryTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import utilities.Utilities;
import database.DatabaseManager;

public class AddTransaction {

	private static final String CREATE_TYPE = "C";
	private static final String BUY_TYPE = "B";
	private static final String SELL_TYPE = "S";
	private static final String SELLBUY_TYPE = "SB";

	private static final String INDIVIDUAL_TYPE = "I";
	private static final String PORTFOLIO_TYPE = "P";

	public static void addTransaction(String[] transactions) throws IllegalArgumentException {
		int i = 0;
		try {
			for (i = 0; i < transactions.length; i++) {
				addTransaction(transactions[i]);
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Transaction " + (i + 1) + " - " + e.getMessage());
		}
	}

	public static void addTransactionFromJSON(String json) throws JSONException, IllegalArgumentException {
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

		Pattern fundCreationPattern = Pattern
				.compile("(fund|individual),(\\S{1,}),([0-9]{0,}),([0-9]{4})-?([0-9]{2})-?([0-9]{2})");
		Pattern buyPattern = Pattern.compile("buy,(\\S{1,}),(\\S{1,}),([0-9]{0,}),([0-9]{4})-?([0-9]{2})-?([0-9]{2})");
		Pattern sellPattern = Pattern.compile("sell,\\S{1,},\\S{1,},[0-9]{4}-?[0-9]{2}-?[0-9]{2}");
		Pattern sellbuyPattern = Pattern.compile("sellbuy,\\S{1,},\\S{1,},\\S{1,},[0-9]{4}-?[0-9]{2}-?[0-9]{2}");
		Matcher fundMatcher = fundCreationPattern.matcher(transaction);
		Matcher buyMatcher = buyPattern.matcher(transaction);
		Matcher sellMatcher = sellPattern.matcher(transaction);
		Matcher sellbuyMatcher = sellbuyPattern.matcher(transaction);
		if (fundMatcher.matches()) {
			String type = fundMatcher.group(1);
			String fundName = fundMatcher.group(2);
			float amount = Float.parseFloat(fundMatcher.group(3));
			int year = Integer.parseInt(fundMatcher.group(4));
			int month = Integer.parseInt(fundMatcher.group(5));
			int day = Integer.parseInt(fundMatcher.group(6));
			if (!Utilities.isValidDate(year, month, day)) {
				throw new IllegalArgumentException("Invalid Date.");
			}
			addCreate(fundName, (type.equals("fund") ? PORTFOLIO_TYPE : INDIVIDUAL_TYPE), amount, year, month, day);
		} else if (buyMatcher.matches()) {
			String fundName = fundMatcher.group(1);
			String securityName = fundMatcher.group(2);
			float amount = Float.parseFloat(fundMatcher.group(3));
			int year = Integer.parseInt(fundMatcher.group(4));
			int month = Integer.parseInt(fundMatcher.group(5));
			int day = Integer.parseInt(fundMatcher.group(6));
			if (!Utilities.isValidDate(year, month, day)) {
				throw new IllegalArgumentException("Invalid Date.");
			}
			addBuy(fundName, securityName, amount, year, month, day);
		} else if (sellMatcher.matches()) {
			System.out.println("Transaction matched sell!");
		} else if (sellbuyMatcher.matches()) {
			System.out.println("Transaction matched sellbuy!");
		} else {
			throw new IllegalArgumentException("Invalid transaction. Syntax error.");
		}
	}

	private static void addCreate(String fundName, String type, float amount, int year, int month, int day)
			throws IllegalArgumentException {
		System.out.println("Create: " + fundName + ", " + type + ", " + amount + ", " + year + ", " + month + ", "
				+ day);

		Connection con = null;
		PreparedStatement query = null;
		try {
			if (Utilities.isCompany(fundName)) {
				throw new IllegalArgumentException("The fund name cannot also be the ticker symbol of a company.");
			}
			
			// Check to see if the fund exists. If it does, make sure the types match.
			con = DatabaseManager.getNewConnection();
			query = con.prepareStatement("INSERT IGNORE INTO Fund (name, type) VALUES (?, ?)");
			query.setString(1, fundName);
			query.setString(2, type);
			DatabaseManager.executeUpdate(query);
			System.out.println("query: " + query.toString());

			query = con
					.prepareStatement("INSERT INTO Activity (name, security, type, year, month, day, amount) VALUES (?, ?, ?, ?, ?, ?, ?);");
			query.setString(1, fundName);
			query.setString(2, fundName);
			query.setString(3, CREATE_TYPE);
			query.setInt(4, year);
			query.setInt(5, month);
			query.setInt(6, day);
			query.setFloat(7, amount);
			System.out.println("query: " + query.toString());

			DatabaseManager.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close SQL connections before returning
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}

	}

	private static void addBuy(String fundName, String securityName, float amount, int year, int month, int day)
			throws IllegalArgumentException {
		// Check to make sure no buys of this security exist in the database
		// Check to make sure the fund and security exist. Ensure the security is not an individual, and if the fund is
		// a portfolio, the security is a stock.
		System.out.println("Buy: " + fundName + ", " + securityName + ", " + amount + ", " + year + ", " + month + ", "
				+ day);

		Connection con = null;
		PreparedStatement query = null;
		try {
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			Date potentialBuyDate = Utilities.getDateObject(year, month, day);
			int responseSize = 0;

			con = DatabaseManager.getNewConnection();
			query = con.prepareStatement("SELECT * FROM Fund WHERE name=? OR name=?");
			query.setString(1, fundName);
			query.setString(2, securityName);
			responseSize = DatabaseManager.executeQuery(query, response);
			//ArrayList<String> funds

			DatabaseManager.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close SQL connections before returning
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	private static void addSell() throws IllegalArgumentException {

	}

	private static void addSellBuy() throws IllegalArgumentException {

	}
}
