package bgu.spl.net.impl.newsfeed;

public class NewsFeedServerMain {

    public static void main(String[] args) {
        NewsFeed feed = new NewsFeed(); //one shared object

// you can use any server...
/*        Server.threadPerClient(
                7777, //port
                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
                ObjectEncoderDecoder::new //message encoder decoder factory
        ).serve();*/

 //       Server.reactor(
 //               Runtime.getRuntime().availableProcessors(),
 //               7777, //port
 //               () ->  new RemoteCommandInvocationProtocol<>(feed), //protocol factory
 //               ObjectEncoderDecoder::new //message encoder decoder factory
//        ).serve();

    }
}
