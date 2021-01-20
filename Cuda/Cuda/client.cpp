#include <stdio.h>
#include <windows.h>
#pragma comment( lib, "ws2_32.lib" )
#define PORT 5000


using namespace std;

int startWinsock()
{

	WSADATA wsaData;
	return WSAStartup(MAKEWORD(2, 2), &wsaData);

}

int main(int argc, char const* argv[]) 
{


	// Initializing Winsocket

	int iResult;
	iResult = startWinsock();

	if (iResult != 0) 
	{
		printf("WSAStartup failed: %d\n", iResult);
		WSACleanup(); // Cleanup after error
		return 1;
	}

	//Creating Socket

	SOCKET s = socket(AF_INET, SOCK_STREAM, 0);

	if (s == INVALID_SOCKET) 
	{
		printf("Couldn't create Socket: %d\n", WSAGetLastError());
		closesocket(s);
		WSACleanup(); // Cleanup after error
		return 1;
	}

	// Initalizing SOCKADDR_IN and trying to connect

	SOCKADDR_IN addr;
	long ec; // errorcode

	memset(&addr, 0, sizeof(SOCKADDR_IN)); // All 0's
	addr.sin_family = AF_INET;
	addr.sin_port = htons(PORT); // Host-to-Network-short function
	addr.sin_addr.s_addr = inet_addr("localhost"); // Change to Serveraddress later

	ec = connect(s, (SOCKADDR*)&addr, sizeof(SOCKADDR));
	if (ec == SOCKET_ERROR) 
	{
		printf("Failed to connect to socket: %d\n", WSAGetLastError());
		closesocket(s);
		WSACleanup(); // Cleanup after error
		return 1;
	}

	closesocket(s);
	WSACleanup(); // Cleanup after error
	return 0;
}



