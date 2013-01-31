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
    //Pull json containing ticket info from a hidden div.
    var ticket_info = JSON.parse($('#ticket-info-map').text());
    ticket_info.url = curlableURL();

    //Format the date from seconds since the epoch to a local time.
    var last_mod_date = new Date(Number($('#lastmod').text()));
    $('#lastmod').text(last_mod_date.toString());

    //Retrieve the templates from the JSON passed up from the backend.
    var import_template = ticket_info.import_template;
    var wget_template = ticket_info.wget_template;
    var curl_template = ticket_info.curl_template;
    var iget_template = ticket_info.iget_template;

    //Generate the command-line strings
    var import_url = _.unescape(Mustache.render(import_template, ticket_info));
    var wget_command = _.unescape(Mustache.render(wget_template, ticket_info));
    var curl_command = _.unescape(Mustache.render(curl_template, ticket_info));
    var iget_command = _.unescape(Mustache.render(iget_template, ticket_info));

    //Populate text boxes with command-lines.
    $('#de-import-url').val(import_url);
    $('#irods-command-line').val(iget_command);
    $('#curl-command-line').val(curl_command);
    $('#wget-command-line').val(wget_command);

    //Workaround for Chrome/Safari issue. Source: 
    //    http://stackoverflow.com/questions/3380458/looking-for-a-better-workaround-to-chrome-select-on-focus-bug
    var kifselect = function (selector) {
        $(selector).select().mouseup(function (e) {
            e.preventDefault();
            $(selector).unbind("mouseup");
        });
    };

    //Auto-select text in the input boxes on focus.
    $('input[type=text]').focus(function () {
        kifselect(this);
    });

    //Select buttons should also select all of the text.
    $('#clippy-import-wrapper').click(function () {
        kifselect('#de-import-url');
    });

    $('#clippy-irods-wrapper').click(function () {
        kifselect('#irods-command-line');
    });

    $('#clippy-wget-wrapper').click(function () {
        kifselect('#wget-command-line');
    });

    $('#clippy-curl-wrapper').click(function () {
        kifselect('#curl-command-line');
    });
});
