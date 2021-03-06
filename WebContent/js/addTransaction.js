// Represents the Add Transaction tab on the webite

function addTransactionInit() {

	$("#addTransactionSubmit").click(function() {
		$("#addTransactionAlertSuccess").hide();
		$("#addTransactionAlertError").hide();
		transactionTextSubmit($("#addTransactionSingle").val());
	});

	$("#addTransactionFileUpload").submit(function(e) {
		$("#addTransactionAlertSuccess").hide();
		$("#addTransactionAlertError").hide();
		e.preventDefault(); //Prevent Default action, which would open a new tab
		transactionFileUpload(this, e);
	});

	$("#addTransactionAlertSuccessButton").click(function(e) {
		$("#addTransactionAlertSuccess").hide();
	});
	$("#addTransactionAlertErrorButton").click(function(e) {
		$("#addTransactionAlertError").hide();
	});
}

function transactionTextSubmit(text) {
	// Submit a single-line text transaction to the server
	$("#addTransactionAlertLoading").show();
	var successCallback = function(data) {
		console.log("SUCCESS");
		console.log(data);
		$("#addTransactionAlertError").hide();
		$("#addTransactionAlertSuccess").show();
		var $t = $("#addTransactionSingle");
		$("#addTransactionSingle").val('');
		$("#addTransactionAlertLoading").hide();
	};
	var errorCallback = function(data) {
		console.log("ERROR");
		console.log(data);
		$("#addTransactionAlertErrorText").text(data);
		$("#addTransactionAlertError").show();
		$("#addTransactionAlertSuccess").hide();
		$("#addTransactionAlertLoading").hide();
	};
	var transaction = controller.newTransaction();
	transaction.transaction = text;

	controller.server.addTransaction(transaction, successCallback, errorCallback);
}

function transactionFileUpload(form, e) {
	// Submit a transaction file to the server
	$("#addTransactionAlertLoading").show();
	var successCallback = function(data) {
		$("#addTransactionAlertError").hide();
		$("#addTransactionAlertSuccess").show();
		$("#addTransactionAlertLoading").hide();
	};
	var errorCallback = function(data) {
		console.log(data);
		$("#addTransactionAlertErrorText").text(data);
		$("#addTransactionAlertError").show();
		$("#addTransactionAlertSuccess").hide();
		$("#addTransactionAlertLoading").hide();
	};
	e.preventDefault(); // Prevent Default action.
	controller.server.uploadTransactionFile(form, successCallback, errorCallback);
}

$(document).ready(function() {
	addTransactionInit();
});