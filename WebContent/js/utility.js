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

function validateDate(fromDate, toDate, formatType) {

	if (formatType = "MM-DD-YYYY") {
		var dateRegEx = new RegExp("[0-9][0-9](\/|-)[0-9][0-9](\/|-)[0-9][0-9][0-9][0-9]");

		if (fromDate != "" && dateRegEx.exec(fromDate) == null) { // Check for mm/dd/yyyy
			alert("Error: FromDate formatted incorrectly.");
			return false;
		}
		if (toDate != "" && dateRegEx.exec(toDate) == null) { // Check for mm/dd/yyyy
			alert("Error: ToDate formatted incorrectly.");
			return false;
		}

		var fromYear = fromDate.substring(6, 10);
		var fromMonth = fromDate.substring(0, 2);
		var fromDay = fromDate.substring(3, 5);
		var toYear = toDate.substring(6, 10);
		var toMonth = toDate.substring(0, 2);
		var toDay = toDate.substring(3, 5);
		if (fromDate != "") { // Check for months 1-12, day that is valid for the given month
			var dateValidation = daysInMonths[fromMonth];
			if (dateValidation == null || dateValidation < parseInt(fromDay) || parseInt(fromDay) < 1) {
				alert("Error: Invalid FromDate.");
				return false;
			}
		}
		if (toDate != "") { // Check for months 1-12, day that is valid for the given month
			var dateValidation = daysInMonths[toMonth];
			if (dateValidation == null || dateValidation < parseInt(toDay) || parseInt(toDay) < 1) {
				alert("Error: Invalid ToDate.");
				return false;
			}
		}

		if (fromYear > toYear || (fromYear == toYear && fromMonth > toMonth) || (fromYear == toYear && fromMonth == toMonth && fromDay > toDay)) {
			alert("Error: From date cannot be after to date.");
		}
		return true;
	} else {
		return false;
	}
}