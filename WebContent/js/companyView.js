var g_companySymbol = [];
var g_companyStartPrice = [];
var g_companyEndPrice = [];
var g_companyReturnRate = [];
var g_companyHigh = [];
var g_companyLow = [];
var g_companyRisk = [];

function companyViewInit() {
	$("#companySubmitButton").click(function() {
		companyQuery();
	});

	$("#quotesSubmitButton").click(function() {
		updateQuotes();
	});
	
	$("#companyExportToCSV").click(
		function() {
			console.log("Company export to CSV clicked.");
			var stringToCSV = "Symbol, Starting Price ($), Ending Price ($), Return/Yr (%), High ($), Low ($), Risk (%)\n";
			for (var i = 0; i < g_companySymbol.length; i++) {
				stringToCSV += (g_companySymbol[i] + ", " + g_companyStartPrice[i] + ", " + g_companyEndPrice[i] + ", " + g_companyReturnRate[i] + ", " + g_companyHigh[i] + ", "
					+ g_companyLow[i] + ", " + g_companyRisk[i] + "\n");
			}
			download("companyView.csv", stringToCSV);
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
		if (data['symbol'] == null || data['startPrice'] == null || data['endPrice'] == null || data['returnRate'] == null || data['high'] == null || data['low'] == null || data['risk'] == null) {
			errorCallback("Error: Server response was missing necessary data.");
			return;
		}
		
		g_companySymbol = data['symbol'];
		g_companyStartPrice = data['startPrice'];
		g_companyEndPrice = data['endPrice'];
		g_companyReturnRate = data['returnRate'];
		g_companyHigh = data['high'];
		g_companyLow = data['low'];
		g_companyRisk = data['risk'];
		
		var tableUpdate = '';
		var i = 0;
		for (var i = 0; i < data['symbol'].length; i++) {
			tableUpdate += ("<tr><td>" + (i + 1) + "</td><td><a href=\"http://finance.yahoo.com/q?s=" + g_companySymbol[i] + "\" target=\"_blank\">" + g_companySymbol[i] + "</a></td><td>"
				+ g_companyStartPrice[i] + "</td><td>" + g_companyEndPrice[i] + "</td><td>" + g_companyReturnRate[i] + "%</td><td>" + g_companyHigh[i] + "</td><td>" + g_companyLow[i] + "</td><td>"
				+ g_companyRisk[i] + "%</td></tr>");
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