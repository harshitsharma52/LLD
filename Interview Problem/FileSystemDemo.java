
import java.util.*;
import java.util.concurrent.locks.*;

abstract class FileSystemNode {

    String name;

    FileSystemNode(String name) {

        this.name = name;
    }

    public abstract void display(String indent);
}

///////////////////////////////////////////////////////
// FILE
///////////////////////////////////////////////////////

class FileNode extends FileSystemNode {

    String content;

    ///////////////////////////////////////////////
    // THREAD SAFETY
    ///////////////////////////////////////////////

    Lock lock = new ReentrantLock();

    FileNode(
            String name,
            String content) {

        super(name);

        this.content = content;
    }

    public void write(
            String newContent) {

        lock.lock();

        try {

            content = newContent;
        }

        finally {

            lock.unlock();
        }
    }

    public String read() {

        lock.lock();

        try {

            return content;
        }

        finally {

            lock.unlock();
        }
    }

    @Override
    public void display(
            String indent) {

        System.out.println(
                indent + "File : "
                        + name);
    }
}

////////////////////////////////////////////////////////
// DIRECTORY
////////////////////////////////////////////////////////

class Directory
        extends FileSystemNode {

    List<FileSystemNode> children = new ArrayList<>();

    ////////////////////////////////////////////////
    // THREAD SAFETY
    ////////////////////////////////////////////////

    Lock lock = new ReentrantLock();

    Directory(String name) {

        super(name);
    }

    ////////////////////////////////////////////////
    // CREATE
    ////////////////////////////////////////////////

    public void add(
            FileSystemNode node) {

        lock.lock();

        try {

            children.add(node);
        }

        finally {

            lock.unlock();
        }
    }

    ////////////////////////////////////////////////
    // DELETE
    ////////////////////////////////////////////////

    public void remove(
            String nodeName) {

        lock.lock();

        try {

            children.removeIf(
                    node -> node.name.equals(
                            nodeName));
        }

        finally {

            lock.unlock();
        }
    }

    ////////////////////////////////////////////////
    // SEARCH
    ////////////////////////////////////////////////

    public FileSystemNode search(
            String targetName) {

        ////////////////////////////////////////////////
        // CURRENT NODE
        ////////////////////////////////////////////////

        if (this.name.equals(targetName)) {

            return this;
        }

        ////////////////////////////////////////////////
        // CHILDREN
        ////////////////////////////////////////////////

        for (FileSystemNode node : children) {

            ////////////////////////////////////////////////
            // FILE
            ////////////////////////////////////////////////

            if (node.name.equals(
                    targetName)) {

                return node;
            }

            ////////////////////////////////////////////////
            // DIRECTORY RECURSION
            ////////////////////////////////////////////////

            if (node instanceof Directory) {

                FileSystemNode found = ((Directory) node).search(targetName);

                if (found != null) {

                    return found;
                }
            }
        }

        return null;
    }

    ////////////////////////////////////////////////
    // DISPLAY
    ////////////////////////////////////////////////

    @Override
    public void display(
            String indent) {

        System.out.println(
                indent
                        + "Directory : "
                        + name);

        for (FileSystemNode node : children) {

            node.display(
                    indent + "   ");
        }
    }
}

class FileSystem {

    Directory root;

    FileSystem() {

        root = new Directory("root");
    }

    public Directory getRoot() {

        return root;
    }
}

class FileSystemDemo {

    public static void main(
            String[] args) {

        /////////////////////////////////////////////////
        // FILE SYSTEM
        /////////////////////////////////////////////////

        FileSystem fs = new FileSystem();

        /////////////////////////////////////////////////
        // DIRECTORIES
        /////////////////////////////////////////////////

        Directory movies = new Directory(
                "Movies");

        Directory songs = new Directory(
                "Songs");

        /////////////////////////////////////////////////
        // FILES
        /////////////////////////////////////////////////

        FileNode avengers = new FileNode(
                "Avengers.mp4",
                "Marvel Movie");

        FileNode song = new FileNode(
                "song.mp3",
                "Music File");

        /////////////////////////////////////////////////
        // CREATE
        /////////////////////////////////////////////////

        movies.add(avengers);

        songs.add(song);

        fs.getRoot().add(movies);

        fs.getRoot().add(songs);

        /////////////////////////////////////////////////
        // DISPLAY
        /////////////////////////////////////////////////

        System.out.println(
                "Initial Structure");

        fs.getRoot().display("");

        /////////////////////////////////////////////////
        // SEARCH
        /////////////////////////////////////////////////

        System.out.println(
                "\nSearching...");

        FileSystemNode result = fs.getRoot()
                .search(
                        "song.mp3");

        if (result != null) {

            System.out.println(
                    "Found : "
                            + result.name);
        }

        /////////////////////////////////////////////////
        // DELETE
        /////////////////////////////////////////////////

        System.out.println(
                "\nDeleting song.mp3");

        songs.remove(
                "song.mp3");

        /////////////////////////////////////////////////
        // DISPLAY AGAIN
        /////////////////////////////////////////////////

        System.out.println(
                "\nAfter Delete");

        fs.getRoot().display("");
    }
}
