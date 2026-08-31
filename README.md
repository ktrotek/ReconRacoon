# 🦝 ReconKit

> A small, dependency-free reconnaissance toolkit written in plain Java. The tool is CLI-driven, with network probes like WHOIS lookup, header fetching, TLS certificate inspection, and service banner grabbing.

`reconkit` is a network probing toolkit built on just two primitives from the standard library: `java.net.Socket` and `java.net.http.HttpClient`. Every tool is a simple, readable wrapper over a real network protocol so you can see exactly what goes over the wire. Developed as a learning project to improve Java code writing while also getting a better understanding of network fundamentals under the hood.

Each sub-command is a self-contained `Tool` plugged into a single dispatcher.

## Tools

| Command   | What it does                                                         | How it happens                              |
| --------- | -------------------------------------------------------------------- | ------------------------------------------- |
| `whois`   | Raw WHOIS lookup for a domain                                        | Raw TCP on port 43                          |
| `headers` | Fetches a URL and grades its HTTP security headers (pass/fail)       | `java.net.http.HttpClient`, HEAD            |
| `tls`     | Inspects a server's TLS certificate chain and flags MITM middleboxes | `SSLSocket` handshake + `X509Certificate`   |
| `banner`  | Grabs the greeting a service announces itself with                   | Raw TCP socket + read timeout; HTTP nudged with a `HEAD` request |

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
banner - banner grabbing tool
tls - tls certificate lookup
```

Then invoke any tool by name, followed by its arguments:

```bash
java -jar target/reconkit-1.0-SNAPSHOT.jar <tool> <args...>
```

For example:

```bash
java -jar target/reconkit-1.0-SNAPSHOT.jar banner example.com 80
java -jar target/reconkit-1.0-SNAPSHOT.jar tls example.com
```

## Shortcut: run it as `rk`

Typing the full `java -jar target/...` every time gets old. Add a shell alias so you
can just run `rk <tool> <args...>`.

Add this line to `~/.bashrc` (adjust the path if your checkout lives elsewhere):

```bash
alias rk='java -jar "$HOME/IdeaProjects/reconkit/target/reconkit-1.0-SNAPSHOT.jar"'
```

Reload your shell so the alias takes effect:

```bash
source ~/.bashrc
```

Now the tools run with the short name from anywhere:

```bash
rk banner example.com 80
rk tls example.com
rk whois example.com
```

Prefer a real command over an alias? Drop a one-line wrapper script on your `PATH`
instead — e.g. `~/.local/bin/rk`:

```bash
#!/usr/bin/env sh
exec java -jar "$HOME/IdeaProjects/reconkit/target/reconkit-1.0-SNAPSHOT.jar" "$@"
```

Then make it executable with `chmod +x ~/.local/bin/rk`.
