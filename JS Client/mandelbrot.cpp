
#include <string>
#include <fstream>      // for files manipulation
#include <complex>      // for complex numbers
#include <thread>       // for multithreading
#include <mutex>        // mutex, lock

#include <arpa/inet.h>

using namespace std;

int imgPicture_getHeight;
int imgPicture_getWidth;
int yourNumber;
int anzClients;

double zx, zy, cx, cy, temp;



int plot(int x, int y, int width, int height, double xMove, double yMove, double zoom, int numItr){
 
    zx = zy = 0;
    cx = (x - (width / 2.0) + xMove) / zoom;
    cy = (y - (height / 2.0) + yMove) / zoom;
    // jeweils Division mit 2, damit in der Mitte des Bildschirms
    int itr = numItr;
    while (zx * zx + zy * zy < 4 && itr > 0) {
        temp = zx * zx - zy * zy + cx;
        zy = 2 * zx * zy + cy;
        zx = temp;
        itr--;
    }
                
    return itr;	      
    
}





