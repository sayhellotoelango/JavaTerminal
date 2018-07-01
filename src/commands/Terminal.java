package commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Terminal {

    //Terminal class can be run both using Terminal and JFrame
    private final String user;
    private final String host;
    private Directory pwd;
    private Directory prevDirectory; //stores the previous directory before cd
    private final FileSystem fs;
    private final String cmds[] = {"ls", "exit", "clear", "pwd", "cd", "mkdir", "rmdir", "rm", "touch", "cp", "mv", "locate", "man"};

    //set of possible commands
    Terminal(String _host, String _user) {
        //Terminal can be initiated with username and computer name(host name)
        host = _host;
        user = _user;
        pwd = new Directory("", "/home/" + user, null);
        prevDirectory = null;
        fs = new FileSystem(pwd);
    }

    void executeCommand(String cmdFull) throws IOException {
        //executes the commands (with arguments)
        try {
            cmdFull = cmdFull.trim(); //removing whitespaces around the command
            String cmd, arg = "";
            if (cmdFull.contains(" ")) {
                cmd = cmdFull.substring(0, cmdFull.indexOf(' '));
                arg = cmdFull.substring(cmdFull.indexOf(' ') + 1);
            } else {
                cmd = cmdFull;
            }

            arg = arg.trim();
            switch (cmd) { //using switch case for commands
                case "":
                    break;
                case "exit":
                    System.out.println("Exitting Terminal");
                    System.exit(0);
                    break;
                case "clear":
                    clear();
                    break;
                case "pwd":
                    pwd();
                    break;
                case "ls":
                    ls(arg);
                    break;
                case "cd":
                    cd(arg);
                    break;
                case "mkdir":
                    mkdir(arg);
                    break;
                case "rmdir":
                    rmdir(arg);
                    break;
                case "rm":
                    rm(arg);
                    break;
                case "touch":
                    touch(arg);
                    break;
                case "cp":
                    cp(arg, false);
                    break;
                case "mv":
                    mv(arg);
                    break;
                case "locate":
                    locate(arg);
                    break;
                case "man":
                    man(arg);
                    break;
                default:
                    System.out.println(cmd + ": not found");

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void pwd() throws Exception {
        System.out.println(pwd.getPath());
    }

    private void ls(String args) throws Exception {
        if (args.equals("")) {
            if (!pwd.isEmpty()) {
                for (String dir : pwd.getChildrenNames()) {
                    System.out.println(dir);
                }
                for (String file : pwd.getFileNames()) {
                    System.out.println(file);
                }
            } else {
                //System.out.println("empty");
            }
        } else {
            Directory tempPwd = pwd;
            Directory tempPrev = prevDirectory; //store current directory
            if (cd(args)) {
                ls("");
            }
            pwd = tempPwd;
            prevDirectory = tempPrev;

        }
    }

    private boolean cd(String path) throws Exception {
        //returns true if cd is successful
        if (path.equals(".")) {
            return true;
        } else if (path.equals(("~"))) {
            prevDirectory = pwd;
            pwd = fs.getHome();
            return true;
        } else if (path.equals("-")) {
            if (prevDirectory == null) {
                System.out.println("No Such File or Directory");
                return false;
            } else {
                Directory t = pwd;
                pwd = prevDirectory;
                prevDirectory = t;
                return true;
            }
        } else {
            Directory temp = prevDirectory;
            prevDirectory = pwd;
            if (path.startsWith("~/")) {
                pwd = fs.getHome();
                path = path.substring(2);
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            for (String n : path.split("/")) {
                if (n.equals("..")) {
                    Directory t = pwd.getParent();
                    if (t == null) {
                        System.out.println("No Parent Directory");
                        pwd = prevDirectory;
                        prevDirectory = temp;
                        return false;
                    } else {
                        pwd = t;
                    }
                } else if (isValidName(n) && pwd.isChild(n)) {
                    pwd = pwd.getChild(n);

                } else {
                    if (!isValidName(n)) {
                        System.out.println("Invalid Directory: " + path);
                    } else {
                        System.out.println("Directory does not exist: " + path);
                    }
                    pwd = prevDirectory;
                    prevDirectory = temp;
                    return false;
                }

            }

        }
        return true;
    }

    private boolean isValidName(String dirName) throws Exception {
        return Pattern.matches("[a-zA-Z0-9\\.]+", dirName);
    }

    private void mkdir(String arg) throws Exception {
        if (arg.equals("")) {
            man("mkdir");
            return;
        }
        for (String dirName : arg.split(" ")) {
            if (isValidName(dirName)) {
                pwd.addDirectory(dirName);
            } else {
                System.out.println("syntax Error: " + dirName + ": Invalid directory name");
            }
        }

    }

    private void rmdir(String arg) throws Exception {
        if (arg.equals("")) {
            man("rmdir");
        } else if (arg.contains("/")) {

            if (arg.startsWith("-p")) {
                arg = arg.substring("-p".length()).trim();
                while (arg.length() > 0) {
                    rmdir(arg); //removing directory recursively if empty
                    if (arg.contains("/")) {
                        arg = arg.substring(0, arg.lastIndexOf('/'));
                    } else {
                        arg = "";
                    }
                }
            } else {
                Directory tempPwd = pwd;
                Directory tempPrev = prevDirectory; //store current directory 
                if (!cd(arg.substring(0, arg.lastIndexOf('/')))) //change current directory
                {
                    return;
                }
                pwd.removeDirectory(arg.substring(arg.lastIndexOf('/') + 1));
                pwd = tempPwd;
                prevDirectory = tempPrev; //restore current directory
            }
        } else {
            for (String dirName : arg.split(" ")) {
                if (isValidName(dirName)) {
                    pwd.removeDirectory(dirName);
                } else {
                    System.out.println("syntax Error: Invalid directory name");
                }
            }
        }

    }

    private void rm(String arg) throws Exception {
        if (arg.equals("")) {
            //System.out.println("Usage: rm [option] DIRECTORY/FILE_NAMES \n Option -r is applicable for directory only");
            man("rm");
        } else if (arg.contains("/")) {
            Directory tempPwd = pwd;
            Directory tempPrev = prevDirectory; //store current directory 

            if (arg.startsWith("-r")) {
                arg = arg.substring("-r".length()).trim();
                if (arg.substring(arg.indexOf("/") + 1).equals("")) {
                    pwd.removeDirectoryRecursive(arg.substring(0, arg.indexOf("/")));
                    return;
                }
                if (!cd(arg.substring(0, arg.lastIndexOf('/')))) //change current directory
                {
                    return;
                }
                pwd.removeDirectoryRecursive(arg.substring(arg.lastIndexOf('/') + 1)); //removing file

            } else {
                if (!cd(arg.substring(0, arg.lastIndexOf('/'))))//change current directory
                {
                    return;
                }
                pwd.removeFile(arg.substring(arg.lastIndexOf('/') + 1)); //removing file

            }
            pwd = tempPwd;
            prevDirectory = tempPrev; //restore current directory
        } else if (arg.startsWith("-r")) {
            arg = arg.substring("-r".length()).trim();
            pwd.removeDirectoryRecursive(arg); //removing in current directory

        } else {
            for (String file : arg.split(" ")) {
                if (isValidName(file)) {
                    pwd.removeFile(file);
                } else {
                    System.out.println("syntax Error: Invalid file name");
                }
            }
        }

    }

    private void touch(String arg) throws Exception {
        //this command creates a file
        if (arg.equals("")) {
            //System.out.println("Usage: touch FILE_NAMES");
            man("touch");
            return;
        }
        for (String file : arg.split(" ")) {
            if (isValidName(file)) {
                pwd.addFile(file);
            } else {
                System.out.println("syntax Error: " + file + ": Invalid file name");
            }
        }
    }

    private void cp(String arg, boolean cut) throws Exception//used for both cp and mv
    {
        if (arg.equals("")) {
            man("cp");
            return;
        }
        String src = arg.substring(0, arg.indexOf(' '));
        String dest = arg.substring(arg.indexOf(' ') + 1);
        Directory tempPwd = pwd;
        Directory tempPrev = prevDirectory; //store current directory
        File f;

        if (src.contains("/"))//src is a directory
        {
            if (!cd(src.substring(0, src.lastIndexOf('/')))) {
                return; //if given path is invalid, return
            }
            src = src.substring(src.lastIndexOf('/') + 1);
        }
        Directory srcPwd = pwd;
        if (pwd.containsFile(src)) {
            f = pwd.getFile(src);
        } else {
            System.out.println("cp: " + src + ": file not found");
            return;
        }

        pwd = tempPwd;
        prevDirectory = tempPrev; //restore current directory

        tempPwd = pwd;
        tempPrev = prevDirectory; //store current directory
        if (!cd(dest)) {
            return; //if given path is invalid, return
        }
        if (pwd.addFile(f) && cut) {
            srcPwd.removeFile(f.getName());
        }
        pwd = tempPwd;
        prevDirectory = tempPrev; //restore current directory

    }

    private void mv(String arg) throws Exception {
        if (arg.equals("")) {
            man("mv");
            return;
        }
        cp(arg, true);
    }

    private void clear() throws IOException {
        //Runtime.getRuntime().exec("cls"); //executes on windows terminal
        for (int i = 0; i < 80; i++) {
            System.out.println(); //for netbeans
        }
    }

    private void locate(String name) {
        Directory dir = fs.getHome();
        if (dir.isChild(name)) {
            System.out.println(dir.getPath());
        }
        search(dir, name);
    }

    private void search(Directory dir, String name) {
        if (dir.containsFile(name)) {
            System.out.println(dir.getPath());
        }

        for (String dirName : dir.getChildren()) {
            Directory d = dir.getChild(dirName);
            if (d.isChild(name)) {
                System.out.println(d.getPath());
            }
            search(d, name);
        }
    }

    String getText() throws Exception {
        return user + "@" + host + ":" + "~" + pwd.getName();
    }

    private void man(String arg) {

        String details[] = {
            "ls [DIRECTORY]",
            "-",
            "-",
            "-",
            "cd DIRECTORY",
            "mkdir DIRECTORY...",
            "rmdir [OPTION] DIRECTORY...\n\toption : -p :remove DIRECTORY and its ancestors",
            "rm [OPTION] FILE...\n\toption : -r :Removes Directories and their contents recursively",
            "touch FILE_NAME(S)",
            "cp SOURCE_FILE DESTINATION",
            "mv SOURCE_FILE DESTINATION",
            "locate FILE_NAME",
            "man COMMAND_NAME"
        };
        HashMap<String, String> hm = new HashMap<>();
        for (int i = 0; i < cmds.length; i++) {
            hm.put(cmds[i], details[i]);
        }

        if (arg.equals("")) {
            for (String cmd : hm.keySet()) {
                System.out.print(cmd + ": ");
                if (hm.get(cmd).equals("-")) {
                    System.out.println("No details available for " + cmd);
                } else {
                    System.out.println(hm.get(cmd));
                }
            }

        } else if (hm.containsKey(arg)) {
            System.out.print(arg + ": ");
            if (hm.get(arg).equals("-")) {
                System.out.println("No details available for " + arg);
            } else {
                System.out.println(hm.get(arg));
            }

        } else {
            System.out.println("No manual entry for " + arg);
        }

    }

    String tabPressed(String lastLine) {
        //this method is useful only when there is a JFrame displaying the terminal

        if (lastLine.equals("")) {
            return "";
        }
        if (lastLine.contains(" ")) {
            String cmd = lastLine.substring(0, lastLine.indexOf(' '));
            String arg = lastLine.substring(lastLine.indexOf(' ') + 1);
            String temp = "";
            switch (cmd) {
                case "rm":
                case "mv": //when file is in source directory
                case "cp": //when file is in source directory
                    for (String file : pwd.getFileNames()) {
                        if (file.startsWith(arg)) {
                            temp += file + " ";
                        }
                    }

                    break;

                case "man":
                    for (String Cmd : cmds) {
                        if (Cmd.startsWith(arg)) {
                            temp += Cmd + " ";
                        }
                    }
                    break;

                case "ls":
                case "cd":
                case "rmdir":
                    for (String dir : pwd.getChildrenNames()) {
                        if (dir.startsWith(arg)) {
                            temp += dir + " ";
                        }
                    }

                    break;
                default:

            }
            temp = temp.trim();
            if (!temp.contains(" ") && temp.length() > 0) {
                temp = cmd + " " + temp + "\n";
            }
            return temp;

        } else {
            String temp = "";
            for (String cmd : cmds) {
                if (cmd.startsWith(lastLine)) {
                    temp += cmd + " ";
                }
            }
            return temp.trim();
        }

    }

}
