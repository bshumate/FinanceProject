<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page
	import="java.util.Date, 
					java.text.DateFormat, 
					java.text.SimpleDateFormat, 
					java.sql.*, 
					java.util.ArrayList"%>
<!DOCTYPE html>
<html>
<head>
<link rel="icon" type="image/jpg"
	href="http://blog.cutcaster.com/wp-content/uploads/2010/01/graph3.jpg">
<title>Ben's Finance Page - CMSC424</title>
<%@ include file="header.jsp"%>
</head>
<body>
	<div id="page-header" class="page-header">
		<h1>
			Ben's Stock Information Tracker <small>CMSC424 Sec 0101</small>
		</h1>
	</div>



	<div class="tabbable">
		<ul class="nav nav-tabs">
			<li><a href="#pane1" data-toggle="tab">Company View</a></li>
			<li class="active"><a href="#pane2" data-toggle="tab">Fund View</a></li>
			<li><a href="#pane3" data-toggle="tab">Add
					Transaction</a></li>
			<li><a href="#pane4" data-toggle="tab">About</a></li>
		</ul>
		<div class="tab-content">
			<div id="pane1" class="tab-pane">
				<%@ include file="companyView.jsp"%>
			</div>
			<div id="pane2" class="tab-pane active">
				<%@ include file="fundView.jsp"%>
			</div>
			<div id="pane3" class="tab-pane">
				<%@ include file="addTransaction.jsp"%>
			</div>
			<div id="pane4" class="tab-pane">
				<!-- /.tabbable -->
				<div class="container">
					<div class="row">
						<div class="col-md-6 col-md-offset-3 panel panel-default">

							<h1 class="margin-base-vertical">About this site.</h1>

							<p>
								I made this site for a <a
									href="http://triffid.cs.umd.edu/classes/424-s14//project/projectFinance.pdf">project
									in my databases class.</a>
							</p>
							<p>It uses HTML, Javascript, and CSS on the front end, and
								JSP and MySQL on the back end.</p>

							<div class="margin-base-vertical">
								<small class="text-muted"><a
									href="https://flic.kr/p/k283C7">Background picture by Kris
										Sage @flickr</a>. Used under <a
									href="http://creativecommons.org/licenses/by/2.0/deed.en">Creative
										Commons - Attribution</a>.</small>
							</div>

						</div>
						<!-- //main content -->
					</div>
					<!-- //row -->
				</div>
				<!-- //container -->
			</div>

		</div>
		<!-- /.tab-content -->
	</div>
	<!-- /.tabbable -->

</body>
</html>