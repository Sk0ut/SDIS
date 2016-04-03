package general;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Afonso on 31/03/2016.
 */
public class FilesMetadataManager {

    private class Entry {
        String filePath;
        String modificationDate;
        String fileId;
        int numChunks;

        Entry(String filePath, String date, String fileId, int numChunks) {
            this.filePath = filePath;
            this.modificationDate = date;
            this.fileId = fileId;
            this.numChunks = numChunks;
        }

        public String toString() {
            return filePath + "|" + modificationDate + "|" + fileId + "|" + numChunks;
        }
    }

    private static FilesMetadataManager instance = null;
    private static final String FILES_METADATA_FILENAME = "files.metadata";
    private List<Entry> metadata;
    private File file;
    private FilesMetadataManager(){
        metadata = new ArrayList<>();
    }

    public static FilesMetadataManager getInstance(){
        if(instance == null)
            instance = new FilesMetadataManager();
        return instance;
    }

    public void init(String localId) throws IOException {
        Path path = FileSystems.getDefault().getPath("peer" + localId, FILES_METADATA_FILENAME);
        file = path.toFile();

        if (!file.exists()) {
            file.createNewFile();
        }
        load();
    }

    private void load() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
            String line;
            while((line = br.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for(Entry m : metadata){
            bw.write(m.toString());
            bw.newLine();
        }
        bw.close();
    }

    private void parseLine(String line) {
        String[] elements = line.split("\\|");
        String filePath = elements[0];
        String modificationDate = elements[1];
        String fileId = elements[2];
        int numChunks = Integer.parseInt(elements[3]);
        metadata.add(new Entry(filePath, modificationDate, fileId, numChunks));
    }

    private Entry findFile(String filePath) {
        for (Entry m : metadata){
            if(Objects.equals(m.filePath, filePath))
                return m;
        }
        return null;
    }

    public void addIfNotExists(String filePath, String date, String fileId, int numChunks) throws IOException {
        Entry m = findFile(filePath);
        if (m == null) {
            metadata.add(new Entry(filePath, date, fileId, numChunks));
            save();
        }
    }

    public void removeIfExists(String filePath) throws IOException {
        Entry m = findFile(filePath);
        if(m != null){
            metadata.remove(m);
            save();
        }
    }

    public String getFileId(String filePath){
        Entry m = findFile(filePath);
        if(m != null){
            return m.fileId;
        }
        return null;
    }
}
