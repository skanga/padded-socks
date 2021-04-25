
:: Levels are ERROR, WARN, INFO, DEBUG, TRACE
set LOGBACK_LOG_LEVEL=WARN
start java -Djava.net.preferIPv4Stack=true -jar target\padded-socks-1.0.jar
::wget -N http://wpad/wpad.dat
java -cp target\padded-socks-1.0.jar paddedsocks.SocksGet http://wpad/wpad.dat localhost 1080 wpad.dat
echo done