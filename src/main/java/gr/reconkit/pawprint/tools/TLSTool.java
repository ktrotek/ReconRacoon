package gr.reconkit.pawprint.tools;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static java.time.temporal.ChronoUnit.DAYS;

public class TLSTool implements Tool {

    public String name() {
        return "tls";
    }

    public String usage() {
        return "tls certificate lookup";
    }

    private static final List<String> vendors = List.of("Fortinet", "Palo Alto", "Sophos", "SonicWall",
            "WatchGuard", "Check Point", "Zscaler", "Netskope", "Forcepoint", "Blue Coat", "Cisco Umbrella", "McAfee",
            "Trend Micro", "Kaspersky", "Bitdefender", "ESET", "Fiddler", "mitmproxy", "Burp");


    public void run(String[] args) throws IOException, SSLPeerUnverifiedException {

        if (args.length == 0) {
            System.out.println(usage());
            return;
        }

        String host = "";
        int defaultPort = 443;
        int port = -1;
        boolean containsFlag = false;

        for (String token : args) {
            if (token.startsWith("--")) {
                containsFlag = true;
            } else if (token.contains(":")) {
                int index = token.lastIndexOf(':');
                port = Integer.parseInt(token.substring(index + 1));
                host = token.substring(0, index);
            } else {
                host = token;
                port = defaultPort;
            }
        }

        SSLSocketFactory sslsocketfactory;

        if (!containsFlag) {
            sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        } else {
            System.out.println("INSECURE mode - certificate validation disabled");

            // A TrustManager rejects a cert by THROWING; empty bodies never throw, so this accepts every certificate.
            X509TrustManager permissive = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{permissive}, null);
                sslsocketfactory = ctx.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException("Could not initialise permissive TLS context", e);
            }
        }

        try (SSLSocket sslSocket = (SSLSocket) sslsocketfactory.createSocket(host, port)) {
            SSLSession session = sslSocket.getSession();

            Certificate[] chain = session.getPeerCertificates();
            X509Certificate leaf = (X509Certificate) chain[0];
            System.out.printf("%-9s %s%n", "Host:", leaf.getSubjectX500Principal());
            System.out.printf("%-9s %s%n", "Issuer:", leaf.getIssuerX500Principal());

            String certificateVendor = leaf.getIssuerX500Principal().getName();
            boolean isMiddlebox = false;

            for (String name : vendors) {
                //  toLowerCase() could produce wrong results on a PC with Greek/etc locale
                if (certificateVendor.toLowerCase(Locale.ENGLISH).contains(name.toLowerCase(Locale.ENGLISH))) {
                    System.out.println(name + " appears to be a middlebox certificate issuer");
                    isMiddlebox = true;
                }
            }
            if (!isMiddlebox) {
                System.out.println("Certificate doesn't appear to originate from a middlebox issuer");
            }

            System.out.printf("%-9s %s %s%n", "TLS:", session.getCipherSuite(), session.getProtocol());

            Instant expiryInstant = leaf.getNotAfter().toInstant();
            long daysRemaining = DAYS.between(Instant.now(), expiryInstant);

            if (daysRemaining > 0) {
                System.out.println("The certificate expires in: " + daysRemaining + " days");
            } else {
                System.out.println("Certificate has expired");
            }

            Collection<List<?>> generalName = leaf.getSubjectAlternativeNames();

            if (generalName == null) {
                return;
            }

            for (List<?> entry : generalName) {
                System.out.println(entry.get(1));
            }

        } catch (SSLPeerUnverifiedException e) {
            System.out.println("Peer not authenticated - possible middlebox certificate - rerun with --insecure");
            throw e;
        } catch (CertificateParsingException e) {
            throw new RuntimeException(e);

        }
    }
}
