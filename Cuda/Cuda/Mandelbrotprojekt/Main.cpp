#include <stdio.h>
#include <windows.h>
#include "Client.h"

#pragma comment( lib, "ws2_32.lib" )
#define PORT 5000



using namespace std;

int main(int argc, char const* argv[])
{
	Client client;
	client.sendMessage("hello world\n");
}

