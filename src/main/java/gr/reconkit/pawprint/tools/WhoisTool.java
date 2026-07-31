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
        try (Socket socket = new Socket("whois.verisign-grs.com", 43)){

            OutputStream output = socket.getOutputStream();
            // Create the query for the output stream, WHOIS demands \r\n to get response
            String query = args[0] + "\r\n";
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

            // Server closes connection when WHOIS request is finished
            while ((line = lines.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}
