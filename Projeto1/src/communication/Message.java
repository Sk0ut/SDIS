package communication;

import java.util.StringJoiner;

/**
 * Created by Flávio on 29/03/2016.
 */
public abstract class Message {
    private String messageType;
    private String version;
    private String senderId;
    private String fileId;
    private String chunkNo;
    private String replicationDeg;
    private byte [] body;

    public Message(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDeg, byte [] body) {
        setMessageType(messageType);
        setVersion(version);
        setSenderId(senderId);
        setFileId(fileId);
        setChunkNo(chunkNo);
        setReplicationDeg(replicationDeg);
        setBody(body);
    }

    public byte [] getBytes() {
        byte [] headerBytes = getHeader().getBytes();
        byte [] bytes = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
        System.arraycopy(body, 0, bytes, headerBytes.length, body.length);

        return bytes;
    }

    public String getHeader() {
        StringJoiner sj = new StringJoiner(" ", "", "\n\n");
        sj.add(messageType)
                .add(version)
                .add(senderId)
                .add(fileId)
                .add(chunkNo)
                .add(replicationDeg);

        return sj.toString();
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public String getReplicationDeg() {
        return replicationDeg;
    }

    public void setReplicationDeg(String replicationDeg) {
        this.replicationDeg = replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
