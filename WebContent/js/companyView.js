var daysInMonths = new Array();
daysInMonths["01"] = 31;
daysInMonths["02"] = 28;
daysInMonths["03"] = 31;
daysInMonths["04"] = 30;
daysInMonths["05"] = 31;
daysInMonths["06"] = 30;
daysInMonths["07"] = 31;
daysInMonths["08"] = 31;
daysInMonths["09"] = 30;
daysInMonths["10"] = 31;
daysInMonths["11"] = 30;
daysInMonths["12"] = 31;

function companyViewInit() {
	$("#companySubmitButton").click(function() {
		companyQuery();
	});

	$("#quotesSubmitButton").click(function() {
		updateQuotes();
	});
	$("#companyAlertSuccessButton").click(function(e) {
		$("#companyAlertSuccess").hide();
	});
	$("#companyAlertErrorButton").click(function(e) {
		$("#companyAlertError").hide();
	});
	$("#quotesAlertSuccessButton").click(function(e) {
		$("#quotesAlertSuccess").hide();
	});
	$("#quotesAlertErrorButton").click(function(e) {
		$("#quotesAlertError").hide();
	});

};

function companyQuery() {
	console.log("Company refresh table button clicked.");
	$("#companyAlertSuccess").hide();
	$("#companyAlertError").hide();
	var filter = controller.filters.companyFilter();

	// Input validation
	var dateRegEx = new RegExp("[0-9][0-9](\/|-)[0-9][0-9](\/|-)[0-9][0-9][0-9][0-9]");
	var fromDate = $("#companyFromDate").val();
	var toDate = $("#companyToDate").val();
	if (fromDate != "" && dateRegEx.exec(fromDate) == null) { // Check for mm/dd/yyyy
		alert("Error: FromDate formatted incorrectly.");
		return;
	}
	if (toDate != "" && dateRegEx.exec(toDate) == null) { // Check for mm/dd/yyyy
		alert("Error: ToDate formatted incorrectly.");
		return;
	}
	if (fromDate != "") { // Check for months 1-12, day that is valid for
		// the given month
		var month = fromDate.substr(0, 2);
		var day = fromDate.substr(3, 5);
		var dateValidation = daysInMonths[month];
		if (dateValidation == null || dateValidation < parseInt(day) || parseInt(day) < 1) {
			alert("Error: Invalid FromDate.");
			return;
		}
	}
	if (toDate != "") { // Check for months 1-12, day that is valid for
		// the given month
		var month = toDate.substr(0, 2);
		var day = toDate.substr(3, 5);
		var dateValidation = daysInMonths[month];
		if (dateValidation == null || dateValidation < parseInt(day) || parseInt(day) < 1) {
			alert("Error: Invalid ToDate.");
			return;
		}
	}

	// Get increasing/decreasing value
	var increasing = false;
	var decreasing = false;
	var interval = "";
	if ($("#companyIncDecCheckbox").is(':checked')) {
		if ($("#companyIncDecMenu").val() == "Increasing") {
			increasing = true;
		} else {
			decreasing = true;
		}
		interval = $("#companyIntervalMenu").val();
	}

	// TODO - Implement showOnly and showAllBut

	filter.fromDate = fromDate;
	filter.toDate = toDate;
	filter.increasing = increasing;
	filter.decreasing = decreasing;
	filter.interval = interval;
	console.log(filter);

	var message = "";
	var successCallback = function(data) {

		console.log("Success!");
		console.log(data);
		console.log(data['symbol']);
		var tableUpdate = '';
		var i = 0;
		for (var i = 0; i < data['symbol'].length; i++) {
			tableUpdate += ("<tr><td>" + (i + 1) + "</td><td><a href=\"#\">" + data['symbol'][i] + "</a></td><td>" + data['startPrice'][i] + "</td><td>" + data['endPrice'][i] + "</td><td>"
				+ data['returnRate'][i] + "%</td><td>" + data['high'][i] + "</td><td>" + data['low'][i] + "</td><td>" + data['risk'][i] + "%</td></tr>");
		}
		// $("#company-table-div").empty().append($_blankCompanyTable);
		$("#company-table-body").empty().append(tableUpdate);
		$("#companyAlertSuccess").show();

		// Tell tablesorter plugin to update the table, sort on the 1st column
		$("#company-table").trigger("update");
		$("#company-table thead").find("th:eq(0)").trigger("sorton", [ [ [ 0, 0 ] ] ]);

	};
	var errorCallback = function(errorMessage) {
		$("#companyAlertError").show();
		$("#companyAlertErrorText").text(errorMessage);
	};
	controller.server.companyQuery(filter, successCallback, errorCallback);
	return message;
}

function updateQuotes() {
	$("#quotesAlertSuccess").hide();
	$("#quotesAlertError").hide();
	var successCallback = function(data) {
		$("#quotesAlertSuccess").show();
	};
	var errorCallback = function(errorMessage) {
		console.log("error updating quotes");
		$("#quotesAlertError").show();
		$("#quotesAlertErrorText").text(errorMessage);
		console.log(errorMessage);
	};
	controller.server.updateQuotes(successCallback, errorCallback);
}

$(document).ready(function() {
	companyViewInit();
	$(function() {
		$("#company-table").tablesorter();
	});
});