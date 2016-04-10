# Specifictaion

### Purpose of the Application

This application provides a distributed backup system using the peer to peer architecture through the internet. This service will be composed of several peers, and each peer will use their own unused disk space to backup the other peers's files. 

### Main features

#### File backup/restoration/deletion

The application will support the main actions needed for a good backup service. Therefore, backing up files, restoring and deleting them from the server will be supported. All files will be split into chunks of maximum 64000 bytes size. When data is sent through the network, it is guaranteed to reach its destination.

#### Secure identification and data.

The application guarantees that each peer will only be able to interact with the system if they succesfully authenticate into the system with a password. Said password will be encrypted. The peer's chunks and metadata will also be encrypted, in order to ensure confidentiality.

#### Tolerance and malign user prevention

A peer will be able to fully recover from a crash as long as the metadata information remains intact. The system will guarantee that each peer identifier is unique and non-manipulateable. Each chunk will have a checksum in order to ensure integrity, and will be immediatly deleted in case the integrity check fails.

### Target platforms

Any computer with an operating system capable of running Java (Solaris / Windows / Linux / OS X) and Android devices (?).
