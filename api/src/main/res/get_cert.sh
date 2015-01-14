#!/bin/bash
###############################################################################
# 
# This script will fetch the certificate from a server
#
# Original author: 
#    http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html
###############################################################################
SERVER='demo.sana.csail.mit.edu'
KEYPASS='Sanamobile1'

CERT_DIR='certs'
CERTSTORE=${CERT_DIR}/keystore.bks
LIB_DIR=../libs
BKS_LIB=${LIB_DIR}/bcprov-jdk15on-148.jar
PEM=${CERT_DIR}/${SERVER}.pem

echo "Creating new BKS keystore"
mkdir -p ${CERT_DIR}

echo "...fetching certificate"
echo | openssl s_client -connect ${SERVER}:443 2>&1 | \
 	sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ${PEM}

echo "...Removing old keystore"

export CLASSPATH=$BKS_LIB
if [ -a $CERTSTORE ]; then
    rm $CERTSTORE || exit 1
fi

echo "...Generating keystore"
keytool \
      -import \
      -v \
      -trustcacerts \
      -alias 0 \
      -file <(openssl x509 -in ${PEM}) \
      -keystore $CERTSTORE \
      -storetype BKS \
      -providerclass org.bouncycastle.jce.provider.BouncyCastleProvider \
      -providerpath $BKS_LIB \
      -storepass ${KEYPASS}