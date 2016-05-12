package vsue.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

public class VSAuctionRMIServer
{
    public static void main(String [] args) throws AlreadyBoundException
    {
        try
        {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        catch(RemoteException exc)
        {
            System.err.println("Unable to create registry. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
        
        
        VSAuctionServiceImpl serviceImplementation = new VSAuctionServiceImpl();
        VSAuctionService stub = null;
        
        try
        {
            stub = (VSAuctionService) UnicastRemoteObject.exportObject(serviceImplementation, 0);
        }
        catch (RemoteException exc)
        {
            System.err.println("Unable to export service. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
        
        if(stub == null)
        {
            System.err.println("Unable to export service.");
            System.exit(1);
        }
        
        RemoteServer.setLog(System.out);
        
        Registry registry = null;
        
        try
        {
            registry = LocateRegistry.getRegistry();
        }
        catch (RemoteException exc)
        {
            System.err.println("Unable to get registry. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
        
        if(registry == null)
        {
            System.err.println("Unable to get registry.");
            System.exit(1);
        }
        
        try
        {
            registry.bind("VSAuctionService", stub);
        }
        catch (RemoteException exc)
        {
            System.err.println("Unable to register stub. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
        
        System.out.println("Service successfully exported.");
    }
}
