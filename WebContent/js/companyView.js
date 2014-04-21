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
	var fromDate = $("#companyFromDate").val();
	var toDate = $("#companyToDate").val();
	var formatType = "MM-DD-YYYY";
	if (validateDate(fromDate, toDate, formatType) != true) {
		return; // There was an error processing the date
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
		if (data['symbol'] == null || data['startPrice'] == null || data['endPrice'] == null || data['returnRate'] == null || data['high'] == null || data['low'] == null || data['risk'] == null) {
			errorCallback("Error: Server response was missing necessary data.");
			return;
		}
		var tableUpdate = '';
		var i = 0;
		for (var i = 0; i < data['symbol'].length; i++) {
			tableUpdate += ("<tr><td>" + (i + 1) + "</td><td><a href=\"http://finance.yahoo.com/q?s=" + data['symbol'][i] + "\" target=\"_blank\">" + data['symbol'][i] + "</a></td><td>"
				+ data['startPrice'][i] + "</td><td>" + data['endPrice'][i] + "</td><td>" + data['returnRate'][i] + "%</td><td>" + data['high'][i] + "</td><td>" + data['low'][i] + "</td><td>"
				+ data['risk'][i] + "%</td></tr>");
		}
		$("#company-table-body").empty().append(tableUpdate);
		$("#companyAlertSuccess").show();

		// Tell tablesorter plugin to update the table, sort on the 1st column
		$("#company-table").trigger("update");
		$("#company-table thead").find("th:eq(0)").trigger("sorton", [ [ [ 0, 0 ] ] ]);

	};
	var errorCallback = function(errorMessage) {
		$("#companyAlertError").show();
		$("#companyAlertErrorText").html(errorMessage);
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