<div class="container">
	<div class="row">
		<div style="background-color: rgba(255, 247, 255, .8)"
			class="col-md-11 col-md-offset-1 panel panel-default">

			<h2 class="margin-base-vertical">Fund Information</h2>

			<div class="myForm">
				<div class="dateRange">
					<label for="fundFromDate">From:&nbsp;</label><input
						id="fundFromDate" type="text" placeholder="01/01/2005"
						disabled=true> <label for="fundToDate">To:&nbsp;</label><input
						id="fundToDate" type="text" placeholder="12/31/2013" disabled=true>
				</div>

				<div style="margin-top: .5em">
					<label class="checkbox-inline"> <input type="checkbox"
						id="fundIndividualCheckbox" value="option1" checked>
						Individuals
					</label> <label class="checkbox-inline"> <input type="checkbox"
						id="fundPortfolioCheckbox" value="option2" checked>
						Portfolios
					</label>
				</div>
				<div class="radio">
					<label> <input type="radio" name="fundOptionsRadios"
						id="fundOptionDisplayAll" value="showAll" checked> Display all
						results
					</label>
				</div>
				<div class="radio">
					<label> <input type="radio" name="fundOptionsRadios"
						id="fundOptionDisplayOnly" value="showOnly">
						Display only:&nbsp;&nbsp;<input type="text" id="fundShowOnly"
						placeholder="fund_1, ind_2...">
					</label>
				</div>
				<div class="submit">
					<button id="fundSubmitButton" type="button" class="btn btn-primary">
						Refresh Fund Info&nbsp;&nbsp;<i
							class="glyphicon glyphicon-refresh"></i>
					</button>
					<button id="fundExportToCSV" type="button" class="btn btn-default">
						<b>Export Fund Table to CSV&nbsp;&nbsp;</b><i
							class="glyphicon glyphicon-download-alt"></i>
					</button>
				</div>
			</div>
			<div class="alert alert-success fade in" id="fundAlertSuccess"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em;">
				<button id="fundAlertSuccessButton" type="button" class="close">�</button>
				<span id="fundAlertSuccessText">Successful fund query!</span>
			</div>
			<div class="alert alert-info fade in" id="fundAlertLoading"
                style="display: none; border-width: 2px; border-color: grey; margin-top: 1em;">
                <span id="fundAlertLoadingText">Loading...</span>
            </div>
			<div class="alert alert-danger fade in" id="fundAlertError"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em">
				<button id="fundAlertErrorButton" type="button" class="close">�</button>
				<span id="fundAlertErrorText" style="white-space: pre">Error
					processing fund query.</span>
			</div>
			<div id="fund-table-div" style="margin-top: 1em">
				<table id="fund-table"
					class="table table-bordered table-hover tablesorter">
					<thead id="fund-table-heading">
						<tr>
							<th class="col0 column">#</th>
							<th class="col1 column">Name</th>
							<th class="col2 column">Type</th>
							<th class="col3 column">Starting Worth ($)</th>
							<th class="col4 column column-active">Ending Worth ($)</th>
							<th class="col5 column">Return (%)</th>
							<th class="col6 column">Cash ($)</th>
							<th class="col7 column">Investments ($)</th>
							<th class="col8 column">Majority Participant In</th>
						</tr>
					</thead>
					<tbody id="fund-table-body">

					</tbody>
				</table>
			</div>

			<!-- Modal -->
			<div class="modal fade" id="viewFundTransactionsModal" tabindex="-1"
				role="dialog" aria-labelledby="viewFundTransactionsModalLabel" aria-hidden="true">
				<div class="modal-dialog" id="addTransactionHelpModalDialog">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="viewFundTransactionsModalLabel"></h4>
						</div>
						<div id="viewFundTransactionsModalBody" class="modal-body" style="color: grey;">
                            <p>Fund Transactions</p>
						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-default"
								data-dismiss="modal">Close</button>
						</div>
					</div>
				</div>
			</div>
			<!-- modal -->
		</div>
		<!-- //main content -->
	</div>
	<!-- //row -->
</div>
<!-- //container -->