<div class="container">
	<div class="row">
		<div style="background-color: rgba(230, 230, 230, .8)"
			class="col-md-8 col-md-offset-2 panel panel-default">

			<h2 class="margin-base-vertical">Add Transaction</h2>

			<p>
				Single-line transactions may be typed into the input box below.<br>
				Alternatively, multiple transactions may be uploaded as a
				CSV-formatted file.
			</p>

			<div class="alert alert-success fade in"
				id="addTransactionAlertSuccess"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em;">
				<button id="addTransactionAlertSuccessButton" type="button"
					class="close">×</button>
				<span id="addTransactionAlertSuccessText">Successful
					transaction add!</span>
			</div>
			<div class="alert alert-danger fade in" id="addTransactionAlertError"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em;">
				<button id="addTransactionAlertErrorButton" type="button"
					class="close">×</button>
				<span id="addTransactionAlertErrorText">Error processing
					transaction.</span>
			</div>


			<div class="input-group">
				<span class="input-group-addon">Transaction:</span> <input
					id="addTransactionSingle" type="text" class="form-control">
				<span class="input-group-btn">
					<button class="btn btn-success" type="button"
						id="addTransactionSubmit">Submit</button>
				</span>
			</div>

			<br>

			<!-- <form ENCTYPE="multipart/form-data" ACTION="FinanceServlet"
				METHOD=POST>
				<div class="input-group">
					<span class="input-group-addon">File Upload:</span> <input
						type="file" class="form-control" id="addTransactionFile">
					<span class="input-group-btn">
						<button class="btn btn-success" type="submit">Upload</button>
					</span>
				</div>
			</form>-->

			<!--  <form name="multiform" id="multiform" action="FinanceServlet"
				method="POST" enctype="multipart/form-data">
					<span class="input-group-addon">File Upload:</span> <input
						type="file" class="form-control"> <span
						class="input-group-btn">
						<button class="btn btn-success" type="submit"
							id="addTransactionSubmit">Upload</button>
					</span>
			</form>-->

			<form name="addTransactionFileUpload" id="addTransactionFileUpload"
				action="FinanceServlet" method="POST" enctype="multipart/form-data">
				<div class="input-group">
					<span class="input-group-addon">File Upload:</span> <span
						class="form-control"> <input type="file" name="File Upload" />
					</span> <span class="input-group-btn">
						<button class="btn btn-success" type="submit">Upload</button>
					</span>
				</div>
			</form>


			<!-- Button trigger modal -->
			<button id="addTransactionHelpButton" class="btn btn-warning"
				data-toggle="modal" data-target="#addTransactionHelpModal"
				style="margin-top: 1em; margin-bottom: 1em; margin-left: 45%;">
				Help&nbsp;&nbsp;<i class="glyphicon glyphicon-question-sign"></i>
			</button>

			<!-- Modal -->
			<div class="modal fade" id="addTransactionHelpModal" tabindex="-1"
				role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-dialog" id="addTransactionHelpModalDialog">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="myModalLabel">Adding
								Transactions</h4>
						</div>
						<div class="modal-body" style="color: grey;">
							<p>Transactions can take one of two forms:
							<ul>
								<li><i style="font-family: monospace; color: #B00000">&lt;fund|individual&gt;,
										&lt;name&gt;, &lt;dollar figure&gt;, &lt;YYYY-MM-DD&gt;</i> - Add
									cash to an individual or portfolio, creating it if it does not
									exist.</li>
								<li><i style="font-family: monospace; color: #B00000;">&lt;buy|sell&gt;,
										&lt;name&gt;, &lt;stock symbol|fund name&gt;, &lt;dollar
										amount&gt;, &lt;YYYY-MM-DD&gt;</i> - buy or sell a number of
									shares on a specific date (assume closing value) through cash
									on hand.</li>
							</ul>
							<p>All transactions should be in comma-separated-value form.
								If using the text input box, there should be no characters after
								the date field in the transaction. For transaction files with
								multiple transactions, only a newline character should be
								present between lines.</p>
						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-default"
								data-dismiss="modal">Close</button>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- //main content -->
	</div>
	<!-- //row -->
</div>
<!-- //container -->