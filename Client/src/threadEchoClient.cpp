//
// Created by eilouz on 27/12/18.
//
#include <thread>
#include <stdlib.h>
#include <connectionHandler.h>
#include <mutex>

using namespace std;

class keyboardReader{
private:
    ConnectionHandler& CH;
    bool interupted ;
    mutex &mutex1;
    const short bufferSize = 1024;

public:
    keyboardReader(ConnectionHandler& connectionHandler ,mutex &mutex ): CH(connectionHandler) , interupted (false) , mutex1 (mutex){}

    bool isInterupted(){
        return interupted;
    }

    void run(){
        while(!interupted){
            char buf[bufferSize];
            cin.getline(buf, bufferSize);
            string line(buf);
            int len = line.length();
            if ( !CH.sendLine(line) || line == "LOGOUT") {
                std::cout << "Disconnected. Exiting...\n" << endl;
                interupted = true;
            }
            //         CH.sendBytes(buf , len+1);
//         cout << "Sent " << len + 1 << " bytes to server" << endl; // todo might need to be deleted

        }
    }
};




int main (int argc, char *argv[]) {


    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
/// mine /////
    bool interupted= false;
    char ch[2];

    mutex mutex;
    keyboardReader keyboardReader1(connectionHandler, mutex);


    thread t1(&keyboardReader::run, &keyboardReader1);


    while (!interupted) {
        if (!connectionHandler.getBytes(ch ,2)) {
            cout << "Disconnected. Exiting...\n" << endl;
            interupted = true;
        }
        string ans = connectionHandler.decode(ch);
        if (ans == "ACK 3"){
            interupted = true;
            cout << "Client disconnected" << endl;
        }
        cout << ans << endl;

    }

    if (keyboardReader1.isInterupted()== true ){
        t1.detach();

    }
    return 0;
}

