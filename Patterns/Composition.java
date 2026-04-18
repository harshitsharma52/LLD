package Patterns;

import java.util.ArrayList;
import java.util.List;

// Common Interface
interface FileSystemNode {
    int size();
    void ls();
}

// Leaf Node → File
class File implements FileSystemNode {
    private String name;
    private int size;

    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void ls() {
        System.out.println("File: " + name + " (" + size + ")");
    }
}

// Composite Node → Folder
class Folder implements FileSystemNode {
    private String name;
    private List<FileSystemNode> children;

    public Folder(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    // Add child (File or Folder)
    public void add(FileSystemNode node) {
        children.add(node);
    }

    @Override
    public void ls() {
        System.out.println("Folder: " + name);
        for (FileSystemNode node : children) {
            node.ls();
        }
    }

    @Override
    public int size() {
        int total = 0;
        for (FileSystemNode node : children) {
            total += node.size(); // recursion
        }
        return total;
    }
}


class Composition{
    public static void main(String[] args) {

        // Root Folder
        Folder root = new Folder("Harshit");

        // Files
        File file1 = new File("LLD Notes", 10);
        File file2 = new File("Resume.pdf", 5);

        // Sub Folder
        Folder hldFolder = new Folder("HLD Notes");
        hldFolder.add(new File("System Design.pdf", 20));

        // Build structure
        root.add(file1);
        root.add(file2);
        root.add(hldFolder);

        // Print structure
        root.ls();

        // Print total size
        System.out.println("Total Size: " + root.size());
    }
}