package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;

import java.util.Vector;

/**
 * Created by Yahel on 23/12/2018.
 */
public class PM extends Message {

    private String content = "";
    private String username = "";
    private Vector<Byte> temp;


    public PM(){
        super();
        opcodeType = OpcodeType.PM;
        temp = new Vector<>();

    }


    @Override
    public void decodeByte(Byte nextByte) {
        if (username.equals("")){
            if(nextByte != '\0'){
                temp.add(nextByte);
            }else{
                username = new String(toByteArray(temp));
                temp.clear();
            }
        }else{
            if(nextByte != '\0'){
                temp.add(nextByte);
            }else{
                content = new String(toByteArray(temp));
                temp.clear();
                doneDecoding = true;
            }
        }
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }
}
