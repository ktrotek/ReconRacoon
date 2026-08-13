package gr.reconkit.pawprint.tools;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Optional;

public class HeaderTool implements Tool {

    public String name() {
        return "headers";
    }

    public String usage() {
        return "Header response";
    }

    public void run(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            System.out.println(usage());
            return;
        }

        // Prevents endless redirects
        int hopCount = 0;

        String uri = args[0];

        HttpResponse<String> response = query(uri);

        Optional<String> location = response.headers().firstValue("location");

        do  {
            System.out.println(response);
            response.headers().map().forEach((name, values) ->
                    System.out.println(name + ": " + values));

            switch (response.statusCode()) {
                case 301:
                    System.out.println("Website redirects to" + location.toString());
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

            uri = location.toString();
            hopCount ++;

        }
        while (location.isPresent() && hopCount <= 5);



    }

    private HttpResponse<String> query(String uri) throws InterruptedException, java.io.IOException{
            HttpClient httpClientDialler = HttpClient.newHttpClient();
            // Timeout so the process doesnt hang
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).HEAD().timeout(Duration.ofSeconds(5)).build();
            HttpResponse<String> response = httpClientDialler.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
    }
}