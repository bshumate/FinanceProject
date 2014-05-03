package main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;

import database.DatabaseManager;
import queryTypes.AddTransaction;
import queryTypes.CompanyQuery;
import queryTypes.FundQuery;
import queryTypes.UpdateQuotes;

/**
 * FinanceServlet is the main server-side interface with the client. It responds to GET and POST requests. It responds
 * to the following methods:
 * <ul>
 * <li><b>GET</b>
 * <ul>
 * <li>companyQuery - Get information about stocks</li>
 * <li>updateQuotes - Update stock quotes in the database, using Yahoo Finance's quote system</li>
 * <li>fundQuery - Get information about funds. A "Fund" can be either a <i>portfolio</i> (which can only hold stocks)
 * or an <i>individual</i> (which can hold stocks and portfolios)</li>
 * <li>getFundTransactions - Returns all the transactions made by a fund</li>
 * <li>addTransaction - Adds 1 or more transactions to the database. The GET version of this method is for single-line
 * text input</li>
 * </ul>
 * </li>
 * <li><b>POST</b>
 * <ul>
 * <li>addTransaction - Adds 1 or more transactions to the database. The POST version of this method is for .csv
 * transaction files</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * 
 * @author Ben_Shumate
 */
@WebServlet("/FinanceServlet")
public class FinanceServlet extends HttpServlet {

	public static long totalDBAccessTime = 0;
	public static long numDBAccesses = 0;
	public static long totalFundLookupTime = 0;

	private static final long serialVersionUID = 942174704437264640L;
	private static final int STATUS_SUCCESS = 0;
	private static final int STATUS_ERROR = 400;
	private static final int STATUS_NOT_IMPLEMENTED = 501;

	private static final String METHOD_COMPANY_QUERY = "companyQuery";
	private static final String METHOD_UPDATE_QUOTES = "updateQuotes";
	private static final String METHOD_FUND_QUERY = "fundQuery";
	private static final String METHOD_GET_FUND_TRANSACTIONS = "getFundTransactions";
	private static final String METHOD_ADD_TRANSACTION = "addTransaction";

	/**
	 * Stores a connection to the MySQL database. Every time a request is received, a connection is opened.
	 */
	public static Connection con;

	/**
	 * Processes and responds to GET requests
	 * 
	 * @request The GET request
	 * @response The GET response
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long time = System.currentTimeMillis();
		String message = "";
		try {
			con = DatabaseManager.getNewConnection(); // Established a connection with the MySQL database
			String method = request.getParameter("method"); // Figure out which method was called by the client
			String json = request.getParameter("json"); // Get the data send by the client
			System.out.println("GET Request: method=" + method + "   json=" + json + "\n");
			if (method != null) {
				if (method.equals(METHOD_COMPANY_QUERY)) {
					System.out.println("Company Query request received.");
					message = CompanyQuery.companyQuery(json);
				} else if (method.equals(METHOD_UPDATE_QUOTES)) {
					System.out.println("Update Quotes request received.");
					message = UpdateQuotes.updateQuotes();
				} else if (method.equals(METHOD_FUND_QUERY)) {
					System.out.println("Fund Query request received.");
					message = FundQuery.fundQuery(json);
				} else if (method.equals(METHOD_GET_FUND_TRANSACTIONS)) {
					System.out.println("Get fund transactions request received.");
					message = FundQuery.getFundTransactions(json);
				} else if (method.equals(METHOD_ADD_TRANSACTION)) {
					System.out.println("Add Transaction request received." + json);
					AddTransaction.addTransactionFromJSON(json);
					message = "{}";
				} else {
					// The request did not match one of the method types
					response.setStatus(STATUS_NOT_IMPLEMENTED);
				}
			}
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			System.out.println("Response: " + message);
			response.getWriter().write(message);
		} catch (ClassNotFoundException e) {
			response.setStatus(STATUS_ERROR);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			response.setStatus(STATUS_ERROR);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			response.setStatus(STATUS_ERROR);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			response.setStatus(STATUS_ERROR);
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			response.setStatus(STATUS_ERROR);
			response.getWriter().write("Internal error");
			e.printStackTrace();
		} finally {
			// Close the MySQL connection
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ignore) {
				}
			}
			time = System.currentTimeMillis() - time;
			System.out.println("Num Accesses: " + numDBAccesses + ". Total DB Access Time: "
					+ (float) totalDBAccessTime / 1000);
			System.out.println("Total fund lookup time: " + (float) totalFundLookupTime / 1000);
			System.out.println("Time for transaction: " + (float) time / 1000);
			numDBAccesses = 0;
			totalDBAccessTime = 0;
		}
	}

	/**
	 * Processes and responds to POST requests
	 * 
	 * @request The POST request
	 * @response The POST response
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long time = System.currentTimeMillis();
		String message = "";
		int status = STATUS_SUCCESS;
		System.out.println("POST request received.");
		if (request.getContentType() != null && request.getContentType().indexOf("multipart/form-data") >= 0) {
			// The post request contains files to upload
			boolean isMultipartContent = ServletFileUpload.isMultipartContent(request);
			if (!isMultipartContent) {
				message = "POST Request should be multipart.\n";
				status = STATUS_ERROR;
			} else {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				try {
					// Make sure the file is present
					List<FileItem> fields = upload.parseRequest(request);
					System.out.println("Size of list: " + fields.size());
					Iterator<FileItem> it = fields.iterator();
					if (fields.size() == 0) {
						message = "No upload file was specified.\n";
						status = STATUS_ERROR;
					} else if (fields.size() > 1) {
						message = "Only 1 item should be uploaded at a time.\n";
						status = STATUS_ERROR;
					} else {
						// Make sure the file is of the right type
						FileItem fileItem = it.next();
						boolean isFormField = fileItem.isFormField();
						if (isFormField) {
							message = "Upload File Item should not be a form field.\n";
							status = STATUS_ERROR;
						} else if (fileItem.getSize() == 0) {
							message = "Upload File was not specified.\n";
							status = STATUS_ERROR;
						} else if (!fileItem.getContentType().equals("text/csv")) {
							message = "File name should have the extension .csv";
							status = STATUS_ERROR;
						} else {
							// Get the text from the file
							String transactionFile = fileItem.getString();
							String[] transactions = transactionFile.split("\n");
							for (int i = 0; i < transactions.length; i++) {
								transactions[i].replaceAll("\\s+", "");
							}
							// Process the uploaded file
							con = DatabaseManager.getNewConnection();
							AddTransaction.addTransaction(transactions);
						}
					}
				} catch (FileUploadException e) {
					e.printStackTrace();
					message = e.getMessage();
					status = STATUS_ERROR;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					message = e.getMessage();
					status = STATUS_ERROR;
				} catch (SQLException e) {
					e.printStackTrace();
					message = e.getMessage();
					status = STATUS_ERROR;
				} finally {
					if (con != null) {
						try {
							con.close();
						} catch (SQLException ignore) {
						}
					}
					time = System.currentTimeMillis() - time;
					System.out.println("Time for transaction: " + (float) time / 1000);
				}
			}
		} else {
			status = STATUS_ERROR;
			message = "POST Request was not of content type multipart/form-data";
		}
		if (status != STATUS_SUCCESS) {
			response.setStatus(STATUS_ERROR);
		}
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.getWriter().write(message);
	}
}