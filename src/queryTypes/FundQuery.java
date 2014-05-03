package queryTypes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import main.FinanceServlet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utilities.Utilities;
import database.DatabaseManager;

/**
 * @author Ben_Shumate
 * 
 */
public class FundQuery {
	private float startNetWorth = -1;
	private float endNetWorth = -1;
	private float returnRate = -1;
	private float cash = -1;
	private float investments = -1;

	/**
	 * 
	 */
	public static int START_NET_WORTH = 0;
	/**
	 * 
	 */
	public static int END_NET_WORTH = 1;
	/**
	 * 
	 */
	public static int RETURN_RATE = 2;
	/**
	 * 
	 */
	public static int HIGH = 3;
	/**
	 * 
	 */
	public static int LOW = 4;

	/**
	 * 
	 */
	public FundQuery() {
		// Default constructor
	}

	/**
	 * @param id
	 *            ID of the desired field
	 * @return the desired field
	 */
	public float getAttribute(int id) {
		if (id == START_NET_WORTH)
			return startNetWorth;
		else if (id == END_NET_WORTH)
			return endNetWorth;
		else if (id == RETURN_RATE)
			return returnRate;
		else if (id == HIGH)
			return cash;
		else if (id == LOW)
			return investments;
		else
			return -1;
	}

	/**
	 * @param id
	 *            ID of the desired field
	 * @param attr
	 *            data to set
	 */
	public void setAttribute(int id, float attr) {
		if (id == START_NET_WORTH)
			startNetWorth = attr;
		else if (id == END_NET_WORTH)
			endNetWorth = attr;
		else if (id == RETURN_RATE)
			returnRate = attr;
		else if (id == HIGH)
			cash = attr;
		else if (id == LOW)
			investments = attr;
	}

	/**
	 * Returns a list of all the transactions made by a given fund. The request will be JSON with the following form:
	 * 
	 * <pre>
	 * {
	 * 	"fund":"fund_name"
	 * }
	 * </pre>
	 * 
	 * The response will be JSON with the following form:
	 * 
	 * <pre>
	 * {
	 * 	"response":"[transaction1, transaction2]"
	 * }
	 * </pre>
	 * 
	 * @param json
	 *            Contains the desired fund
	 * @return A list of transactions made by the fund
	 * @throws SQLException
	 * @throws JSONException
	 * @throws ClassNotFoundException
	 */
	public static String getFundTransactions(String json) throws SQLException, JSONException, ClassNotFoundException {
		PreparedStatement query = null;
		try {
			// Get the input
			JSONObject dataIn = new JSONObject(json);
			String fund = dataIn.getString("fund");

			// Get the type of the fund
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			query = FinanceServlet.con.prepareStatement("SELECT type FROM Fund WHERE name=?");
			query.setString(1, fund);
			DatabaseManager.executeQuery(query, response);

			// Set the type of the fund
			String fundType = response.get("type")[0];
			if (fundType.equals("I")) {
				fundType = "individual";
			} else {
				fundType = "fund";
			}

			// Get all associated transactions from the database
			query = FinanceServlet.con
					.prepareStatement("SELECT security, security2, type, year, month, day, amount FROM Activity WHERE name=? ORDER BY year ASC, month ASC, day ASC, type ASC");
			query.setString(1, fund);

			DatabaseManager.executeQuery(query, response);

			// Construct a list of strings of properly-formated transactions
			String[] securities = response.get("security");
			String[] securities2 = response.get("security2");
			String[] types = response.get("type");
			String[] years = response.get("year");
			String[] months = response.get("month");
			String[] days = response.get("day");
			String[] amounts = response.get("amount");

			String[] queries = new String[securities.length];
			for (int i = 0; i < securities.length; i++) {
				String queryText = "";
				if (AddTransaction.CREATE_TYPE.equals(types[i])) {
					queryText += (fundType + ", ");
				} else if (AddTransaction.BUY_TYPE.equals(types[i])) {
					queryText += "buy, ";
				} else if (AddTransaction.SELL_TYPE.equals(types[i])) {
					queryText += "sell, ";
				} else if (AddTransaction.SELLBUY_TYPE.equals(types[i])) {
					queryText += "sellbuy, ";
				}

				queryText += (fund + ", ");
				if (!AddTransaction.CREATE_TYPE.equals(types[i])) {
					queryText += (securities[i] + ", ");
				}

				if (AddTransaction.SELLBUY_TYPE.equals(types[i])) {
					queryText += (securities2[i] + ", ");
				}

				if (AddTransaction.BUY_TYPE.equals(types[i]) || AddTransaction.CREATE_TYPE.equals(types[i])) {
					queryText += (amounts[i] + ", ");
				}

				queryText += (years[i] + "-" + months[i] + "-" + days[i]);
				queries[i] = queryText;
			}

			return (new JSONObject()).put("response", queries).toString();
		} finally {
			// Always close prepared statements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	/**
	 * Get information about funds in the database. The request from the web app. This request should be JSON and have
	 * the following form:
	 * 
	 * <pre>
	 * {
	 * 	fromDate : "",
	 * 	toDate : "",
	 * 	individual : true,
	 * 	portfolio : true,
	 * 	isShowOnly : false,
	 * 	onlyShow : []
	 * }
	 * </pre>
	 * 
	 * Information about the funds in the database. The response will be JSON and has the following form:
	 * 
	 * <pre>
	 *  {
	 * 	"symbol":"[ticker1, ticker2]",
	 * 	"startPrice":"[12.34, 23.45]",
	 * 	"endPrice":"[45.56, 56.67]",
	 * 	"returnRate":"[2.23, 24.43, -1.24]",
	 * 	"high":"[51.10, 71.10]",
	 * 	"low":"[11.12, 12.23]",
	 * 	"risk":"[4.0, 9.8]"
	 * }
	 * </pre>
	 * 
	 * @param json
	 *            Request for company information
	 * @return JSON response with fund information
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static String fundQuery(String json) throws ClassNotFoundException, SQLException, JSONException {
		PreparedStatement query = null;
		long calcTime = 0;
		try {
			// Get the input data
			System.out.println("Request: " + json);
			JSONObject dataIn = new JSONObject(json);
			int fromDay = dataIn.getString("fromDate").equals("") ? 1 : Integer.parseInt(dataIn.getString("fromDate")
					.substring(3, 5));
			int fromMonth = dataIn.getString("fromDate").equals("") ? 1 : Integer.parseInt(dataIn.getString("fromDate")
					.substring(0, 2));
			int fromYear = dataIn.getString("fromDate").equals("") ? 2005 : Integer.parseInt(dataIn.getString(
					"fromDate").substring(6, 10));
			int toDay = dataIn.getString("toDate").equals("") ? 31 : Integer.parseInt(dataIn.getString("toDate")
					.substring(3, 5));
			int toMonth = dataIn.getString("toDate").equals("") ? 12 : Integer.parseInt(dataIn.getString("toDate")
					.substring(0, 2));
			int toYear = dataIn.getString("toDate").equals("") ? 2013 : Integer.parseInt(dataIn.getString("toDate")
					.substring(6, 10));
			boolean individual = dataIn.getBoolean("individual");
			boolean portfolio = dataIn.getBoolean("portfolio");
			boolean isShowOnly = dataIn.getBoolean("isShowOnly");
			JSONArray onlyShow = dataIn.getJSONArray("onlyShow");
			System.out.println("Only show: " + onlyShow);
			HashSet<String> onlyShowSet = new HashSet<String>();
			if (isShowOnly) {
				for (int i = 0; i < onlyShow.length(); i++) {
					String s = onlyShow.getString(i).trim();
					if (s != null && !s.trim().equals("")) {
						onlyShowSet.add(s);
					}
				}
			}

			// Make sure that the "From Date" is on or after Jan 3rd, 2005, which is the first market day of 2005
			if (fromYear < Utilities.EARLIEST_YEAR
					|| (fromYear == Utilities.EARLIEST_YEAR && fromMonth < Utilities.EARLIEST_MONTH)
					|| (fromYear == Utilities.EARLIEST_YEAR && fromMonth == Utilities.EARLIEST_MONTH && fromDay < Utilities.EARLIEST_DAY)) {
				fromYear = Utilities.EARLIEST_YEAR;
				fromMonth = Utilities.EARLIEST_MONTH;
				fromDay = Utilities.EARLIEST_DAY;
			}

			// Get a list of all individuals or portfolios, or both (depending on the request)
			if (individual == true && portfolio == false) {
				query = FinanceServlet.con.prepareStatement("SELECT * From Fund WHERE type=\"I\";");
			} else if (individual == false && portfolio == true) {
				query = FinanceServlet.con.prepareStatement("SELECT * From Fund WHERE type=\"P\";");
			} else { // Assume both are true
				query = FinanceServlet.con.prepareStatement("SELECT * From Fund ORDER BY type desc;");
			}
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			int responseSize = 0;
			responseSize = DatabaseManager.executeQuery(query, response);

			// Iterate through the response, create a mapping from fund name to fund type(i.e., "I" or "P")
			HashMap<String, String> fundNameToFundType = new HashMap<String, String>();
			String[] fundNames = response.get("name");
			String[] fundTypes = response.get("type");
			for (int i = 0; i < responseSize; i++) {
				if (!isShowOnly || (isShowOnly && onlyShowSet.contains(fundNames[i]))) {
					fundNameToFundType.put(fundNames[i], fundTypes[i]);
				}
			}

			// Iterate through each of these fund names (portfolios first, individuals second)
			LinkedList<String> fundsToCalculate = new LinkedList<String>(fundNameToFundType.keySet());
			HashMap<String, FundWorth> fundWorths = new HashMap<String, FundWorth>();
			HashMap<String, Float> tempSellBuySoldPrice = new HashMap<String, Float>(); // "security sold,fund" -->
																						// price
			calcTime = System.currentTimeMillis();
			while (!fundsToCalculate.isEmpty()) {
				// Get all transactions where the fund name matches the current fund name
				String fundToProcess = fundsToCalculate.pollFirst();
				query = FinanceServlet.con
						.prepareStatement("SELECT * FROM Activity WHERE year <= ? AND (name=? OR security=? OR security2=?) ORDER BY year ASC, month ASC, day ASC, type ASC");
				query.setInt(1, toYear);
				query.setString(2, fundToProcess);
				query.setString(3, fundToProcess);
				query.setString(4, fundToProcess);
				long time = System.currentTimeMillis();
				responseSize = DatabaseManager.executeQuery(query, response);
				FinanceServlet.totalFundLookupTime += (System.currentTimeMillis() - time);
				if (responseSize < 1) {
					// There are no transactions associated with this fund
					continue;
				}

				// If any of these transactions contain a bought/sold security that hasn't yet been processed, put this
				// at the back of the list
				boolean skipThisSecurity = false;
				for (String securityToCheck : Arrays.asList(response.get("security"))) {
					if (!securityToCheck.equals(fundToProcess) && !fundWorths.containsKey(securityToCheck)
							&& !Utilities.isCompany(securityToCheck)) {
						if (!fundsToCalculate.contains(securityToCheck)) {
							fundsToCalculate.addLast(securityToCheck); // Add the missing fund to the list to be
																		// processed
						}
						fundsToCalculate.addLast(fundToProcess); // Add the current fund to the back of the list to be
																	// processed later
						System.out.println("1st check: " + fundToProcess + " depends on " + securityToCheck);

						skipThisSecurity = true;
						break;
					}
				}
				if (skipThisSecurity) {
					continue;
				}
				// Check "security2" column - the "security2" column in a sellbuy depends on the "security" column
				String[] security2 = response.get("security2");
				String[] name = response.get("name");
				for (int i = 0; i < security2.length; i++) {
					String securityToCheck = security2[i];
					if (!name[i].equals(fundToProcess)) // There will only be conflicts if the name depends on security2
						continue;
					if (!securityToCheck.isEmpty() && !securityToCheck.equals(fundToProcess)
							&& !fundWorths.containsKey(securityToCheck) && !Utilities.isCompany(securityToCheck)) {
						if (!fundsToCalculate.contains(securityToCheck)) {
							fundsToCalculate.addLast(securityToCheck); // Add the missing fund to the list to be
																		// processed
						}
						fundsToCalculate.addLast(fundToProcess); // Add the current fund to the back of the list to be
																	// processed later
						System.out.println("2nd check: " + fundToProcess + " depends on " + securityToCheck);
						skipThisSecurity = true;
						break;
					}
				}
				if (skipThisSecurity) {
					continue;
				}

				System.out.println("Processing " + fundToProcess);

				// Process transactions - build a FundWorth object for this fund during processing
				String[] names = response.get("name");
				String[] securities = response.get("security");
				String[] securities2 = response.get("security2");
				String[] types = response.get("type");
				String[] years = response.get("year");
				String[] months = response.get("month");
				String[] days = response.get("day");
				String[] amounts = response.get("amount");

				for (int i = 0; i < responseSize; i++) {
					Date transactionDate = Utilities.getDateObject(years[i], months[i], days[i]);
					// System.out.println("Transaction: " + types[i] + "\tname: " + names[i] + "\tsecurities: " +
					// securities[i]);
					if (types[i].equals(AddTransaction.CREATE_TYPE)) {
						// If this fund does not exist, create it with the initial cash amount
						if (fundWorths.get(fundToProcess) == null) {
							FundWorth fw = new FundWorth(names[i], transactionDate, Float.parseFloat(amounts[i]));
							fundWorths.put(fundToProcess, fw);
						}
						// If this fund already exists, simply add cash to it
						else {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.addCash(Float.parseFloat(amounts[i]), transactionDate);
						}
					} else if (types[i].equals(AddTransaction.BUY_TYPE)) {
						// Check to see if the buy transaction is this fund buying into a different security
						if (names[i].equals(fundToProcess) && !securities[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.fundBuyFund(securities[i], Float.parseFloat(amounts[i]), transactionDate);
						}
						// Check to see if the buy transaction is someone else buying into this fund
						else if (securities[i].equals(fundToProcess) && !names[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.shareHolderBuy(names[i], Float.parseFloat(amounts[i]), transactionDate);
						}
						// If this buy transaction is a fund buying into itself, or does not involve the current fund at
						// all, there is an error
						else {
							throw new SQLException("Database constraint violated. Invalid buy transaction on "
									+ years[i] + "-" + months[i] + "-" + days[i]);
						}
					} else if (types[i].equals(AddTransaction.SELL_TYPE)) {
						// Check to see if the sell transaction is this fund selling one of its securities
						if (names[i].equals(fundToProcess) && !securities[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.fundSellFund(securities[i], transactionDate);
						}
						// Check to see if the sell transaction is someone else selling this fund
						else if (securities[i].equals(fundToProcess) && !names[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.shareHolderSell(names[i], transactionDate);
						}
						// If this sell transaction is a fund selling itself, or does not involve the current fund at
						// all, there is an error
						else {
							throw new SQLException("Database constraint violated. Invalid sell transaction on "
									+ years[i] + "-" + months[i] + "-" + days[i]);
						}
					} else if (types[i].equals(AddTransaction.SELLBUY_TYPE)) {
						// Check to see if the sellbuy is this fund selling/buying securities
						if (names[i].equals(fundToProcess) && !securities[i].equals(fundToProcess)
								&& !securities2[i].equals(fundToProcess)) {
							// Execute sell, fundToProcess, security, date
							// Execute buy, fundToProcess, security2, <amt from last sell>, date
							FundWorth fw = fundWorths.get(fundToProcess);
							float amount = fw.fundSellFund(securities[i], transactionDate);
							fw.fundBuyFund(securities2[i], amount, transactionDate);
						}
						// Check to see if the sellbuy is selling this security
						else if (securities[i].equals(fundToProcess) && !names[i].equals(fundToProcess)
								&& !securities2[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							float sellAmount = fw.shareHolderSell(names[i], transactionDate);
							tempSellBuySoldPrice.put((fundToProcess + "," + names[i]), sellAmount);
						}
						// Check to see if the sellbuy is buying this security
						else if (securities2[i].equals(fundToProcess) && !names[i].equals(fundToProcess)
								&& !securities[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							Float buyAmount = null;
							if (Utilities.isCompany(securities[i])) {
								buyAmount = getSellAmountForCompany(securities[i], name[i], years[i], months[i],
										days[i]);
							} else {
								String tempSellBuyName = securities[i] + "," + names[i];
								buyAmount = tempSellBuySoldPrice.get(tempSellBuyName);
								if (buyAmount == null) {
									throw new SQLException(
											"Processing error. Could not find dollar amount for sellbuy of "
													+ fundToProcess + " on " + transactionDate);
								}
								tempSellBuySoldPrice.remove(tempSellBuyName);
							}
							fw.shareHolderBuy(names[i], buyAmount, transactionDate);

						}
						// If the sellbuy doesn't match any of the above cases, there is an error
						else {
							throw new SQLException("Database constraint violated. Invalid sellbuy transaction on "
									+ years[i] + "-" + months[i] + "-" + days[i]);
						}
					} else {
						throw new SQLException("Database constraint violated. Type must be C, B, S, or SB.");
					}
				}
				// fundWorths.put(fundToProcess, null);
			}
			calcTime = System.currentTimeMillis() - calcTime;
			System.out.println("Time to process/build FundWorth objects: " + (float) calcTime / 1000);

			calcTime = System.currentTimeMillis();
			// Calculate the majority participants - ask each fund for its majority participant
			HashMap<String, String> majorityParticipantMap = new HashMap<String, String>();
			for (String s : fundWorths.keySet()) {
				String majorityParticipantForS = fundWorths.get(s).getMajorityParticipant();
				if (!majorityParticipantForS.isEmpty()) {
					if (majorityParticipantMap.containsKey(majorityParticipantForS)) {
						String combinedVal = majorityParticipantMap.get(majorityParticipantForS);
						combinedVal += "," + s;
						majorityParticipantMap.put(majorityParticipantForS, combinedVal);
					} else {
						majorityParticipantMap.put(majorityParticipantForS, s);
					}
				}
			}

			// Calculate the final set of funds to be sent to the client.
			// This set of funds should be all funds selected at the beginning, intersected with the funds for which a
			// FundWorth object was created
			ArrayList<String> finalFundList = new ArrayList<String>();
			for (String s : fundNameToFundType.keySet()) {
				if (fundWorths.containsKey(s)) {
					finalFundList.add(s);
				}
			}

			// Format JSON response. Build string arrays to store into the response
			JSONObject queryResult = new JSONObject();
			String[] name = new String[finalFundList.size()];
			String[] startWorth = new String[finalFundList.size()];
			String[] endWorth = new String[finalFundList.size()];
			String[] returnRate = new String[finalFundList.size()];
			String[] cash = new String[finalFundList.size()];
			String[] investments = new String[finalFundList.size()];
			String[] type = new String[finalFundList.size()];
			String[] majorityParticipant = new String[finalFundList.size()];
			int i = 0;
			for (String s : finalFundList) {
				// Only print out the security if it was processed/didn't have 0 associated transactions
				if (fundWorths.containsKey(s)) {
					FundWorth fw = fundWorths.get(s);
					FundDailyQuote toQuote = fw.getFundQuoteForDay(toYear, toMonth, toDay);
					float startWorthF = fw.getInitialFundWorth();
					float endWorthF = toQuote.getNetWorth();
					float cashF = fw.getFundQuoteForDay(toYear, toMonth, toDay).getCashAmount();

					name[i] = s;
					startWorth[i] = String.format("%.02f", startWorthF);
					endWorth[i] = String.format("%.02f", endWorthF);
					returnRate[i] = String.format("%.02f",
							100 * FundWorth.calcTotalFundPercentReturn(fw, toQuote, startWorthF, endWorthF));

					cash[i] = String.format("%.02f", cashF);
					investments[i] = String.format("%.02f", endWorthF - cashF);
					type[i] = fundNameToFundType.get(s);
					majorityParticipant[i] = majorityParticipantMap.get(s);
					if (majorityParticipant[i] == null)
						majorityParticipant[i] = "";
					i++;
				}
			}

			// Build the JSON response
			queryResult.put("name", name);
			queryResult.put("startWorth", startWorth);
			queryResult.put("endWorth", endWorth);
			queryResult.put("returnRate", returnRate);
			queryResult.put("cash", cash);
			queryResult.put("investments", investments);
			queryResult.put("type", type);
			queryResult.put("majorityParticipant", majorityParticipant);

			return queryResult.toString();
		} finally {
			FundWorth.clearFundWorthSet(); // Done processing - remove this set of FundWorth objects
			calcTime = System.currentTimeMillis() - calcTime;
			System.out.println("Time to format result: " + (float) calcTime / 1000);
			// Always close prepared statements
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	// Used in a sellbuy to determine how much cash would be raised from the sale of a previously-bought company
	private static float getSellAmountForCompany(String company, String fundThatSold, String sellYearS,
			String sellMonthS, String sellDayS) throws SQLException, ClassNotFoundException {
		PreparedStatement query = null;
		HashMap<String, String[]> response = new HashMap<String, String[]>();
		int responseSize = 0;
		int sellYear = Integer.parseInt(sellYearS);
		int sellMonth = Integer.parseInt(sellMonthS);
		int sellDay = Integer.parseInt(sellDayS);

		query = FinanceServlet.con
				.prepareStatement("SELECT * FROM Activity WHERE name=? AND (security=? || security2=?) ORDER BY year ASC, month ASC, day ASC, type ASC");
		query.setString(1, fundThatSold);
		query.setString(2, company);
		query.setString(3, company);
		responseSize = DatabaseManager.executeQuery(query, response);
		if (responseSize == 0)
			return 0;
		String amountForBuy = response.get("amount")[0]; // The first transaction will be a buy transaction.
		if (amountForBuy.isEmpty() || amountForBuy.equalsIgnoreCase("null")) {
			// Since this transaction didn't have an amount for its purchase, it was a sellbuy.
			// Recursively find the value of the company that was sold in this sellbuy.

		} else {
			int buyYear = Integer.parseInt(response.get("year")[0]);
			int buyMonth = Integer.parseInt(response.get("month")[0]);
			int buyDay = Integer.parseInt(response.get("day")[0]);
			float buyQuote = CompanyQuery.getQuoteOfCompany(company, buyYear, buyMonth, buyDay);
			float sharesBought = Float.parseFloat(amountForBuy) / buyQuote;
			float sellQuote = CompanyQuery.getQuoteOfCompany(company, sellYear, sellMonth, sellDay);
			return sellQuote * sharesBought;
		}

		return 0;
	}
}
