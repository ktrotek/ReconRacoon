package gr.reconkit.pawprint.tools;

import java.io.IOException;

public interface Tool {
    String name();

    String usage();

    void run(String[] args) throws IOException, InterruptedException;
}
