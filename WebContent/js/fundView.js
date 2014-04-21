function fundViewInit() {
	$("#fundSubmitButton").click(function() {
		fundQuery();
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

		console.log("Success!");
		console.log(data);

		if (data['name'] == null || data['startWorth'] == null || data['endWorth'] == null || data['returnRate'] == null || data['cash'] == null || data['investments'] == null || data['type'] == null) {
			errorCallback("Error: Server response was missing necessary data.");
			return;
		}
		for(var i = 0; i < data['type'].length; i++) {
			if(data['type'][i] == 'I')
				data['type'][i] = 'Individual';
			else if(data['type'][i] == 'P')
				data['type'][i] = 'Portfolio';
		}
		data['dummy'] = 0;
		var tableUpdate = '';
		var i = 0;
		for (var i = 0; i < data['name'].length; i++) {
			tableUpdate += ("<tr><td>" + (i + 1) + "</td><td>" + data['name'][i] + "</td><td>" + data['startWorth'][i] + "</td><td>" + data['endWorth'][i] + "</td><td>" + data['returnRate'][i] + "%</td><td>"
				+ data['cash'][i] + "</td><td>" + data['investments'][i] + "</td><td>" + data['type'][i] + "</td></tr>");
		}
		$("#fund-table-body").empty().append(tableUpdate);
		$("#fundAlertSuccess").show();

		// Tell tablesorter plugin to update the table, sort on the 1st column
		$("#fund-table").trigger("update");
		$("#fund-table thead").find("th:eq(0)").trigger("sorton", [ [ [ 0, 0 ] ] ]);

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