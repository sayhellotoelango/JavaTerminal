package commands;

class FileSystem {

    //the beginning directory
    private Directory home;

    public FileSystem(Directory home) {
        this.home = home;
    }

    Directory getHome() {
        return home;
    }
}
