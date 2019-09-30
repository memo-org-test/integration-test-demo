#!/bin/sh

DOCKER_BRIDGE_GATEWAY=$(docker network inspect bridge --format='{{json (index .IPAM.Config 0).Gateway}}' | sed 's/\"//g' )
echo "resolved docker bridge gateway ip=${DOCKER_BRIDGE_GATEWAY}"
# this arg  -Pdocker-network-host should be provided if jenkins was it's self a running docker image.
./gradlew clean check --no-daemon -Pdocker-network-host=${DOCKER_BRIDGE_GATEWAY}

