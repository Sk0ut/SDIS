package general;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Afonso on 31/03/2016.
 */
public class MetadataFile {
    class File {
        String fileId;
        String chunkNo;
        String repDegree;
        List<String> peers;

        public File(String fileId, String chunkNo, String repDegree, List<String> peers) {
            this.fileId = fileId;
            this.chunkNo = chunkNo;
            this.repDegree = repDegree;
            this.peers = peers;
        }
    }

    private List<File> metadata;
    private Path path;
    private static MetadataFile instance = null;

    private MetadataFile(){
        metadata = new ArrayList<>();
    }

    public static MetadataFile getInstance(){
        if (instance == null)
            instance = new MetadataFile();
        return instance;
    }

    public void init(String localId){
        this.path = FileSystems.getDefault().getPath("peer" + localId, ".metadata");
        getMetadata();
    }

    private void saveMetadata() throws IOException {
        Files.deleteIfExists(path);
        metadata.forEach(this::writeFileMetadata);
    }

    private void getMetadata(){
        try {
            Files.lines(path).forEach(this::parseLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFileMetadata(File file) {
        String line = file.fileId + " " + file.chunkNo + " " + file.repDegree;
        for (String s : file.peers){
            line += " " + s;
        }
        try {
            Files.write(path, line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File findChunk(String fileId, String chunkNo){
        for(File f : metadata){
            if (Objects.equals(f.fileId, fileId) && Objects.equals(f.chunkNo, chunkNo))
                return f;
        }
        return null;
    }

    private void parseLine(String line){
        String[] elements = line.split(" ");
        String fileId = elements[0];
        String chunkNo = elements[1];
        String repDegree = elements[2];
        List<String> senders = new ArrayList<>();
        senders.addAll(Arrays.asList(elements).subList(3, elements.length));
        metadata.add(new File(fileId, chunkNo, repDegree, senders));
    }

    public void removeFileIfExists(String fileId, String chunkNo) throws IOException {
        File f = findChunk(fileId, chunkNo);
        if (f != null){
            metadata.remove(f);
            saveMetadata();
        }
    }

    public void removePeerIfExists(String fileId, String chunkNo, String peer) throws IOException {
        File f = findChunk(fileId, chunkNo);
        if (f != null){
            if(f.peers.contains(peer)) {
                f.peers.remove(peer);
                saveMetadata();
            }
        }
    }

    public void addPeerIfNotExists(String fileId, String chunkNo, String peer) throws IOException {
        File f = findChunk(fileId, chunkNo);
        if (f == null){
            if(!f.peers.contains(peer)) {
                f.peers.add(peer);
                saveMetadata();
            }
        }
    }

    public void addFileIfNotExists(String fileId, String chunkNo, String replicationDeg, List<String> peers) throws IOException {
        File f = findChunk(fileId, chunkNo);
        if (f == null){
            metadata.add(new File(fileId, chunkNo, replicationDeg, peers));
            saveMetadata();
        }
    }
}
