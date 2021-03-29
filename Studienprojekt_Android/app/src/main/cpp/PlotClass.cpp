
#include "PlotClass.h"

/**
 * constructor of PlotClass
 */
PlotClass::PlotClass() = default;

/**
 * plot()
 * @param y int
 * @param xMove double
 * @param yMove double
 * @param zoom double
 * @param itr int
 */
void PlotClass::plot(int y, double xMove, double yMove, double zoom, int itr) {
    double tmp;
    for (int x = 0; x < WIDTH && !STOP && !INTERRUPT; x++) {
        zx = zy = 0;
        cx = (x - (WIDTH / 2.0) + xMove) / zoom;
        cy = (y - (HEIGHT / 2.0) + yMove) / zoom;
        tmp = itr;
        while ((zx * zx + zy * zy) < 4 && tmp > 0 && !STOP && !INTERRUPT) {
            temp = zx * zx - zy * zy + cx;
            zy = 2 * zx * zy + cy;
            zx = temp;
            tmp -= 1;
        }
        if(!INTERRUPT) {
            format(x, y, (int) tmp);
        }
    }
}

/**
 * format()
 * @param x int
 * @param y int
 * @param tmp int
 */
void PlotClass::format(int x, int y, int tmp) {
    packageComplete += to_string(x);
    packageComplete += "\n";
    packageComplete += to_string(y);
    packageComplete += "\n";
    packageComplete += to_string(tmp);
    packageComplete += "\n";
}

/**
 * setStop()
 * @param stop bool
 */
void PlotClass::setStop(bool stop) {
    STOP = stop;
}

/**
 * setInterrupt()
 * @param interrupt bool
 */
void PlotClass::setInterrupt(bool interrupt) {
    INTERRUPT = interrupt;
}

/**
 * setSize()
 * @param width int
 * @param height int
 */
void PlotClass::setSize(int width, int height) {
    WIDTH = width;
    HEIGHT = height;
}

/**
 * resetPackage()
 */
void PlotClass::resetPackage() {
    packageComplete = "";
}

/**
 * getPackageComplete()
 * @return packageComplete as string
 */
string PlotClass::getPackageComplete() {
    return packageComplete;
}

