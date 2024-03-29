#!/bin/bash

# This script will start the FedOne wave client.
#

if [ -f run-config.sh ] ; then
  . run-config.sh
else
  . run-nofed-config.sh
fi

if [ -z "$WEBSOCKET_SERVER_PORT" -o -z "$WEBSOCKET_SERVER_HOSTNAME" ]; then
  echo "You need to specity WEBSOCKET_SERVER_HOSTNAME and WEBSOCKET_SERVER_PORT in run-config.sh"; exit 1
fi

. process-script-args.sh

if [[ $ARGC != 1 ]]; then
  echo "usage: ${0} <username EXCLUDING DOMAIN>"
else
  USER_NAME=${ARGV[0]}@$WAVE_SERVER_DOMAIN_NAME
  echo "running client as user: ${USER_NAME}"
  exec java $DEBUG_FLAGS -jar dist/fedone-client-console-$FEDONE_VERSION.jar $USER_NAME $WAVE_SERVER_HOSTNAME $WAVE_SERVER_PORT
fi
