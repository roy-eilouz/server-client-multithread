package bgu.spl.net.api.bidi.Messages;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.api.bidi.OpcodeType;

/**
 * Created by Yahel on 23/12/2018.
 */
public class Error extends Message {
    private Byte tempByte;
    private OpcodeType opCode;
    private OpcodeType opcodeType;

    public Error(){
        super();
        tempByte = -1;
        opCode = OpcodeType.NULL; // for debugging
        opcodeType = OpcodeType.ERROR;

    }

    public Error(OpcodeType code){ // for BidiMessagingProtocolImpl
        super();
        tempByte = -1;
        opcodeType = OpcodeType.ERROR;

        opCode = code;

    }


    @Override
    public void decodeByte(Byte nextByte) {
        // do nothing
    }

    @Override
    public OpcodeType getOpcodeType() {
        return opcodeType;
    }

    public OpcodeType getOpCode() {
        return opCode;
    }
}
