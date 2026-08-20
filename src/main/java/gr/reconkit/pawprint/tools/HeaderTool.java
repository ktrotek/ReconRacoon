package gr.reconkit.pawprint.tools;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class HeaderTool implements Tool {

    public String name() {
        return "headers";
    }
    public String usage() {
        return "Header response";
    }
    public void run(String[] args) throws IOException, InterruptedException, NoSuchElementException {
        if (args.length == 0) {
            System.out.println(usage());
            return;
        }
        // Prevents endless redirects
        int hopCount = 0;

        URI uri = URI.create(args[0]);

        Optional<String> redirectLocation;
        do {
            HttpResponse<String> response = query(uri);
            redirectLocation = response.headers().firstValue("location");

            System.out.println(response);
            response.headers().map().forEach((name, values) ->
                    System.out.println(name + ": " + values));

            if (redirectLocation.isPresent()) {
                switch (response.statusCode()) {
                    case 301:
                        System.out.println("Website redirects to " + redirectLocation.get());
                        System.out.println("Redirect number: " + hopCount);
                        break;
                    case 302:
                        System.out.println("Found");
                        break;
                    case 307:
                        System.out.println("Temporary Redirect");
                        break;
                    case 308:
                        System.out.println("Permanent Redirect");
                        break;
                    default:
                        return;
                }
                uri = URI.create(redirectLocation.get());
            }
            else {
                List<String> securityHeaders = List.of("strict-transport-security", "referrer-policy", "x-frame-options",
                        "x-content-type-options", "content-security-policy");
                System.out.println("Security audit results: ");

                for (String header : securityHeaders) {
                    boolean isPresent = response.headers().firstValue(header).isPresent();
                    System.out.println(header + ": " + (isPresent ? "PASS" : "FAIL"));
                }
                break;
            }
            hopCount++;
        }
        while (hopCount <= 4);
    }
    private HttpResponse<String> query(URI uri) throws InterruptedException, java.io.IOException{
            HttpClient httpClientDialler = HttpClient.newHttpClient();
            // Timeout so the process doesnt hang
            HttpRequest request = HttpRequest.newBuilder().uri(uri).HEAD().timeout(Duration.ofSeconds(5)).build();
        return httpClientDialler.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
