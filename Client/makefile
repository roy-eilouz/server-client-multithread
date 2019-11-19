CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude 
LDFLAGS:=-lboost_system -pthread

all: BGSClient
	g++ -o bin/BGSClient bin/connectionHandler.o bin/threadEchoClient.o $(LDFLAGS) 

BGSClient: bin/connectionHandler.o bin/threadEchoClient.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/threadEchoClient.o: src/threadEchoClient.cpp
	g++ $(CFLAGS) -o bin/threadEchoClient.o src/threadEchoClient.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
