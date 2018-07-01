package commands;

import java.util.HashMap;
import java.util.Set;

class Directory {

    //this class stores directories and files
    private final String path;
    private final Directory parent;
    private final HashMap<String, Directory> children;
    private final HashMap<String, File> files;
    private final String name;

    public Directory(String _name, String _path, Directory _parent) {
        name = _name;
        path = _path;
        parent = _parent;
        children = new HashMap<>();
        files = new HashMap<>();
    }

    boolean addDirectory(String dirName) {
        if (children.containsKey(dirName) || files.containsKey(dirName)) {
            System.out.println("mkdir: cannot make directory '" + dirName + "':File Exists");
            return false;
        }
        children.put(dirName, new Directory(dirName, path + "/" + dirName, this));
        return true;
    }

    Set<String> getChildrenNames() {
        return children.keySet();
    }

    boolean hasChildren() {
        return !children.isEmpty();
    }

    String getPath() {
        return path;
    }

    Directory getParent() {
        return parent;
    }

    boolean isChild(String n) {
        return (children.keySet().contains(n));
    }

    Directory getChild(String n) {
        return children.get(n);
    }

    boolean removeDirectory(String dirName) {
        if (!children.containsKey(dirName)) {
            System.out.println("rmdir: '" + dirName + "': No such file or directory");
            return false;
        } else if (!children.get(dirName).isEmpty()) {
            System.out.println("rmdir: '" + dirName + "': Directory not empty");
            return false;
        }
        children.remove(dirName);
        return true;
    }

    boolean isEmpty() {
        return children.isEmpty() && files.isEmpty();
    }

    boolean addFile(String file) {
        if (files.containsKey(file) || children.containsKey(file)) {
            System.out.println("touch: cannot create file '" + file + "':File Exists");
            return false;
        }
        files.put(file, new File(file, path, this));
        return true;
    }

    Set<String> getFileNames() {
        return files.keySet();
    }

    boolean removeFile(String file) {
        if (!files.containsKey(file)) {
            System.out.println("rm: '" + file + "': No such file or directory");
            return false;
        }
        files.remove(file);
        return true;
    }

    boolean removeDirectoryRecursive(String dirName) {
        if (!children.containsKey(dirName)) {
            System.out.println("rm: '" + dirName + "': No such file or directory");
            return false;
        }
        children.remove(dirName);
        return true;
    }

    boolean containsFile(String src) {
        return files.containsKey(src);
    }

    File getFile(String file) {
        return files.get(file);
    }

    boolean addFile(File f) {
        //this function is used to copy a complete file object from one location to another
        if (files.containsKey(f.getName())) {
            System.out.println("touch: cannot copy file '" + f.getName() + "':File already exists");
            return false;
        }
        f.setParent(this);
        f.setPath(path);
        files.put(f.getName(), f);
        return true;
    }

    Set<String> getChildren() {
        return children.keySet();
    }

    String getName() {
        return name;
    }

}
