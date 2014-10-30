#!/bin/sh

ln -s /opt/datasense/bin/datasense /etc/init.d/datasense
ln -s /opt/datasense/var /var/run/datasense

if [ ! -e /var/log/datasense ]; then
    mkdir /var/log/datasense
fi

# Add bdshr service to chkconfig
chkconfig --add datasense