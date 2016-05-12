package vsue.rmi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class VSAuctionServiceImpl implements VSAuctionService
{

    private final HashMap<String, VSAuction> _auctions = new HashMap<>();
    private final HashMap<VSAuction, VSAuctionEventHandler> _highest = new HashMap<>();
    private final Object _lock = new Object();
    private final Timer _timer = new Timer();

    @Override
    public void registerAuction(VSAuction auction, int duration, VSAuctionEventHandler handler) throws VSAuctionException
    {
        // Push the auction to the _auctions field in a thread safe manner.
        synchronized (_lock)
        {
            // There is already an auction with the specified name present.
            if (_auctions.containsKey(auction.getName()))
            {
                throw new VSAuctionException("An auction with this name is already registered.");
            }

            _auctions.put(auction.getName(), auction);
        }

        _timer.schedule(new VSAuctionEndTask(this, auction, handler), duration * 1000L);
    }

    @Override
    public VSAuction[] getAuctions()
    {
        Collection<VSAuction> values;

        synchronized (_lock)
        {
            values = _auctions.values();
        }

        return (VSAuction[]) values.toArray();
    }

    @Override
    public boolean placeBid(String userName, String auctionName, int price, VSAuctionEventHandler handler) throws VSAuctionException
    {
        VSAuction auction;
        VSAuctionEventHandler oldHandler = null;

        synchronized (_lock)
        {
            auction = _auctions.get(auctionName);

            if (auction.price < price)
            {
                oldHandler = _highest.put(auction, handler);
                auction.price = price;
            }
        }

        if (oldHandler != null)
        {
            Thread callbackThread = new Thread(new VSAuctionCallbackRunnable(oldHandler, VSAuctionEventType.HIGHER_BID, auction));

            callbackThread.start();

            return true;
        }

        return false;
    }

    private static final class VSAuctionEndTask extends TimerTask
    {

        private final VSAuction _auction;
        private final VSAuctionEventHandler _creatorHandle;
        private final VSAuctionServiceImpl _owner;

        public VSAuctionEndTask(VSAuctionServiceImpl owner, VSAuction auction, VSAuctionEventHandler creatorHandle)
        {
            if (owner == null)
            {
                throw new IllegalArgumentException("The argument 'owner' must not be null.");
            }

            if (auction == null)
            {
                throw new IllegalArgumentException("The argument 'auction' must not be null.");
            }

            if (creatorHandle == null)
            {
                throw new IllegalArgumentException("The argument 'creatorHandle' must not be null.");
            }

            _owner = owner;
            _auction = auction;
            _creatorHandle = creatorHandle;
        }

        @Override
        public void run()
        {
            VSAuctionEventHandler _highestHandler;

            // The auction is over.
            // First remove the auction from the hash table.
            synchronized (_owner._lock)
            {
                _owner._auctions.remove(_auction.getName());

                _highestHandler = _owner._highest.remove(_auction);
            }

            //Now we have the handler of the user with the highest bid.
            if (_highestHandler != null)
            {
                Thread callbackThread = new Thread(new VSAuctionCallbackRunnable(_highestHandler, VSAuctionEventType.AUCTION_WON, _auction));

                callbackThread.start();
            }

            // Inform the auction creator
            Thread callbackThread = new Thread(new VSAuctionCallbackRunnable(_creatorHandle, VSAuctionEventType.AUCTION_END, _auction));

            callbackThread.start();
        }
    }

    private static final class VSAuctionCallbackRunnable implements Runnable
    {

        private final VSAuctionEventHandler _handler;
        private final VSAuctionEventType _type;
        private final VSAuction _auction;

        public VSAuctionCallbackRunnable(VSAuctionEventHandler handler, VSAuctionEventType type, VSAuction auction)
        {
            if (auction == null)
            {
                throw new IllegalArgumentException("The argument 'auction' must not be null.");
            }

            if (handler == null)
            {
                throw new IllegalArgumentException("The argument 'handler' must not be null.");
            }

            if (type == null)
            {
                throw new IllegalArgumentException("The argument 'type' must not be null.");
            }

            _handler = handler;
            _auction = auction;
            _type = type;
        }

        @Override
        public void run()
        {
            try
            {
                _handler.handleEvent(_type, _auction);
            }
            catch (RemoteException exc)
            {
                System.out.println("Error on calling back client:");
                System.out.println(exc);
            }
        }

    }
}
