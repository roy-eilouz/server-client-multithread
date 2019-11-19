package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Yahel on 23/12/2018.
 */
public class Ack extends Message {



    //for follow or userlist messages ack:
    private Byte[] numOfUsers = new Byte[2];
    private int usersAmount;
    private int numRead = 0;
    private ConcurrentLinkedQueue<String> userNameList;
    private ConcurrentLinkedQueue<Byte> tempUserVector;

    //for STAT message ack:
    private Byte[] numPosts = null;
    private Byte[] numFollowers = null;
    private  Byte[] numFollowing = null;



    private OpcodeType secondOpcodeType;



    public Ack(){
        super();
        opcodeType = OpcodeType.ACK;
        //for follow or userlist message ack:
        userNameList = new ConcurrentLinkedQueue<>();
        tempUserVector = new ConcurrentLinkedQueue<>();
        usersAmount = -1;
    }

    public Ack(OpcodeType code){ // for BidiMessageingProtocolImpl
        super();
        opcodeType = OpcodeType.ACK;
        //for follow or userlist message ack:
        userNameList = new ConcurrentLinkedQueue<>();
        tempUserVector = new ConcurrentLinkedQueue<>();
        usersAmount = -1;


        secondOpcodeType = code;
        }

    // for BidiMessageingProtocolImpl (for un/follow messages  / userlist messages)
    public Ack (OpcodeType code, int numOfUsers, ConcurrentLinkedQueue<String> userNameList){
        super();
        opcodeType = OpcodeType.ACK;
        secondOpcodeType = code;
        usersAmount = numOfUsers;
        this.numOfUsers = shortToByteArray((short)usersAmount);

        this.userNameList = userNameList;
    }

    // for STAT ack
    public Ack (OpcodeType code, int numPosts, int numFollowers, int numFollowing){
        super();
        opcodeType = OpcodeType.ACK;
        secondOpcodeType = code;
        this.numPosts = shortToByteArray((short)numPosts);
        this.numFollowers = shortToByteArray((short)numFollowers);
        this.numFollowing = shortToByteArray((short)numFollowing);
    }






    @Override
    public void decodeByte(Byte nextByte) {
        // do nothing
    }

    public OpcodeType getSecondOpcodeType() {
        return secondOpcodeType;
    }

    public Byte[] getNumOfUsers() {
        return numOfUsers;
    }

    public Byte[] getNumFollowers() {
        return numFollowers;
    }

    public Byte[] getNumFollowing() {
        return numFollowing;
    }

    public ConcurrentLinkedQueue<String> getUserNameList() {
        return userNameList;
    }

    public Byte[] getNumPosts() {
        return numPosts;
    }
}
