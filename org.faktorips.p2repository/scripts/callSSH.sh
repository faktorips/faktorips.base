#!/bin/bash
# executes a loacl script on a given ssh server
# $1: URL of ssh server name@host
# $2: Qualified name of the script (local)
# $*: Arguments of the script
URL=$1
SCRIPT_NAME=$2
shift
shift

ssh $URL bash -s -- "$@" < $SCRIPT_NAME
