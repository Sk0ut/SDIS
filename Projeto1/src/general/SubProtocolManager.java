package general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubProtocolManager {
    private static List<SubProtocol> subProtocols = new ArrayList<>();

    public static void addSubProtocol(SubProtocol subProtocol){
        subProtocols.add(subProtocol);
    }

    public void dispatchMessage(String message) throws MalformedMessageException {
        String[] args = message.split(" ");
        for (SubProtocol subProtocol : subProtocols)
            if (subProtocol.messageOwner(args[0])) {
                args = Arrays.copyOfRange(args, 1, args.length);
                subProtocol.validateArgs(args);
                subProtocol.execute(args);
                return;
            }
        throw new MalformedMessageException("Unrecognized protocol name: " + args[0]);
    }
}
