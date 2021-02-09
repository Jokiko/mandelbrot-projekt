#include <stdio.h>
#include <windows.h>
#include "Client.h"
#include "CudaCalculator.h"
#pragma comment( lib, "ws2_32.lib" )
#define PORT 5000



using namespace std;

int main(int argc, char const* argv[])
{
	Client client;
	CudaCalculator cc(client, 2560, 1440);
	cc.calculate(2);
}

