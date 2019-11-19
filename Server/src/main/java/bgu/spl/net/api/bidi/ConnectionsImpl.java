package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Yahel on 22/12/2018.
 */
public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> handlersMap;

    public ConnectionsImpl(){
        handlersMap = new ConcurrentHashMap<>();
    }

    public boolean send(int connectionId, T msg){
        if(handlersMap.containsKey(connectionId)){
            if(msg != null){
                handlersMap.get(connectionId).send(msg);
                return true;
            } else return true;
        } else {
            return false; // we'll send the message to the user next time he/she logs in
        }
    }

    public void broadcast(T msg){
        for(Integer userId : handlersMap.keySet()) {
            handlersMap.get(userId).send(msg);
        }
    }

    public void disconnect(int connectionId){

        handlersMap.remove(connectionId);
    }

    public ConcurrentHashMap<Integer, bgu.spl.net.srv.bidi.ConnectionHandler<T>> getHandlersMap() {
        return handlersMap;
    }

    public void addConnection(ConnectionHandler handler, int cid){
        handlersMap.put(cid, handler);
    }


}
