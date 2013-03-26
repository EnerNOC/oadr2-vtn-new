function dropdownRedirect(program){
	alert("Here");
	$.getScript($("select[name=program]").change(function(){
		location.href= $(this).val();
	}));
};

$("select[name='program']").change(function(){
	location.href= $(this).val();
});

$(function(){
	$('#dp1').datepicker({
		format: 'mm-dd-yyyy'
	});
	$('#dp2').datepicker({
		format: 'mm-dd-yyyy'
	});
	$('#tp1').timepicker();
	$('#tp2').timepicker();
});

function confirmSubmit(){
	var agree=confirm("Are you sure you wish to delete this event?");
	if(agree)
		return true;
	else
		return false;
}

function enableField(){
	document.getElementById("clientURI").disabled=false
}
function disableField(){
	document.getElementById("clientURI").disabled=true
}