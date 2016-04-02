package general;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

public class MulticastListener extends Observable implements Runnable {

    private MulticastSocket ms;

    public MulticastListener(MulticastSocket ms) {
        this.ms = ms;
    }

    private List<SubProtocolListener> subProtocolListeners = new ArrayList<>();

    public void addSubProtocol(SubProtocolListener subProtocolListener){
        subProtocolListeners.add(subProtocolListener);
    }

    public void dispatchMessage(byte[] message) throws MalformedMessageException, IOException {

        for (SubProtocolListener subProtocolListener : subProtocolListeners) {
            try {
                subProtocolListener.processMessage(message);
                return;
            } catch (MalformedMessageException ignored){}
        }
        throw new MalformedMessageException("Unrecognized protocol name");
    }

    @Override
    public void run() {
        byte[] buf = new byte[255+64*1024];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        while(true){
            try {
                ms.receive(receivePacket);
                byte[] message = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
                setChanged();
                notifyObservers(message);
                //dispatchMessage(message);
            } catch (IOException ignored) {}
        }
    }
}
