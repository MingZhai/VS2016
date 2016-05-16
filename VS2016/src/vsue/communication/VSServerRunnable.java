/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vsue.communication;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import vsue.rpc.VSMethodInvocationRequestMessage;
import vsue.rpc.VSMethodInvocationResponseMessage;
import vsue.rpc.VSRemoteObjectManager;


public final class VSServerRunnable implements Runnable
{
    private final Socket _socket;

    public VSServerRunnable(Socket socket)
    {
        _socket = socket;
    }

    @Override
    public void run()
    {
        VSConnection connection = new VSConnection(_socket);
        VSObjectConnection objConnection = new VSObjectConnection(connection);
        
        try
        {
           VSMethodInvocationRequestMessage request = (VSMethodInvocationRequestMessage)objConnection.receiveObject();
           
           VSRemoteObjectManager objectManager = VSRemoteObjectManager.getInstance();
           
           VSMethodInvocationResponseMessage response = null;
           
           try
           {
               Object result = objectManager.invokeMethod(request.getObjectID(), request.getMethod(), request.getArgs());
               
               // The invoked method did not throw
               response = new VSMethodInvocationResponseMessage(result, null);
           }
           catch(Throwable exc)
           {
               // The invoked method did throw
               response = new VSMethodInvocationResponseMessage(null, exc);
           }
           finally
           {
               objConnection.sendObject(response);
           }
        }
        catch(Throwable exc)
        {
            System.err.println("Error while performing request from client: " + _socket.getInetAddress());
            return;
        }
        finally
        {
            try
            {
                _socket.close();
            }
            catch (IOException exc) { }
        }
    }
}
