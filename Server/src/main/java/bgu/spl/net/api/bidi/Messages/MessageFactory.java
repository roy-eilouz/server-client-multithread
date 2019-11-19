package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;


/**
 * Created by Yahel on 24/12/2018.
 */
public class MessageFactory {
    public MessageFactory(){

    }

    public static Message create(OpcodeType type){
        if (type == OpcodeType.REGISTER) {
            return new Register();
        }else if (type == OpcodeType.LOGIN){
            return new Login();
        }else if (type == OpcodeType.LOGOUT){
            return new Logout();
        }else if (type == OpcodeType.FOLLOW){
            return new Follow();
        }else if (type == OpcodeType.POST){
            return new Post();
        }else if (type == OpcodeType.PM){
            return new PM();
        }else if (type == OpcodeType.USERLIST){
            return new Userlist();
        }else if (type == OpcodeType.STAT){
            return new Stat();
        }else if (type == OpcodeType.NOTIFICATION){
            return new Notification();
        }else if (type == OpcodeType.ACK){
            return new Ack();
        }else if (type == OpcodeType.ERROR){
            return new Error();
        }else return null; // some error
    }
}

