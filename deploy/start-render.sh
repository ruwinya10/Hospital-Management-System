#!/bin/sh
set -eu
: "${DB_URL:?Set DB_URL to the Aiven JDBC URL in Render}"
: "${DB_USER:?Set DB_USER in Render}"
: "${DB_PASSWORD:?Set the rotated DB_PASSWORD in Render}"
: "${JWT_SECRET:?Set a random JWT_SECRET in Render}"
: "${HMS_ADMIN_PASSWORD:?Set an initial admin password in Render}"
: "${FRONTEND_ORIGIN:?Set the exact Vercel frontend origin in Render}"

case "$DB_URL" in
  jdbc:mysql://*sslMode=VERIFY_IDENTITY*) ;;
  *) echo 'DB_URL must be a JDBC MySQL URL with sslMode=VERIFY_IDENTITY.' >&2; exit 1 ;;
esac

# Accept a base64-encoded public CA certificate on hosts without secret files.
umask 077
trust_dir=$(mktemp -d /tmp/hms-tls.XXXXXX)
ca_file="${DB_CA_CERT_FILE:-/etc/secrets/aiven-ca.pem}"
if [ -n "${DB_CA_CERT_BASE64:-}" ]; then
  ca_file="$trust_dir/aiven-ca.pem"
  if ! printf '%s' "$DB_CA_CERT_BASE64" | base64 --decode > "$ca_file"; then
    echo 'DB_CA_CERT_BASE64 must contain the base64-encoded Aiven CA certificate.' >&2
    exit 1
  fi
fi
if [ ! -r "$ca_file" ]; then
  echo 'Set DB_CA_CERT_BASE64 or provide an Aiven CA certificate file via DB_CA_CERT_FILE.' >&2
  exit 1
fi
keytool -importcert -noprompt -alias aiven-mysql -file "$ca_file" \
  -keystore "$trust_dir/truststore.p12" -storetype PKCS12 -storepass changeit >/dev/null
# The truststore contains public certificates only; its password is not a credential.
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djavax.net.ssl.trustStore=$trust_dir/truststore.p12 -Djavax.net.ssl.trustStoreType=PKCS12 -Djavax.net.ssl.trustStorePassword=changeit"
exec java -jar /app/hms.jar
