﻿#include "Client.h"
#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include "CudaCalc.cuh"
#include <stdio.h>
#include <string>
#include <sstream>
#include <iostream>
#include <windows.h>
#include <time.h>

#pragma comment( lib, "ws2_32.lib" )
#define PORT 5000



using namespace std;


Client client;
char input_buffer[256];
int image_width, image_height;
int result_package_size;

char* host_result_package;
char* device_result_package;

int task_y;
double task_xMove;
double task_yMove;
double task_zoom;
int task_itr;

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

	result_package_size = (3 * (sizeof(int) + 1) * image_width) + 1;

}

void setupHostVariables() {
	host_result_package = (char*)malloc(result_package_size);
}

void setupDeviceVariables() {
	cudaMalloc(&device_result_package, result_package_size);
}

void copyTask() {
	//cudaMemcpy(&device);
}

void copyResults() {
	cudaMemcpy(host_result_package, device_result_package, result_package_size, cudaMemcpyDeviceToHost);
}

int getTask() {

	char check[128];
	char y_bytes[4];
	char xMove_bytes[8];
	char yMove_bytes[8];
	char zoom_bytes[8];
	char itr_bytes[4];

	client.sendMessage("task\n");
	client.receiveMessage(check, 128);

	if (strcmp(check, "task") == 0) {

		client.sendMessage("s\n");
		client.receiveMessage(y_bytes, 4);
		client.sendMessage("s\n");
		client.receiveMessage(xMove_bytes, 8);
		client.sendMessage("s\n");
		client.receiveMessage(yMove_bytes, 8);
		client.sendMessage("s\n");
		client.receiveMessage(zoom_bytes, 8);
		client.sendMessage("s\n");
		client.receiveMessage(itr_bytes, 4);

		task_y = *(int*)y_bytes;
		task_xMove = *(double*)xMove_bytes;
		task_yMove = *(double*)yMove_bytes;
		task_zoom = *(double*)zoom_bytes;
		task_itr = *(int*)itr_bytes;

		ZeroMemory(check, 128);
		ZeroMemory(y_bytes, 4);
		ZeroMemory(xMove_bytes, 8);
		ZeroMemory(yMove_bytes, 8);
		ZeroMemory(zoom_bytes, 8);
		ZeroMemory(itr_bytes, 4);

		return 1;
	}
	else {
		client.sendMessage("tick\n");
		return 0;
	}
}

int main(int argc, char const* argv[])
{
	connect();
	receiveSize();
	setupHostVariables();
	setupDeviceVariables();

	//Create instance of CudaCalculator
	//CudaCalculator cpucalc(client, image_width, image_height);
	//clock_t t;

/*	char* string = "this is 4234\n";
	int xd = 200;
	char* string2 = (char*)&xd;

	for (int i = 0; i < 13; i++) {
		printf("Byte at %d is %x\n", i, string[i]);
	}

	for (int i = 0; i < 4; i++) {
		printf("Byte at %d is %x\n", i, string2[i]);
	}*/



	while (true) {

		getTask();
		calculate << <1, 1 >> > (image_width, image_height, task_y, task_xMove, task_yMove, task_zoom, task_itr, device_result_package);
		cudaDeviceSynchronize();
		copyResults();
		
		client.sendMessage((const char*)host_result_package);
		//t = clock();
		//cpucalc.getTask();
		//	t = clock() - t;
		//	double time_taken = ((double)t) / CLOCKS_PER_SEC;
	};
}



