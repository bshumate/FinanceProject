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

	var server = {
		// error handler functions
		handleAjaxError : function(jqXHR, textStatus, errorThrown, apiMethod) {
			return ("Error: " + jqXHR.status + "(" + jqXHR.statusText+").&nbsp;&nbsp;&nbsp;Method: " + apiMethod);
		},
		handleError : function(method, errorcode, message) {
			return ((method != null ? "Method: " + method + "\n" : "") + (errorcode != null ? "Error Code: " + errorcode + "\n" : "") + (message != null ? "Message: " + message : "No message"));
		},
		// ajax request function
		ajaxRequest : function(requestMethod, apiMethod, data, successCallback, errorCallback) {
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
				cache : false,
				dataType : "json",
				type : "GET",
				data : requestData,
				success : function(data) {
					console.log(data);
					if (data != null) {
						// Check for an error code in the JSON
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
			addTransaction : function(transactionList, successCallback, errorCallback) {
				server.ajaxRequest("GET", "addTransaction", transactionList, successCallback, errorCallback);
			}
		},

		filters : {
			companyFilter : function() {
				return new CompanyFilter();
			},
			fundFilter : function() {
				return new FundFilter();
			}
		}
	};

	return api;
};