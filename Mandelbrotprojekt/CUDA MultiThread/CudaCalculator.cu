﻿#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include <stdio.h>
#include <iostream>
#include <time.h>

/*
Naive Bestimmung der Anzahl an Ziffern einer Integer. ;)
*/
__device__ int getNumberOfDigits(int n) {

	if (n >= 1000000000)
		return 10;

	if (n >= 100000000)
		return 9;

	if (n >= 10000000)
		return 8;

	if (n >= 1000000)
		return 7;

	if (n >= 100000)
		return 6;

	if (n >= 10000)
		return 5;

	if (n >= 1000)
		return 4;

	if (n >= 100)
		return 3;

	if (n >= 10)
		return 2;

	return 1;
}

__global__ void calculate(int imageWidth, int imageHeight, int y, double xMove, double yMove, double zoom, int itr, char* package) {

	int tmp_x = threadIdx.x;
	int tmp_y = y;
	int tmp_itr = itr;

	int x_digits = 0;
	int y_digits = 0;
	int itr_digits = 0;

	int x_digits_tmp = 0;
	int y_digits_tmp = 0;
	int itr_digits_tmp = 0;

	int offset = threadIdx.x * 15;

	double zx = 0;
	double zy = 0;
	double cx = ((double)tmp_x - (imageWidth / 2.0) + xMove) / zoom;
	double cy = ((double)y - (imageHeight / 2.0) + yMove) / zoom;
	double temp = 0;

	while ((zx * zx + zy * zy) < 4.0 && tmp_itr > 0) {
		temp = zx * zx - zy * zy + cx;
		zy = 2 * zx * zy + cy;
		zx = temp;
		tmp_itr -= 1;
	}

	/*
	Da der Server Strings empfängt, muss ein ordentlich formatierter ASCII String mühselig
	selbst erzeug werden. CUDA __device__ code kann keine std::string Methoden oder
	ähnliches verwenden. :(
	*/

	if (tmp_x == 0) {
		*(package + offset++) = '0';
		*(package + offset++) = '\n';
	}
	else {
		x_digits = getNumberOfDigits(tmp_x);
		x_digits_tmp = x_digits - 1;

		while (x_digits_tmp >= 0) {

			/*
			* Die erste Ziffer einer Zahl wird als letztes berechnet, und umgekehrt.
			* Die zuletzt berechnete Zahl muss also als erstes geschrieben werden,
			* daher der zusätzliche offset "x_digits_tmp".
			*/
			*(package + x_digits_tmp-- + offset) = (tmp_x % 10) + '0';
			tmp_x /= 10;
		}

		offset += x_digits;

		*(package + offset++) = '\n';
	}

	if (tmp_y == 0) {
		*(package + offset++) = '0';
		*(package + offset++) = '\n';
	}
	else {

		y_digits = getNumberOfDigits(tmp_y);
		y_digits_tmp = y_digits - 1;

		while (y_digits_tmp >= 0) {
			*(package + y_digits_tmp-- + offset) = tmp_y % 10 + '0';
			tmp_y /= 10;
		}

		offset += y_digits;
		*(package + offset++) = '\n';
	}

	if (tmp_itr == 0) {
		*(package + offset++) = '0';
		*(package + offset++) = '\n';
	}
	else {

		itr_digits = getNumberOfDigits(tmp_itr);
		itr_digits_tmp = itr_digits - 1;
		while (itr_digits_tmp >= 0) {
			*(package + itr_digits_tmp-- + offset) = tmp_itr % 10 + '0';
			tmp_itr /= 10;
		}
		offset += itr_digits;
		*(package + offset++) = '\n';
	}

	if (threadIdx.x == imageWidth - 1) {
		package[offset] = '\0';
	}
	else {
		while (offset < (threadIdx.x + 1) * 15)
			package[offset++] = 0x20;
	}
}

