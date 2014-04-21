package main;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import queryTypes.CompanyQuery;
import queryTypes.FundQuery;
import queryTypes.UpdateQuotes;
import utilities.Utilities;

@WebServlet("/FinanceServlet")
public class FinanceServlet extends HttpServlet {

	private static final long serialVersionUID = 5955178292419622844L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String message = "";
		try {
			String method = request.getParameter("method");
			String json = request.getParameter("json");
			System.out.println("Request: method=" + method + "   json=" + json + "\n");
			if (method != null) {
				if (method.equals(Utilities.METHOD_COMPANY_QUERY)) {
					System.out.println("Company Query request received.\n");
					message = CompanyQuery.companyQuery(json);
				} else if (method.equals(Utilities.METHOD_UPDATE_QUOTES)) {
					System.out.println("Update Quotes request received.\n");
					message = UpdateQuotes.updateQuotes(json);
				} else if (method.equals(Utilities.METHOD_FUND_QUERY)) {
					System.out.println("Fund Query request received");
					message = FundQuery.fundQuery(json);
				} else {
					// Ignore the request if it does not match one of the method types
					response.setStatus(501);
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
		} catch (Exception e) {
			response.setStatus(400);
			response.getWriter().write("Internal error");
			e.printStackTrace();
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}