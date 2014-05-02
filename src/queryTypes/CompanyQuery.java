package queryTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.FinanceServlet;

import org.json.JSONException;
import org.json.JSONObject;

import utilities.QuoteDate;
import utilities.Utilities;
import database.DatabaseManager;

public class CompanyQuery {

	public float startPrice = -1;
	public float endPrice = -1;
	public float returnRate = -1;
	public float high = -1;
	public float low = -1;
	public float risk = -1;

	public static int START_PRICE = 0;
	public static int END_PRICE = 1;
	public static int RETURN_RATE = 2;
	public static int HIGH = 3;
	public static int LOW = 4;
	public static int RISK = 5;
	
	public static HashMap<QuoteDate, Float> quoteCache = new HashMap<QuoteDate, Float>();

	public CompanyQuery() {
		// Default constructor
	}

	public float getResponseData(int id) {
		if (id == START_PRICE)
			return startPrice;
		else if (id == END_PRICE)
			return endPrice;
		else if (id == RETURN_RATE)
			return returnRate;
		else if (id == HIGH)
			return high;
		else if (id == LOW)
			return low;
		else if (id == RISK)
			return risk;
		else
			return -1;
	}

	public void setResponseData(int id, float data) {
		if (id == START_PRICE)
			startPrice = data;
		else if (id == END_PRICE)
			endPrice = data;
		else if (id == RETURN_RATE)
			returnRate = data;
		else if (id == HIGH)
			high = data;
		else if (id == LOW)
			low = data;
		else if (id == RISK)
			risk = data;
	}

	// Return -1 on error
	public static Float getQuoteOfCompany(String ticker, int year_in, int month_in, int day_in) {
		PreparedStatement query = null;
		HashMap<String, String[]> response = new HashMap<String, String[]>();
		int responseSize = 0;
		float price = -1;
		int year = year_in;
		int month = month_in;
		int day = day_in;
		// Make sure that the "From Date" is on or after Jan 3rd, 2005, which is the first market day of 2005
		if (year < Utilities.EARLIEST_YEAR
				|| (year == Utilities.EARLIEST_YEAR && month < Utilities.EARLIEST_MONTH)
				|| (year == Utilities.EARLIEST_YEAR && month == Utilities.EARLIEST_MONTH && day < Utilities.EARLIEST_DAY)) {
			year = Utilities.EARLIEST_YEAR;
			month = Utilities.EARLIEST_MONTH;
			day = Utilities.EARLIEST_DAY;
		}
		try {
			QuoteDate tempQuote = new QuoteDate(ticker, year, month, day);
			if (quoteCache.containsKey(tempQuote)) {
				return quoteCache.get(tempQuote);
			}
			
			query = FinanceServlet.con.prepareStatement("SELECT price FROM Quotes WHERE ticker=? AND year=? AND month=? AND day=?");
			int counter = 0;
			
			while (responseSize < 1) {
				counter++;
				if (counter == 10) {
					query = FinanceServlet.con.prepareStatement("SELECT price FROM Quotes WHERE ticker=? ORDER BY year ASC, month ASC, day ASC LIMIT 1");
					query.setString(1, ticker);
					responseSize = DatabaseManager.executeQuery(query, response);
					break;
				}
				query.setString(1, ticker);
				query.setInt(2, year);
				query.setInt(3, month);
				query.setInt(4, day);
				responseSize = DatabaseManager.executeQuery(query, response);
				if (responseSize < 1) {
					// Decrement the date by 1 day and try again
					day--;
					if (day == 0) {
						month--;
						if (month == 0) {
							month = 12;
							year--;
						}
						day = Utilities.maxDaysInMonth(month);
					}
				}
				
				tempQuote.setNew(year, month, day);
				if (quoteCache.containsKey(tempQuote)) {
					return quoteCache.get(tempQuote);
				}
			}
			if (responseSize > 0) {
				String[] prices = response.get("price");
				price = Float.parseFloat(prices[0]);
				QuoteDate qd = new QuoteDate(ticker, year, month, day);
				QuoteDate qd_in = new QuoteDate(ticker, year_in, month_in, day_in);
				quoteCache.put(qd, price);
				quoteCache.put(qd_in, price);
			} else {
				return -1F;
			}
			return price;

		} catch (SQLException e) {
			e.printStackTrace();
			return -1F;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return -1F;
		} finally {
			// Always close the prepared statement
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	public static String companyQuery(String json) throws ClassNotFoundException, SQLException, JSONException {
		PreparedStatement query = null;
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
			HashMap<String, CompanyQuery> responseMap = new HashMap<String, CompanyQuery>();

			// Make sure that the "From Date" is on or after Jan 3rd, 2005, which is the first market day of 2005
			if (fromYear < Utilities.EARLIEST_YEAR
					|| (fromYear == Utilities.EARLIEST_YEAR && fromMonth < Utilities.EARLIEST_MONTH)
					|| (fromYear == Utilities.EARLIEST_YEAR && fromMonth == Utilities.EARLIEST_MONTH && fromDay < Utilities.EARLIEST_DAY)) {
				fromYear = Utilities.EARLIEST_YEAR;
				fromMonth = Utilities.EARLIEST_MONTH;
				fromDay = Utilities.EARLIEST_DAY;
			}

			// Get starting prices
			query = FinanceServlet.con
					.prepareStatement("SELECT ticker, price FROM Quotes WHERE day=? AND month=? AND year=? GROUP BY ticker;");
			query.setInt(1, fromDay);
			query.setInt(2, fromMonth);
			query.setInt(3, fromYear);
			HashMap<String, String[]> response = new HashMap<String, String[]>();

			// Deal with the case where the market was closed on the start date
			int responseSize = 0, counter = 0;
			while (responseSize == 0) {
				responseSize = DatabaseManager.executeQuery(query, response);
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
					if (fromMonth == 0) {
						fromMonth = 12;
						fromYear--;
					}
					fromDay = Utilities.maxDaysInMonth(fromMonth);
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
				CompanyQuery c;
				if (responseMap.get(ticker) == null) {
					c = new CompanyQuery();
				} else {
					c = responseMap.get(ticker);
				}
				c.setResponseData(CompanyQuery.START_PRICE, startPrice);
				responseMap.put(ticker, c);
			}

			// Get ending prices
			query = FinanceServlet.con
					.prepareStatement("SELECT ticker, price FROM Quotes WHERE (day=? OR day=? OR day=? OR day=?) AND month=? AND year=? GROUP BY ticker;");
			query.setInt(1, toDay);
			query.setInt(2, toDay + 1);
			query.setInt(3, toDay + 2);
			query.setInt(4, toDay + 3);
			query.setInt(5, toMonth);
			query.setInt(6, toYear);
			response = new HashMap<String, String[]>();
			responseSize = DatabaseManager.executeQuery(query, response);
			tickers = response.get("ticker");
			String[] endPrices = response.get("price");
			for (int i = 0; i < responseSize; i++) {
				// Iterate through DB response, map tickers to response entry objects, fill in start price
				String ticker = tickers[i];
				float endPrice = endPrices[i] != null ? Float.parseFloat(endPrices[i]) : -1;
				CompanyQuery c;
				if (responseMap.get(ticker) == null) {
					c = new CompanyQuery();
				} else {
					c = responseMap.get(ticker);
				}
				c.setResponseData(CompanyQuery.END_PRICE, endPrice);
				responseMap.put(ticker, c);
			}

			// Calculate return
			int totalDaysHeld = Utilities.calcTotalDaysHeld(fromDay, fromMonth, fromYear, toDay, toMonth, toYear);
			query = FinanceServlet.con
					.prepareStatement("SELECT price FROM Quotes WHERE ticker=? ORDER BY year asc, month asc, day asc LIMIT 1;");
			for (String s : responseMap.keySet()) {
				CompanyQuery c = responseMap.get(s);
				float start = c.getResponseData(CompanyQuery.START_PRICE);
				if (start < 0) {
					// This company did not have a price on the start date. Search to find it's first valid price within
					// the date range

					query.setString(1, s);
					response = new HashMap<String, String[]>();
					DatabaseManager.executeQuery(query, response);
					start = Float.parseFloat(response.get("price")[0]);
					c.setResponseData(CompanyQuery.START_PRICE, start);
				}
				float end = c.getResponseData(CompanyQuery.END_PRICE);
				float returnRate = Utilities.calcAnnualReturnRate(start, end, totalDaysHeld);
				c.setResponseData(CompanyQuery.RETURN_RATE, returnRate);
			}

			// Calculate high, low, and risk
			response = new HashMap<String, String[]>();
			String[] responseMapKeySet = responseMap.keySet().toArray(new String[responseMap.size()]);
			query = FinanceServlet.con
					.prepareStatement("SELECT price, year FROM Quotes WHERE ((year > ? AND year < ?) "
							+ "OR ((year=? AND month < ?) OR (year=? AND month > ?)) OR ((year = ? AND month = ? AND day <= ?) "
							+ "OR (year = ? AND month = ? AND day >= ?))) AND ticker=? ORDER BY year asc, month asc, day asc;");
			for (String s : responseMapKeySet) {
				// Query the database for a list of daily prices for the given ticker within the given date range
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
				responseSize = DatabaseManager.executeQuery(query, response);

				// Iterate through responses. Find the high and low. Calculate risk. Get each year-end price.
				float high = -1;
				float low = Float.MAX_VALUE;
				float maxPercentDropFromHigh = 0;
				String[] prices = response.get("price");
				String[] years = response.get("year");
				ArrayList<Float> annualPrices = new ArrayList<Float>(); // Find and store a yearly price list
				annualPrices.add(Float.parseFloat(prices[0])); // Add the first price to the list
				int currentYear = Integer.parseInt(years[0]);
				for (int i = 0; i < responseSize; i++) {
					float price = Float.parseFloat(prices[i]);
					if (price > high) { // Find new high price
						high = price;
					}
					if (price < low) { // Find new low price
						low = price;
					}
					if (((high - price) / high) > maxPercentDropFromHigh) { // Find new risk
						maxPercentDropFromHigh = ((high - price) / high);
					}
					if (currentYear < Integer.parseInt(years[i])) {
						// If the year of this price is greater than the last price, the last price was the year-end
						// price.
						currentYear = Integer.parseInt(years[i]);
						annualPrices.add(Float.parseFloat(prices[i - 1]));
					}
				}
				annualPrices.add(Float.parseFloat(prices[responseSize - 1])); // Add the last price to the list
				boolean shouldRemove = false;
				if (increasing) {
					// The user only wants to see stocks that have increased every year
					float lastPrice = -1;
					for (Float f : annualPrices) {
						if (f.floatValue() >= lastPrice) {
							lastPrice = f.floatValue();
						} else {
							// This stock is not increasing annually. Don't add it to the list
							shouldRemove = true;
							break;
						}
					}
				} else if (decreasing) {
					// The user only wants to see stocks that have increased every year
					float lastPrice = Float.MAX_VALUE;
					for (Float f : annualPrices) {
						if (f.floatValue() <= lastPrice) {
							lastPrice = f.floatValue();
						} else {
							// This stock is not decreasing annually. Don't add it to the list
							shouldRemove = true;
							break;
						}
					}
				}
				if (shouldRemove) {
					responseMap.remove(s);
				} else {
					CompanyQuery c = responseMap.get(s);
					c.setResponseData(CompanyQuery.HIGH, high);
					c.setResponseData(CompanyQuery.LOW, low);
					c.setResponseData(CompanyQuery.RISK, 100 * (maxPercentDropFromHigh));
				}
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
				CompanyQuery c = responseMap.get(finalTickers[i]);
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
			// Always close the prepared statement
			if (query != null) {
				try {
					query.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

}
