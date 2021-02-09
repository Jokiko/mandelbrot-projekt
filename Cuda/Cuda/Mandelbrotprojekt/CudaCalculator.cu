
#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include <stdio.h>
#include "Client.h"

class CudaCalculator {

public:
	CudaCalculator(Client client, int imageWidth, int imageHeight);
	void calculate(int iteration);
	void cudaCalculate(int iteration);

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


CudaCalculator::CudaCalculator(Client client, int imageWidth, int imageHeight) {
	this->client = client;
	this->imageWidth = imageWidth;
	this->imageHeight = imageHeight;
}

void CudaCalculator::calculate(int iteration) {

	for (int y = 0; y < imageHeight; y++) {
		for (int x = 0; x < imageWidth; x++) {
			zx = zy = 0;
			cx = x - (imageWidth / 2.0);
			cy = y - (imageHeight / 2.0);
			itr = iteration;

			while (zx * zx + zy * zy < 4 && itr > 0) {
				temp = zx * zx - zy * zy + cx;
				zy = 2 * zx * zy + cy;
				zx = temp;
				itr--;
			}
		}
	}
}

