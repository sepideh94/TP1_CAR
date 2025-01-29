# FTP Server in Java

## Features
- User authentication (default: 'miage' / 'sepideh')
- Retrieve files using 'get' command which is 'RETR' in server.
- Change working directory using 'cd' which is known as 'CWD' in server.
- List files in the current directory using 'dir' known as 'LIST' in server.

## Example FTP Client Commands
Server ready to accept connections on port 2121
USER miage
PASS sepideh
CWD data
RETR file1.txt
QUIT

### Sepideh Soleimani
