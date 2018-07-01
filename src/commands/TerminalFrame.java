package commands;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

public class TerminalFrame extends JFrame implements KeyListener {

    private final JTextArea ta;
    private final ProtectedTextComponent ptc;
    private final Terminal terminal;
    private ArrayList<String> cmdHistory;
    private int pos;
    private int prevPos;
    JScrollBar bar;

    TerminalFrame(String title, JTextArea _ta, Terminal _terminal) throws Exception {

        super(title);
        terminal = _terminal;
        ta = _ta;
        ptc = new ProtectedTextComponent(ta);
        cmdHistory = new ArrayList<>();
        pos = -1;
        prevPos = -1;

        ta.addKeyListener(this);
        ta.setBackground(Color.black);
        ta.setForeground(Color.white);
        ta.setCaretColor(Color.white);

        JScrollPane scroller = new JScrollPane(ta);
        bar = new JScrollBar();
        scroller.add(bar); //adding scroll bar to text area
        add(scroller);
        Font font = new Font("Lucida Console", Font.PLAIN, 18); //setting font
        ta.setFont(font);
        setSize(1200, 700);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });//helps close JFrame
        setVisible(true); //enables JFrame

        String a = terminal.getText() + "$ ";
        System.out.print(a); //first attempt to get command
        ptc.protectText(0, a.length() - 1);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                ta.setCaretPosition(ta.getDocument().getLength()); //on pressing the key, caret is moved to the end
                break;
            case KeyEvent.VK_UP:
                Robot robot;
                try {
                    robot = new Robot();
                    robot.keyPress(KeyEvent.VK_END); //bringing back cursor to end while up arrow is pressed, prevents unwanted scroll upwards
                } catch (AWTException ex) {
                    Logger.getLogger(TerminalFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                try {
                    callExecute();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                break;
            case KeyEvent.VK_UP:
                ta.setCaretPosition(ta.getDocument().getLength());
                if ((!cmdHistory.isEmpty()) && pos >= 0 && pos < cmdHistory.size()) {
                    try {
                        if (prevPos == pos && pos == cmdHistory.size() - 1) {
                            pos = pos - 1;
                        }

                        if (pos == -1) {
                            pos = 0;
                        }
                        prevPos = pos;
                        setCmd(cmdHistory.get(pos--));

                        if (pos == -1) {
                            pos = 0;
                        }
                    } catch (Exception ex) {
                        System.out.print(ex);
                    }
                }
                break;
            case KeyEvent.VK_DOWN:

                if ((!cmdHistory.isEmpty()) && pos >= 0 && pos < cmdHistory.size()) {
                    try {
                        if (prevPos == pos && pos == 0) {
                            pos = pos + 1;
                        }
                        if (pos == cmdHistory.size()) {
                            pos = cmdHistory.size() - 1;
                        }
                        prevPos = pos;
                        setCmd(cmdHistory.get(pos++));
                        if (pos == cmdHistory.size()) {
                            pos = cmdHistory.size() - 1;
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
                break;
            case KeyEvent.VK_TAB: {
                try {
                    String cmdLastLine = getCmdInLastLine();
                    while (cmdLastLine.endsWith("\t")) { //removing tabs that were pressed
                        //System.err.print("TAB");
                        cmdLastLine = cmdLastLine.substring(0, cmdLastLine.length() - 1);
                    }
                    String content = terminal.tabPressed(cmdLastLine);

                    if (content.equals("")) {
                        return;
                    } else if (content.endsWith("\n")) {
                        setCmd(content.replace("\n", ""));
                    } else if (content.contains(" ")) {
                        ptc.clear(); //clearing protection
                        System.out.println("\n" + content);
                        String a = terminal.getText() + "$ ";
                        System.out.print(a);
                        ptc.protectText(0, ta.getText().length() - 1); //enabling protection
                        System.out.print(cmdLastLine);

                    } else {
                        setCmd(content);
                    }
                } catch (BadLocationException ex) {
                    System.out.println(ex);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
            break;
            default:
                break;
        }

    }

    private void callExecute() throws BadLocationException, IOException, Exception {

        String a = terminal.getText() + "$ ";
        String lineContainingCmd = getLastLine();

        String cmd = lineContainingCmd.substring(a.length());

        if (!cmd.trim().equals("")) {
            cmdHistory.add(cmd);
            pos = cmdHistory.size() - 1;
            prevPos = -1;
            if (cmd.trim().equals("clear")) {
                ptc.clear(); //clears Protection
                ta.setText("");
                System.out.print(a);
                ptc.protectText(0, ta.getText().length() - 1); //enabling protection
                return;
            } else {
                terminal.executeCommand(cmd);
            }
        }

        System.out.print("\n" + terminal.getText() + "$ ");
        ptc.protectText(0, ta.getText().length() - 1);

    }

    public String getLastLine() throws BadLocationException {
        int end = ta.getDocument().getLength();
        int start = Utilities.getRowStart(ta, end);

        while (start == end) {
            end--;
            start = Utilities.getRowStart(ta, end);
        }
        return ta.getText(start, end - start);
    }

    private String getCmdInLastLine() throws BadLocationException, Exception {
        String lineContainingCmd = getLastLine();
        String a = terminal.getText() + "$ ";
        String cmd = lineContainingCmd.substring(a.length());
        return cmd;
    }

    private void setCmd(String newCmd) throws BadLocationException, Exception {

        ptc.clear();
        String cmd = getCmdInLastLine();
        ta.setText(ta.getText().substring(0, ta.getText().length() - cmd.length()) + newCmd);
        ptc.protectText(0, ta.getText().length() - newCmd.length() - 1);
    }

}
