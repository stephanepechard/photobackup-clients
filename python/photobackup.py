#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import getpass
import hashlib
import requests
import sys
from blessings import Terminal


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("url", help="The address of your server.", type=str)
    parser.add_argument("image", help="The image to be sent.",
                                 type=argparse.FileType('rb'))
    return parser.parse_args()


def main():
    parsed_args = parse_args()
    if parsed_args:
        args = vars(parsed_args)
        upfile = { 'upfile': args['image'] }
        term = Terminal()

        # ask the password of the server
        password = getpass.getpass(prompt='The server password: ')
        payload = { 'server_pass':
                    hashlib.sha512(password.encode('utf-8')).hexdigest() }

        # send the request
        try:
            request = requests.post(args['url'], files=upfile, data=payload)
        except requests.exceptions.MissingSchema:
            sys.exit(term.red + "ERROR: invalid URL: {}".format(args['url']))
        except requests.exceptions.ConnectionError:
            sys.exit(term.red + "ERROR: Connection refused")

        # manage the response
        if request.status_code == 200:
            print(term.green + "Upload was successful!")
        elif request.status_code == 403:
            print(term.red + "ERROR: Wrong password, upload failed!")
        else:
            print(term.red + "ERROR: Upload failed!")


if __name__ == '__main__':
    main()
