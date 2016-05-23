#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <android/log.h>

#define TAG "myDemo-jni" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(
		JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong out,
		jintArray parameters);

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(
		JNIEnv* env, jobject, jlong addrGray, jlong addrRgba, jlong out,
		jintArray parameters) {
	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;
	Mat& mOut = *(Mat*) out;
//    vector<KeyPoint> v;
//
//    Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
//    detector->detect(mGr, v);
//    for( unsigned int i = 0; i < v.size(); i++ )
//    {
//        const KeyPoint& kp = v[i];
//        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
//    }
	Mat matbgr, matHsv, imgThresholded;
	jint * parametersInt = env->GetIntArrayElements(parameters, 0);
	int iLowH = parametersInt[0];
	int iHighH = parametersInt[1];

	int iLowS = parametersInt[2];
	int iHighS = parametersInt[3];

	int iLowV = parametersInt[4];
	int iHighV = parametersInt[5];

	//Create a black image with the size as the camera output
	Mat imgLines = Mat::zeros(mRgb.size(), CV_8UC4);

	cvtColor(mRgb, matHsv, COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV

	inRange(matHsv, Scalar(iLowH, iLowS, iLowV), Scalar(iHighH, iHighS, iHighV),
			imgThresholded);

	//morphological opening (remove small objects from the foreground)
	erode(imgThresholded, imgThresholded,
			getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
	dilate(imgThresholded, imgThresholded,
			getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));

	//morphological closing (fill small holes in the foreground)
	dilate(imgThresholded, imgThresholded,
			getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));
	erode(imgThresholded, imgThresholded,
			getStructuringElement(MORPH_ELLIPSE, Size(5, 5)));


	mOut = imgThresholded;
}
}
