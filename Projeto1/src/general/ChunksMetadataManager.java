package general;

import java.io.File;
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
public class ChunksMetadataManager {

    private static final String CHUNKS_METADATA_FILENAME = "chunks.metadata";

    private class Metadata {
        String fileId;
        String chunkNo;
        String repDegree;
        List<String> peers;

        Metadata(String fileId, String chunkNo, String repDegree, List<String> peers) {
            this.fileId = fileId;
            this.chunkNo = chunkNo;
            this.repDegree = repDegree;
            this.peers = peers;
        }
    }

    private List<Metadata> metadata;
    private Path path;
    private static ChunksMetadataManager instance = null;

    private ChunksMetadataManager(){
        metadata = new ArrayList<>();
    }

    public static ChunksMetadataManager getInstance(){
        if (instance == null)
            instance = new ChunksMetadataManager();
        return instance;
    }

    public void init(String localId) throws IOException {
        this.path = FileSystems.getDefault().getPath("peer" + localId, CHUNKS_METADATA_FILENAME);
        File fileMetadata = path.toFile();
        if(!fileMetadata.exists())
            fileMetadata.createNewFile();
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

    private void writeFileMetadata(Metadata file) {
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

    private Metadata findChunk(String fileId, String chunkNo){
        for(Metadata f : metadata){
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
        metadata.add(new Metadata(fileId, chunkNo, repDegree, senders));
    }

    public void removeFileIfExists(String fileId, String chunkNo) throws IOException {
        Metadata f = findChunk(fileId, chunkNo);
        if (f != null){
            metadata.remove(f);
            saveMetadata();
        }
    }

    public List<String> getChunksFromFile(String fileId) {
        List<String> ret = new ArrayList<>();
        for(Metadata m : metadata)
            if (Objects.equals(m.fileId, fileId))
                ret.add(m.chunkNo);
        return ret;
    }

    public void removePeerIfExists(String fileId, String chunkNo, String peer) throws IOException {
        Metadata f = findChunk(fileId, chunkNo);
        if (f != null){
            if(f.peers.contains(peer)) {
                f.peers.remove(peer);
                saveMetadata();
            }
        }
    }

    public void addPeerIfNotExists(String fileId, String chunkNo, String peer) throws IOException {
        Metadata f = findChunk(fileId, chunkNo);
        if (f == null){
            if(!f.peers.contains(peer)) {
                f.peers.add(peer);
                saveMetadata();
            }
        }
    }

    public void addFileIfNotExists(String fileId, String chunkNo, String replicationDeg, List<String> peers) throws IOException {
        Metadata f = findChunk(fileId, chunkNo);
        if (f == null){
            metadata.add(new Metadata(fileId, chunkNo, replicationDeg, peers));
            saveMetadata();
        }
    }
}
