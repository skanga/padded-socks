#!/usr/bin/env bash

# Levels are ERROR, WARN, INFO, DEBUG, TRACE
export LOGBACK_LOG_LEVEL=WARN
java -Djava.net.preferIPv4Stack=true -jar target/netty-socks5-1.0.jar &
# wget -N http://wpad/wpad.dat
java -cp target/netty-socks5-1.0.jar paddedsocks.SocksGet http://wpad/wpad.dat localhost 1080 wpad.dat
echo done
