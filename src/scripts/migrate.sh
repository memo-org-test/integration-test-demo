#!/bin/sh
ENVIRONMENT=$1
DB_PASS=aws secretsmanager get-secret-value --secret-id ${ENVIRONMENT}/some/path/to/secret
DB_URL='resove url by getting value from consul or what ever Key value store one is using'

./gradlew -Pflyway.url=${DB_URL} -Pflyway.password=${DB_PASS} flywayMigrate --info
