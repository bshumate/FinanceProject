var g_fundName = [];
var g_fundType = [];
var g_fundStartWorth = [];
var g_fundEndWorth = [];
var g_fundReturnRate = [];
var g_fundCash = [];
var g_fundInvestments = [];
var g_majorityParticipant = [];

function fundViewInit() {
	$("#fundSubmitButton").click(function() {
		fundQuery();
	});

	$("#fundExportToCSV").click(
		function() {
			console.log("Fund export to CSV clicked.");
			var stringToCSV = "Name, Type, Starting Worth ($), Ending Worth ($), Return/Yr (%), Cash ($), Investments ($), Majority Participant In\n";
			for (var i = 0; i < g_fundName.length; i++) {
				stringToCSV += (g_fundName[i] + ", " + g_fundType[i] + ", " + g_fundStartWorth[i] + ", " + g_fundEndWorth[i] + ", " + g_fundReturnRate[i] + ", " + g_fundCash[i] + ", "
					+ g_fundInvestments[i] + ", " + g_majorityParticipant[i] + "\n");
			}
			download("fundView.csv", stringToCSV);
		});

	$("#fundAlertSuccessButton").click(function(e) {
		$("#fundAlertSuccess").hide();
	});
	$("#fundAlertErrorButton").click(function(e) {
		$("#fundAlertError").hide();
	});
};

function fundQuery() {
	console.log("Fund refresh table button clicked.");
	$("#fundAlertSuccess").hide();
	$("#fundAlertError").hide();
	var filter = controller.filters.fundFilter();

	// Input validation
	var fromDate = $("#fundFromDate").val();
	var toDate = $("#fundToDate").val();
	var formatType = "MM-DD-YYYY";
	if (validateDate(fromDate, toDate, formatType) != true) {
		return; // There was an error processing the date
	}

	// Get increasing/decreasing value
	var increasing = false;
	var decreasing = false;
	var interval = "";
	if ($("#fundIncDecCheckbox").is(':checked')) {
		if ($("#fundIncDecMenu").val() == "Increasing") {
			increasing = true;
		} else {
			decreasing = true;
		}
		interval = $("#fundIntervalMenu").val();
	}

	// Get individual and portfolio checkboxes
	var individual = false;
	var portfolio = false;
	if ($("#fundIndividualCheckbox").is(':checked')) {
		individual = true;
	}
	if ($("#fundPortfolioCheckbox").is(':checked')) {
		portfolio = true;
	}

	// TODO - Implement showOnly and showAllBut

	filter.fromDate = fromDate;
	filter.toDate = toDate;
	filter.increasing = increasing;
	filter.decreasing = decreasing;
	filter.interval = interval;
	filter.individual = individual;
	filter.portfolio = portfolio;

	var message = "";
	var successCallback = function(data) {
		if (data['name'] == null || data['type'] == null || data['startWorth'] == null || data['endWorth'] == null || data['returnRate'] == null || data['cash'] == null || data['investments'] == null) {
			errorCallback("Error: Server response was missing necessary data.");
			return;
		}

		g_fundName = data['name'];
		g_fundType = data['type'];
		g_fundStartWorth = data['startWorth'];
		g_fundEndWorth = data['endWorth'];
		g_fundReturnRate = data['returnRate'];
		g_fundCash = data['cash'];
		g_fundInvestments = data['investments'];
		g_majorityParticipant = data['majorityParticipant'];
		
		var deepCopyEndWorth = $.extend(true, [], g_fundEndWorth);
		for (var i = 0; i < deepCopyEndWorth.length; i++) {
			deepCopyEndWorth[i] = parseFloat(deepCopyEndWorth[i]);
		}
		deepCopyEndWorth.sort(function(a,b) { return a - b;}).reverse();
		var rankingOfNetWorths = [];
		for (var i = 0; i < deepCopyEndWorth.length; i++) {
			rankingOfNetWorths[i] = deepCopyEndWorth[i];
		}
		
		console.log(rankingOfNetWorths);
		
		for (var i = 0; i < g_fundType.length; i++) {
			if (g_fundType[i] == 'I')
				g_fundType[i] = 'Individual';
			else if (g_fundType[i] == 'P')
				g_fundType[i] = 'Portfolio';
		}
		var tableUpdate = '';
		var i = 0;
		for (var i = 0; i < g_fundName.length; i++) {
			tableUpdate += ("<tr><td>" + ((rankingOfNetWorths.indexOf(parseFloat(g_fundEndWorth[i])))+1) + "</td><td>" + g_fundName[i] + "</td><td>" + g_fundType[i] + "</td><td>" + g_fundStartWorth[i] + "</td><td>" + g_fundEndWorth[i] + "</td><td>"
				+ g_fundReturnRate[i] + "%</td><td>" + g_fundCash[i] + "</td><td>" + g_fundInvestments[i] + "</td><td>" + g_majorityParticipant[i] + "</td></tr>");
		}
		$("#fund-table-body").empty().append(tableUpdate);
		$("#fundAlertSuccess").show();

		// Tell tablesorter plugin to update the table, sort on the End Worth column in descending order
		$("#fund-table").trigger("update");
		$("#fund-table thead").find("th:eq(4)").trigger("sorton", [ [ [ 4, 1 ] ] ]);

	};
	var errorCallback = function(errorMessage) {
		$("#fundAlertError").show();
		$("#fundAlertErrorText").html(errorMessage);
	};
	controller.server.fundQuery(filter, successCallback, errorCallback);
	return message;
}

$(document).ready(function() {
	fundViewInit();
	$(function() {
		$("#fund-table").tablesorter();
	});
});