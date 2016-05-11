package vsue.communication;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteOrder;

public class VSConnection
{
    private final Socket _socket;
    
    protected VSConnection(Socket socket) 
    {
        if(socket == null)
            throw new IllegalArgumentException("The argument 'socket' must not be null.");
        
        if(!socket.isConnected() || socket.isClosed())
            throw new IllegalArgumentException("The socket must be connected but not closed yet.");
        
        _socket = socket;
    }
    
    public void sendChunk(byte[]chunk) throws IOException
    {
        int length = chunk.length;
        
        int start = 0;
        int end = 4;
        int iter = 1;
        
        if(!ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
        {
            start = 4;
            end = 0;
            iter = -1;
        }
        
        for(int i = start; i != end; i += iter)
        {
            _socket.getOutputStream().write((length >> 8*i) & 0xFF);
        }
        
        _socket.getOutputStream().write(chunk);
        _socket.getOutputStream().flush();
    }
    
    public byte[] receiveChunk() throws IOException
    {
        int length = 0;
        
        int start = 0;
        int end = 4;
        int iter = 1;
        
        if(!ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
        {
            start = 4;
            end = 0;
            iter = -1;
        }
        
        for(int i = start; i != end; i += iter)
        {
            int b = _socket.getInputStream().read();
            
            if(b == -1)
            {
                // End of stream
                throw new IOException();
            }
            
            length |= (b & 0xFF) << 8*i;
        }
        
        byte[] result = new byte[length];
                
        int readBytes = 0;
        
        while(readBytes < length)
        {
            int bytes = _socket.getInputStream().read(result, readBytes, length - readBytes);
            
            if(bytes == 0)
            {
                throw new IOException();
            }
            
            readBytes += bytes;
        }
        
        return result;
    }
}


