$(document).ready(function() {
	console.log("The document is ready!");

	// Page header gradually changes color between blue and purple
	var count = 100;
	var direction = 1;
	setInterval(function() {
		var pane1 = $("#page-header");
		pane1[0].style.backgroundColor = ("rgba(" + String(count) + ",100,200, .5)");
		count += direction;
		if (count >= 170 || count <= 0) {
			direction *= -1;
		}
	}, 15);

	// Allow for the table to be sorted
	$(".column").click(function hoveron() {
		console.log($(this));
		if ($(this).hasClass("column-active")) {
			console.log("Change sort direction!");
		} else {
			console.log("Change sorted column!");

		}
	});

	$("#timeButton").click(function getCompanies() {
		var successCallback = function(data) {
			console.log("Success!");
			var tableUpdate = '';
			for (var i = 0; i < data.length; i++) {
				tableUpdate += ("<tr><td>" + (i + 1) + "</td><td>" + data[i]['city'] + "</td><td>" + data[i]['state'] + "</td><td>" + data[i]['population'] + "</td></tr>");
			}
			$("#city-table-body").empty().append(tableUpdate);
		};
		var errorCallback = function() {
			console.log("Error!");
		};
		controller.server.companyQuery(null, successCallback, errorCallback);
	});

});
