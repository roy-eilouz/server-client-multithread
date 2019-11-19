#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
                                           
#include <string>
#include <iostream>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;\
using namespace std;

class ConnectionHandler {
private:
	const std::string host_;
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
	vector <char> buffer;
	char bytes[1024] = "" ;
 
public:

    //   ---------build in ----------
    ConnectionHandler(std::string host, short port);
    virtual ~ConnectionHandler();
 
    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes( int bytesToWrite);
	
    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& line);
	
	// Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);
 
    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame, char delimiter);
 
    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame, char delimiter);

    // ------------- encoder (with send line)

    void shortToBytes(short num);

    bool encode12(short opCode , std::string line);

    bool encode37(short opCode);

    bool encode58(short opCode, std::string command);

    bool encode4(short op, std::string basic_string);

    bool encode6(short op, std::string basic_string);
    void replaceSpacesByZeroes();

    // ------------- decoder

    std::string decode (char ch[]);
    // Close down the connection properly.

    short bytesToShort(char* bytes);
    std::string notification();
    std::string ack4();

    std::string ack7();

    std::string ack8();



    std::string error();



    void close();


}; //class ConnectionHandler
 
#endif