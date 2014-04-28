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


});
