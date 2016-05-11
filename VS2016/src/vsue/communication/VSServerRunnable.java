/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vsue.communication;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;


public final class VSServerRunnable implements Runnable
{
    private final Socket _socket;

    public VSServerRunnable(Socket socket)
    {
        _socket = socket;
    }

    public void run()
    {
        VSConnection connection = new VSConnection(_socket);
        VSObjectConnection objConnection = new VSObjectConnection(connection);
        
        for(;;)
        {
            try
            {
                Serializable obj = objConnection.receiveObject();
                
                objConnection.sendObject(obj);
            }
            catch (IOException | ClassNotFoundException ex)
            {
                return;
            }
        }
    }
}
