#include "Client.h"
#include "CPUCalculator.h"
#include <stdio.h>
#include <windows.h>
#include <time.h>

#pragma comment( lib, "ws2_32.lib" )

using namespace std;

Client client;
char input_buffer[256];
int image_width, image_height;

//Connect to Server
void connect() {
	client.sendMessage("connect/.../\n");
	client.receiveMessage(input_buffer, 256);
	printf(input_buffer);
}

//Request image resolution
void receiveSize() {

	client.sendMessage("width\n");
	client.receiveMessage(input_buffer, 256);
	image_width = *(int*)input_buffer;

	client.sendMessage("height\n");
	client.receiveMessage(input_buffer, 256);
	image_height = *(int*)input_buffer;

}

int main(int argc, char const* argv[])
{
	connect();
	receiveSize();
	CPUCalculator cpucalc(client, image_width, image_height);
	while (true) {
		cpucalc.getTask();
	};
}
