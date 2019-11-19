package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;

import java.util.Vector;

/**
 * Created by Yahel on 23/12/2018.
 */
public class Stat extends Message {

    private String username="";
    private Vector<Byte> temp;


    public Stat(){
        super();
        opcodeType = OpcodeType.STAT;
        temp = new Vector<>();

    }


    public void decodeByte(Byte nextByte) {

        if(nextByte != '\0'){
            temp.add(nextByte);
        }else{
            username = new String(toByteArray(temp));
            doneDecoding = true;
        }
    }

    public String getUsername() {
        return username;
    }
}
