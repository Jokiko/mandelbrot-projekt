//Lars Klee

#include <jni.h>
#include <string>
#include <complex>          // for complex numbers
#include <thread>           // for multithreading
#include <mutex>            // mutex, lock
#include <android/log.h>    // debugging

using namespace std;
#define  LOG_TAG    "native-lib"
#define  ALOG(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

bool STOP = false;
bool INTERRUPT = false;

/*****************************************
 * Reader
 */
jclass mClassReader;
jobject mObjectReader;
jmethodID ReaderSendPlotPointsFinishedID;
jmethodID ReaderInitializePackageID;

int imgPicture_getHeight;
int imgPicture_getWidth;

double zx, zy, cx, cy, temp;

/**
 * plot()
 * @param env JNIEnv
 * @param y int
 * @param xMove double
 * @param yMove double
 * @param zoom double
 * @param itr int
 */
void plot(JNIEnv *env, int y, double xMove, double yMove, double zoom, int itr){
    double tmp;
    for (int x = 0; x < imgPicture_getWidth && !STOP && !INTERRUPT; x++) {
        ALOG("stop (for): %d", STOP);
        ALOG("interrupt (for): %d", INTERRUPT);
        //ALOG("beginn: x: %d; y: %d; xMove: %f; yMove: %f; zoom: %f; itr: %d; tmp: %f.", x, y, xMove, xMove, zoom, itr, tmp);
        zx = zy = 0;
        cx = (x - (imgPicture_getWidth / 2.0) + xMove) / zoom;
        cy = (y - (imgPicture_getHeight / 2.0) + yMove) / zoom;
        tmp = itr;
        //ALOG("vor while: zx: %f; zy: %f; cx: %f; cy: %f; tmp: %f.", zx, zy, cx, cy, tmp);
        while ((zx * zx + zy * zy) < 4 && tmp > 0 && !STOP && !INTERRUPT) {
            //ALOG("in while: zx: %f; zy: %f; cx: %f; cy: %f; tmp: %f; temp: %f.", zx, zy, cx, cy, tmp, temp);
            temp = zx * zx - zy * zy + cx;
            zy = 2 * zx * zy + cy;
            zx = temp;
            tmp -= 1;
            //ALOG("end while: zx * zx + zy * zy < 4: %d; itr > 0: %d; stop: %d", (zx * zx + zy * zy < 4), (itr > 0), STOP);
        }
        //ALOG("nach while: %d", INTERRUPT);
        //ALOG("end: x: %d; y: %d; xMove: %f; yMove: %f; zoom: %f; itr: %d; tmp: %f.", x, y, xMove, xMove, zoom, itr, tmp);
        if(!INTERRUPT) {
            env->CallVoidMethod(mObjectReader, ReaderInitializePackageID, x, y, (int) tmp);
        }
    }
    if(!INTERRUPT) {
        env->CallVoidMethod(mObjectReader, ReaderSendPlotPointsFinishedID);
    }
}

/*******
 * Methoden fuer Java
 */

/**
 * initializeNativeLib()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_initializeNativeLib(JNIEnv *env, jobject obj) {
    jclass clazz = env->GetObjectClass(obj);
    mClassReader = (jclass) env->NewGlobalRef(clazz);
    mObjectReader = (jobject) env->NewGlobalRef(obj);
    ReaderSendPlotPointsFinishedID = env->GetMethodID(mClassReader, "sendPlotPointsFinished", "()V");
    ReaderInitializePackageID = env->GetMethodID(mClassReader, "initializePackage", "(III)V");
}

/**
 * getSize()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_getSize(JNIEnv* /* env */, jobject /* thiz */, int width, int height) {
    imgPicture_getWidth = width;
    imgPicture_getHeight = height;
}

/**
 * pointsPlot()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_pointsPlot(JNIEnv *env, jobject /* thiz */, int y, double MoveX, double MoveY, double zoom, int itr) {
    plot(env, y, MoveX, MoveY, zoom, itr);
}


/*****************************************
 * SecondFragment
 */

/*******
 * Methoden fuer Java
 */

/**
 * setStop()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_setStop(JNIEnv* /* env */, jobject /* thiz */, jboolean stop) {
    STOP = stop;
    //ALOG("stop: %d", STOP);
}

/**
 * setInterrupt()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_setInterrupt(JNIEnv* /* env */, jobject /* thiz */, jboolean interrupt) {
    INTERRUPT = interrupt;
    //ALOG("interrupt: %d", INTERRUPT);
}



