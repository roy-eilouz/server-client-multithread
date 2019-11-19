#include <utility>

#include <connectionHandler.h>
#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>

using boost::asio::ip::tcp;


using namespace std;
 
ConnectionHandler::ConnectionHandler(string host, short port): host_(std::move(host)), port_(port), io_service_(), socket_(io_service_){}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    /* basic connection function
     *  try to connect to sever socker through endpoint , and creating an error if the connaction fails
     *  return true on success
     */
    cout << "Starting connect to " << host_ << ":" << port_ << endl;

    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), (unsigned short) port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (exception& e) {
        cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(int bytesToWrite) {

    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, static_cast<size_t>(bytesToWrite - tmp)), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }

    return true;
}



bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\n');
}

bool ConnectionHandler::sendLine(std::string& line) {
    buffer.clear();
    memset (bytes , 0 , 1024);
    short op=0;
    size_t length= line.find(' ');
    string command = line.substr(0 , length);
    if (command == "REGISTER")
        op=1;
    else if (command == "LOGIN")
        op=2;
    else if (command == "LOGOUT")
        op=3;
    else if (command == "FOLLOW")
        op=4;
    else if (command == "POST")
        op=5;
    else if (command == "PM")
        op=6;
    else if (command == "USERLIST")
        op=7;
    else if (command == "STAT")
        op=8;

    command = line.substr(length+1);
    if (op == 1 || op == 2 ) {
        return encode12(op , command);
    }
    else if (op == 3 || op == 7){
        return encode37(op);

    }
    else if (op == 5 || op == 8){
        return encode58(op , command);
    }
    else if (op == 4){
        return encode4(op , command);

    }
    else if (op == 6){
        return encode6(op , command);
    }
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
			if (ch!='\0') {
                frame.append(1, ch);
            }
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
/*	bool result=sendBytes(frame.c_str(),frame.length());
	if(!result) return false;
	return sendBytes(&delimiter,1);*/
return false;
}

void ConnectionHandler::shortToBytes(short num)
{
    buffer.push_back(static_cast<char>((num >> 8) & 0xFF));
    buffer.push_back(static_cast<char>(num & 0xFF)) ;
}



bool ConnectionHandler::encode12(short opCode , string command) {

    shortToBytes(opCode);
    for (char i : command) {
        buffer.push_back(i);
    }
    replaceSpacesByZeroes();
    buffer.push_back('\0');
    int amount = static_cast<int>(buffer.size());

    for (int i = 0 ; i < amount ; i++){
        bytes[i] = buffer[i];
    }

    return sendBytes(amount);

}

bool ConnectionHandler::encode37(short opCode) {
    int amount = 2;
    shortToBytes(opCode);

    for (int i = 0 ; i < amount ; i++){
        bytes[i] = buffer[i];
    }

    return sendBytes(amount);

}

bool ConnectionHandler::encode58(short opCode, std::string command) {

    shortToBytes(opCode);

    for (int i = 0 ; i < command.length() ; i++){
        buffer.push_back(command[i]);
    }
    buffer.push_back('\0');
    int amount = static_cast<int>(buffer.size());

    for (int i = 0 ; i < amount ; i++){
        bytes[i] = buffer[i];
    }

    return sendBytes( amount);
}

bool ConnectionHandler::encode4(short opCode, std::string command) {
    char length = static_cast<char>(command.find(' '));
    string subCommand = command.substr(0, static_cast<unsigned long>(length));
    short follow = static_cast<short>(stoi (subCommand));
    command = command.substr(static_cast<unsigned long>(length + 1)); 
    length = (command.find(' '));
    subCommand = command.substr(0 ,length);
    int amountOfUsers = stoi(subCommand);

    command = command.substr(static_cast<unsigned long>(length + 1));
    shortToBytes(opCode);
    buffer.push_back(follow);
    shortToBytes(static_cast<short>(amountOfUsers));
    vector <string> users;
    boost::split(users , command, [](char c){return c== ' ';});

    for(string user : users) {
        for (char i : user) {
            buffer.push_back(i);
        }
        buffer.push_back('\0');
    }


    int amount = static_cast<int>(buffer.size());

    for (int i = 0 ; i < amount; i++){
        bytes[i] = buffer[i];
    }
    return sendBytes(amount);

}

bool ConnectionHandler::encode6(short opCode, std::string command) {
    shortToBytes(opCode);
    int length = static_cast<int>(command.find(' '));
    string user = command.substr(0, static_cast<unsigned long>(length));
    for (char i : user) {
        buffer.push_back(i);
    }
    buffer.push_back('\0');
    command = command.substr(static_cast<unsigned long>(length + 1));
    for (char i : command) {
        buffer.push_back(i);
    }

    buffer.push_back('\0');

    int amount = static_cast<int>(buffer.size());

    for (int i = 0 ; i < amount ; i++){
        bytes[i] = buffer[i];
    }
    return sendBytes( amount);
}

void ConnectionHandler::replaceSpacesByZeroes() {
    for (char &i : buffer) {
        if (i == *(" ")) {
            i = '\0';
        }
    }
}




short ConnectionHandler:: bytesToShort(char* bytes){
    short result = (short)((bytes[0] & 0xff) << 8);
    result = result + (short)(bytes[1] & 0xff);
    return result;
}

string ConnectionHandler ::decode(char ch[]) {

    short command = bytesToShort(ch);
    if (command == 9){
        return notification();
    }
    if (command == 10){
        char op[2];
        getBytes(op , 2);
        short insideCommand = bytesToShort(op);
        if (insideCommand == 4){
            return ack4();
        }
        else if (insideCommand == 7){
            return ack7();

        }
        else if (insideCommand == 8){
            return ack8();
        }
        else {
            return ( "ACK " + to_string(insideCommand));

        }

    }
    if (command == 11){
        return error();
    }
    return "decode : non of the above";

}

string ConnectionHandler::notification() {
    string toReturn;
    char pmOrNot[1];
    getBytes(pmOrNot , 1);
    string pmOrPublic(pmOrNot);
    if (pmOrPublic == "\001"){
        toReturn = "Notification Public ";
    }
    else{
        toReturn = "Notification PM ";
    }
    string name;
    getFrameAscii(name , '\0');//getting the user name
    toReturn += name + ' ';
    string contant="";
    getFrameAscii(contant , '\0'); // get the msg
    toReturn += contant;
    return toReturn;
}

string ConnectionHandler::ack4() {
    string toReturn = "ACK 4 ";
    char howMany[2];
    getBytes(howMany , 2);
    short howManyUsers = bytesToShort(howMany);
    toReturn +=  to_string(howManyUsers) +" ";
    string user;
    for (short i = 0; i < howManyUsers ; i++){
        getFrameAscii(user , '\0');
        user.append(1,' ');
    }
    toReturn += user + " ";
    return toReturn;

}

string ConnectionHandler::ack7() {
    string toReturn = "ACK 7 ";
    char howMany[2];
    getBytes(howMany , 2);
    short howManyUsers = bytesToShort(howMany);
    toReturn += to_string(howManyUsers)+ " " ;
    string user;
    for (short i = 0 ; i < howManyUsers; i++){
        getFrameAscii(user , '\0');
        user.append(1,' ');
    }
    toReturn += user;
    return toReturn;
}

string ConnectionHandler::ack8() {
    string toReturn = "ACK 8 ";
    char howMany[2];
    getBytes(howMany , 2);
    short howManyPost = bytesToShort(howMany);
    toReturn += to_string(howManyPost)+ " ";
    memset (howMany , 0 , 2);
    getBytes(howMany , 2);
    short howManyFollow = bytesToShort(howMany);
    toReturn += to_string(howManyFollow)+ " ";
    memset (howMany , 0 , 2);
    getBytes(howMany , 2);
    short howManyFollowing = bytesToShort(howMany);
    toReturn += to_string(howManyFollowing)+ " ";

    return toReturn;

}

string ConnectionHandler::error() {
    char op[2];
    getBytes(op , 2);
    short opCode = bytesToShort(op);
    return ("Error " + to_string(opCode));
}



// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}










