$(function(){
	$("#items").stoc({
		search: "#main_content_wrap"	
	});

	var $topLink  = $("#top-link");

	// hide top button on top
	$( window ).scroll( function() {
		if ($ (this).scrollTop() == 0){
			$topLink.fadeOut();
		} else {
			$topLink.fadeIn();
		}
	});

	// scroll body to 0px on click
	$topLink.click(function () {
		$('body,html').animate({
			scrollTop: 0
		}, 250);
		return false;
	});
});