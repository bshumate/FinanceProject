package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

public class DatabaseManager {

	private static String username = "root";
	private static String pwd = "pwd";
	private static String dbLocation = "localhost:3306/FinanceDB";

	public static final int QUERY = 0;
	public static final int UPDATE = 1;

	public static Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(("jdbc:mysql://") + dbLocation, username, pwd);
	}

	public static String executeUpdate(String query) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(("jdbc:mysql://") + dbLocation, username, pwd);
		Statement st = con.createStatement();
		try {
			return String.valueOf(st.executeUpdate(query));
		} finally {
			if (con != null) {
				try {
					st.close();
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	public static void executeUpdate(PreparedStatement query) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(("jdbc:mysql://") + dbLocation, username, pwd);
		Statement st = con.createStatement();
		try {
			query.executeUpdate();
		} finally {
			if (con != null) {
				try {
					st.close();
					con.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

	public static int executeQuery(PreparedStatement query, HashMap<String, String[]> response)
			throws ClassNotFoundException, SQLException {

		// Class.forName("com.mysql.jdbc.Driver");
		ResultSet rs;
		rs = query.executeQuery();

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
