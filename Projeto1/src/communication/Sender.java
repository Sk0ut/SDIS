package communication;

import communication.message.DeleteMessage;

import java.io.IOException;

/**
 * Created by afonso on 30-03-2016.
 */
public class Sender implements Runnable {
    //Message message = new DeleteMessage(Integer.toString(Peer.localId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5");
    //Message message2 = new RemovedMessage(Integer.toString(Peer.localId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5", "0");
    //Message message3 = new GetChunkMessage(Integer.toString(Peer.localId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5", "0");
    //Message message4 = new PutChunkMessage(Integer.toString(Peer.localId), "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5", "0", "3", "1234567893453453534534".getBytes());

    @Override
    public void run() {
        byte[] message = new DeleteMessage(Integer.toString(Peer.localId),
                "46070d4bf934fb0d4b06d9e2c46e346944e322444900a435d7d9a95e6d7435f5").getBytes();
        try {
            while(true) {
                Thread.sleep(1000);
                ChannelManager.getInstance().send(ChannelManager.ChannelType.CONTROLCHANNEL, message);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
