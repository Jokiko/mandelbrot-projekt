//Lars Klee

#include <jni.h>
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

/*****************************************
 * Reader
 */
jclass mClassReader;
jobject mObjectReader;
jmethodID ReaderSendPlotPointsID;

int xMove, yMove = 0;
double zoomX = 200;
double zoomY = 200;
double zx, zy, cx, cy, temp;
int numItr = 50;

int X, Y;

bool calculateBool;
bool restartBool;
bool resumeBool;
bool threadInterrupted;

/**
 * getThreadInterrupted()
 * @param interrupted bool
 */
void getThreadInterrupted(bool interrupted){
    threadInterrupted = interrupted;
}

/**
 * plot()
 * @param env JNIEnv
 * @param type jstring
 * @param count int
 * @param anzThreads int
 */
void plot(JNIEnv *env, jstring type, int count, int anzThreads){
    int startY;
    if(Y == 0){
        startY = (imgPicture_getHeight/anzThreads) * count;
    }else{
        startY = Y;
    }
    for (int y = startY + (((imgPicture_getHeight/anzThreads) * (count + 1)) / anzClients) * (yourNumber-1); y < (((imgPicture_getHeight/anzThreads) * (count + 1)) / anzClients) * yourNumber; y++) {
        for (int x = X; x < imgPicture_getWidth; x++) {//*/
            if(!threadInterrupted) {
                zx = zy = 0;
                cx = (x - (imgPicture_getWidth / 2.0) + xMove) / zoomX;
                cy = (y - (imgPicture_getHeight / 2.0) + yMove) / zoomY;
                // jeweils Division mit 2, damit in der Mitte des Bildschirms
                int itr = numItr;
                while (zx * zx + zy * zy < 4 && itr > 0) {
                    temp = zx * zx - zy * zy + cx;
                    zy = 2 * zx * zy + cy;
                    zx = temp;
                    itr--;
                }
                if (calculateBool){
                    env->CallVoidMethod(mObjectReader, ReaderSendPlotPointsID, type, x, y, itr);
                } else {
                    X = x;
                    Y = y;
                    break;
                }//*/
                if (resumeBool) {
                    X = 0;
                    resumeBool = false;
                }
            }else{
                if(!calculateBool && !resumeBool){
                    X = x;
                    Y = y;
                }
                break;
            }
        }
        if(!calculateBool || restartBool){
            if(restartBool){
                restartBool = false;
            }
            break;
        }//*/
        if(threadInterrupted){
            break;
        }
    }
}

/**
 * restartMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param count int
 * @param anzThreads int
 */
void restartMethod(JNIEnv *env, jstring type, int count, int anzThreads){
    //restartBool = true;
    xMove = 0;
    yMove = 0;
    zoomX = 200;
    zoomY = 200;
    zx = 0;
    zy = 0;
    cx = 0;
    cy = 0;
    temp = 0;
    numItr = 50;
    X = 0;
    Y = 0;
    plot(env, type, count, anzThreads);
}

/**
 * upMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void upMethod(JNIEnv *env, jstring type, double factor, int count, int anzThreads){
    yMove -= (int) factor;
    plot(env, type, count, anzThreads);
}

/**
 * downMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void downMethod(JNIEnv *env, jstring type, double factor, int count, int anzThreads){
    yMove += (int) factor;
    plot(env, type, count, anzThreads);
}

/**
 * leftMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void leftMethod(JNIEnv *env, jstring type, double factor, int count, int anzThreads){
    xMove -= (int) factor;
    plot(env, type, count, anzThreads);
}

/**
 * rightMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void rightMethod(JNIEnv *env, jstring type, double factor, int count, int anzThreads){
    xMove += (int) factor;
    plot(env, type, count, anzThreads);
}

/**
 * zoomInMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void zoomInMethod(JNIEnv *env, jstring type, double factor, int count, int anzThreads){
    zoomX *= (1 + factor);
    zoomY *= (1 + factor);
    xMove += (int)(xMove*factor);
    yMove += (int)(yMove*factor);
    plot(env, type, count, anzThreads);
}

/**
 * zoomOutMethod()
 * @param env JNIEnv
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void zoomOutMethod(JNIEnv *env, jstring type, double factor, int count, int anzThreads){
    zoomX *= (1 - factor);
    zoomY *= (1 - factor);
    xMove -= (int)(xMove*factor);
    yMove -= (int)(yMove*factor);
    plot(env, type, count, anzThreads);
}

/**
 * calculateMethod()
 * @param env JNIEnv
 * @param str const string&
 * @param type jstring
 * @param factor double
 * @param count int
 * @param anzThreads int
 */
void calculateMethod(JNIEnv *env, const string& str, jstring type, double factor, int count, int anzThreads){
    if(str == "restart"){
        restartMethod(env, type, count, anzThreads);
    }

    if(str == "Up"){
        upMethod(env, type, factor, count, anzThreads);
    }

    if(str == "Down"){
        downMethod(env, type, factor, count, anzThreads);
    }

    if(str == "Left"){
        leftMethod(env, type, factor, count, anzThreads);
    }

    if(str == "Right"){
        rightMethod(env, type, factor, count, anzThreads);
    }

    if(str == "zoomIn"){
        zoomInMethod(env, type, factor, count, anzThreads);
    }

    if(str == "zoomOut"){
        zoomOutMethod(env, type, factor, count, anzThreads);
    }

    if((str == "plot" || str == "resume" || str == "restartResume")) {
        plot(env, type, count, anzThreads);
    }

    if(str == "click" || str == "rectangle"){
        zoomInMethod(env, type, factor, count, anzThreads);
    }

    if(restartBool){
        restartBool = false;
    }
}

/**
 * Methoden fuer Java
 */

/**
 * getWidth()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_getWidth(JNIEnv* /* env */, jobject /* thiz */, int getWidth) {
    imgPicture_getWidth = getWidth;
}

/**
 * getHeight()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_getHeight(JNIEnv* /* env */, jobject  /* thiz */, int getHeight) {
    imgPicture_getHeight = getHeight;
}

/**
 * getYourNumber()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_getYourNumber(JNIEnv* /* env */, jobject /* thiz */, int your_number) {
    yourNumber = your_number;
}

/**
 * getAnzClients()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_getAnzClients(JNIEnv* /* env */, jobject /* thiz */, int anz_clients) {
    anzClients = anz_clients;
}

/**
 * pointsPlot()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_pointsPlot(JNIEnv *env, jobject thiz, jstring type, double factor, int count, int anzThreads) {

    const char *cstr = env->GetStringUTFChars(type, nullptr);
    string str = string(cstr);

    jclass clazz = env->GetObjectClass(thiz);
    mClassReader = (jclass) env->NewGlobalRef(clazz);
    mObjectReader = (jobject) env->NewGlobalRef(thiz);
    ReaderSendPlotPointsID = env->GetMethodID(mClassReader, "sendPlotPoints", "(Ljava/lang/String;III)V");

    //yourNumber, anzClients

    calculateMethod(env, str, type, factor, count, anzThreads);
}

/**
 * pointsPlotRectangle()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_pointsPlotRectangle(JNIEnv *env, jobject thiz, jstring type, jdouble factor, jint moveX, jint moveY, jint count, jint anzThreads) {

    const char *cstr = env->GetStringUTFChars(type, nullptr);
    string str = string(cstr);

    jclass clazz = env->GetObjectClass(thiz);
    mClassReader = (jclass) env->NewGlobalRef(clazz);
    mObjectReader = (jobject) env->NewGlobalRef(thiz);
    ReaderSendPlotPointsID = env->GetMethodID(mClassReader, "sendPlotPoints", "(Ljava/lang/String;III)V");

    //yourNumber, anzClients
    xMove = moveX;
    yMove = moveY;

    calculateMethod(env, str, type, factor, count, anzThreads);
}

/**
 * Reader.getThreadInterrupted()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_getThreadInterrupted(JNIEnv* /*env*/, jobject /*thiz*/, jboolean interrupted) {
    getThreadInterrupted(interrupted);
}


/*****************************************
 * Second Fragment
 */

/**
 * startCalculation()
 */
void startCalculation(){
    resumeBool = true;
    calculateBool = true;
    restartBool = false;
}

/**
 * pauseCalculation()
 */
void pauseCalculation(){
    resumeBool = false;
    calculateBool = false;
    restartBool = false;
}

/**
 * resumeCalculation()
 */
void resumeCalculation(){
    resumeBool = true;
    calculateBool = true;
}

/**
 * endCalculation()
 */
void endCalculation(){
    calculateBool = false;
    xMove = 0;
    yMove = 0;
    zoomX = 200;
    zoomY = 200;
    zx = 0;
    zy = 0;
    cx = 0;
    cy = 0;
    temp = 0;
    numItr = 50;
    X = 0;
    Y = 0;
}

/**
 * Methoden fuer Java
 */

/**
 * startCalculation()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_startCalculation(JNIEnv* /* env */, jobject /* this */){
    startCalculation();
}

/**
 * pauseCalculation()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_pauseCalculation(JNIEnv* /* env */, jclass /*clazz*/){
    pauseCalculation();
}

/**
 * resumeCalculation()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_resumeCalculation(JNIEnv* /* env */, jobject /* this */){
    resumeCalculation();
}

/**
 * endCalculation()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_endCalculation(JNIEnv* /* env */, jclass /* clazz */){
    endCalculation();
}

