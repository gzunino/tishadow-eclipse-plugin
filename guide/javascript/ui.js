$(function(){
	$("#items").stoc({
		search: "#main_content_wrap"	
	});
});

$( window ).scroll( function() {
	
	var $topLink = $("#top-link");

	if ($ (this).scrollTop() == 0){
		$topLink.fadeOut();
	} else {
		$topLink.fadeIn();
	}
	
});
