# kifshare

A simple web page that allows users to download publicly available files without an iPlant account.

# Prerequisites
You'll need an iRODS install that's of version 3.1 or later. It doesn't need to live on the same box as kifshare, but kifshare will need to be able to connect to it.

You'll want Leiningen 2 installed as well. See http://leiningen.org for more details.

# Configuring kifshare

Create a .properties file that looks like this:

{code}
kifshare.irods.host = <irods-host>
kifshare.irods.port = <irods-port>
kifshare.irods.user = <irods-user>
kifshare.irods.password = <irods-password>
kifshare.irods.home = <irods-home>
kifshare.irods.zone = <irods-zone>
kifshare.irods.defaultResource = <irods-resource>

kifshare.app.port = <listen-port>
kifshare.app.mode = dev

kifshare.app.curl-flags = curl -o {{filename}} {{url}}/d/{{ticket-id}}/{{filename}}
kifshare.app.wget-flags = wget {{url}}/d/{{ticket-id}}/{{filename}}}
kifshare.app.iget-flags = iget {{abspath}}
kifshare.app.external-url = http://localhost:8080

kifshare.app.css-files = /css/reset.css,\
                         /css/960.css,\
                         http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.8.2/css/jquery.dataTables.css,\
                         /css/jquery-ui-1.8.21.custom.css,\
                         /css/kif.css
                         
                         
kifshare.app.javascript-files = https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js,\
                                https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js,\
                                http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.8.2/jquery.dataTables.min.js,\
                                /js/jquery.clippy.min.js,\
				                        /js/jquery.dimensions.js,\
				                        /js/jquery.tooltip.min.js,\
                                /js/kif.js
{code}

Worth noting is that the "kifshare.app.external-url" option is used when doing redirects. If you have multiple instances of kifshare set up behind HAProxy, then set this value to the URL that HAProxy is listening on.

# Running kifshare

If you're working out of a git checkout, then you can run the following:

    lein run --config </path/to/config/file>

If you're running from a jar, then you should be able to run the following:

    java -jar <kifshare-jar> --config </path/to/config>

If you're running it from the RPM with Zookeeper, then run the following:

    sudo /sbin/service kifshare start

# Downloading through a browser

Hit kifshare with a browser, setting the path portion of the URL to the name of the publicly available iRODS ticket.

    http://<kifshare-host>:<kifshare-port>/<ticket-name>

Once the kifshare page comes up, click on the big "Download" button.

# Downloading with curl

You can download with curl in two different ways. Firstly, you can hit the same page that you hit with a browser. Make sure you include -L in the curl options, non-html accepting clients are redirected to the actual download location.

    curl -L http://<kifshare-host>:<kifshare-port>/<ticket-name>

You can also hit another URL, but this one requires you to know the name of the file referred to by the ticket.

    curl http://<kifshare-host>:<kifshare-port>/<ticket-name>/<filename>



