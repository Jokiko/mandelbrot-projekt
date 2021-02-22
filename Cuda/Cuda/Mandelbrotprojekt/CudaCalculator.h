#pragma once
#include "Client.h"
#include <string>

class CudaCalculator {

public:
	CudaCalculator(Client& client, int imageWidth, int imageHeight);
	void calculate(int y, double xMove, double yMove, double zoom, int itr);
	void getTask();
	void formatResult(int x, int y, int itr);

	int getIntOfCharArray(char arr[]);
	double getDoubleOfCharArray(char arr[]);

private:
	Client* client;
	std::string package;
	int imageWidth;
	int imageHeight;
	int len;
	double zy;
	double zx;
	double cx;
	double cy;
	double temp;
	char check[128];
	char y_bytes[4];
	char xMove_bytes[8];
	char yMove_bytes[8];
	char zoom_bytes[8];
	char itr_bytes[4];
};
