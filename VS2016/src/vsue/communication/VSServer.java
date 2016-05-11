package vsue.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VSServer
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket socket = new ServerSocket(12348);
        
       
        for (;;)
        {
            Socket client = socket.accept();

            VSServerRunnable runnable = new VSServerRunnable(client);
            
            (new Thread(runnable)).start();
        }

    }
    
    
}
