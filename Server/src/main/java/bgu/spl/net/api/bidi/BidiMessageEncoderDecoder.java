package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Messages.Ack;
import bgu.spl.net.api.bidi.Messages.Error;
import bgu.spl.net.api.bidi.Messages.MessageFactory;
import bgu.spl.net.api.bidi.Messages.Notification;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * Created by Yahel on 23/12/2018.
 */
public class BidiMessageEncoderDecoder implements MessageEncoderDecoder<Message> {

    private Message message = null;
    private Message result = null;
    private final ByteBuffer opCode = ByteBuffer.allocate(2);


    @Override
    public Message decodeNextByte(byte nextByte) {
        if (message == null){   // not done decoding the opcode
            result = null;
            opCode.put(nextByte);
            if(!opCode.hasRemaining()){ // we'll pass here once opCode is full - once we've finsihed loading both bytes
                byte[] toShort = opCode.array();
                short OpcodeNumber = (short)((toShort[0] & 0xff) << 8);
                OpcodeNumber += (short)(toShort[1] & 0xff);
                message = MessageFactory.create(OpcodeType.values()[OpcodeNumber]);
                opCode.clear();
                if (OpcodeNumber == 3 || OpcodeNumber == 7){
                    result = message;
                    message = null;
                }
            }
        }else{
            message.decodeByte(nextByte);
            if (message.isDoneDecoding()){
                result = message;
                message = null;
            }
        }
        return result;
    }



    @Override
    public byte[] encode(Message message) {
        Vector<Byte> temp = new Vector<>();
        OpcodeType type = message.getOpcodeType();
        Byte[] opCodeByte = message.shortToByteArray((short)type.ordinal());
        temp.add(opCodeByte[0]);
        temp.add(opCodeByte[1]);
        switch (type){
            case NOTIFICATION:
                return encodeNotification((Notification)message, temp);

            case ACK:
                return encodeAck((Ack)message, temp);

            case ERROR:
                Error err = (Error)message;
                Byte[] errOpCodeByte = message.shortToByteArray((short)err.getOpCode().ordinal());
                temp.add(errOpCodeByte[0]);
                temp.add(errOpCodeByte[1]);
                break;
        }

        return message.toByteArray(temp);
    }


    private byte[] encodeNotification(Notification notifiMessage, Vector<Byte> temp) {
        if (notifiMessage.isPublicPost()){
            temp.add((byte)1);
        }else{
            temp.add((byte)'\0');
        }

        byte[] userBytes = notifiMessage.getPostingUser().getBytes();
        for(byte bait: userBytes){
            temp.add(bait);
        }
        temp.add(((byte)'\0'));
        byte[] contentBytes = notifiMessage.getContent().getBytes();

        for(byte bait: contentBytes){
            temp.add(bait);
        }
        temp.add(((byte)'\0'));

        return notifiMessage.toByteArray(temp);
    }

    private byte[] encodeAck(Ack ack, Vector<Byte> temp) {
        OpcodeType ackType = ack.getSecondOpcodeType();
        Byte[] ackOpCodeByte = ack.shortToByteArray((short)ackType.ordinal());
        temp.add(ackOpCodeByte[0]);
        temp.add(ackOpCodeByte[1]);
        switch (ackType){
            case LOGIN: case LOGOUT: case PM: case POST:
                break;
            case FOLLOW: case USERLIST:

                temp.add(ack.getNumOfUsers()[0]);
                temp.add(ack.getNumOfUsers()[1]);
                int counter = 1;
                for(String username: ack.getUserNameList()){
                    byte[] usersBytes;
                    if (counter<ack.getUserNameList().size()){
                        usersBytes = (username+"\0").getBytes();
                    }else{
                        usersBytes = username.getBytes();
                    }
                    for(byte bait: usersBytes){
                        temp.add(bait);
                    }
                    counter++;
                }
                temp.add((byte)'\0');
                break;
            case STAT:
                temp.add(ack.getNumPosts()[0]);
                temp.add(ack.getNumPosts()[1]);
                temp.add(ack.getNumFollowers()[0]);
                temp.add(ack.getNumFollowers()[1]);
                temp.add(ack.getNumFollowing()[0]);
                temp.add(ack.getNumFollowing()[1]);
                break;
        }
        return ack.toByteArray(temp);

    }

}
