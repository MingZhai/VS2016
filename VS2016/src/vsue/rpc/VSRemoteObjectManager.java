package vsue.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VSRemoteObjectManager
{
    private static final VSRemoteObjectManager _instance = new VSRemoteObjectManager();
    private final HashMap<Integer, Remote> _exportedObjects = new HashMap<>();
    private int _nextID = 1;
    private final Object _lock = new Object();

    private VSRemoteObjectManager() { }

    public static VSRemoteObjectManager getInstance()
    {
        return _instance;
    }

    public Remote exportObject(Remote object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException("The argument 'object' must not be null.");
        }

        ClassLoader loader = object.getClass().getClassLoader();
        Class<?>[] interfaces = object.getClass().getInterfaces();

        int objectID;

        synchronized (_lock)
        {
            objectID = _nextID++;
        }

        VSRemoteReference remoteReference;

        try
        {
            remoteReference = new VSRemoteReference(Inet4Address.getLocalHost().getHostAddress(), 1234, objectID); //TODO
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(VSRemoteObjectManager.class.getName()).log(Level.SEVERE, null, ex);

            return null; // TODO
        }

        VSInvocationHandler handler = new VSInvocationHandler(remoteReference);

        Remote proxy = (Remote) Proxy.newProxyInstance(loader, interfaces, handler);

        synchronized (_lock)
        {
            _exportedObjects.put(objectID, object);
        }

        return proxy;
    }

    public Object invokeMethod(int objectID, String genericMethodName, Object[] args)
    {
        Object object;

        synchronized (_lock)
        {
            object = _exportedObjects.get(objectID);
        }

        if (object == null)
        {
            // TODO: No object with this id exists.
            return null;
        }
        
        Method[] methods = object.getClass().getMethods();

        for(Method method : methods)
        {
            if(!(method.toGenericString().equals(genericMethodName)))
                continue;
            
            try
            {
                return method.invoke(object, args);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                Logger.getLogger(VSRemoteObjectManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // TODO: No matching method found
        return null;
    }
}
