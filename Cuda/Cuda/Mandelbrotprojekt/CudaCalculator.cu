
#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include <string>
#include <sstream>
#include <stdio.h>
#include <iostream>
#include "Client.h"

using namespace std;

class CudaCalculator {

public:
	CudaCalculator(Client &client, int imageWidth, int imageHeight);
	void calculate(int y, double xMove, double yMove, double zoom, int itr);
	void getTask();
	void formatResult(int x, int y, int itr);

	int getIntOfCharArray(char arr[]);
	double getDoubleOfCharArray(char arr[]);

private:
	Client *client;
	string package;
	int imageWidth;
	int imageHeight;
	int len = 0;
	double zy = 0;
	double zx = 0;
	double cx = 0;
	double cy = 0;
	double temp = 0;
	char check[128];
	char y_bytes[4];
	char xMove_bytes[8];
	char yMove_bytes[8];
	char zoom_bytes[8];
	char itr_bytes[4];
};


CudaCalculator::CudaCalculator(Client &client, int imageWidth, int imageHeight) {
	this->client = &client;
	this->imageWidth = imageWidth;
	this->imageHeight = imageHeight;
}

void CudaCalculator::calculate(int y, double xMove, double yMove, double zoom, int itr) {

	double tmp;

		for (int x = 0; x < imageWidth; x++) {
			zx = 0;
			zy = 0;
			cx = ((double) x - (imageWidth / 2.0) + xMove) / zoom;
			cy = ((double) y - (imageHeight / 2.0) + yMove) / zoom;
			tmp = itr;

			while ((zx * zx + zy * zy) < 4.0 && tmp > 0) {
				temp = zx * zx - zy * zy + cx;
				zy = 2 * zx * zy + cy;
				zx = temp;
				tmp -= 1;
			}
			formatResult(x, y, tmp);
		}
			client->sendMessage(package.c_str());
			package.clear();

	client->sendMessage("tick\n");
}

void CudaCalculator::formatResult(int x, int y, int itr) {

//	package.append("\n").append(to_string(y)).append("\n").append(to_string(itr)).append("\n");
	package += to_string(x);
	package	+= "\n";
	package	+= to_string(y);
	package	+= "\n";
	package	+= to_string(itr);
	package	+= "\n";

}


void CudaCalculator::getTask() {

	do {
		client->sendMessage("task\n");
		client->receiveMessage(check, 128);
	} while (strcmp(check, "noTask") == 0);

	if(strcmp(check, "task") == 0) {

		client->receiveMessage(y_bytes, 4);
		client->receiveMessage(xMove_bytes, 8);
		client->receiveMessage(yMove_bytes, 8);
		client->receiveMessage(zoom_bytes, 8);
		client->receiveMessage(itr_bytes, 8);


		int y = getIntOfCharArray(y_bytes);
		double xMove = getDoubleOfCharArray(xMove_bytes);
		double yMove = getDoubleOfCharArray(yMove_bytes);
		double zoom = getDoubleOfCharArray(zoom_bytes);
		int itr = getIntOfCharArray(itr_bytes);

		calculate(y, xMove, yMove, zoom, itr);
	}
}

int CudaCalculator:: getIntOfCharArray(char arr[]){

	unsigned char* res = (unsigned char*)arr;
	int result = (int)(res[0] | res[1] << 8 | res[2] << 16 | res[3] << 24);
	return result;
}

double CudaCalculator::getDoubleOfCharArray(char arr[]) {

	unsigned char* res = (unsigned char*)(arr);
	double result = *reinterpret_cast<double*>(res);
	return result;
}
