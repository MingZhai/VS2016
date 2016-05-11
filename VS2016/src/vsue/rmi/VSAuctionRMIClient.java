package vsue.rmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VSAuctionRMIClient implements VSAuctionEventHandler
{
    /* The user name provided via command line. */
    private final String userName;
    private Registry _registry;
    private VSAuctionService _service;

    public VSAuctionRMIClient(String userName)
    {
        this.userName = userName;
        
        
    }

    // #############################
    // # INITIALIZATION & SHUTDOWN #
    // #############################
    public void init(String registryHost, int registryPort)
    {
    	
    	try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        try
        {
            _registry = LocateRegistry.getRegistry(registryHost, registryPort);
        }
        catch (RemoteException exc)
        {
            System.err.println("Unable to locate registry. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }

        if (_registry == null)
        {
            System.err.println("Unable to locate registry.");
            System.exit(1);
        }

        try
        {
            _service = (VSAuctionService) _registry.lookup("VSAuctionService");
        }
        catch (RemoteException exc)
        {
            System.err.println("Unable to lookup service. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
        catch (NotBoundException exc)
        {
            System.err.println("Unable to lookup service. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }

        if (_service == null)
        {
            System.err.println("Unable to lookup service.");
            System.exit(1);
        }
    }

    public void shutdown()
    {
        
    }

    // #################
    // # EVENT HANDLER #
    // #################
    @Override
    public void handleEvent(VSAuctionEventType event, VSAuction auction)
    {
        System.out.println(auction.toString() + ": " + event.toString());
    }

    // ##################
    // # CLIENT METHODS #
    // ##################
    public void register(String auctionName, int duration, int startingPrice)
    {
        VSAuction auction = new VSAuction(auctionName, startingPrice);
        
        try
        {
            _service.registerAuction(auction, duration, this);
        }

        catch (VSAuctionException | RemoteException exc)
        {
            System.err.println("Unable to register auction. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
    }

    public void list()
    {
        VSAuction[] auctions = null;

        try
        {
            auctions = _service.getAuctions();
        }
        catch (RemoteException exc)
        {
            System.err.println("Unable to lookup auctions. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }

        if(auctions == null)
        {
            System.err.println("Unable to lookup auctions.");
            System.exit(1);
        }
        
        for (VSAuction a : auctions)
        {
            System.out.println(a.getName());
        }
    }

    public void bid(String auctionName, int price)
    {
        try
        {
            _service.placeBid(userName, auctionName, price, this);
        }
        catch (VSAuctionException | RemoteException exc)
        {
            System.err.println("Unable to place bid. Exception:");
            System.err.println(exc.getMessage());
            System.exit(1);
        }
    }

    // #########
    // # SHELL #
    // #########
    public void shell()
    {
        // Create input reader and process commands
        BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            // Print prompt
            System.out.print("> ");
            System.out.flush();

            // Read next line
            String command = null;
            try
            {
                command = commandLine.readLine();
            }
            catch (IOException ioe)
            {
                break;
            }
            if (command == null)
                break;

            if (command.isEmpty())
                continue;

            // Prepare command
            String[] args = command.split(" ");
            if (args.length == 0)
                continue;

            args[0] = args[0].toLowerCase();

            // Process command
            try
            {
                if (!processCommand(args))
                    break;
            }
            catch (IllegalArgumentException iae)
            {
                System.err.println(iae.getMessage());
            }
        }

        // Close input reader
        try
        {
            commandLine.close();
        }
        catch (IOException ioe)
        {
            // Ignore
        }
    }

    private boolean processCommand(String[] args)
    {
        switch (args[0])
        {
            case "register":
            case "r":
                if (args.length < 3)
                    throw new IllegalArgumentException("Usage: register <auction-name> <duration> [<starting-price>]");
                int duration = Integer.parseInt(args[2]);
                int startingPrice = (args.length > 3) ? Integer.parseInt(args[3]) : 0;
                register(args[1], duration, startingPrice);
                break;
            case "list":
            case "l":
                list();
                break;
            case "bid":
            case "b":
                if (args.length < 3)
                    throw new IllegalArgumentException("Usage: bid <auction-name> <price>");
                int price = Integer.parseInt(args[2]);
                bid(args[1], price);
                break;
            case "exit":
            case "quit":
            case "x":
            case "q":
                return false;
            default:
                throw new IllegalArgumentException("Unknown command: " + args[0]);
        }
        return true;
    }

    // ########
    // # MAIN #
    // ########
    public static void main(String[] args)
    {
        // Check arguments
        if (args.length < 3)
        {
            System.err.println("usage: java " + VSAuctionRMIClient.class.getName() + " <user-name> <registry_host> <registry_port>");
            System.exit(1);
        }

        // Create and execute client
        VSAuctionRMIClient client = new VSAuctionRMIClient(args[0]);
        client.init(args[1], Integer.parseInt(args[2]));
        client.shell();
        client.shutdown();
    }

}
