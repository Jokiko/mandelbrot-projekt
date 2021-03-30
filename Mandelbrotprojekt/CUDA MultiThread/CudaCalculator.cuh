#include "cuda_runtime.h"
#include "device_launch_parameters.h"

__device__ int getNumberOfDigits(int n);
__global__ void calculate(int imageWidth, int imageHeight, int y, double xMove, double yMove, double zoom, int itr, char* package);