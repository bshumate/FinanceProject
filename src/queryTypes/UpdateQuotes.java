package queryTypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.FinanceServlet;

import org.json.JSONException;

import utilities.Utilities;
import database.DatabaseManager;

/**
 * UpdateQuotes is used to update the stock quotes via the Yahoo Finance quote system
 * 
 * @author Ben_Shumate
 * 
 */
public class UpdateQuotes {

	/**
	 * Updates all of the quotes
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws JSONException
	 * @throws IOException
	 */
	public static String updateQuotes() throws ClassNotFoundException, SQLException, JSONException, IOException {

		PreparedStatement query = FinanceServlet.con
				.prepareStatement("INSERT IGNORE INTO Company (ticker) VALUES (?);");

		// Update the Company table
		for (String s : Utilities.stockList) {
			query.setString(1, s);
			// executeUpdate(query);
		}

		// Update the Quotes table
		int i = 0;
		for (String s : Utilities.stockList) {
			i++;
			if (i < 70)
				continue;
			try {
				System.out.println(i + ": Quote for " + s);
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
						float price = Float.parseFloat(input[6]); // The "adjusted close" column in the Yahoo csv format
						String query2 = "REPLACE INTO Quotes (ticker, year, month, day, price) VALUES (\"" + s + "\","
								+ year + "," + month + "," + day + "," + price + ");";
						// System.out.println(query2);
						/*
						 * query.clearParameters(); query =
						 * con.prepareStatement("REPLACE INTO Quotes (ticker, year, month, day, price) VALUES (?,?,?,?,?);"
						 * ); query.setString(1, s); query.setInt(2, year); query.setInt(3, month); query.setInt(4,
						 * day); query.setFloat(5, price);
						 */
						DatabaseManager.executeUpdate(query2);
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
}
