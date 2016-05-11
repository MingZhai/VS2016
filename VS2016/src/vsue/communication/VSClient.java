package vsue.communication;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import vsue.rmi.VSAuction;


public class VSClient
{
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
    {
        Socket socket = new Socket("localhost", 12348);
        
        VSConnection connection = new VSConnection(socket);
        VSObjectConnection objConnection = new VSObjectConnection(connection);
        
        int i = 6;
        
        objConnection.sendObject(i);
        
        int iBack = (int)objConnection.receiveObject();
        
        if(i == iBack)
        {
            System.out.println("Int ok");
        }
        else
        {
            System.out.println("Int failed");
            
            System.exit(1);
        }
        
        String s = "kfdnvoakb";
        
        objConnection.sendObject(s);
        
        String sBack = (String)objConnection.receiveObject();
        
        if(s.equals(sBack))
        {
            System.out.println("String ok");
        }
        else
        {
            System.out.println("String failed");
            
            System.exit(1);
        }
        
        float[] fa = new float[] { 1.0f, 2.0f, 3.567f };
        
        objConnection.sendObject(fa);
        
        float[] faBack = (float[])objConnection.receiveObject();
        
        for(int j = 0; j < fa.length; j++)
        {
            if(faBack.length <= j|| faBack[j] != fa[j])
            {
                System.out.println("Float Array failed");
            
                System.exit(1);
            } 
        }
        System.out.println("Float Array  ok");
        
        VSAuction auction = new VSAuction("name", 23);
        
        objConnection.sendObject(auction);
        
        VSAuction auctionBack = (VSAuction)objConnection.receiveObject();
        
        if(auctionBack == null || !auctionBack.getName().equals(auction.getName()) || auctionBack.getPrice() != auction.getPrice())
        {
            System.out.println("VSAuction failed");
            
            System.exit(1);
        }
        else
        {
            System.out.println("VSAuction ok");
        }
    }
}
