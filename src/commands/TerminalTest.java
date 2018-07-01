package commands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JTextArea;

public class TerminalTest {

    public static void main(String[] args) throws UnknownHostException, IOException, Exception {
        String user = System.getProperty("user.name");
        InetAddress myHost = InetAddress.getLocalHost();
        String hostname = myHost.getHostName();
        Terminal terminal = new Terminal(hostname, user);

        JTextArea ta = new JTextArea();

        System.setOut(new PrintStream(new TextAreaOutputStream(ta)));

        TerminalFrame frame = new TerminalFrame("Terminal", ta, terminal);
        //Launching the custom terminal

    }
}
