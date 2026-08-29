# 🦝 ReconKit 

> A small, dependency-free reconnaissance toolkit written in plain Java. The tool is CLI-driven, with network probes like WHOIS lookup, Header fetching and TLS certificate inspection.

`reconkit` is a network probing toolkit built on just two primitives from the standard library: `java.net.Socket` and `java.net.http.HttpClient`. Every tool is a simple, readable wrapper over a real network protocol so you can see exactly what goes over the wire. Developed as a learning project to improve Java code writting while also getting a better understanding of network fundamentals under the hood.

Each sub-command is a self-contained `Tool` plugged into a single dispatcher.

## Tools

| Command   | What it does                                                         | How it happens                           |
| --------- | -------------------------------------------------------------------- | -------------------------------------    |
| `whois`   | Raw WHOIS lookup for a domain                                        | Raw TCP on port 43                       |
| `headers` | Fetches a URL and grades its HTTP security headers (pass/fail)       | `java.net.http.HttpClient`, HEAD         |
| `tls`     | Inspects a server's TLS certificate chain and flags MITM middleboxes | `SSLSocket` handshake + `X509Certificate`|

## Build

```bash
./mvnw clean package
```

This produces a runnable jar at `target/reconkit-1.0-SNAPSHOT.jar`.

## Run

Run with no arguments to see the tool list:

```bash
$ java -jar target/reconkit-1.0-SNAPSHOT.jar
Help List
headers - Header response
whois - raw WHOIS lookup
tls - tls certificate lookup
```

Then invoke any tool by name, followed by its arguments:

```bash
java -jar target/reconkit-1.0-SNAPSHOT.jar <tool> <args...>
```
