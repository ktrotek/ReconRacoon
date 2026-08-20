package gr.reconkit.pawprint.tools;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class TlsTool implements Tool {

    public String name() {
        return "tls";
    }
    public String usage(){
        return "tls certificate lookup";
    }

    public void run(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println(usage());
            return;
        }
        String host = args[0];

        try (SSLSocket socket = new SSL(host, 443)) {

            return sslCertificate().getIssuerX500Principal();

        }
    }
}
