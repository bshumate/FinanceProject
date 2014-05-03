package queryTypes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.FinanceServlet;

import org.json.JSONException;
import org.json.JSONObject;

import utilities.Utilities;
import database.DatabaseManager;

/**
 * Adds transactions to the database. Transactions are received from the web application in either a single-line string
 * of text, or in a .csv file that can contain many transactions. Input is validated before being inserted into the
 * database, and will not be added if it is invalid.
 * 
 * @author Ben_Shumate
 * 
 */
public class AddTransaction {

	/**
	 * String representing create transactions in the DB
	 */
	public static final String CREATE_TYPE = "A";
	/**
	 * String representing buy transactions in the DB
	 */
	public static final String BUY_TYPE = "B";
	/**
	 * String representing sell transactions in the DB
	 */
	public static final String SELL_TYPE = "S";
	/**
	 * String representing sellbuy transactions in the DB
	 */
	public static final String SELLBUY_TYPE = "SB";

	/**
	 * String representing individual funds in the DB
	 */
	public static final String INDIVIDUAL_TYPE = "I";
	/**
	 * String representing portfolio funds in the DB
	 */
	public static final String PORTFOLIO_TYPE = "P";

	/**
	 * Iterates through each transaction in the String array, and calls {@link #addTransaction(String)
	 * addTransaction(transaction)}
	 * 
	 * @param transactions
	 *            An array of Strings that holds each transaction to be entered into the database
	 * @throws IllegalArgumentException
	 *             See {@link #addTransaction(String) addTransaction}
	 */
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

	/**
	 * Extracts the transaction from the JSON string, and calls {@link #addTransaction(String) addTransaction}
	 * 
	 * @param json
	 *            A JSON-Formatted String that contains the string to be processed
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 *             See {@link #addTransaction(String) addTransaction}
	 */
	public static void addTransactionFromJSON(String json) throws JSONException, IllegalArgumentException {
		JSONObject dataIn = new JSONObject(json);
		String transaction = dataIn.getString("transaction");
		addTransaction(transaction.trim());
	}

	/**
	 * Receives a string representing a transaction to be inserted into the database. Determines whether this string is
	 * valid, and if so, which type of transaction it represents. Then, the transaction checked against existing
	 * database entries for conflicts before being inserted.
	 * 
	 * Transactions can take one of two forms:
	 * <ul>
	 * <li><i style="font-family: monospace; color: #B00000">&lt;fund|individual&gt;, &lt;name&gt;, &lt;dollar
	 * figure&gt;, &lt;YYYY-MM-DD&gt;</i> - Add cash to an individual or portfolio, creating it if it does not exist.</li>
	 * <li><i style="font-family: monospace; color: #B00000;">&lt;buy|sell&gt;, &lt;name&gt;, &lt;stock symbol|fund
	 * name&gt;, &lt;dollar amount&gt;, &lt;YYYY-MM-DD&gt;</i> - buy or sell a number of shares on a specific date
	 * (assume closing value) through cash on hand.</li>
	 * </ul>
	 * 
	 * @param transaction
	 *            A string representing a transaction
	 * @throws IllegalArgumentException
	 *             Thrown whenever the transaction is invalid, either because of a syntax error or because of a conflict
	 *             with the current data in the database
	 */
	public static void addTransaction(String transaction) throws IllegalArgumentException {
		// Create RegEx to match transactions
		// Integrity checks on database - make sure any funds referenced have already been created, must buy before
		// sell, etc.
		transaction = transaction.replaceAll("\\s{0,},\\s{0,}", ",");
		System.out.println("Processing transaction: " + transaction);

		Pattern fundCreationPattern = Pattern
				.compile("(fund|individual),(\\S{1,}),([0-9]{0,}),([0-9]{4})-?([0-9]{2})-?([0-9]{2})");
		Pattern buyPattern = Pattern.compile("buy,(\\S{1,}),(\\S{1,}),([0-9]{0,}),([0-9]{4})-?([0-9]{2})-?([0-9]{2})");
		Pattern sellPattern = Pattern.compile("sell,(\\S{1,}),(\\S{1,}),([0-9]{4})-?([0-9]{2})-?([0-9]{2})");
		Pattern sellbuyPattern = Pattern
				.compile("sellbuy,(\\S{1,}),(\\S{1,}),(\\S{1,}),([0-9]{4})-?([0-9]{2})-?([0-9]{2})");
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
			if (type.contains(",") || fundName.contains(",")) {
				throw new IllegalArgumentException("Too many commas in transaction. Either you have an extra field in"
						+ " the transaction, or have an illegal comma in a fund name.");
			}
			addCreate(fundName, (type.equals("fund") ? PORTFOLIO_TYPE : INDIVIDUAL_TYPE), amount, year, month, day);
		} else if (buyMatcher.matches()) {
			String fundName = buyMatcher.group(1);
			String securityName = buyMatcher.group(2);
			float amount = Float.parseFloat(buyMatcher.group(3));
			int year = Integer.parseInt(buyMatcher.group(4));
			int month = Integer.parseInt(buyMatcher.group(5));
			int day = Integer.parseInt(buyMatcher.group(6));
			if (!Utilities.isValidDate(year, month, day)) {
				throw new IllegalArgumentException("Invalid Date.");
			}
			if (fundName.contains(",") || securityName.contains(",")) {
				throw new IllegalArgumentException("Too many commas in transaction. Either you have an extra field in"
						+ " the transaction, or have an illegal comma in a fund name.");
			}
			addBuy(fundName, securityName, amount, year, month, day);
		} else if (sellMatcher.matches()) {
			String fundName = sellMatcher.group(1);
			String securityName = sellMatcher.group(2);
			int year = Integer.parseInt(sellMatcher.group(3));
			int month = Integer.parseInt(sellMatcher.group(4));
			int day = Integer.parseInt(sellMatcher.group(5));
			if (!Utilities.isValidDate(year, month, day)) {
				throw new IllegalArgumentException("Invalid Date.");
			}
			if (fundName.contains(",") || securityName.contains(",")) {
				throw new IllegalArgumentException("Too many commas in transaction. Either you have an extra field in"
						+ " the transaction, or have an illegal comma in a fund name.");
			}
			addSell(fundName, securityName, year, month, day);
		} else if (sellbuyMatcher.matches()) {
			String fundName = sellbuyMatcher.group(1);
			String soldSecurity = sellbuyMatcher.group(2);
			String boughtSecurity = sellbuyMatcher.group(3);
			int year = Integer.parseInt(sellbuyMatcher.group(4));
			int month = Integer.parseInt(sellbuyMatcher.group(5));
			int day = Integer.parseInt(sellbuyMatcher.group(6));
			if (!Utilities.isValidDate(year, month, day)) {
				throw new IllegalArgumentException("Invalid Date.");
			}
			if (fundName.contains(",") || soldSecurity.contains(",") || boughtSecurity.contains(",")) {
				throw new IllegalArgumentException("Too many commas in transaction. Either you have an extra field in"
						+ " the transaction, or have an illegal comma in a fund name.");
			}
			addSellBuy(fundName, soldSecurity, boughtSecurity, year, month, day);
		} else {
			throw new IllegalArgumentException("Invalid transaction. Syntax error.");
		}
	}

	/**
	 * Adds a fund create transaction to the database
	 * 
	 * @param fundName
	 *            Name of the fund to be created
	 * @param type
	 *            "I" for individual or "P" for portfolio
	 * @param amount
	 *            Initial dollar amount for the fund
	 * @param year
	 *            Creation year
	 * @param month
	 *            Creation month
	 * @param day
	 *            Creation day
	 * @throws IllegalArgumentException
	 *             Thrown when the input is invalid. This can happen if the type parameter is invalid, or the name of
	 *             the fund is already the name of an existing company.
	 */
	private static void addCreate(String fundName, String type, float amount, int year, int month, int day)
			throws IllegalArgumentException {
		// System.out.println("Create: " + fundName + ", " + type + ", " + amount + ", " + year + ", " + month + ", "
		// + day);

		if (Utilities.isCompany(fundName)) {
			throw new IllegalArgumentException("The fund name cannot also be the ticker symbol of a company.");
		}

		PreparedStatement query = null;
		try {
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			int responseSize = 0;

			// Check to see if the fund exists. If it does, make sure the types match.
			query = FinanceServlet.con.prepareStatement("SELECT * FROM Fund WHERE name=?");
			query.setString(1, fundName);
			responseSize = DatabaseManager.executeQuery(query, response);

			if (responseSize == 0) { // The fund does not yet exist. Create it.
				query = FinanceServlet.con.prepareStatement("INSERT INTO Fund (name, type) VALUES (?, ?)");
				query.setString(1, fundName);
				query.setString(2, type);
				DatabaseManager.executeUpdate(query);
			} else { // The fund already exists. Make sure its type of this add request ("I" or "P").
				if (!type.equals(response.get("type")[0]) || !type.equals(response.get("type")[0])) {
					throw new IllegalArgumentException("Fund " + fundName + " is of type " + type
							+ " but was referred to as type " + response.get("type")[0]);
				}
			}

			// Insert this transaction into the database
			query = FinanceServlet.con
					.prepareStatement("INSERT INTO Activity (name, security, type, year, month, day, amount) VALUES (?, ?, ?, ?, ?, ?, ?);");
			query.setString(1, fundName);
			query.setString(2, fundName);
			query.setString(3, CREATE_TYPE);
			query.setInt(4, year);
			query.setInt(5, month);
			query.setInt(6, day);
			query.setFloat(7, amount);

			DatabaseManager.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	// Verifies the buy transaction by checking it against the current data in the database
	private static void verifyBuy(String fundName, String securityName, int year, int month, int day)
			throws IllegalArgumentException {
		PreparedStatement query = null;
		try {
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			Date potentialBuyDate = Utilities.getDateObject(year, month, day);

			// Check that both funds exist and are valid, and that a portfolio isn't buying an individual
			query = FinanceServlet.con.prepareStatement("SELECT * FROM Fund WHERE name=? OR name=?");
			query.setString(1, fundName);
			query.setString(2, securityName);
			DatabaseManager.executeQuery(query, response);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(response.get("name")));
			ArrayList<String> types = new ArrayList<String>(Arrays.asList(response.get("type")));
			// The fund does not exist
			if (!names.contains(fundName)) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" in buy transaction was never created.");
			}
			// The purchased security doesn't exist
			if (!names.contains(securityName) && !Utilities.isCompany(securityName)) {
				throw new IllegalArgumentException("Security \"" + securityName
						+ "\" purchased in buy transaction was never created/doesn't exist.");
			}
			// A portfolio is trying to purchase an individual or another portfolio (illegal)
			if (PORTFOLIO_TYPE.equals(types.get(names.indexOf(fundName))) && !Utilities.isCompany(securityName)) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" is a portfolio, but tried to purchase \""
						+ securityName + "\" another portfolio/individual. Portfolios can only purchase stocks.");
			}

			// Check to make sure this fund hasn't already purchased this security before
			query = FinanceServlet.con
					.prepareStatement("SELECT * FROM Activity WHERE name=? ORDER BY year ASC, month ASC, day ASC");
			query.setString(1, fundName);
			response.clear();
			DatabaseManager.executeQuery(query, response);
			ArrayList<String> securityNames = new ArrayList<String>(Arrays.asList(response.get("security")));
			ArrayList<String> security2Names = new ArrayList<String>(Arrays.asList(response.get("security2")));
			if (securityNames.contains(securityName) || security2Names.contains(securityName)) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" has already already bought \""
						+ securityName + "\". It cannot buy the same security twice.");
			}
			// Check to make sure the date of the buy transaction is after the creation date of the fund
			Date fundCreationDate = Utilities.getDateObject(response.get("year")[0], response.get("month")[0],
					response.get("day")[0]);
			if (Utilities.compareDates(fundCreationDate, potentialBuyDate) > 0) {
				System.out.println("fund creation date: " + fundCreationDate);
				System.out.println("potential buy date: " + potentialBuyDate);
				System.out.println(potentialBuyDate.compareTo(fundCreationDate));
				System.out.println(fundCreationDate.compareTo(potentialBuyDate));
				throw new IllegalArgumentException("Fund \"" + fundName + "\" is trying to buy something on " + year
						+ "-" + month + "-" + day + ", which is before it's creation date.");
			}

			// Ensure that if the security being purchased is a fund, it has been created by the purchase date
			if (!Utilities.isCompany(securityName)) {
				query = FinanceServlet.con
						.prepareStatement("SELECT * FROM Activity WHERE name=? ORDER BY year ASC, month ASC, day ASC");
				query.setString(1, securityName);
				response.clear();
				DatabaseManager.executeQuery(query, response);
				Date securityCreationDate = Utilities.getDateObject(response.get("year")[0], response.get("month")[0],
						response.get("day")[0]);
				if (Utilities.compareDates(securityCreationDate, potentialBuyDate) > 0) {
					throw new IllegalArgumentException("Security \"" + securityName + "\" is being bought on " + year
							+ "-" + month + "-" + day + ", which is before it's creation date.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close PreparedStatements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}

	}

	// Adds a buy transaction to the database
	private static void addBuy(String fundName, String securityName, float amount, int year, int month, int day)
			throws IllegalArgumentException {
		// Check to make sure no buys of this security exist in the database
		// Check to make sure the fund and security exist. Ensure the security is not an individual, and if the fund is
		// a portfolio, the security is a stock.

		// The fund name cannot be the same as an already-existing company
		if (Utilities.isCompany(fundName)) {
			throw new IllegalArgumentException("The fund name cannot also be the ticker symbol of a company.");
		}

		PreparedStatement query = null;
		try {
			verifyBuy(fundName, securityName, year, month, day);

			query = FinanceServlet.con
					.prepareStatement("INSERT INTO Activity (name, security, type, year, month, day, amount) VALUES (?, ?, ?, ?, ?, ?, ?)");
			query.setString(1, fundName);
			query.setString(2, securityName);
			query.setString(3, BUY_TYPE);
			query.setInt(4, year);
			query.setInt(5, month);
			query.setInt(6, day);
			query.setFloat(7, amount);
			DatabaseManager.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close PreparedStatements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	// Verifies a sell transaction by checking it against existing data in the database
	private static void verifySell(String fundName, String securityName, int year, int month, int day) {
		PreparedStatement query = null;
		try {
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			Date potentialSellDate = Utilities.getDateObject(year, month, day);

			// Check that both funds exist and are valid, and that a portfolio isn't buying an individual
			query = FinanceServlet.con.prepareStatement("SELECT * FROM Fund WHERE name=? OR name=?");
			query.setString(1, fundName);
			query.setString(2, securityName);
			DatabaseManager.executeQuery(query, response);
			ArrayList<String> names = new ArrayList<String>(Arrays.asList(response.get("name")));
			ArrayList<String> types = new ArrayList<String>(Arrays.asList(response.get("type")));
			// The fund does not exist
			if (!names.contains(fundName)) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" in sell transaction was never created.");
			}
			// The sold security doesn't exist
			if (!names.contains(securityName) && !Utilities.isCompany(securityName)) {
				throw new IllegalArgumentException("Security \"" + securityName
						+ "\" sold in sell transaction was never created/doesn't exist.");
			}
			// A portfolio is trying to sell an individual or another portfolio (illegal)
			if (PORTFOLIO_TYPE.equals(types.get(names.indexOf(fundName))) && !Utilities.isCompany(securityName)) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" is a portfolio, but tried to sell \""
						+ securityName + "\" another portfolio/individual. Portfolios can only own stocks.");
			}

			// Check to make sure this fund was purchased exactly 1 time before, but not yet sold
			query = FinanceServlet.con
					.prepareStatement("SELECT * FROM Activity WHERE name=? ORDER BY year ASC, month ASC, day ASC");
			query.setString(1, fundName);
			response.clear();
			DatabaseManager.executeQuery(query, response);
			ArrayList<String> securityNames = new ArrayList<String>(Arrays.asList(response.get("security")));
			ArrayList<String> security2Names = new ArrayList<String>(Arrays.asList(response.get("security2")));
			int numOccurancesOfSecurityName = 0;
			for (int i = 0; i < securityNames.size(); i++) {
				// If the security appears in the "security" column, ensure the transaction was a buy
				if (securityName.equals(securityNames.get(i))) {
					numOccurancesOfSecurityName++;
					if (!BUY_TYPE.equals(response.get("type")[i])) {
						throw new IllegalArgumentException("Fund \"" + fundName + "\" tried to sell \"" + securityName
								+ "\" without every buying it.");
					}
				}
				// If the security appears in the "security2" column, the transaction has to have been a sellbuy
				else if (securityName.equals(security2Names.get(i))) {
					if (!SELLBUY_TYPE.equals(response.get("type")[i])) {
						throw new IllegalArgumentException("Fund \"" + fundName + "\" tried to sell \"" + securityName
								+ "\" without every buying it.");
					}
					numOccurancesOfSecurityName++;
				}
			}
			if (numOccurancesOfSecurityName > 1) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" tried to sell \"" + securityName
						+ "\" without every buying it.");
			}
			if (numOccurancesOfSecurityName < 1) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" tried to sell \"" + securityName
						+ "\" without every buying it.");
			}
			// Check to make sure the date of the sell transaction is after the date of the buy transaction
			int buyTransactionIndex = securityNames.indexOf(securityName);
			Date fundBuyDate = Utilities.getDateObject(response.get("year")[buyTransactionIndex],
					response.get("month")[buyTransactionIndex], response.get("day")[buyTransactionIndex]);
			if (fundBuyDate.compareTo(potentialSellDate) > 0) {
				throw new IllegalArgumentException("Fund \"" + fundName + "\" is trying to sell \"" + securityName
						+ "\" on " + year + "-" + month + "-" + day + ", which is before the day it bought it.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close PreparedStatements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	// Adds a sell transaction to the database
	private static void addSell(String fundName, String securityName, int year, int month, int day)
			throws IllegalArgumentException {
		// Make sure the everything exists, as in the buy case
		// Make sure this transaction has occurred exactly 1 time before, and it was a buy that took place before this
		// date

		// The fund name cannot be the same as an already-existing company
		if (Utilities.isCompany(fundName)) {
			throw new IllegalArgumentException("The fund name cannot also be the ticker symbol of a company.");
		}

		PreparedStatement query = null;
		try {
			verifySell(fundName, securityName, year, month, day);

			query = FinanceServlet.con
					.prepareStatement("INSERT INTO Activity (name, security, type, year, month, day) VALUES (?, ?, ?, ?, ?, ?)");
			query.setString(1, fundName);
			query.setString(2, securityName);
			query.setString(3, SELL_TYPE);
			query.setInt(4, year);
			query.setInt(5, month);
			query.setInt(6, day);
			DatabaseManager.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close PreparedStatements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	// Adds a sellbuy transaction to the database. Verification done with existing buy and sell verification methods.
	private static void addSellBuy(String fundName, String soldSecurity, String boughtSecurity, int year, int month,
			int day) throws IllegalArgumentException {
		// System.out.println("Sellbuy: " + fundName + ", " + soldSecurity + ", " + boughtSecurity + ", " + year + ", "
		// + month + ", " + day);

		// The fund name cannot be the same as an already-existing company
		if (Utilities.isCompany(fundName)) {
			throw new IllegalArgumentException("The fund name cannot also be the ticker symbol of a company.");
		}

		PreparedStatement query = null;
		try {
			verifySell(fundName, soldSecurity, year, month, day);
			verifyBuy(fundName, boughtSecurity, year, month, day);

			query = FinanceServlet.con
					.prepareStatement("INSERT INTO Activity (name, security, security2, type, year, month, day) VALUES (?, ?, ?, ?, ?, ?, ?)");
			query.setString(1, fundName);
			query.setString(2, soldSecurity);
			query.setString(3, boughtSecurity);
			query.setString(4, SELLBUY_TYPE);
			query.setInt(5, year);
			query.setInt(6, month);
			query.setInt(7, day);
			DatabaseManager.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			// Always close PreparedStatements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}
}
