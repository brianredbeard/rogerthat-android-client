# Signing & encryption infrastructure

## Steps
1. Register your account
2. Enter your secure pin

  You will be asked to enter a pin code and confirm the pin code.
  When finished we will create a Public & Private key for encrypting and decrypting the Ethereum private key

  The private key will be generated using the [Android Keystore System](https://developer.android.com/training/articles/keystore.html) an kept on your device. This key will be bound to secure hardware using Trusted Execution Environment so that [Extraction Prevention](https://developer.android.com/training/articles/keystore.html#ExtractionPrevention) is not possible.

  Now we create the Ethereum Public & Private key, the Ethereum private key will be encrypted with the pin code and then encrypted with the public key using 'RSA/ECB/OAEPWithSHA-256AndMGF1Padding'.

  The Ethereum public key will be send to the server

3. You can now use the application


## Ethereum Public and private key

These keys will be based on Elliptic curve cryptography. Using the android [KeyPairGenerator](https://developer.android.com/training/articles/keystore.html#SupportedKeyPairGenerators).

A private key is 256 bits, 32 bytes or 64 characters in the range 0-9 or A-F.

## Storing the Ethereum public key on the server (New user)
To public key will be uploaded to the server using an api call with function 'com.mobicage.api.system.setSecureInfo' and parametes:
- public_key type 'unicode'

Which will then be saved on UserProfile

## Loading old secure info (Existing user)
 - TBD


## Example 'Wallet'

### Usage in the application

A HTML-5 application will do a request to the application to sign some data using 'rogerthat.security.sign(payload, success, error)'.

You will be asked to enter your pincode and then the payload will be signed and a success or error callback will returned to the HTML-5 application.
