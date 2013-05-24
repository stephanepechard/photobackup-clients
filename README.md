Photo Backup clients
====================

You can use Photo Backup with many different clients, as long as you
follow the server's API. Here you can find:

 * an Android client, made of a service backuping your pictures
 automatically to your server ;
 * a minimal Python client, to be used from the command line.


Android client
--------------
The most usable client, dedicated to free your pictures from your
device. Plugging it to your PC is so 2000's, right? Letting them on
Google+ is too G-centric. Better to keep them warm on your home server.

The Android client is a service (always running in silence, launching
at boot) that detects the pictures you take and upload them to your
server. It's that simple. A configuration view is the only interaction
with the user, to set the server address and the password up.


Minimal Python client
---------------------
This client is mostly used to test servers. You can use it with:

    $ python photobackup.py http://myserver.com/ image.jpg

A password is asked to the user, then the request is operated.
When the request to the server is done, the response status is shown.
You got:

 * 200 when the request was successful ;
 * 400 when the request was badly formed ;
 * 403 when the given password is not the same as the server's one ;
 * 408 when the request timed out ;
 * 500 when an error occured on the server in the meanwhile.


Anyone's client
---------------
Can't find the client that suit your needs?
Write it and push it to this repository!
