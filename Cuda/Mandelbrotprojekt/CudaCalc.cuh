#include "cuda_runtime.h"
#include "device_launch_parameters.h"

__device__ double zx;
__device__ double zy;
__device__ double cx;
__device__ double cy;
__device__ double temp;
__device__ int offset;

__device__ int getNumberOfDigits(int n);
__device__ void formatResult(int x, int y, int itr, char* package);
__global__ void calculate(int imageWidth, int imageHeight, int y, double xMove, double yMove, double zoom, int itr, char* package);