function trimURLPath (pathname) {
    var split_paths = pathname.split("/");
    var new_path_array = split_paths.slice(0, split_paths.length - 1);
    return new_path_array.join("/");
}

function curlableURL() {
    var a = document.createElement('a');
    a.href = document.URL;

    var retval = "";
    retval = retval + a.protocol + "//";
    retval = retval + a.hostname;

    if (a.port !== 'undefined' && a.port !== '80' && a.port !== null && a.port !== '') {
        retval = retval + ":" + a.port;
    }

    retval = retval + trimURLPath(a.pathname);
    
    return retval; 
}

$(document).ready(function() {
    var ticket_info = JSON.parse($('#ticket-info-map').text());
    ticket_info.url = curlableURL();

    var last_mod_date = new Date(Number($('#lastmod').text()));
    $('#lastmod').text(last_mod_date.toString());

    var import_template = ticket_info.import_template;
    var wget_template = ticket_info.wget_template;
    var curl_template = ticket_info.curl_template;
    var iget_template = ticket_info.iget_template;

    var import_url = _.unescape(Mustache.render(import_template, ticket_info));
    var wget_command = _.unescape(Mustache.render(wget_template, ticket_info));
    var curl_command = _.unescape(Mustache.render(curl_template, ticket_info));
    var iget_command = _.unescape(Mustache.render(iget_template, ticket_info));

    $('#clippy-import-wrapper').attr('data-clipboard-target', 'de-import-url');
    $('#clippy-irods-wrapper').attr('data-clipboard-target', 'irods-command-line');
    $('#clippy-wget-wrapper').attr('data-clipboard-target', 'wget-command-line' );
    $('#clippy-curl-wrapper').attr('data-clipboard-target', 'curl-command-line');

    $('#de-import-url').val(import_url);
    $('#irods-command-line').val(iget_command);
    $('#curl-command-line').val(curl_command);
    $('#wget-command-line').val(wget_command);

    var zero_clip_path = "flash/ZeroClipboard.swf";
    var import_clip = new ZeroClipboard($('#clippy-import-wrapper'), { moviePath: zero_clip_path});
    var irods_clip = new ZeroClipboard($('#clippy-irods-wrapper'), { moviePath: zero_clip_path});
    var curl_clip = new ZeroClipboard($('#clippy-curl-wrapper'), { moviePath: zero_clip_path});
    var wget_clip = new ZeroClipboard($('#clippy-wget-wrapper'), { moviePath: zero_clip_path});
});
