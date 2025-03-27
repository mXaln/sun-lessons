#!/bin/sh

# Decrypt the file
# --batch to prevent interactive command
# --yes to assume "yes" for questions
gpg --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output $GITHUB_WORKSPACE/keystore.jks keystore.jks.gpg