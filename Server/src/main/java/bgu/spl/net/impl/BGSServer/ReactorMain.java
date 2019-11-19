package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.srv.Server;
import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.api.bidi.DataBase;

public class ReactorMain {

    public static void main (String [] args) {
        DataBase DB17 = DataBase.getInstance();
        Server.reactor(
                Integer.parseInt(args[1]),
                Integer.parseInt(args[0]), //port
                () -> new BidiMessagingProtocolImpl(DB17), //protocol factory
                BidiMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
        System.out.println("Mission accomplished");
    }

}

