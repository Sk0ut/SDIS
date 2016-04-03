package general;


public class ChunkIdentifier{
    private String fileId;
    private String chunkNo;

    public ChunkIdentifier(String fileId, String chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(String chunkNo) {
        this.chunkNo = chunkNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj == this)
            return true;

        if (!(obj instanceof ChunkIdentifier))
            return false;

        ChunkIdentifier rhs = (ChunkIdentifier) obj;

        return (this.getFileId().equals(rhs.getFileId())) && (this.getChunkNo().equals(rhs.getChunkNo()));
    }
}
