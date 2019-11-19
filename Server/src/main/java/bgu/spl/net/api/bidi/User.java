package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.Notification;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Yahel on 28/12/2018.
 */
public class User {
    private String username;
    private String password;
    private boolean loggedIn;
    private HashSet<String> followingNameList;
    private HashSet<String> followersNameList;
    private ConcurrentLinkedQueue<Notification> pendingMessagesQueue;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
        followingNameList = new HashSet<>();
        followersNameList = new HashSet<>();
        pendingMessagesQueue = new ConcurrentLinkedQueue<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void logIn() {
        loggedIn = true;
    }

    public void logOut(){
        loggedIn = false;

    }

    public boolean isLoggedIn() {

        return loggedIn;
    }

    public HashSet<String> getFollowingNameList() {
        return followingNameList;
    }

    public HashSet<String> getFollowersNameList() {
        return followersNameList;
    }

    public ConcurrentLinkedQueue<Notification> getPendingMessagesQueue() {
        return pendingMessagesQueue;
    }
}
