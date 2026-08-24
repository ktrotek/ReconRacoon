package gr.reconkit.pawprint;
import gr.reconkit.pawprint.tools.TLSTool;
import gr.reconkit.pawprint.tools.Tool;
import gr.reconkit.pawprint.tools.WhoisTool;
import gr.reconkit.pawprint.tools.HeaderTool;

import java.io.IOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {


        HashMap<String, Tool> menu = new HashMap<String, Tool>();

        Tool headers = new HeaderTool();
        Tool whois = new WhoisTool();
        Tool tls = new TLSTool();

        menu.put(whois.name(), whois);
        menu.put(headers.name(), headers);
        menu.put(tls.name(), tls);

        try {
            if (args.length == 0) {
                System.out.println("Help List");

                menu.forEach((name, tool) ->
                        System.out.println(name + " - " + tool.usage()));
            } else {
                Tool tool = menu.get(args[0]);
                if (tool == null) {
                    System.out.println("Tool not Found");
                    return;
                } else tool.run(java.util.Arrays.copyOfRange(args, 1, args.length));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println();
            System.err.println(e);
            System.exit(1);
        }
    }
}