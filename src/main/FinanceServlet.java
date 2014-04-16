package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

@WebServlet("/FinanceServlet")
public class FinanceServlet extends HttpServlet {

	private static final long serialVersionUID = -5732935417833331356L;
	private static DatabaseManager dbManager;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String message = "";
		try {
			if (dbManager == null) {
				dbManager = DatabaseManager.getInstance();
			}
			String method = request.getParameter("method");
			String json = request.getParameter("json");
			System.out.println("Request: method=" + method + "   json=" + json + "\n");
			if (method != null) {
				if (method.equals(Properties.METHOD_COMPANY_QUERY)) {
					System.out.println("Company Query request received.\n");
					message = dbManager.companyQuery(json);
				} else if (method.equals(Properties.METHOD_UPDATE_QUOTES)) {
					System.out.println("Update Quotes request received.\n");
					message = dbManager.updateQuotes(json);
				} else {
					// Ignore the request if it does not match one of the method types
				}
			}
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			// PrintWriter out = response.getWriter();
			System.out.println("Response: " + message);
			response.getWriter().write(message);
		} catch (ClassNotFoundException e) {
			response.setStatus(400);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			response.setStatus(400);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			response.setStatus(400);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}