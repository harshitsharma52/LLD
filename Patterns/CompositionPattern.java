import java.util.ArrayList;
import java.util.List;

// Common interface
interface FileSystemItem {

    void showDetails();

    int getSize();
}

// Leaf Object
class FileItem implements FileSystemItem {

    private String name;
    private int size;

    public FileItem(String name, int size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public void showDetails() {
        System.out.println("File: " + name + " Size: " + size);
    }

    @Override
    public int getSize() {
        return size;
    }
}

// Composite Object
class Folder implements FileSystemItem {

    private String name;

    // Composition
    private List<FileSystemItem> items = new ArrayList<>();

    public Folder(String name) {
        this.name = name;
    }

    public void addItem(FileSystemItem item) {
        items.add(item);
    }

    @Override
    public void showDetails() {

        System.out.println("Folder: " + name);

        for (FileSystemItem item : items) {
            item.showDetails();
        }
    }

    @Override
    public int getSize() {

        int totalSize = 0;

        for (FileSystemItem item : items) {
            totalSize += item.getSize();
        }

        return totalSize;
    }
}

public class CompositionPattern {

    public static void main(String[] args) {

        // Files
        FileItem file1 = new FileItem("Resume.pdf", 10);
        FileItem file2 = new FileItem("Photo.png", 20);
        FileItem file3 = new FileItem("Video.mp4", 50);

        // Folder
        Folder documents = new Folder("Documents");

        documents.addItem(file1);
        documents.addItem(file2);

        // Another Folder
        Folder media = new Folder("Media");

        media.addItem(file3);

        // Root Folder
        Folder root = new Folder("Root");

        root.addItem(documents);
        root.addItem(media);

        // Same operation on all objects
        root.showDetails();

        System.out.println("Total Size: " + root.getSize());
    }

}
