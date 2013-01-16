$(document).ready(function() {
    $("#irods_avus").dataTable({
	"bJQueryUI" : true,
	"sPaginationType" : "full_numbers",
	"aoColumns" : [
            {"sWidth" : "315px"},
            {"sWidth" : "315px"},
            {"sWidth" : "315px"}
	]
    });
    
    $('#download_link').button();
    
    var last_mod_date = new Date(Number($('#lastmod').text()));
    $('#lastmod').text(last_mod_date.toString());
    
    $('.clippy-curl').clippy({clippy_path : 'flash/clippy.swf'});
    $('.clippy-wget').clippy({clippy_path : 'flash/clippy.swf'});
    $('.clippy-irods').clippy({clippy_path : 'flash/clippy.swf'});

    $('table').css("border-bottom", "2px rgb(210,210,210) solid");
    $('table').css("border-left", "1px rgb(210,210,210) solid");
    $('table').css("border-right", "1px rgb(210,210,210) solid");
    $('table').css("border-top", "1px rgb(210,210,210) solid");
});
