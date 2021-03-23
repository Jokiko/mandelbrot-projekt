#pragma once
#include "Client.h"
#include <string>

using namespace std;

class CudaCalculator {

public:

	CudaCalculator(Client& client, int imageWidth, int imageHeight);
	void calculate(int y, double xMove, double yMove, double zoom, int itr);
	void getTask();
	void formatResult(int x, int y, int itr);

private:
	CudaCalculator();
	Client* client;
	string package;
	int imageWidth;
	int imageHeight;
	int len;
	double zx;
	double zy;
	double cx;
	double cy;
	double temp;
};
