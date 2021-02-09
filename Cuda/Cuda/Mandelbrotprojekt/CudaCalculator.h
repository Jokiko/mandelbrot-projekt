#pragma once

class CudaCalculator {

public:
	CudaCalculator(Client client, int imageWidth, int imageHeight);
	void calculate(int iteration);

private:
	Client client;
	int imageWidth;
	int imageHeight;
	double zy = 0;
	double zx = 0;
	double cx = 0;
	double cy = 0;
	double temp = 0;
	int itr = 0;
};
