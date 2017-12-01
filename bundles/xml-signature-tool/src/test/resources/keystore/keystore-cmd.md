# Generate KeyStore in user home
keytool -genkey -alias kemix -storepass 123456 -keyalg DSA -keysize 1024 -keypass 654321 -validity 3650 -keystore ~/kemix-dsa.jks 
keytool -genkey -alias kemix -storepass 123456 -keyalg RSA -keysize 1024 -keypass 654321 -validity 3650 -keystore ~/kemix-rsa.jks 

# List Certificate
keytool -list -rfc -keystore ~/kemix-rsa.jks -storepass 123456
keytool -list  -v  -keystore ~/kemix-rsa.jks -storepass 123456

# Export Certificate
keytool -export -alias kemix -keystore ~/kemix-rsa.jks -file ~/kemix-rsa.crt -storepass 123456

# Check Certificate
keytool -printcert -file ~/kemix-rsa.crt
