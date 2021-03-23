#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include <stdio.h>
#include <iostream>

__device__ double zx;
__device__ double zy;
__device__ double cx;
__device__ double cy;
__device__ double temp;
__device__ int offset;

__device__ void formatResult(int x, int y, int itr, char* package) {

	//printf("x: %d ; y: %d; itr: %d\n", x, y, itr);

	char* tmp_x = (char*)&x;
	char* tmp_y = (char*)&y;
	char* tmp_itr = (char*)&itr;

	for (int i = 3; i >= 0; i--) {
		*(package + offset++) = tmp_x[i];
	}

	package[offset++] = '\n';

	for (int i = 3; i >= 0; i--) {
		package[offset++] = tmp_y[i];
	}

	package[offset++] = '\n';

	for (int i = 0; i < 4; i++) {

		package[offset++] = tmp_itr[i];
	}

	package[offset++] = '\n';
}

__global__ void calculate(int imageWidth, int imageHeight, int y, double xMove, double yMove, double zoom, int itr, char* package) {

	int tmp_itr;

	offset = 0;

	for (int x = 0; x < imageWidth; x++) {
		zx = 0;
		zy = 0;
		cx = ((double)x - (imageWidth / 2.0) + xMove) / zoom;
		cy = ((double)y - (imageHeight / 2.0) + yMove) / zoom;
		tmp_itr = itr;

		while ((zx * zx + zy * zy) < 4.0 && tmp_itr > 0) {
			temp = zx * zx - zy * zy + cx;
			zy = 2 * zx * zy + cy;
			zx = temp;
			tmp_itr -= 1;
		}
		formatResult(x, y, tmp_itr, package);
	}

	package[offset] = '\0';
}

