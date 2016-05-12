package vsue.rpc;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class VSInvocationHandler implements InvocationHandler, Serializable 
{
    public VSInvocationHandler(VSRemoteReference remoteReference)
    {
        
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        return null;
    }
}
