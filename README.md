# Padded Socks
A netty based socks5 proxy server with support for proxy auto-config (wpad) based backend redirection.

![topology](Padded%20Socks.jpg)

## Features
- listen with socks5 protocol
- configure browser OR cli tools like curl to use the socks5 proxy
Eg: curl -x socks5h://localhost:1080 https://www.oracle.com/java/
- can access the target directly OR via a normal HTTP proxy

## Building
Run "mvn clean package" and look for padded-socks-1.0.jar in the target folder.

## Running
For JDK8 and lower use run8.[sh|bat] and for higher jdk versions look at run.[sh|bat]

Review app.properties for configuration information before starting.  

## TODO/KNOWN LIMITATIONS

- The wpad file can return the following types:
        DIRECT, PROXY, SOCKS, HTTP, HTTPS, SOCKS4, SOCKS5
  Currently we ONLY support DIRECT and PROXY. Others can be added.
- The wpad file can return MULTIPLE proxy servers to use in a failover scenarion. Currently we ONLY look at the first one.  
  PROXY w3proxy.example.com:8080; PROXY proxy1.example.com:8080
  Failover can be added.

## References

- https://blog.gwlab.page/vpn-over-ssh-the-socks-proxy-8a8d7bdc7028
 