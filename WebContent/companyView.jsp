<div class="container">
	<div class="row">
		<div style="background-color: rgba(240, 250, 255, .8);"
			class="col-md-8 col-md-offset-2 panel panel-default">

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
				</select> every year

				<div class="radio">
					<label> <input type="radio" name="companyOptionsRadios"
						id="companyOptionDisplayAll" value="displayAll" checked=true>
						Display all results
					</label>
				</div>
				<div class="radio">
					<label> <input type="radio" name="companyOptionsRadios"
						id="companyOptionDisplayOnly" value="showOnly">
						Display only:&nbsp;&nbsp;<input type="text" id="companyShowOnly"
						placeholder="GOOGL,MSFT, T ...">
					</label>
				</div>
				<div class="submit">
					<button id="companySubmitButton" type="button"
						class="btn btn-primary">
						Refresh Company Info&nbsp;&nbsp;<i
							class="glyphicon glyphicon-refresh"></i>
					</button>
					<button id="companyExportToCSV" type="button"
						class="btn btn-default">
						<b>Export Company Table to CSV&nbsp;&nbsp;</b><i
							class="glyphicon glyphicon-download-alt"></i>
					</button>
					<button id="quotesSubmitButton" type="button" class="btn btn-info"
						disabled=true>
						Update Daily Quotes&nbsp;&nbsp;<i
							class="glyphicon glyphicon-refresh"></i>
					</button>
				</div>
			</div>
			<div class="alert alert-success fade in" id="companyAlertSuccess"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em;">
				<button id="companyAlertSuccessButton" type="button" class="close">×</button>
				<span id="companyAlertSuccessText">Successful company query!</span>
			</div>
			<div class="alert alert-info fade in" id="companyAlertLoading"
                style="display: none; border-width: 2px; border-color: grey; margin-top: 1em;">
                <span id="companyAlertLoadingText">Loading...</span>
            </div>
			<div class="alert alert-danger fade in" id="companyAlertError"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em">
				<button id="companyAlertErrorButton" type="button" class="close">×</button>
				<span id="companyAlertErrorText" style="white-space: pre">Error
					processing company query.</span>
			</div>
			<div class="alert alert-success fade in" id="quotesAlertSuccess"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em;">
				<button id="quotesAlertSuccessButton" type="button" class="close">×</button>
				<span id="quotesAlertSuccessText">Successful quote query!</span>
			</div>
			<div class="alert alert-danger fade in" id="quotesAlertError"
				style="display: none; border-width: 2px; border-color: black; margin-top: 1em">
				<button id="quotesAlertErrorButton" type="button" class="close">×</button>
				<span id="quotesAlertErrorText" style="white-space: pre">Error
					updating quotes.</span>
			</div>
			<div id="company-table-div" style="margin-top: 1em">
				<table id="company-table"
					class="table table-bordered table-hover tablesorter">
					<thead id="company-table-heading">
						<tr>
							<th class="col0 column column-active">#</th>
							<th class="col1 column">Symbol</th>
							<th class="col2 column">Starting Price ($)</th>
							<th class="col3 column">Ending Price ($)</th>
							<th class="col4 column">Return/Yr (%)</th>
							<th class="col5 column">High ($)</th>
							<th class="col6 column">Low ($)</th>
							<th class="col7 column">Risk (%)</th>
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