# padded-socks

A netty4-based socks5 proxy server with support for proxy auto-config (wpad) based backend redirection.

## Features

- listen with socks5 protocol
- configure browser OR cli tools like curl to use the socks5 proxy
Eg: curl -x socks5h://localhost:1080 https://www.oracle.com/java/
- can access the target directly OR via a normal HTTP proxy

## TODO/KNOWN LIMITATIONS

- The wpad file can return the following types:
        DIRECT, PROXY, SOCKS, HTTP, HTTPS, SOCKS4, SOCKS5
  Currently we ONLY support DIRECT and PROXY. Others can be added.
- The wpad file can return MULTIPLE proxy servers to use in a failover scenarion. Currently we ONLY look at the first one.  
  PROXY w3proxy.example.com:8080; PROXY proxy1.example.com:8080
  Failover can be added.
