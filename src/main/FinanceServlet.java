package main;

import java.io.IOException;
import java.io.PrintWriter;
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

import queryTypes.AddTransaction;
import queryTypes.CompanyQuery;
import queryTypes.FundQuery;
import queryTypes.UpdateQuotes;
import utilities.Utilities;

@WebServlet("/FinanceServlet")
public class FinanceServlet extends HttpServlet {

	private static final long serialVersionUID = 5955178292419622844L;
	private static final int STATUS_SUCCESS = 0;
	private static final int STATUS_ERROR = 400;
	private static final int STATUS_NOT_IMPLEMENTED = 501;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String message = "";
		try {
			String method = request.getParameter("method");
			String json = request.getParameter("json");
			System.out.println("GET Request: method=" + method + "   json=" + json + "\n");
			if (method != null) {
				if (method.equals(Utilities.METHOD_COMPANY_QUERY)) {
					System.out.println("Company Query request received.");
					message = CompanyQuery.companyQuery(json);
				} else if (method.equals(Utilities.METHOD_UPDATE_QUOTES)) {
					System.out.println("Update Quotes request received.");
					message = UpdateQuotes.updateQuotes(json);
				} else if (method.equals(Utilities.METHOD_FUND_QUERY)) {
					System.out.println("Fund Query request received.");
					message = FundQuery.fundQuery(json);
				} else if (method.equals(Utilities.METHOD_ADD_TRANSACTION)) {
					System.out.println("Add Transaction request received." + json);
					AddTransaction.addTransactionFromJSON(json);
					message = "{}";
				} else {
					// Ignore the request if it does not match one of the method types
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
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String message = "";
		int status = STATUS_SUCCESS;
		System.out.println("POST request received.");
		if (request.getContentType() != null && request.getContentType().indexOf("multipart/form-data") >= 0) {

			boolean isMultipartContent = ServletFileUpload.isMultipartContent(request);
			if (!isMultipartContent) {
				message = "POST Request should be multipart.\n";
				status = STATUS_ERROR;
			} else {

				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				try {
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
							String transactionFile = fileItem.getString();
							String[] transactions = transactionFile.split("\n");
							for (int i = 0; i < transactions.length; i++) {
								transactions[i].replaceAll("\\s+", "");
								//System.out.println("Transaction " + i + ": " + transactions[i]);
							}
							AddTransaction.addTransaction(transactions);
						}
					}
				} catch (FileUploadException e) {
					e.printStackTrace();
					
				} catch (IllegalArgumentException e) {
					message = e.getMessage();
					status = STATUS_ERROR;
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
		// System.out.println("Response: " + message);
		response.getWriter().write(message);
	}
}