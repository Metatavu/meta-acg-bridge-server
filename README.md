# Meta ACG Bridge Server

## Request authorization

Authentication to server is done by sending HMAC encrypted signature inside Authorization -header.

Signature is should be formed in following format:

Base64([CLIENT_ID]:HMAC(SHA256[URL]|[BODY_CONTENT],[KEY]))

where 

  - CLIENT: is your clientId
  - KEY: is your secretKey
  - URL: is the complete URL -address you are calling
  - BODY_CONTENT: the request body content

See example in: https://github.com/Metatavu/meta-acg-bridge-server/blob/master/examples/authtester/AuthTester.java
