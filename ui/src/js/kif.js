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

    $('#de-import-url').val(import_url);
    $('#irods-command-line').val(iget_command);
    $('#curl-command-line').val(curl_command);
    $('#wget-command-line').val(wget_command);

    var copy_func = function (selector) {
        return $(selector).val();
    };

    var aftercopy_func = function (selector) {
        return function () {
            $(selector).text("Copied!");
            window.setTimeout(function () {
                $(selector).text("Copy");
            }, 1000);
        };
    };

    var zero_clip_path = "flash/ZeroClipboard.swf";
    $('#clippy-import-wrapper').zclip({
        path: zero_clip_path,
        copy: copy_func('#de-import-url'),
        afterCopy: aftercopy_func('#clippy-import-wrapper')
    });

    $('#clippy-irods-wrapper').zclip({
        path: zero_clip_path,
        copy: copy_func('#irods-command-line'),
        afterCopy: aftercopy_func('#clippy-irods-wrapper')
    });

    $('#clippy-curl-wrapper').zclip({
        path: zero_clip_path,
        copy: copy_func('#curl-command-line'),
        afterCopy: aftercopy_func('#clippy-curl-wrapper')
    });

    $('#clippy-wget-wrapper').zclip({
        path: zero_clip_path,
        copy: copy_func('#wget-command-line'),
        afterCopy: aftercopy_func('#clippy-wget-wrapper')
    });
});
