package vsue.rpc;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Socket;
import vsue.communication.VSConnection;
import vsue.communication.VSObjectConnection;

public class VSInvocationHandler implements InvocationHandler, Serializable 
{
    private final VSRemoteReference _remoteReference;
    
    public VSInvocationHandler(VSRemoteReference remoteReference)
    {
        if(remoteReference == null)
            throw new IllegalArgumentException("The argument 'remoteReference' must not be null.");
        
        _remoteReference = remoteReference;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Socket socket = new Socket(_remoteReference.getHost(), _remoteReference.getPort());
        
        VSConnection connection = new VSConnection(socket);
        VSObjectConnection objectConnection = new VSObjectConnection(connection);
        
        VSMethodInvocationRequestMessage request = new VSMethodInvocationRequestMessage(_remoteReference.getObjectID(), method.toGenericString(), args);
        
        objectConnection.sendObject(request);
        
        VSMethodInvocationResponseMessage response = (VSMethodInvocationResponseMessage)objectConnection.receiveObject();
        
        Throwable exc = response.getException();
        
        // The invoked method threw an exception
        if(exc != null)
        {
            Type[] allowedExceptionTypes = method.getGenericExceptionTypes();
            
            for (Type allowedExceptionType : allowedExceptionTypes)
            {
                if(!(allowedExceptionType instanceof Class<?>))
                    continue;
                
                if(((Class<?>)allowedExceptionType).isAssignableFrom(exc.getClass()))
                {
                    throw exc;
                }
            }
            
            // TODO: The method is not allowed to throw exceptions of this type. Something went wrong
        }
        
        return response.getResult();
    }
}
