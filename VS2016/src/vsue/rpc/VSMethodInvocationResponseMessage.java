package vsue.rpc;

import java.io.Serializable;


public class VSMethodInvocationResponseMessage implements Serializable
{
    private final Object _result;
    private final Throwable _exception;
    
    public VSMethodInvocationResponseMessage(Object result, Throwable exception)
    {
        _result = result;
        _exception = exception;
    }
    
    public Object getResult()
    {
        return _result;
    }
    
    public Throwable getException()
    {
        return _exception;
    }
}
