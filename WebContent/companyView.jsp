<div class="container">
	<div class="row">
		<div class="col-md-8 col-md-offset-2 panel panel-default">

			<h2 class="margin-base-vertical">Company Information</h2>

			<div class="myForm">
				<div class="dateRange">
					<label for="companyFromDate">From:&nbsp;</label><input
						id="companyFromDate" type="text" placeholder="01/01/2005">
					<label for="companyToDate">To:&nbsp;</label><input
						id="companyToDate" type="text" placeholder="12/31/2013">
				</div>
				<input type="checkbox" id="companyIncDecCheckbox"
					style="whitespace: nowrap; margin-top: 1em"> <select
					id="companyIncDecMenu">
					<option>Increasing</option>
					<option>Decreasing</option>
				</select> every <select id="companyIntervalMenu">
					<option>Month</option>
					<option>Year</option>
				</select>

				<div class="radio">
					<label> <input type="radio" name="optionsRadios"
						id="optionsRadios2" value="1" checked> Display all results
					</label>
				</div>
				<div class="radio">
					<label> <input type="radio" name="optionsRadios"
						id="optionsRadios1" value="2" disabled="true"> Display
						only:&nbsp;&nbsp;<input type="text"
						placeholder="AAPL,GOOG,MSFT ...">
					</label>
				</div>
				<div class="radio">
					<label> <input type="radio" name="optionsRadios"
						id="optionsRadios2" value="3" disabled="true"> Display all
						but:&nbsp;&nbsp;<input type="text"
						placeholder="AAPL,GOOG,MSFT ...">
					</label>
				</div>
				<div class="submit">
					<button id="companySubmitButton" type="button"
						class="btn btn-primary">
						Refresh Table&nbsp;&nbsp;<i class="glyphicon glyphicon-refresh"></i>
					</button>
					<button id="quotesSubmitButton" type="button" class="btn btn-info" disabled=true>
						Update Daily Quotes&nbsp;&nbsp;<i class="glyphicon glyphicon-refresh"></i>
					</button>
				</div>
			</div>
			<div class="alert alert-success fade in" id="companyAlertSuccess"
				style="display: none; margin-top: 1em;">
				<button id="companyAlertSuccessButton" type="button" class="close">×</button>
				<span id="companyAlertSuccessText">Successful company query!</span>
			</div>
			<div class="alert alert-danger fade in" id="companyAlertError"
				style="display: none; margin-top: 1em">
				<button id="companyAlertErrorButton" type="button" class="close">×</button>
				<span id="companyAlertErrorText">Error processing company query.</span>
			</div>
			<div class="alert alert-success fade in" id="quotesAlertSuccess"
				style="display: none; margin-top: 1em;">
				<button id="quotesAlertSuccessButton" type="button" class="close">×</button>
				<span id="quotesAlertSuccessText">Successful quote query!</span>
			</div>
			<div class="alert alert-danger fade in" id="quotesAlertError"
				style="display: none; margin-top: 1em">
				<button id="quotesAlertErrorButton" type="button" class="close">×</button>
				<span id="quotesAlertErrorText">Error updating quotes.</span>
			</div>
			<div id="company-table-div" style="margin-top: 1em">
				<table id="company-table"
					class="table table-bordered table-hover tablesorter">
					<thead id="company-table-heading">
						<tr>
							<th class="col0 column column-active">#</th>
							<th class="col1 column">Symbol</th>
							<th class="col2 column">Starting Price</th>
							<th class="col3 column">Ending Price</th>
							<th class="col4 column">Return</th>
							<th class="col5 column">High</th>
							<th class="col6 column">Low</th>
							<th class="col7 column">Risk</th>
						</tr>
					</thead>
					<tbody id="company-table-body">

					</tbody>
				</table>
			</div>

		</div>
		<!-- //main content -->
	</div>
	<!-- //row -->
</div>
<!-- //container -->