package vsue.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class VSObjectConnection
{
    private final VSConnection _connection;

    public VSObjectConnection(VSConnection connection)
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("The argument 'connection' must not be null.");
        }

        _connection = connection;
    }

    public void sendObject(Serializable object) throws IOException
    {
        if (object == null)
        {
            throw new IllegalArgumentException("The argument 'object' must not be null.");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try
        {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            byte[] result = bos.toByteArray();

            _connection.sendChunk(result);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException exc)
            {
            }

            try
            {
                bos.close();
            }
            catch (IOException exc)
            {
            }
        }
    }

    public Serializable receiveObject() throws IOException, ClassNotFoundException
    {
        byte[] bytes;
        
        do
        {
            bytes = _connection.receiveChunk();
        } while(bytes == null || bytes.length == 0);


        //System.out.println("received bytes:");
        //for(byte b : bytes)
        //{
        //    System.out.println(b);
        //}
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try
        {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();


            return (Serializable) o;
        }
        finally
        {
            try
            {
                bis.close();
            }
            catch (IOException exc)
            {
            }
            try
            {
                if (in != null)
                {
                    in.close();
                }

            }
            catch (IOException exc)
            {
            }
        }
    }
}
