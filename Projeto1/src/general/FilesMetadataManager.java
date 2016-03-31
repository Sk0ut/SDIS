package general;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Afonso on 31/03/2016.
 */
public class FilesMetadataManager {

    private class Metadata {
        String filePath;
        String modificationDate;
        String fileId;
        int numChunks;

        Metadata(String filePath, String date, String fileId, int numChunks) {
            this.filePath = filePath;
            this.modificationDate = date;
            this.fileId = fileId;
            this.numChunks = numChunks;
        }
    }

    private static FilesMetadataManager instance = null;
    private static final String FILES_METADATA_FILENAME = "files.metadata";
    private List<Metadata> metadata;
    private Path path;

    private FilesMetadataManager(){
        metadata = new ArrayList<>();
    }

    public static FilesMetadataManager getInstance(){
        if(instance == null)
            instance = new FilesMetadataManager();
        return instance;
    }

    public void init(String localId){
        this.path = FileSystems.getDefault().getPath("peer" + localId, FILES_METADATA_FILENAME);
        getMetadata();
    }

    private void getMetadata() {
        try {
            Files.lines(path).forEach(this::parseLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMetadata() throws IOException {
        Files.deleteIfExists(path);
        metadata.forEach(this::writeFileMetadata);
    }

    private void writeFileMetadata(Metadata file) {
        String line = file.filePath + " " + file.modificationDate + " " + file.fileId + " " + file.numChunks;
        try {
            Files.write(path, line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseLine(String line) {
        String[] elements = line.split(" ");
        String filePath = elements[0];
        String modificationDate = elements[1];
        String fileId = elements[2];
        int numChunks = Integer.parseInt(elements[3]);
        metadata.add(new Metadata(filePath, modificationDate, fileId, numChunks));
    }

    private Metadata findFile(String filePath) {
        for (Metadata m : metadata){
            if(Objects.equals(m.filePath, filePath))
                return m;
        }
        return null;
    }

    public void addIfNotExists(String filePath, String date, String fileId, int numChunks) throws IOException {
        Metadata m = findFile(filePath);
        if (m == null) {
            metadata.add(new Metadata(filePath, date, fileId, numChunks));
            saveMetadata();
        }
    }

    public void removeIfExists(String filePath) throws IOException {
        Metadata m = findFile(filePath);
        if(m != null){
            metadata.remove(m);
            saveMetadata();
        }
    }

    public String getFileId(String filePath){
        Metadata m = findFile(filePath);
        if(m != null){
            return m.fileId;
        }
        return null;
    }
}
