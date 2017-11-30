# JDK-XML-Signature

ONLY use the JDK APIs to sign xml with public/private keys. ( Support DSA and RSA)

## Class JdkXmlEnvelopedSign

After sign the "Data", will add the "Signature" node inside of "Data", means "Enveloped":
```
<?xml version="1.0" encoding="UTF-8"?>
<Data>
	..............
	<Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
		..............
	</Signature>
</Data>
```

## Class JdkXmlEnvelopingSign
After sign the "Data", will add the "Signature" node outside of "Data", means "Enveloping":
```
<?xml version="1.0" encoding="UTF-8"?>
<Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
	..............
	<Object>
		<Data>
			..............
		</Data>
	</Object>
</Signature>
```

## Class JdkXmlDetachedSign
After sign the "Data", will add the "Signature" node same level of "Data", means "Detached":
```
<?xml version="1.0" encoding="UTF-8"?>
<Data>
	..............
</Data>
<Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
	..............
</Signature>
```