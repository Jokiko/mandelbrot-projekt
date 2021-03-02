#include <string>
#include <sstream>
#include <stdio.h>
#include <iostream>
#include "Client.h"

using namespace std;

class CPUCalculator {

public:

	CPUCalculator(Client& client, int imageWidth, int imageHeight);

	void calculate(int y, double xMove, double yMove, double zoom, int itr);
	void getTask();
	void formatResult(int x, int y, int itr);

private:
	CPUCalculator();
	Client* client;
	string package;
	int imageWidth;
	int imageHeight;
	int len = 0;
	double zx = 0;
	double zy = 0;
	double cx = 0;
	double cy = 0;
	double temp = 0;
};


CPUCalculator::CPUCalculator(Client& client, int imageWidth, int imageHeight) {
	this->client = &client;
	this->imageWidth = imageWidth;
	this->imageHeight = imageHeight;
}

void CPUCalculator::calculate(int y, double xMove, double yMove, double zoom, int itr) {

	double tmp;

	for (int x = 0; x < imageWidth; x++) {
		zx = 0;
		zy = 0;
		cx = ((double)x - (imageWidth / 2.0) + xMove) / zoom;
		cy = ((double)y - (imageHeight / 2.0) + yMove) / zoom;
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
}

void CPUCalculator::formatResult(int x, int y, int itr) {

	//	package.append("\n").append(to_string(y)).append("\n").append(to_string(itr)).append("\n");
	package += to_string(x);
	package += "\n";
	package += to_string(y);
	package += "\n";
	package += to_string(itr);
	package += "\n";

}


void CPUCalculator::getTask() {

	char check[128];
	char y_bytes[4];
	char xMove_bytes[8];
	char yMove_bytes[8];
	char zoom_bytes[8];
	char itr_bytes[4];

	client->sendMessage("task\n");
	client->receiveMessage(check, 128);

	if (strcmp(check, "task") == 0) {

		client->sendMessage("s\n");
		client->receiveMessage(y_bytes, 4);
		client->sendMessage("s\n");
		client->receiveMessage(xMove_bytes, 8);
		client->sendMessage("s\n");
		client->receiveMessage(yMove_bytes, 8);
		client->sendMessage("s\n");
		client->receiveMessage(zoom_bytes, 8);
		client->sendMessage("s\n");
		client->receiveMessage(itr_bytes, 8);

		int y = *(int*)y_bytes;
		double xMove = *(double*)xMove_bytes;
		double yMove = *(double*)yMove_bytes;
		double zoom = *(double*)zoom_bytes;
		int itr = *(int*)itr_bytes;

		calculate(y, xMove, yMove, zoom, itr);
		client->sendMessage("tick\n");

		ZeroMemory(check, 128);
		ZeroMemory(y_bytes, 4);
		ZeroMemory(xMove_bytes, 8);
		ZeroMemory(yMove_bytes, 8);
		ZeroMemory(zoom_bytes, 8);
		ZeroMemory(itr_bytes, 4);
	}
	else {
		client->sendMessage("frame\n");
	}
}
