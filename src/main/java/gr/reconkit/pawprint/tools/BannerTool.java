package gr.reconkit.pawprint.tools;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class BannerTool implements Tool {

    public String name() {
        return "banner";
    }

    public String usage() {
        return "banner grabbing tool";
    }

    public void run(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println(usage());
            return;
        }

        String domain = args[0];
        String port = args[1];

        String bannerResponse = query(domain, port);
        System.out.println(bannerResponse);
    }

    private String query(String domain, String port) throws IOException {
        try (Socket socket = new Socket(domain, Integer.parseInt(port))) {

            OutputStream output = socket.getOutputStream();
            // Give up on any read that blocks longer than 2s (services that greet then hold the line open)
            socket.setSoTimeout(2000);
            // HTTP stays silent until nudged with a request; greeters (SSH, SMTP) ignore this and talk first
            String query = "HEAD / HTTP/1.0\r\n\r\n";
            // Convert query to bytes, explicit charset so there's no issue between machines
            byte[] bytes = query.getBytes(StandardCharsets.UTF_8);
            output.write(bytes);
            output.flush();
            InputStream input = socket.getInputStream();
            // Decodes bytes to characters
            InputStreamReader chars = new InputStreamReader(input, StandardCharsets.UTF_8);
            // Groups characters into lines
            BufferedReader lines = new BufferedReader(chars);
            String line;

            StringBuilder sb = new StringBuilder();

            // Two things can end this loop:
            //   server closes the connection  -> readLine() returns null
            //   server greets then holds open -> setSoTimeout fires SocketTimeoutException
            try {
                while ((line = lines.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (SocketTimeoutException e) {
                // greeting captured, nothing more is coming - fall through and return it
            }

            return sb.toString();
        }
    }

}
