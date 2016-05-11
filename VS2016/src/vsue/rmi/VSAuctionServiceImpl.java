package vsue.rmi;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VSAuctionServiceImpl implements VSAuctionService
{
    private final ArrayList<InternalAuction> _auctions = new ArrayList<>();
    
    @Override
    public synchronized void registerAuction(VSAuction auction, int duration, VSAuctionEventHandler handler) throws VSAuctionException, RemoteException
    {
        for(InternalAuction element : _auctions)
            if(element.getAuction().getName().equals(auction.getName()))
                throw new VSAuctionException("An auction with this name is already registered.");
        
        InternalAuction intAuc = new InternalAuction(auction, handler, duration);
        
        _auctions.add(intAuc);
    }

    @Override
    public synchronized VSAuction[] getAuctions() throws RemoteException
    {
        VSAuction[] result = new VSAuction[_auctions.size()];
        
        for(int i = 0; i < _auctions.size(); i++)
        {
            result[i] = _auctions.get(i).getAuction();
        }
        
        return result;
    }

    @Override
    public  boolean placeBid(String userName, String auctionName, int price, VSAuctionEventHandler handler) throws VSAuctionException, RemoteException
    {
    	InternalAuction auction = null;
    	synchronized(this)
    	{
        
        
        for(int i = 0; i < _auctions.size(); i++)
            if(_auctions.get(i).getAuction().getName().equals(auctionName))
                auction = _auctions.get(i);
        
        if(auction == null)
            throw new VSAuctionException("An auction with the specified name does not exist.");
    	}
        AuctionUser user = auction.getUser(userName);
        
        if(user == null)
        {
            user = auction.addUser(userName, price);
        }
        else
        {
            user.setBiddenAmount(price);
        }
        
        user.addEventHandler(handler);
        
        return auction.getHighest() == user;
    }
    
    private class AuctionUser
    {
        private final String _name;
        private final ArrayList<VSAuctionEventHandler> _handlers = new ArrayList<>();
        private final InternalAuction _auction;
        
        private int _biddenAmount;
        
        public AuctionUser(InternalAuction auction, String name, int biddenAmount)
        {
            if(auction == null)
                throw new IllegalArgumentException("The argument 'auction' must not be null.");
            
            if(name == null)
                throw new IllegalArgumentException("The argument 'name' must not be null.");
            
            if(biddenAmount <= 0)
                throw new IllegalArgumentException("The argument 'biddenAmount' must neither be zero nor negative.");
            
            _name = name;
            _biddenAmount = biddenAmount;
            _auction = auction;
        }
        
        public String getName() 
        {
            return _name;
        }
        
        public int getBiddenAmount()
        {
            return _biddenAmount;
        }
        
        public void setBiddenAmount(int value) throws RemoteException // TODO: It would be clearer to catch the exception
        {
            if(value <= 0)
                throw new IllegalArgumentException("The argument 'value' must neither be zero nor negative.");
            
            if(value <= _biddenAmount)
                return;
            
            _biddenAmount = value;
            
            _auction.tryOverbidHighest(this);
        }
        
        public void addEventHandler(VSAuctionEventHandler handler)
        {
            if(handler == null)
                throw new IllegalArgumentException("The argument 'handler' must not be null.");
            
            if(_handlers.contains(handler))
                return;
            
            _handlers.add(handler);
        }
        
        public void removeEventHandler(VSAuctionEventHandler handler)
        {
            if(handler == null)
                throw new IllegalArgumentException("The argument 'handler' must not be null.");
            
            _handlers.remove(handler);
        }
        
        // The user won the auction.
        public void won() throws RemoteException
        {
            for(VSAuctionEventHandler handler : _handlers)
            {
                handler.handleEvent(VSAuctionEventType.AUCTION_WON, _auction.getAuction());
            }
        }
        
        // The user was overbidden.
        public void overbid() throws RemoteException // TODO: It would be clearer to catch the exception
        {
            for(VSAuctionEventHandler handler : _handlers)
            {
                handler.handleEvent(VSAuctionEventType.HIGHER_BID, _auction.getAuction());
            }
        }
    }
    
    private class InternalAuction
    {
        private final VSAuction _auction;
        private final int _duration;
        private final VSAuctionEventHandler _handler;
        private final ArrayList<AuctionUser> _users = new ArrayList<>();
        
        private AuctionUser _highest = null;
        
        private final Timer timer = new Timer();
        
        public InternalAuction(VSAuction auction, VSAuctionEventHandler handler, int duration)
        {
            if(auction == null)
                throw new IllegalArgumentException("The argument 'auction' must not be null.");
            
            if(handler == null)
                throw new IllegalArgumentException("The argument 'handler' must not be null.");
            
            if(duration < 0)
                throw new IllegalArgumentException("The argument 'duration' must not be negative.");
            
            _auction = auction;
            _handler = handler;
            _duration = duration;
            
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        end(); // We are not allowed to rethrow the possible exception here. See TODO
                    }
                    catch (RemoteException ex)
                    {
                        System.err.println("Error in remote connection: ");
                        System.err.println(ex.getMessage());
                    }
                }
            }, (long)duration * 1000);
        }
        
        public VSAuction getAuction()
        {
            return _auction;
        }
        
        public int getDuration()
        {
            return _duration;
        }
        
        public List<AuctionUser> getUsers()
        {
            return Collections.unmodifiableList(_users);
        }
        
        public AuctionUser getUser(String name)
        {
            if(name == null)
                throw new IllegalArgumentException("The argument 'name' must not be null");
            
            for(AuctionUser user : _users)
                if(user.getName().equals(name))
                    return user;
            
            return null;
        }
        
        public AuctionUser addUser(String name, int biddenAmount) throws RemoteException // TODO: It would be clearer to catch the exception
        {
            if(name == null)
                throw new IllegalArgumentException("The argument 'name' must not be null.");
            
            if(getUser(name) != null)
                throw new IllegalArgumentException("A user with the specified name is already present.");
            
            AuctionUser user = new AuctionUser(this, name, biddenAmount);
            
            _users.add(user);
            
            tryOverbidHighest(user);
            
            return user;
        }
        
        public AuctionUser getHighest()
        {
            return _highest;
        }
        
        void tryOverbidHighest(AuctionUser user) throws RemoteException // TODO: It would be clearer to catch the exception
        {
            if(user.getBiddenAmount() > _auction.getPrice() || 
               (_highest == null && user.getBiddenAmount() == _auction.getPrice()))
            {
                AuctionUser overbidden = _highest;
                
                _highest = user;
                
                _auction.price = user.getBiddenAmount();
                
                if(overbidden != null)
                {
                    overbidden.overbid();
                }
            }
        }
        
        private void end() throws RemoteException // TODO: It would be clearer to catch the exception
        {
            
            if(_highest != null)
            {
                _highest.won();
            }
            
            _handler.handleEvent(VSAuctionEventType.AUCTION_END, _auction);
        }
    }
}
