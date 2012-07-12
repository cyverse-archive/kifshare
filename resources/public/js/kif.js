$(document).ready(function() {
    $("#irods_avus").dataTable({
	"bJQueryUI" : true,
	"sPaginationType" : "full_numbers",
	"aoColumns" : [
            {"sWidth" : "320px"},
            {"sWidth" : "320px"},
            {"sWidth" : "320px"}
	]
    });
    
    $('#download_link').button();
    
    var last_mod_date = new Date(Number($('#lastmod').text()));
    $('#lastmod').text(last_mod_date.toString());
    
    $('.clippy-curl').clippy({clippy_path : "/flash/clippy.swf"});
    $('.clippy-wget').clippy({clippy_path : "/flash/clippy.swf"});
    $('.clippy-irods').clippy({clippy_path : "/flash/clippy.swf"});
});
