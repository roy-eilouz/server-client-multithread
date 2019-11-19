package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Created by Yahel on 28/12/2018.
 */
public class DataBase {

    private ConcurrentHashMap<String, User> userMap;
    private ConcurrentLinkedQueue<String> userlist;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<PM>> userSentPMsMap;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Post>> userSentPostsMap;
    private ConcurrentLinkedQueue<Post> allPosts;
    private ConcurrentHashMap<String, Integer> onlineUsersMap;
    private ConcurrentHashMap<String, Semaphore> lockers;
    private Semaphore locker;


    private static class singletonHolder{
        private static DataBase DB = new DataBase();
    }

    public static DataBase getInstance() {

        return singletonHolder.DB;
    }

    private DataBase(){
        userMap = new ConcurrentHashMap<>();
        userSentPMsMap = new ConcurrentHashMap<>();
        userSentPostsMap = new ConcurrentHashMap<>();
        allPosts = new ConcurrentLinkedQueue<>();
        onlineUsersMap = new ConcurrentHashMap<>();
        userlist = new ConcurrentLinkedQueue<>();
        lockers = new ConcurrentHashMap<>();
        locker = new Semaphore(1, true);
    }

    public ConcurrentHashMap<String, User> getUserMap() {
        return userMap;
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<PM>> getUserSentPMsMap() {
        return userSentPMsMap;
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<Post>> getUserSentPostsMap() {
        return userSentPostsMap;
    }

    public ConcurrentLinkedQueue<Post> getAllPosts() {
        return allPosts;
    }

    public ConcurrentHashMap<String, Integer> getOnlineUsersMap() {
        return onlineUsersMap;
    }

    public ConcurrentLinkedQueue<String> getUserlist() {
        return userlist;
    }

    public ConcurrentHashMap<String, Semaphore> getLockers() {
        return lockers;
    }

    public Semaphore getLocker() {
        return locker;
    }
}
