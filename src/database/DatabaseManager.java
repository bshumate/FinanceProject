package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import main.FinanceServlet;

/**
 * DatabaseManager manages all communication with the MySQL database. It can return connections, execute updates, and
 * execute queries
 * 
 * @author Ben_Shumate
 * 
 */
public class DatabaseManager {

	private static String username = "root";
	private static String pwd = "pwd";
	private static String dbLocation = "localhost:3306/FinanceDB";

	public static final int QUERY = 0;
	public static final int UPDATE = 1;

	/**
	 * Get a new connection to the database
	 * 
	 * @return The Connection object
	 * @throws SQLException
	 */
	public static Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(("jdbc:mysql://") + dbLocation, username, pwd);
	}

	/**
	 * Executes an update
	 * 
	 * @param update
	 *            A String representation of the update
	 * @return result of the update
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static String executeUpdate(String update) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Statement st = FinanceServlet.con.createStatement();
		return String.valueOf(st.executeUpdate(update));
	}

	/**
	 * Executes an update
	 * 
	 * @param update
	 *            A PreparedStatement representation of the update
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void executeUpdate(PreparedStatement update) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		long time = System.currentTimeMillis();
		FinanceServlet.numDBAccesses++;
		update.executeUpdate();
		time = System.currentTimeMillis() - time;
		FinanceServlet.totalDBAccessTime += time;
	}

	/**
	 * Executes a query and stores the response as a set of String arrays
	 * 
	 * @param query
	 *            Query to be executed
	 * @param response
	 *            Response object to store the database response
	 * @return The response size
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static int executeQuery(PreparedStatement query, HashMap<String, String[]> response)
			throws ClassNotFoundException, SQLException {

		// Class.forName("com.mysql.jdbc.Driver");
		ResultSet rs;
		long time = System.currentTimeMillis();
		rs = query.executeQuery();
		time = System.currentTimeMillis() - time;
		FinanceServlet.totalDBAccessTime += time;
		FinanceServlet.numDBAccesses++;

		int numCols = rs.getMetaData().getColumnCount();
		int responseSize = 0;
		ArrayList<ArrayList<String>> queryResult = new ArrayList<ArrayList<String>>(); // This will have a list entry
																						// for each column

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
		rs.close();
		return responseSize;
	}
}
