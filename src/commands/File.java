package commands;

public class File {

    //File object
    private String path;
    private Directory parent;
    private final String name;

    File(String _name, String _path, Directory _parent) {
        path = _path;
        parent = _parent;
        name = _name;
    }

    String getName() {
        return name;
    }

    void setPath(String _path) {
        path = _path;
    }

    void setParent(Directory _parent) {
        parent = _parent;
    }
}
