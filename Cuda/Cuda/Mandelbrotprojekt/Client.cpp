#include <stdio.h>
#include <windows.h>

#pragma comment( lib, "ws2_32.lib" )
#define PORT 5000 //default server port 
#define BUFLEN 1024 //defaul bufferlength for messages

/*
Client class handles the communication with our Javaserver

*/


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

Client::Client() {
	startWinsock();
	createSocket();
	connectToServer();
};

void Client::startWinsock() {

	iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);

	if (iResult != 0)
	{
		printf("WSAStartup failed: %d\n", iResult);
		WSACleanup(); // Cleanup after error
		exit(1);
	}

};

void Client::checkSendError(int ec) {

	if (ec == SOCKET_ERROR)
	{
		printf("Failed to send message: %d\n", WSAGetLastError());
		closesocket(s_socket);
		WSACleanup(); // Cleanup after error
	}

};

void Client::checkReceiveError(int ec) {

	if (ec == SOCKET_ERROR)
	{
		printf("Failed to receive message: %d\n", WSAGetLastError());
		closesocket(s_socket);
		WSACleanup(); // Cleanup after error
	}

};

void Client::createSocket() {

	s_socket = socket(AF_INET, SOCK_STREAM, 0);

	if (s_socket == INVALID_SOCKET)
	{
		printf("Couldn't create Socket: %d\n", WSAGetLastError());
		closesocket(s_socket);
		WSACleanup(); // Cleanup after error
		exit(1);
	}

};

void Client::connectToServer() {

	memset(&addr, 0, sizeof(SOCKADDR_IN)); // All 0's
	addr.sin_family = AF_INET;
	addr.sin_port = htons(PORT); // Host-to-Network-short function
	addr.sin_addr.s_addr = inet_addr("136.199.5.5"); // Change to Serveraddress later
	FIRST_CONTACT = "type/.../Cuda\n";
	iResult = connect(s_socket, (SOCKADDR*)&addr, sizeof(SOCKADDR));

	if (iResult == SOCKET_ERROR) //ERROR-Handling
	{
		printf("Failed to connect to socket: %d\n", WSAGetLastError());
		closesocket(s_socket);
		WSACleanup(); // Cleanup after error
		exit(1);
	}

	iResult = send(s_socket, FIRST_CONTACT, strlen(FIRST_CONTACT), 0);
	checkSendError(iResult);

	iResult = recv(s_socket, recvbuf, BUFLEN, 0);
	checkReceiveError(iResult);

	printf(recvbuf);

};

void Client::close() {
	closesocket(s_socket);
	WSACleanup();
}

void Client::sendMessage(const char* message) {
	iResult = send(s_socket, message, strlen(message), 0);
	checkSendError(iResult);
}

void Client::receiveMessage(char* receiveBuf, int len) {
	iResult = recv(s_socket, receiveBuf, len, 0);
	checkReceiveError(iResult);
}

