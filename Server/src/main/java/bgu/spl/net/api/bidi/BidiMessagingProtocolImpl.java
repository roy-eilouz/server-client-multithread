package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;
import bgu.spl.net.api.bidi.Messages.Error;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yahel on 23/12/2018.
 */
public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Message> {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<Message> connections;
    private DataBase DB;
    private User theUser = null;
    private Semaphore locker1;


    public BidiMessagingProtocolImpl(DataBase DB){
        this.DB = DB;
    }

    public void start(int connectionId, Connections<Message> connections){
        this.connectionId = connectionId;
        this.connections = connections;
        shouldTerminate = false;
        DB = DataBase.getInstance();
        locker1 = DB.getLocker();

    }


    public void process(Message message){
        OpcodeType type = message.getOpcodeType();

        // ## making sure the user is logged in ##

        if(type == OpcodeType.LOGOUT || type == OpcodeType.FOLLOW || type == OpcodeType.POST ||
                type == OpcodeType.PM || type == OpcodeType.USERLIST || type == OpcodeType.STAT){
            if (theUser == null){
                connections.send(connectionId, new Error(type));
                return;
            }else{
                Semaphore locker = DB.getLockers().get(theUser.getUsername());

                try {
                    locker.acquire();


                    if(!theUser.isLoggedIn()){
                        connections.send(connectionId, new Error(message.getOpcodeType()));
                        return;
                    }
                    locker.release();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } // ## done making sure the user is logged in  ##

        switch (type){
            case REGISTER:
                processRegister(message, type);
                break;
            case LOGIN:
                processLogin(message, type);
                break;
            case LOGOUT:
                processLogout(type); // only need the message type
                break;
            case FOLLOW:
                processFollow(message, type);
                break;
            case POST:
                processPost(message, type);
                break;
            case PM:
                processPM(message, type);
                break;
            case USERLIST:
                processUserlist(type); // only need the message type
                break;
            case STAT:
                processStat(message, type);
                break;
            case ACK:
            case ERROR:
            case NOTIFICATION:
            case NULL:
                break; // should never get here - means some error
        }
    }




    private void processRegister(Message message, OpcodeType type){
        try {// if a more than one registration is occurring at once, we only want one of them to succeed
            locker1.acquire();

            String username = ((Register)message).getUsername();
            if (DB.getUserMap().containsKey(username)){
                connections.send(connectionId, new Error(type));
            }else{
                DB.getUserMap().put(username,new User(username, ((Register)message).getPassword()));
                DB.getUserSentPostsMap().put(username, new ConcurrentLinkedQueue<>());
                DB.getUserSentPMsMap().put(username, new ConcurrentLinkedQueue<>());
                DB.getUserlist().add(username);
                DB.getLockers().put(username, new Semaphore(1,true));
                connections.send(connectionId, new Ack(type));
            }
            locker1.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } // processRegister


    private void processLogin(Message message, OpcodeType type){
        String username =((Login)message).getUsername();

        if(!DB.getUserMap().containsKey(username)|| !(DB.getUserMap().get(username).getPassword().equals(((Login)message).getPassword())) || DB.getUserMap().get(username).isLoggedIn() || theUser!=null){
            connections.send(connectionId, new Error(type));
        }else{
            Semaphore locker = DB.getLockers().get(username);
                  // we don't want to lose notifications in case a user logs out/in right when a notification is sent
            try {// also, we don't want a user to be able to log in from two different connections at the same time
                locker.acquire();
                if(DB.getUserMap().get(username).isLoggedIn()){
                    connections.send(connectionId, new Error(type));
                }else{
                    theUser = DB.getUserMap().get(username);
                    theUser.logIn();
                    DB.getOnlineUsersMap().put(username, connectionId);
                    connections.send(connectionId, new Ack(type));
                    for(Notification notific: theUser.getPendingMessagesQueue()){// taking care of pending messages
                        connections.send(connectionId, notific);
                        theUser.getPendingMessagesQueue().remove(notific);
                    }
                }

                locker.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    } // processLogin


    private void processLogout(OpcodeType type){


        if (theUser != null && theUser.isLoggedIn()){
            Semaphore locker = DB.getLockers().get(theUser.getUsername());
                 // we don't want to lose notifications in case a user logs out/in right when a notification is sent
            try {
                locker.acquire();
                theUser.logOut();
                DB.getOnlineUsersMap().remove(theUser.getUsername(), connectionId);
                theUser = null;
                connections.send(connectionId, new Ack(type));
                connections.disconnect(connectionId);
                locker.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{

            connections.send(connectionId, new Error(type));
        }
    } // processLogout


    private void processFollow(Message message, OpcodeType type){
        ConcurrentLinkedQueue<String> successfullyProcessedUsers = new ConcurrentLinkedQueue<>();
        Follow followMessage = (Follow)message;
        int numSuccessfullyProcessed = 0;
        if (followMessage.getUnfollow() == 0){ // #follow message#
            for (String username: followMessage.getUserNameList()){
                if(!username.equals(theUser.getUsername())){ // can't follow yourself
                    if(DB.getUserMap().containsKey(username) && theUser.getFollowingNameList().add(username)){
                        DB.getUserMap().get(username).getFollowersNameList().add(theUser.getUsername()); // add this user to other user's followers list
                        successfullyProcessedUsers.add(username);
                        numSuccessfullyProcessed++;
                    }
                }

            }

        }else{ // #unfollow message#
            for (String username: followMessage.getUserNameList()){
                if(!username.equals(theUser.getUsername())){  // can't unfollow yourself
                    if(DB.getUserMap().containsKey(username) && theUser.getFollowingNameList().remove(username)){
                        DB.getUserMap().get(username).getFollowersNameList().remove(theUser.getUsername()); // remove this user from other user's followers list
                        successfullyProcessedUsers.add(username);
                        numSuccessfullyProcessed++;
                    }
                }

            }
        }
        if (numSuccessfullyProcessed == 0){
            connections.send(connectionId, new Error(type));
        }else{
            connections.send(connectionId, new Ack(type, numSuccessfullyProcessed, successfullyProcessedUsers));
        }
    } // processFollow


    private void processPost(Message message, OpcodeType type){

        if (!theUser.isLoggedIn()){
            connections.send(connectionId, new Error(type));
            return;
        }

        Post postMessage = (Post)message;
        DB.getUserSentPostsMap().get(theUser.getUsername()).add(postMessage);
        HashSet<String> taggedUsernames = extractUsernames(postMessage.getContent());


        HashSet<String> usersToSendTo = new HashSet<>(theUser.getFollowersNameList());



        for (String taggedUser: taggedUsernames){   // making sure we won't send more than 1 notification
            if (!usersToSendTo.contains(taggedUser)){
                if(DB.getUserMap().containsKey(taggedUser)){
                    usersToSendTo.add(taggedUser);
                }
            }
        }
        for (String user: usersToSendTo){
            Integer userConnectionId = DB.getOnlineUsersMap().get(user);
            Notification notific = new Notification(true, theUser.getUsername(), postMessage.getContent());
            Semaphore locker = DB.getLockers().get(theUser.getUsername());

            try {   // we don't want to lose notifications in case a user logs out/in right when a notification is sent
                locker.acquire();

                if(DB.getUserMap().get(user).isLoggedIn()) {
                    if(!connections.send(userConnectionId, notific)){
                        DB.getUserMap().get(user).getPendingMessagesQueue().add(notific);
                    }
                }else{ // the user is offline
                    DB.getUserMap().get(user).getPendingMessagesQueue().add(notific);
                }
                locker.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        connections.send(connectionId, new Ack(type));
    } // processPost


    private void processPM(Message message, OpcodeType type){
        PM PMMs = (PM)message;
        String targetUser = PMMs.getUsername();
        if(!DB.getUserMap().containsKey(targetUser)){ // target user isn't registered
            connections.send(connectionId, new Error(type));
        }else{
            Notification notific = new Notification(false, theUser.getUsername(), PMMs.getContent());
            Semaphore locker = DB.getLockers().get(theUser.getUsername());

            try {   // we don't want to lose notifications in case a user logs out/in right when a notification is sent
                locker.acquire();
                if(DB.getUserMap().get(targetUser).isLoggedIn()) {
                    if(!connections.send(DB.getOnlineUsersMap().get(targetUser), notific)){
                        DB.getUserMap().get(targetUser).getPendingMessagesQueue().add(notific);
                    }
                }else{
                    DB.getUserMap().get(targetUser).getPendingMessagesQueue().add(notific);
                }
                locker.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connections.send(connectionId, new Ack(type));
            DB.getUserSentPMsMap().get(PMMs.getUsername()).add(PMMs);
        }
    } // processPM


    private void processUserlist(OpcodeType type){
        ConcurrentLinkedQueue<String> userNameList = DB.getUserlist();
        int numOfUsers = userNameList.size();
        connections.send(connectionId, new Ack(type, numOfUsers, userNameList));
    } // processUserlist


    private void processStat(Message message, OpcodeType type){
        String username = ((Stat)message).getUsername();

        if (!DB.getUserMap().containsKey(username)){
            connections.send(connectionId, new Error(type));
        }else{
            connections.send(connectionId, new Ack(type, DB.getUserSentPostsMap().get(username).size(), //NumPosts
                    DB.getUserMap().get(username).getFollowersNameList().size(), // NumFollowers
                    DB.getUserMap().get(username).getFollowingNameList().size())); //NumFollowing
        }

    } // processStat






    private HashSet<String> extractUsernames(String str){
        HashSet<String> ret = new HashSet<>();
        Pattern pattern = Pattern.compile("@\\w+");

        Matcher matcher = pattern.matcher(str);
        while (matcher.find()){
            ret.add(matcher.group().substring(1)); // remove the "@" and add the username to the Set
        }
        return ret;
    }
    public boolean shouldTerminate(){
        return shouldTerminate;
    }
}
