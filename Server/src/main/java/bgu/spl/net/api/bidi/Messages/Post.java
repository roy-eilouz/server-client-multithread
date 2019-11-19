package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;

import java.util.Vector;

/**
 * Created by Yahel on 23/12/2018.
 */
public class Post extends Message {

    private String content="";
    private Vector<Byte> tempContentVector;


    public Post(){
        super();
        tempContentVector = new Vector<>();
        opcodeType = OpcodeType.POST;

    }

    @Override
    public void decodeByte(Byte nextByte) {

        if(nextByte != '\0'){
            tempContentVector.add(nextByte);
        }else{
            content = new String(toByteArray(tempContentVector));
            doneDecoding = true;
        }
    }

    public String getContent() {
        return content;
    }

}
