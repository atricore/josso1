# $Id: README.txt 693 2008-10-28 17:40:58Z sgonzalez $

cacert.pem
----------
JOSSO Sample Certification Authority (CA) X.509 Certificate in PEM format.
This certificate is the CA's certificate associated with the CA's Private key used to sign the user certificates.

cacert.crt
----------
JOSSO Certification Authority (CA) X.509 Certificate in DER base-64 format.

cacerts
-------
JKS Keystore with the JOSSO Sample Certification Authority X.509 Certificate imported. The keystore password is 'changeit'. 

josso-server.pem
----------------
JOSSO Sample SSL Web Server X.509 Certificate in PEM format. This certificate is signed by the Sample CA.

josso-server.crt
----------------
JOSSO Sample SSL Web Server X.509 Certificate in DER base-64 format. This certificate is signed by the Sample CA.

josso-server.jks
----------------
JKS Keystore with the JOSSO Sample SSL Certificate imported.

user1.pfx
---------
PKCS12-formatted file for the private and public Key (certificate) of the 'user1' user. 
It should be installed in the client tier (Browser, MUA, etc.).  
