(function () {
    "use strict";

    function encodeFilename(file_url) {
        var split_paths = file_url.split("/"),
            file_index = split_paths.length - 1;
        split_paths[file_index] = encodeURIComponent(split_paths[file_index]);
        return split_paths.join("/");
    }

    function trimURLPath(pathname) {
        var split_paths = pathname.split("/"),
            new_path_array = split_paths.slice(0, split_paths.length - 1);
        return new_path_array.join("/");
    }

    function curlableURL() {
        var a = document.createElement('a'),
            retval = "";

        a.href = document.URL;
        retval = retval + a.protocol + "//";
        retval = retval + a.hostname;

        if (a.port !== 'undefined' && a.port !== '80' && a.port !== null && a.port !== '') {
            retval = retval + ":" + a.port;
        }

        retval = retval + trimURLPath(a.pathname);

        return retval;
    }

    function get_ticket_info() {
        var info = JSON.parse($('#ticket-info-map').text());
        info.url = curlableURL();
        return info;
    }

    $(document).ready(function() {
        var ticket_info = get_ticket_info(),
            last_mod_date = new Date(Number($('#lastmod').text())),
            import_template = ticket_info.import_template,
            wget_template = ticket_info.wget_template,
            curl_template = ticket_info.curl_template,
            iget_template = ticket_info.iget_template,
            import_url = encodeFilename(decodeURIComponent(Mustache.render(import_template, ticket_info))),
            wget_command = decodeURIComponent(Mustache.render(wget_template, ticket_info)),
            curl_command = decodeURIComponent(Mustache.render(curl_template, ticket_info)),
            iget_command = decodeURIComponent(Mustache.render(iget_template, ticket_info)),

            copy_func = function (selector) {
                return $(selector).val();
            },

            aftercopy_func = function (selector) {
                return function () {
                    $(selector).text("Copied!");
                    window.setTimeout(function () {
                        $(selector).text("Copy");
                    }, 1000);
                };
            },

            zero_clip_path = "flash/ZeroClipboard.swf";

        $('#lastmod').text(last_mod_date.toString());
        $('#de-import-url').val(import_url);
        $('#irods-command-line').val(iget_command);
        $('#curl-command-line').val(curl_command);
        $('#wget-command-line').val(wget_command);

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

}());
