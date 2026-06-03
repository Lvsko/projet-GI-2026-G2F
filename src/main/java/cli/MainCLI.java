package cli;
import cli.Menu;

/**
 * Entry point for the command-line version of the EXIT simulation.
 * @author Ruben
 */
public class MainCLI {

    /**
     * Main method - launches the CLI menu.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.start();
    }
}
