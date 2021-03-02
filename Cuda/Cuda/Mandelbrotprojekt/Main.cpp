#include "Client.h"
#include "CPUCalculator.h"
#include <stdio.h>
#include <windows.h>
#include <time.h>

#pragma comment( lib, "ws2_32.lib" )
#define PORT 5000



using namespace std;

int main(int argc, char const* argv[])
{
	Client client;
	char recvBuf[256];
	clock_t t;
	CPUCalculator cpucalc(client, 1000, 1000);

	client.sendMessage("connect/.../\n");
	client.receiveMessage(recvBuf, 256);
	printf(recvBuf);
	while (true) {
		t = clock();
		cpucalc.getTask();
		t = clock() - t;
		double time_taken = ((double)t) / CLOCKS_PER_SEC;
	};
}

