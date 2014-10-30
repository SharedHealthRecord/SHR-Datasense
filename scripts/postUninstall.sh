#!/bin/sh

rm -f /etc/init.d/datasense
rm -f /etc/default/datasense
rm -f /var/run/datasense

#Remove datasense from chkconfig
chkconfig --del datasense || true
