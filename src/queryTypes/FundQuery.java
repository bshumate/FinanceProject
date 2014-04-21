package queryTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import utilities.Utilities;
import database.DatabaseManager;

public class FundQuery {
	public float startNetWorth = -1;
	public float endNetWorth = -1;
	public float returnRate = -1;
	public float cash = -1;
	public float investments = -1;

	public static int START_NET_WORTH = 0;
	public static int END_NET_WORTH = 1;
	public static int RETURN_RATE = 2;
	public static int HIGH = 3;
	public static int LOW = 4;

	public FundQuery() {
		// Default constructor
	}

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

	public static String fundQuery(String json) throws ClassNotFoundException, SQLException, JSONException {
		Connection con = DatabaseManager.getNewConnection();
		PreparedStatement query = null;
		try {
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
			boolean increasing = dataIn.getBoolean("increasing");
			boolean decreasing = dataIn.getBoolean("decreasing");
			boolean individual = dataIn.getBoolean("individual");
			boolean portfolio = dataIn.getBoolean("portfolio");

			// Set up a map to store query responses for processing
			HashMap<String, FundQuery> responseMap = new HashMap<String, FundQuery>();

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
				query = con.prepareStatement("SELECT * From Fund WHERE type=\"I\";");
			} else if (individual == false && portfolio == true) {
				query = con.prepareStatement("SELECT * From Fund WHERE type=\"P\";");
			} else { // Assume both are true
				query = con.prepareStatement("SELECT * From Fund ORDER BY type desc;");
			}
			HashMap<String, String[]> response = new HashMap<String, String[]>();
			int responseSize = 0;
			responseSize = DatabaseManager.executeQuery(query, response);
			
			// Iterate through the response, create a mapping from fund name to fund type(i.e., "I" or "P")
			HashMap<String, String> fundNameToFundType = new HashMap<String, String>();
			String[] fundNames = response.get("name");
			String[] fundTypes = response.get("type");
			for(int i = 0; i < responseSize; i++) {
				fundNameToFundType.put(fundNames[i], fundTypes[i]);
			}

			// Iterate through each of these fund names (portfolios first, individuals second)
			LinkedList<String> fundsToCalculate = new LinkedList<String>(Arrays.asList(response.get("name")));
			HashMap<String, FundWorth> fundWorths = new HashMap<String, FundWorth>();
			while (!fundsToCalculate.isEmpty()) {
				// Get all transactions where the fund name matches the current fund name
				String fundToProcess = fundsToCalculate.pollFirst();
				System.out.println("next fund: " + fundToProcess + " size of list: " + fundsToCalculate.size());
				query = con.prepareStatement("SELECT * FROM Activity WHERE name=? OR security=? ORDER BY year ASC, month ASC, day ASC");
				query.setString(1, fundToProcess);
				query.setString(2, fundToProcess);
				responseSize = DatabaseManager.executeQuery(query, response);
				if (responseSize < 1) {
					throw new SQLException(
							"Database constraint violated. Fund can't exist without being created first.");
				}

				// If any of these transactions contain a bought/sold security that hasn't yet been processed, put this
				// at the back of the list
				for (String securityToCheck : Arrays.asList(response.get("security"))) {
					if (!securityToCheck.equals(fundToProcess) && !fundWorths.containsKey(securityToCheck)
							&& !Utilities.isCompany(securityToCheck)) {
						if (!fundsToCalculate.contains(securityToCheck)) {
							fundsToCalculate.addLast(securityToCheck); // Add the missing fund to the list to be
																		// processed
						}
						fundsToCalculate.addLast(fundToProcess); // Add the current fund to the back of the list to be
						// processed later
						continue;
					}
				}

				// Process transactions - build a FundWorth object for this fund during processing
				String[] names = response.get("name");
				String[] securities = response.get("security");
				String[] types = response.get("type");
				String[] years = response.get("year");
				String[] months = response.get("month");
				String[] days = response.get("day");
				String[] amounts = response.get("amount");
				System.out.println("Date: " + Utilities.getDateObject(2012, 12, 12));

				for (int i = 0; i < responseSize; i++) {
					Date transactionDate = Utilities.getDateObject(years[i], months[i], days[i]);
					System.out.println("Transaction: " + types[i] + " name: " + names[i] + " securities: " + securities[i]);
					if (types[i].equals("C")) {
						// If this fund does not exist, create it with the initial cash amount
						if (fundWorths.get(fundToProcess) == null) {
							FundWorth fw = new FundWorth(names[i], transactionDate, Float.parseFloat(amounts[i]));
							fundWorths.put(fundToProcess, fw);
						}
						// If this fund already exists, simply add cash to it
						else {
							FundWorth fw = fundWorths.get(fundToProcess);
						}
					} else if (types[i].equals("B")) {
						// Check to see if the buy transaction is this fund buying into a different security
						if (names[i].equals(fundToProcess) && !securities[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.fundBuyFund(securities[i], Float.parseFloat(amounts[i]), transactionDate);
						}
						// Check to see if the buy transaction is someone else buying into this fund
						else if (securities[i].equals(fundToProcess) && !names[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.stakeHolderBuy(securities[i], Float.parseFloat(amounts[i]), transactionDate);
						}
						// If this buy transaction is a fund buying into itself, or does not involve the current fund at
						// all, there is an error
						else {
							throw new SQLException("Database constraint violated. Invalid buy transaction on "
									+ years[i] + "-" + months[i] + "-" + days[i]);
						}
					} else if (types[i].equals("S")) {
						// Check to see if the sell transaction is this fund selling one of its securities
						if (names[i].equals(fundToProcess) && !securities[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.fundSellFund(securities[i], transactionDate);
						}
						// Check to see if the sell transaction is someone else selling this fund
						else if (securities[i].equals(fundToProcess) && !names[i].equals(fundToProcess)) {
							FundWorth fw = fundWorths.get(fundToProcess);
							fw.stakeHolderSell(securities[i], transactionDate);
						}
						// If this sell transaction is a fund selling itself, or does not involve the current fund at
						// all, there is an error
						else {
							throw new SQLException("Database constraint violated. Invalid sell transaction on "
									+ years[i] + "-" + months[i] + "-" + days[i]);
						}
					} else if (types[i].equals("SB")) {

					} else {
						throw new SQLException("Database constraint violated. Type must be C, B, or S.");
					}
				}
				//fundWorths.put(fundToProcess, null);
			}
			
			// Format JSON response. Build string arrays to store into the response
			JSONObject queryResult = new JSONObject();
			String[] name = new String[fundWorths.size()];
			String[] startWorth = new String[fundWorths.size()];
			String[] endWorth = new String[fundWorths.size()];
			String[] returnRate = new String[fundWorths.size()];
			String[] cash = new String[fundWorths.size()];
			String[] investments = new String[fundWorths.size()];
			String[] type = new String[fundWorths.size()];
			int i = 0;
			for (String s : fundWorths.keySet()) {
				FundWorth fw = fundWorths.get(s); 
				name[i] = s;
				startWorth[i] = String.valueOf(fw.getInitialFundWorth());
				endWorth[i] = String.valueOf(fw.getLastQuote().getNetWorth());
				returnRate[i] = "-1";
				cash[i] = String.valueOf(fw.getLastQuote().getCashAmount());
				investments[i] = String.valueOf(fw.getLastQuote().getInvestmentAmount());
				type[i] = fundNameToFundType.get(s);
				i++;
			}
			queryResult.put("name", name);
			queryResult.put("startWorth", startWorth);
			queryResult.put("endWorth", endWorth);
			queryResult.put("returnRate", returnRate);
			queryResult.put("cash", cash);
			queryResult.put("investments", investments);
			queryResult.put("type", type);
			return queryResult.toString();
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
}
