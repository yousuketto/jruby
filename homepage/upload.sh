#!/bin/sh

if [ -z $1]; then
	user=$USER
else
	user=$1
fi

exec scp build/* $user@shell.sf.net:/home/groups/j/jr/jruby/htdocs/
