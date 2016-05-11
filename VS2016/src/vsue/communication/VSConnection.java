package vsue.communication;
import java.io.IOException;
import java.net.Socket;

public class VSConnection
{
    private final Socket _socket;
    
    protected VSConnection(Socket socket) 
    {
        if(socket == null)
            throw new IllegalArgumentException("The argument 'socket' must not be null.");
        
        if(!socket.isConnected() || socket.isClosed())
            throw new IllegalArgumentException("The socket must connected but not closed yet.");
        
        _socket = socket;
    }
    
    public void sendChunk(byte[]chunk) throws IOException // Todo
    {
        _socket.getOutputStream().write(chunk);
        
        _socket.getOutputStream().flush();
    }
    
    public byte[] receiveChunk() throws IOException //Todo
    {
        byte[] result = new byte[_socket.getInputStream().available()];
                
        _socket.getInputStream().read(result);
        
        return result;
    }
}


