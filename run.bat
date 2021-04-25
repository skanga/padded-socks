
:: Levels are ERROR, WARN, INFO, DEBUG, TRACE
set LOGBACK_LOG_LEVEL=WARN
start java --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true -Dnashorn.args=--no-deprecation-warning --illegal-access=warn -Djava.net.preferIPv4Stack=true -jar target\padded-socks-1.0.jar
::wget -N http://wpad/wpad.dat
java -cp target\padded-socks-1.0.jar paddedsocks.SocksGet http://wpad/wpad.dat localhost 1080 wpad.dat
echo done
