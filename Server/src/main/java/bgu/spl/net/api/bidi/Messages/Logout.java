package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;

/**
 * Created by Yahel on 23/12/2018.
 */
public class Logout extends Message {
    public Logout(){
        super();
        doneDecoding = true;
        opcodeType = OpcodeType.LOGOUT;

    }


    @Override
    public void decodeByte(Byte nextByte) {
        // no data to decode
    }
}
