package gr.reconkit.pawprint.tools;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class TLSTool implements Tool {

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
        String host = "";
        int defaultPort = 443;
        int port = 0;

        if (args[0].contains(":")){
            int index = args[0].lastIndexOf(':');
            port = Integer.parseInt(args[0].substring(index + 1));
            host = args[0].substring(0, index);
        }

        else {
            host = args[0];
            port = defaultPort;
        }

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (SSLSocket sslSocket =  (SSLSocket) sslsocketfactory.createSocket(host, port)) {
        SSLSession session = sslSocket.getSession();

        Certificate[] chain = session.getPeerCertificates();
        X509Certificate leaf = (X509Certificate) chain[0];

        System.out.printf("%-9s %s%n", "Host:", leaf.getSubjectX500Principal());
        System.out.printf("%-9s %s%n", "Issuer:", leaf.getIssuerX500Principal());
        System.out.printf("%-9s %s%n", "TLS:", session.getCipherSuite(), session.getProtocol());

        Instant expiryInstant =  leaf.getNotAfter().toInstant();
        long daysRemaining = DAYS.between(Instant.now(), expiryInstant);

            if (daysRemaining > 0) {
                System.out.println("The certificate expires in: " + daysRemaining + " days");
            }
            else {
                System.out.println("Certificate has expired");
            }

            Collection<List<?>> generalName= leaf.getSubjectAlternativeNames();

            if (generalName == null) {
                return;
            }

            for (List<?> entry : generalName) {
                System.out.println(entry.get(1));
            }

        } catch (CertificateParsingException e) {
            throw new RuntimeException(e);
        }
    }
}
