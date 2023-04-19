#include <jni.h>
#include "headers/libretro.h"

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("poketouch");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("poketouch")
//      }
//    }
extern "C"
JNIEXPORT jstring JNICALL
Java_xyz_heurlin_poketouch_MainActivity_helloWorld(JNIEnv *env, jobject thiz) {
    jstring jstr = (*env).NewStringUTF("Hello, world!");
    return jstr;
}
extern "C"
JNIEXPORT jint JNICALL
Java_xyz_heurlin_poketouch_MainActivity_retroAPIVersion(JNIEnv *env, jobject thiz) {
    return retro_api_version();
}
extern "C"
JNIEXPORT jstring JNICALL
Java_xyz_heurlin_poketouch_MainActivity_retroLibraryName(JNIEnv *env, jobject thiz) {
    retro_system_info info{};
    retro_get_system_info(&info);
    return (*env).NewStringUTF(info.library_name);
}