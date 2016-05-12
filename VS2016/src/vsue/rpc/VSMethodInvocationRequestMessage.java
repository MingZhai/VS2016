package vsue.rpc;

import java.io.Serializable;


public final class VSMethodInvocationRequestMessage implements Serializable
{
    private final int _objectID;
    private final String _method;
    private final Object[] _args;
    
    //TODO: Do we need seq nums?
    
    public VSMethodInvocationRequestMessage(int objectID, String method, Object[] args)
    {
        _objectID = objectID;
        _method = method;
        _args = args;
    }
    
    public int getObjectID()
    {
        return _objectID;
    }
    
    public String getMethod()
    {
        return _method;
    }
    
    public Object[] getArgs()
    {
        return _args;
    }
}
