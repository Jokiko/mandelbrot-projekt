#pragma once
#include <stdio.h>
#include <windows.h>
#define PORT 5000 
#define BUFLEN 1024 
#define IP "192.168.178.23"

class Client {

public:
	Client();
	void sendMessage(const char* message);
	void receiveMessage(char* receiveBuf, int len);
	void close();

private:

	SOCKET s_socket;
	SOCKADDR_IN addr;
	WSADATA wsaData;

	const char* FIRST_CONTACT;
	char recvbuf[BUFLEN];
	int iResult;

	void startWinsock();
	void checkSendError(int ec);
	void checkReceiveError(int ec);
	void createSocket();
	void connectToServer();

};


