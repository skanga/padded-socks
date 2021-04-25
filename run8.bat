
:: Levels are ERROR, WARN, INFO, DEBUG, TRACE
set LOGBACK_LOG_LEVEL=WARN
start java -Djava.net.preferIPv4Stack=true -jar target/netty-socks5-1.0.jar
::wget -N http://wpad/wpad.dat
java -cp target/netty-socks5-1.0.jar nettysocks.SocksGet http://wpad/wpad.dat localhost 1080 wpad.dat
echo done