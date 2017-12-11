# Apache-XML-Signature

Use the Apache XML sign API with KeyStore

## Class XmlEnvelopedKeyStoreApacheDomSign

Used for sign the whole XML doc.

## Class XmlEnvelopedKeyStorePartApacheDomSign

Used for sign the some parts XML doc.
If modify the non-sign nodes, won't effect the verify for sign nodes.

## Class XmlKeyStorePartApacheStAXSign

Use the Apache StAX to sign for special nodes.

# NOTE

1. Can use DOM to sign and valid by StAX. Also enable to sign with StAX, valid by DOM.

2. If KeyStore is DSA, make sure the SignatureMethod parameter must be DSA too, also when using RSA.