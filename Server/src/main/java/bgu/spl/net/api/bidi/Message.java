package bgu.spl.net.api.bidi;

import java.util.Vector;

/**
 * Created by Yahel on 23/12/2018.
 */
public abstract class Message {
    protected OpcodeType opcodeType = OpcodeType.NULL;
    protected boolean doneDecoding = false;

    public Message(){

    }



    public abstract void decodeByte(Byte nextByte);
    public boolean isDoneDecoding(){
        return doneDecoding;
    }


    // couldn't find a function that does this. there might be a better way
    protected byte[] toByteArray(Vector<Byte> vect){
        byte[] ret = new byte[vect.size()];
        int i = 0;
        for (Byte byt: vect){
            ret[i] = byt;
            i++;
        }
        return ret;
    }

    protected int twoBytesToInt(Byte[] bytes){
       return ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);
    }



    public OpcodeType getOpcodeType() {
        return opcodeType;
    }

    protected Byte[] shortToByteArray(short code){
        Byte[] ret = new Byte[2];
        ret[0] = (byte)((code >> 8) & 0xff);
        ret[1] = (byte)(code & 0xff);
        return ret;
    }
}
