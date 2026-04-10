#include <jni.h>
#include <string>
#include <vector>
#include <numeric>
#include <algorithm>
#include <android/log.h>

#define LOG_TAG "UrbanCanopyEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

struct UserStats {
    int level;
    int currentXP;
    int nextLevelXP;
    float progress;
};

// Logic for XP and Leveling
JNIEXPORT jobject JNICALL
Java_com_example_urbancanopy_logic_GameEngine_calculateUserStats(JNIEnv *env, jobject thiz, jint totalPoints) {
    int level = 1;
    int xp = totalPoints;

    // Simple level logic: Level 1: 0-100, Level 2: 100-300, Level 3: 300-600 (base 100, increment 100 per level)
    int threshold = 100;
    int step = 100;

    while (xp >= threshold) {
        xp -= threshold;
        level++;
        threshold += step;
    }

    jclass statsClass = env->FindClass("com/example/urbancanopy/logic/UserStats");
    jmethodID constructor = env->GetMethodID(statsClass, "<init>", "(IIIF)V");

    float progress = (float)xp / (float)threshold;

    return env->NewObject(statsClass, constructor, level, xp, threshold, progress);
}

// Basic Greenness Check using RGB Histogram
JNIEXPORT jboolean JNICALL
Java_com_example_urbancanopy_logic_GameEngine_isPatchGreen(JNIEnv *env, jobject thiz, jbyteArray image_data) {
    jsize len = env->GetArrayLength(image_data);
    jbyte* data = env->GetByteArrayElements(image_data, nullptr);

    // In a real app, we would decode the JPEG/PNG here.
    // For this skeleton, we simulate processing or assume raw RGB if passed.
    // Let's assume we process a small sample or a simplified check.

    long long green_sum = 0;
    long long red_sum = 0;
    long long blue_sum = 0;

    // Simulating RGB analysis on raw data (this is a placeholder for actual image decoding)
    for (int i = 0; i < len - 2; i += 3) {
        red_sum += (unsigned char)data[i];
        green_sum += (unsigned char)data[i+1];
        blue_sum += (unsigned char)data[i+2];
    }

    env->ReleaseByteArrayElements(image_data, data, JNI_ABORT);

    if (len == 0) return JNI_FALSE;

    // Simple NDVI-like heuristic: if Green is significantly higher than Red and Blue
    double avg_g = (double)green_sum / (len / 3);
    double avg_r = (double)red_sum / (len / 3);
    double avg_b = (double)blue_sum / (len / 3);

    // If green is 20% more than others, it's "green"
    if (avg_g > avg_r * 1.2 && avg_g > avg_b * 1.2) {
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

// Marker Clustering logic
JNIEXPORT jintArray JNICALL
Java_com_example_urbancanopy_logic_GameEngine_clusterMarkers(JNIEnv *env, jobject thiz, jdoubleArray latitudes, jdoubleArray longitudes, jfloat zoomLevel) {
    jsize len = env->GetArrayLength(latitudes);
    jdouble* lats = env->GetDoubleArrayElements(latitudes, nullptr);
    jdouble* lngs = env->GetDoubleArrayElements(longitudes, nullptr);

    // Simple mock: return all indices if zoom is high, or just the first one if low
    std::vector<int> indices;
    if (zoomLevel > 10.0f) {
        indices.resize(len);
        std::iota(indices.begin(), indices.end(), 0);
    } else if (len > 0) {
        indices.push_back(0);
    }

    jintArray result = env->NewIntArray(indices.size());
    env->SetIntArrayRegion(result, 0, indices.size(), (jint*)indices.data());

    env->ReleaseDoubleArrayElements(latitudes, lats, JNI_ABORT);
    env->ReleaseDoubleArrayElements(longitudes, lngs, JNI_ABORT);

    return result;
}

// Rank calculation logic
JNIEXPORT jint JNICALL
Java_com_example_urbancanopy_logic_GameEngine_calculateRank(JNIEnv *env, jobject thiz, jint userPoints, jintArray allPoints) {
    jsize len = env->GetArrayLength(allPoints);
    jint* points = env->GetIntArrayElements(allPoints, nullptr);

    int rank = 1;
    for (int i = 0; i < len; i++) {
        if (points[i] > userPoints) {
            rank++;
        }
    }

    env->ReleaseIntArrayElements(allPoints, points, JNI_ABORT);
    return rank;
}

}
