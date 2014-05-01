var controller = new function() {

	// Object definitions
	var CompanyFilter = function() {
		return {
			fromDate : "",
			toDate : "",
			increasing : false,
			decreasing : false,
			interval : "",
			onlyShow : [],
			showAllBut : []
		};
	};
	var FundFilter = function() {
		return {
			fromDate : "",
			toDate : "",
			increasing : false,
			decreasing : false,
			interval : "",
			individual : true,
			portfolio : true,
			onlyShow : [],
			showAllBut : []
		};
	};
	
	var NewTransaction = function() {
		return {
			transaction : ""
		};
	};

	var server = {
		// error handler functions
		handleAjaxError : function(jqXHR, textStatus, errorThrown, apiMethod) {
			var errorMessage = jqXHR.responseText;
			if (!errorMessage) {
				errorMessage = jqXHR.statusText;
			}
			return ("Error: " + errorMessage);
		},
		handleError : function(method, errorcode, message) {
			return ((method != null ? "Method: " + method + "\n" : "") + (errorcode != null ? "Error Code: " + errorcode + "\n" : "") + (message != null ? "Message: " + message : "No message"));
		},
		// ajax request function
		ajaxRequest : function(requestMethod, apiMethod, data, successCallback, errorCallback) {

			if (requestMethod == "POST") {
				console.log("POST request method");
				var formData = new FormData(data);
				$.ajax({
					url : "FinanceServlet",
					type : 'POST',
					data : formData,
					mimeType : "multipart/form-data",
					contentType : false,
					cache : false,
					processData : false,
					success : function(data) {
						if (data != null) {
							console.log("successful response");
							console.log(data);
							successCallback(data);
						} else {
							var message = server.handleError(apiMethod, null, apiMethod + " returned no data");
							if (errorCallback) {
								errorCallback(message);
							}
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						var message = server.handleAjaxError(jqXHR, textStatus, errorThrown, apiMethod);
						if (errorCallback) {
							errorCallback(message);
						}
					}
				});
			} else {
				var requestData = {
					method : apiMethod
				};

				if (data != null) {
					requestData.json = JSON.stringify(data);
				} else {
					requestData.json = "{}";
				}
				console.log(requestData);

				$.ajax({
					url : "FinanceServlet",
					dataType : "json",
					type : requestMethod,
					data : requestData,
					cache : false,
					success : function(data) {
						if (data != null) {
							console.log("successful response");
							console.log(data);
							successCallback(data);
						} else {
							var message = server.handleError(apiMethod, null, apiMethod + " returned no data");
							if (errorCallback) {
								errorCallback(message);
							}
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						var message = server.handleAjaxError(jqXHR, textStatus, errorThrown, apiMethod);
						if (errorCallback) {
							errorCallback(message);
						}
					}
				});
			}
		}
	};

	var api = {
		server : {
			companyQuery : function(companyFilter, successCallback, errorCallback) {
				server.ajaxRequest("GET", "companyQuery", companyFilter, successCallback, errorCallback);
			},
			updateQuotes : function(successCallback, errorCallback) {
				server.ajaxRequest("GET", "updateQuotes", null, successCallback, errorCallback);
			},
			fundQuery : function(fundFilter, successCallback, errorCallback) {
				server.ajaxRequest("GET", "fundQuery", fundFilter, successCallback, errorCallback);
			},
			addTransaction : function(transaction, successCallback, errorCallback) {
				server.ajaxRequest("GET", "addTransaction", transaction, successCallback, errorCallback);
			},
			uploadTransactionFile : function(postData, successCallback, errorCallback) {
				server.ajaxRequest("POST", "uploadTransactionFile", postData, successCallback, errorCallback);
			}
		},

		filters : {
			companyFilter : function() {
				return new CompanyFilter();
			},
			fundFilter : function() {
				return new FundFilter();
			}
		},
		
		newTransaction : function() {
			return new NewTransaction();
		}
	};

	return api;
};