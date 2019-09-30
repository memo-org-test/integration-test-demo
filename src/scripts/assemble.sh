#!/bin/sh

./gradlew docker --no-daemon -x check -x composeUp --info
