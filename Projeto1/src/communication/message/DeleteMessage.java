package communication.message;

import communication.Message;

/**
 * Created by Flávio on 29/03/2016.
 */
public class DeleteMessage extends Message {

    public DeleteMessage(String senderId, String fileId) {
        super("DELETE", "1.0", senderId, fileId, "", "", new byte[]{});
    }
}
