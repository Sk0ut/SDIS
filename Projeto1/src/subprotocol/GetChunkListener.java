package subprotocol;

import communication.Message;
import communication.MessageParser;
import communication.message.GetChunkMessage;
import general.MalformedMessageException;
import general.MulticastChannel;
import general.Subprotocol;

import java.io.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class GetChunkListener extends Subprotocol implements Observer{
    private static final MessageParser getChunkParser = new GetChunkMessage.Parser();
    private MulticastChannel mc;
    private MulticastChannel mdr;
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public GetChunkListener(String localId, MulticastChannel mc, MulticastChannel mdr) {
        super(localId);
        this.mc = mc;
        this.mdr = mdr;
    }

    public void start() {
        mc.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        Message msg = null;
        try {
            msg = getChunkParser.parse((byte[])arg);
            String fileName = "peer" + getLocalId() + "/" + msg.getFileId() + "-" + msg.getChunkNo() + ".chunk";
            File f = new File(fileName);
            if(f.exists() && !f.isDirectory()){
                ChunkInitiator initiator = new ChunkInitiator(getLocalId(), msg.getFileId(), msg.getChunkNo(), mdr);
                executor.execute(initiator);
            }
        } catch (IOException | MalformedMessageException e) {
            //e.printStackTrace();
        }


    }
}
