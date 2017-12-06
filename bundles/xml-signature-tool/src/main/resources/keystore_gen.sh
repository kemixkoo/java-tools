#!/bin/sh

export PASSWORD=changeit

P12=code-signing.p12
CERTPEM=cert.pem
PRIVPEM=privkey.pem
JKSFILE=code-signing.jks
VRFYJKSFILE=verify.jks

CONFIG="
[ req ]
distinguished_name = req_dn
x509_extensions    = codesign_exts
default_md   = sha256
default_bits = 2048
preserve     = no

[ req_dn ]
countryName                     = Country Name (2 letter code)
countryName_value               = CN
stateOrProvinceName             = State or Province Name (full name)
stateOrProvinceName_value       = BJ
localityName                    = Locality Name (eg, city)
localityName_value              = BJ
0.organizationName              = Organization Name (eg, company)
0.organizationName_value        = Kemix
organizationalUnitName          = Organizational Unit Name (eg, section)
organizationalUnitName_value    = Code-signing certificate
commonName                      = Common Name (e.g. server FQDN or YOUR name)
commonName_value                = Kemix code-signing certificate
emailAddress                    = Email Address
emailAddress_value              = kemix_koo@163.com

[ codesign_exts ]
subjectKeyIdentifier = hash
keyUsage         = critical,digitalSignature
extendedKeyUsage = critical,codeSigning
basicConstraints = CA:FALSE
"

TMPFILE=$(mktemp)
trap "rm -f $TMPFILE" 0 KILL

echo "$CONFIG" > "$TMPFILE"

# Generate 2048 RSA key
openssl genrsa -out "$PRIVPEM" 2048
# Self-sign a coding certificate
openssl req -new -key "$PRIVPEM" -x509 -out "$CERTPEM" -config "$TMPFILE"
# Generate the PKCS12 file, and stash priv + cert inside
openssl pkcs12 -export -out "$P12" -inkey "$PRIVPEM" -in "$CERTPEM" -password "env:PASSWORD" -name code-signing

# Generate JKS used for signing
rm -f "$JKSFILE"
keytool -importkeystore -srckeystore "$P12" -srcstoretype pkcs12 \
        -srcalias code-signing -destalias code-signing       \
	-destkeystore "$JKSFILE" -deststoretype jks          \
	-srcstorepass "$PASSWORD" -deststorepass "$PASSWORD"

# Generate JKS used for verifying
rm -f "$VRFYJKSFILE"
keytool -import -file "$CERTPEM" \
	-alias code-signing -noprompt \
	-destkeystore "$VRFYJKSFILE" -deststoretype jks \
	-deststorepass "$PASSWORD"

cat << EOF
Files generated:
 - JKS keystore (verify): "$VRFYJKSFILE"
 - JKS keystore (sign): "$JKSFILE"
 - P12 file: "$P12"
 - private key: "$PRIVPEM"
 - certificate: "$CERTPEM"

You can now use the files generated in the cwd to sign and verify ZIP files.
To sign a ZIP file, use the following command:

	$ jarsigner -keystore $JKSFILE <your-file> code-signing

o verify the validity of the signature:

	$ jarsigner -verify -strict -keystore $VRFYJKSFILE <your-file> code-signing

Take 
EOF
