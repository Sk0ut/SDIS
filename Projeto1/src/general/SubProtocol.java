package general;

/**
 * Created by afonso on 26-03-2016.
 */
public abstract class SubProtocol {
    public abstract boolean messageOwner(String id);
    public abstract void validateArgs(String[] args) throws MalformedMessageException;
    public abstract void execute(String[] args);
}
