package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import responses.CompanyQueryResponseEntry;

public class DatabaseManager {

	private static final DatabaseManager dbInstance = new DatabaseManager();
	private static String username = "root";
	private static String pwd = "pwd";
	private static String dbLocation = "localhost:3306/FinanceDB";

	public static final int QUERY = 0;
	public static final int UPDATE = 1;

	private DatabaseManager() {
		super();
	}

	public static synchronized DatabaseManager getInstance() {
		return dbInstance;
	}

	protected String companyQuery(String json) throws ClassNotFoundException, SQLException, JSONException {
		Connection con = DriverManager.getConnection(("jdbc:mysql://") + dbLocation, username, pwd);
		try {
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

			// Set up a map to store query responses for processing
			HashMap<String, CompanyQueryResponseEntry> responseMap = new HashMap<String, CompanyQueryResponseEntry>();

			// Make sure that the "From Date" is on or after Jan 3rd, 2005, which is the first market day of 2005
			if (fromYear < 2005 || (fromYear == 2005 && fromMonth < 1)
					|| (fromYear == 2005 && fromMonth == 1 && fromDay < 3)) {
				fromYear = 2005;
				fromMonth = 1;
				fromDay = 3;
			}

			// Get starting prices
			PreparedStatement query = con
					.prepareStatement("SELECT ticker, price FROM Quotes WHERE day=? AND month=? AND year=? GROUP BY ticker;");
			query.setInt(1, fromDay);
			query.setInt(2, fromMonth);
			query.setInt(3, fromYear);
			HashMap<String, String[]> response = new HashMap<String, String[]>();

			// Deal with the case where the market was closed on the start date
			int responseSize = 0, counter = 0;
			while (responseSize == 0) {
				responseSize = executeQuery(query, response);
				if (responseSize > 0)
					continue;
				counter++;
				if (counter == 10) {
					throw new SQLException("Query processing problem. Possibly a date issue.");
				}

				// Decrement the date by 1 day and try again
				fromDay--;
				if (fromDay == 0) {
					fromMonth--;
					if (fromMonth == 0)
						fromMonth = 12;
					fromDay = maxDaysInMonth(fromMonth);
				}
				query.setInt(1, fromDay);
				query.setInt(2, fromMonth);
				query.setInt(3, fromYear);
			}

			// Iterate through DB response, map tickers to response entry objects, fill in start price
			String[] tickers = response.get("ticker");
			String[] startPrices = response.get("price");
			for (int i = 0; i < responseSize; i++) {
				String ticker = tickers[i];
				float startPrice = startPrices[i] != null ? Float.parseFloat(startPrices[i]) : -1;
				CompanyQueryResponseEntry c;
				if (responseMap.get(ticker) == null) {
					c = new CompanyQueryResponseEntry();
				} else {
					c = responseMap.get(ticker);
				}
				c.setAttribute(CompanyQueryResponseEntry.START_PRICE, startPrice);
				responseMap.put(ticker, c);
			}

			// Get ending prices
			query = con
					.prepareStatement("SELECT ticker, price FROM Quotes WHERE (day=? OR day=? OR day=? OR day=?) AND month=? AND year=? GROUP BY ticker;");
			query.setInt(1, toDay);
			query.setInt(2, toDay + 1);
			query.setInt(3, toDay + 2);
			query.setInt(4, toDay + 3);
			query.setInt(5, toMonth);
			query.setInt(6, toYear);
			response = new HashMap<String, String[]>();
			responseSize = executeQuery(query, response);
			tickers = response.get("ticker");
			String[] endPrices = response.get("price");
			for (int i = 0; i < responseSize; i++) {
				// Iterate through DB response, map tickers to response entry objects, fill in start price
				String ticker = tickers[i];
				float endPrice = endPrices[i] != null ? Float.parseFloat(endPrices[i]) : -1;
				CompanyQueryResponseEntry c;
				if (responseMap.get(ticker) == null) {
					c = new CompanyQueryResponseEntry();
				} else {
					c = responseMap.get(ticker);
				}
				c.setAttribute(CompanyQueryResponseEntry.END_PRICE, endPrice);
				responseMap.put(ticker, c);
			}

			// If the user only wants to view strictly increasing/decreasing stocks, find stocks that meet that criteria
			if(increasing || decreasing) {
				
			}
			
			
			// Calculate return
			for (String s : responseMap.keySet()) {
				CompanyQueryResponseEntry c = responseMap.get(s);
				float start = c.getAttribute(CompanyQueryResponseEntry.START_PRICE);
				if (start < 0) {
					// This company did not have a price on the start date. Search to find it's first valid price within the date range
					query = con
							.prepareStatement("SELECT price FROM Quotes WHERE ticker=? ORDER BY year asc, month asc, day asc LIMIT 1;");
					query.setString(1, s);
					response = new HashMap<String, String[]>();
					executeQuery(query, response);
					start = Float.parseFloat(response.get("price")[0]);
					c.setAttribute(CompanyQueryResponseEntry.START_PRICE, start);
				}
				float end = c.getAttribute(CompanyQueryResponseEntry.END_PRICE);
				float returnRate = (start == -1 || end == -1.0) ? -1 : (100 * (end - start)) / start;
				c.setAttribute(CompanyQueryResponseEntry.RETURN_RATE, returnRate);
			}

			// Calculate price highs and lows
			/*
			 * response = new HashMap<String, String[]>(); for (String s : responseMap.keySet()) { query = con
			 * .prepareStatement("SELECT ticker, Max(price), Min(price) FROM Quotes WHERE ((year > ? AND year < ?) " +
			 * "OR ((year=? AND month < ?) OR (year=? AND month > ?)) OR ((year = ? AND month = ? AND day <= ?) " + "OR (year = ? AND month = ? AND day >= ?))) AND ticker=?;"); query.setInt(1,
			 * fromYear); query.setInt(2, toYear); query.setInt(3, toYear); query.setInt(4, toMonth); query.setInt(5, fromYear); query.setInt(6, fromMonth); query.setInt(7, toYear); query.setInt(8,
			 * toMonth); query.setInt(9, toDay); query.setInt(10, fromYear); query.setInt(11, fromMonth); query.setInt(12, fromDay); query.setString(13, s); executeQuery(query, response); tickers =
			 * response.get("ticker"); String[] highs = response.get("Max(price)"); String[] lows = response.get("Min(price)"); CompanyQueryResponseEntry c = responseMap.get(s);
			 * c.setAttribute(CompanyQueryResponseEntry.HIGH, Float.parseFloat(highs[0])); c.setAttribute(CompanyQueryResponseEntry.LOW, Float.parseFloat(lows[0])); responseMap.put(s, c); }
			 */

			// Calculate high, low, and risk
			response = new HashMap<String, String[]>();
			for (String s : responseMap.keySet()) {
				query = con
						.prepareStatement("SELECT price FROM Quotes WHERE ((year > ? AND year < ?) "
								+ "OR ((year=? AND month < ?) OR (year=? AND month > ?)) OR ((year = ? AND month = ? AND day <= ?) "
								+ "OR (year = ? AND month = ? AND day >= ?))) AND ticker=? ORDER BY year asc, month asc, day asc;");
				query.setInt(1, fromYear);
				query.setInt(2, toYear);
				query.setInt(3, toYear);
				query.setInt(4, toMonth);
				query.setInt(5, fromYear);
				query.setInt(6, fromMonth);
				query.setInt(7, toYear);
				query.setInt(8, toMonth);
				query.setInt(9, toDay);
				query.setInt(10, fromYear);
				query.setInt(11, fromMonth);
				query.setInt(12, fromDay);
				query.setString(13, s);
				executeQuery(query, response);
				float high = -1;
				float low = Float.MAX_VALUE;
				float maxPercentDropFromHigh = 0;
				for (String priceString : response.get("price")) {
					float price = Float.parseFloat(priceString);
					if (price > high) { // Find new high price
						high = price;
					}
					if (price < low) { // Find new low price
						low = price;
					}
					if (((high - price) / high) > maxPercentDropFromHigh) { // Find new risk
						maxPercentDropFromHigh = ((high - price) / high);
					}
				}
				CompanyQueryResponseEntry c = responseMap.get(s);
				c.setAttribute(CompanyQueryResponseEntry.HIGH, high);
				c.setAttribute(CompanyQueryResponseEntry.LOW, low);
				c.setAttribute(CompanyQueryResponseEntry.RISK, 100 * (maxPercentDropFromHigh));
			}

			// Format all results into JSON and return
			String[] finalTickers = responseMap.keySet().toArray(new String[responseMap.keySet().size()]);
			Arrays.sort(finalTickers);
			String[] finalStartPrice = new String[finalTickers.length];
			String[] finalEndPrice = new String[finalTickers.length];
			String[] finalReturnRate = new String[finalTickers.length];
			String[] finalHigh = new String[finalTickers.length];
			String[] finalLow = new String[finalTickers.length];
			String[] finalRisk = new String[finalTickers.length];
			for (int i = 0; i < finalTickers.length; i++) {
				CompanyQueryResponseEntry c = responseMap.get(finalTickers[i]);
				finalStartPrice[i] = (String.valueOf(c.startPrice).equals("-1.0") ? "--" : String.valueOf(c.startPrice));
				finalEndPrice[i] = (String.valueOf(c.endPrice).equals("-1.0") ? "--" : String.valueOf(c.endPrice));
				finalReturnRate[i] = (String.valueOf(c.returnRate).equals("-1.0") ? "--" : String.format("%.02f",
						c.returnRate));
				finalHigh[i] = (String.valueOf(c.high).equals("-1.0") ? "--" : String.valueOf(c.high));
				finalLow[i] = (String.valueOf(c.low).equals("-1.0") ? "--" : String.valueOf(c.low));
				finalRisk[i] = (String.valueOf(c.risk).equals("-1.0") ? "--" : String.format("%.02f", c.risk));
			}

			JSONObject queryResult = new JSONObject();
			queryResult = queryResult.put("symbol", finalTickers);
			queryResult.put("startPrice", finalStartPrice);
			queryResult.put("endPrice", finalEndPrice);
			queryResult.put("returnRate", finalReturnRate);
			queryResult.put("high", finalHigh);
			queryResult.put("low", finalLow);
			queryResult.put("risk", finalRisk);
			return queryResult.toString();
		} finally {
			// Always close SQL connections before returning
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	protected String updateQuotes(String json) throws ClassNotFoundException, SQLException, JSONException, IOException {
		int count = 0;

		// Update the Company table
		for (String s : Properties.stockList) {
			count++;
			String query = "INSERT IGNORE INTO Company (ticker) VALUES (\"" + s + "\");";
			System.out.println("Insertion " + count + "/" + Properties.stockList.length + ": " + s);
			System.out.println("Query: " + query);
			// executeUpdate(query);
		}

		// Update the Quotes table
		int i = 0;
		for (String s : Properties.stockList) {
			if (s != "GOOGL")
				continue;
			try {
				System.out.println(i + ": Quote for " + s);
				i++;
				URL csv = new URL("http://ichart.yahoo.com/table.csv?s=" + s
						+ "&a=0&b=1&c=2005&d=04&e=13&f=2014&g=d&f=a");
				BufferedReader in = new BufferedReader(new InputStreamReader(csv.openStream()));

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					// If the line we have is not the heading, parse it and send it as a query to the db
					Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
					Matcher matcher = pattern.matcher(inputLine);
					if (matcher.find() == true) {
						int year = Integer.parseInt(inputLine.substring(0, 4));
						int month = Integer.parseInt(inputLine.substring(5, 7));
						int day = Integer.parseInt(inputLine.substring(8, 10));
						String[] input = inputLine.split(",");
						float price = Float.parseFloat(input[4]);
						String query = "REPLACE INTO Quotes (ticker, day, month, year, price) VALUES (\"" + s + "\","
								+ day + "," + month + "," + year + "," + price + ")";
						// executeUpdate(query);
					}

				}
				in.close();
			} catch (Exception ignore) {
				// Ignore this exception and finish updating the rest of the quotes
				ignore.printStackTrace();
			}
		}
		return "{}";
	}

	protected String executeUpdate(PreparedStatement query) throws ClassNotFoundException, SQLException, JSONException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(("jdbc:mysql://") + dbLocation, username, pwd);
		try {
			return String.valueOf(query.executeUpdate());
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	protected int executeQuery(PreparedStatement query, HashMap<String, String[]> response)
			throws ClassNotFoundException, SQLException, JSONException {

		Class.forName("com.mysql.jdbc.Driver");
		ResultSet rs;
		rs = query.executeQuery();

		int numCols = rs.getMetaData().getColumnCount();
		int responseSize = 0;
		ArrayList<ArrayList<String>> queryResult = new ArrayList<ArrayList<String>>(); // This will have a list entry for each column

		// Initialize the array to hold each column
		for (int i = 0; i < numCols; i++) {
			queryResult.add(i, (new ArrayList<String>()));
		}

		// Fill each array. Every column is held in 1 list
		rs.absolute(0);
		while (rs.next()) {
			responseSize++;
			for (int i = 1; i <= numCols; i++) {
				ArrayList<String> col = queryResult.get(i - 1);
				col.add(rs.getString(i));
			}
		}

		// Map each array to its column name, store as JSON Object
		rs.absolute(0);
		for (int i = 0; i < numCols; i++) {
			String[] blank = new String[responseSize];
			response.put(rs.getMetaData().getColumnName(i + 1), queryResult.get(i).toArray(blank));
		}
		return responseSize;
	}

	private int maxDaysInMonth(int month) {
		int days = 31;  // January, March, May, July, August, October, December
		if (month == 2) {
			days = 29;  // February (has 29 days on leap years)
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			days = 30;  // April, June, September, November
		}
		
		return days;
	}
}
