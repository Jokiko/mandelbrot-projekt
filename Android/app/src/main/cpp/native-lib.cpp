
#include "PlotClass.h"


// prevents:
// Clang-Tidy: Initialization of 'plotClass' with static storage
// duration may throw an exception that cannot be caught
PlotClass& getPlotClass(){
    static PlotClass plotClass;
    return plotClass;
}

/*****************************************
 * Reader
 */

/**
 * getSize()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_setSize(JNIEnv* /* env */, jobject /* obj */, int width, int height) {
    getPlotClass().setSize(width, height);
}

/**
 * pointsPlot()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_pointsPlot(JNIEnv* /* env */, jobject /* obj */, int y, double xMove, double yMove, double zoom, int itr) {
    getPlotClass().plot(y, xMove, yMove, zoom, itr);
}

/**
 * getPackage()
 * @return plotClass.getPackageComplete() as jstring
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_studienprojekt_1android_Reader_getPackage(JNIEnv *env, jobject /* obj */) {
    return env->NewStringUTF(getPlotClass().getPackageComplete().c_str());
}

/**
 * resetPackage();
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_Reader_resetPackage(JNIEnv*/* env */, jobject /* obj */) {
    getPlotClass().resetPackage();
}


/*****************************************
 * SecondFragment
 */

/**
 * setStop()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_setStop(JNIEnv* /* env */, jobject /* obj */, jboolean stop) {
    getPlotClass().setStop(stop);
}

/**
 * setInterrupt()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_example_studienprojekt_1android_SecondFragment_setInterrupt(JNIEnv* /* env */, jobject /* obj */, jboolean interrupt) {
    getPlotClass().setInterrupt(interrupt);
}


