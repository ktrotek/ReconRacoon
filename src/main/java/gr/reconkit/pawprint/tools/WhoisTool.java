package gr.reconkit.pawprint.tools;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WhoisTool implements Tool{

    public String name() {
        return "whois";
    }
    public String usage(){
        return "raw WHOIS lookup";
    }


    public void run(String[] args){

        if (args.length == 0) {
            System.out.println(usage());
            return;
        }
        try {

            int index = args[0].lastIndexOf('.');
            String tld = args[0].substring(index + 1);
            String ianaResponse = query("whois.iana.org", tld);
            String[] responseLines = ianaResponse.split("\n");

            String server = null;

            for (String line : responseLines) {
                    if (line.startsWith("whois:"))
                        server = line.substring("whois:".length()).trim();
                }
            if (server == null || server.isEmpty()) {
                System.out.println("No WHOIS server for ." + tld);
                return;
            }
            String response = query(server, args[0]);
            System.out.println(response);

        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private String query(String server, String domain) throws IOException{
        try (Socket socket = new Socket(server, 43)){

            OutputStream output = socket.getOutputStream();
            // Create the query for the output stream, WHOIS demands \r\n to get response
            String query = domain + "\r\n";
            // Convert query to bytes, explicit charset so there no issue between machines
            byte[] bytes = query.getBytes(StandardCharsets.UTF_8);
            output.write(bytes);
            // Makes sure that
            output.flush();
            InputStream input = socket.getInputStream();
            // Decodes Bytes to characters
            InputStreamReader chars = new InputStreamReader(input, StandardCharsets.UTF_8);
            // Groups characters into lines
            BufferedReader lines = new BufferedReader(chars);
            String line;

            StringBuilder sb = new StringBuilder();
            // Server closes connection when WHOIS request is finished
            while ((line = lines.readLine()) != null) {
                    sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }


}
