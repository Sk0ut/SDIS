package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.ChunkMessage;
import communication.message.GetChunkMessage;
import general.Logger;
import general.MalformedMessageException;
import general.MulticastChannel;
import general.Subprotocol;

import java.io.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by afonso on 26-03-2016.
 */
public class GetChunkListener extends Subprotocol implements Observer{
    private static final MessageParser parser = new GetChunkMessage.Parser();
    private MulticastChannel mc;
    private MulticastChannel mdr;

    public GetChunkListener(String localId, MulticastChannel mc, MulticastChannel mdr) {
        super(localId);
    }

    public void start() {
        mc.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        Message msg = null;
        try {
            msg = parser.parse((byte[])arg);
            String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";
            File f = new File(fileName);
            if(f.exists() && !f.isDirectory()){
                FileInputStream in = new FileInputStream(fileName);
                byte[] buffer =  new byte[64*1000];
                in.read(buffer, 0, buffer.length);
                Logger.getInstance().printLog(msg.getHeader());
                try {
                    Thread.sleep((long) (Math.random() * 400));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] message = new ChunkMessage(getLocalId(), msg.getFileId(), msg.getChunkNo(),
                        buffer).getBytes();
                mdr.send(message);
            }
        } catch (IOException | MalformedMessageException e) {
            //e.printStackTrace();
        }
    }
}
