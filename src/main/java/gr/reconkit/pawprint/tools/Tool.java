package gr.reconkit.pawprint.tools;


public interface Tool {
    String name();

    String usage();

    void run(String[] args);
}
