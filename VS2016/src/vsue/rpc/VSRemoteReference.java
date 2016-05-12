package vsue.rpc;

import java.io.Serializable;

public class VSRemoteReference implements Serializable
{
    private final String _host;
    private final int _port;
    private final int _objectID;
    
    public VSRemoteReference(String host, int port, int objectID)
    {
        _host = host;
        _port = port;
        _objectID = objectID;
    }
    
    public String getHost()
    {
        return _host;
    }
    
    public int getPort()
    {
        return _port;
    }
    
    public int getObjectID()
    {
        return _objectID;
    }
}
