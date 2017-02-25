#include <jni.h>

#include <dlib/opencv.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>

#include <dlib/image_io.h>

#include "include/radium_DLib__.h"

using namespace dlib;
using namespace std;


class Size
{
    public:
        Size(int, int);
        int width();
        int height();
        jobject to_jobject(JNIEnv *jniEnv);

    private:
        int width_;
        int height_;
};

class Point
{
    public:
        explicit Point(dlib::point dlib_point);
        Point(int, int);
        int x();
        int y();
        jobject to_jobject(JNIEnv *jniEnv);

    private:
        int x_;
        int y_;
};

class Rectangle
{
    public:
        explicit Rectangle(dlib::rectangle dlib_rectangle);
        Rectangle(Point *, Size *);
        Size *size();
        Point *position();
        jobject to_jobject(JNIEnv *);

    private:
        Size *size_;
        Point *position_;

};

template<typename T>
jobject vector_to_jobject(JNIEnv *jni_env, std::vector<T> vect, jobject (*val_conv)(T))
{
    jclass list_class = jni_env->FindClass("java/util/ArrayList");
    jmethodID list_constructor_method = jni_env->GetMethodID(list_class, "<init>", "()V");
    jobject list_instance = jni_env->NewObject(list_class, list_constructor_method);
    jmethodID list_add_method = jni_env->GetMethodID(list_class, "add", "(Ljava/lang/Object;)Z");
    for(auto val: vect) {
        jni_env->CallObjectMethod(list_instance, list_add_method, val_conv(val));
    }
    return list_instance;
}

Size::Size(int width, int height)
{
    width_ = width;
    height_ = height;
}

int Size::width()
{
    return width_;
}

int Size::height()
{
    return height_;
}

jobject Size::to_jobject(JNIEnv *jniEnv)
{
    jclass size_class = (*jniEnv).FindClass("radium/Size");
    jmethodID size_class_constructor = (*jniEnv).GetMethodID(size_class, "<init>", "(II)V");
    jobject size_instance = (*jniEnv).NewObject(size_class, size_class_constructor, width(), height());
    return size_instance;
}

Point::Point(dlib::point dlib_point)
{
    x_ = dlib_point.x();
    y_ = dlib_point.y();
}

Point::Point(int x, int y)
{
    x_ = x;
    y_ = y;
}

int Point::x()
{
    return x_;
}

int Point::y()
{
    return y_;
}



jobject Point::to_jobject(JNIEnv *jniEnv)
{
    jclass point_class = (*jniEnv).FindClass("radium/Point");
    jmethodID point_class_constructor = (*jniEnv).GetMethodID(point_class, "<init>", "(II)V");
    jobject point_instance = (*jniEnv).NewObject(point_class, point_class_constructor, x(), y());
    return point_instance;
}

Rectangle::Rectangle(rectangle dlib_rectangle)
{
    int x = dlib_rectangle.left();
    int y = dlib_rectangle.top();
    int width = dlib_rectangle.right() - dlib_rectangle.left();
    int height = dlib_rectangle.bottom() - dlib_rectangle.top();

    position_ = new Point(x, y);
    size_ = new Size(width, height);
}

Rectangle::Rectangle(Point *position, Size *size)
{
    position_ = position;
    size_ = size;
}

jobject Rectangle::to_jobject(JNIEnv *jniEnv)
{
    jclass rectangle_class = (*jniEnv).FindClass("radium/Rectangle");
    jmethodID rectangle_class_constructor = (*jniEnv).GetMethodID(rectangle_class, "<init>", "(Lradium/Point;Lradium/Size;)V");
    jobject rectangle_instance = (*jniEnv).NewObject(rectangle_class, rectangle_class_constructor, position() -> to_jobject(jniEnv), size() -> to_jobject(jniEnv));
    return rectangle_instance;
    //return NULL;
}

Point *Rectangle::position()
{
    return position_;
}

Size *Rectangle::size()
{
    return size_;
}



array2d<bgr_pixel> *bgrJByteArrayToBGRPixelArray2d(int width, int height, jbyte *bgr_jbyte_array)
{
    array2d<bgr_pixel> *bgr_pixel_array2d = new array2d<bgr_pixel>(width, height);
    for (int x = 0; x < width; x++)
    {
        for(int y = 0; y < height; y++) {
            int offset = (x * width + y) * 3;
            unsigned char blue = bgr_jbyte_array[offset + 0];
            unsigned char green = bgr_jbyte_array[offset + 1];
            unsigned char red = bgr_jbyte_array[offset + 2];
            bgr_pixel pixel(blue, green, red);
            (*bgr_pixel_array2d)[x][y] = pixel;
        }
    }
    return bgr_pixel_array2d;

}

JNIEXPORT jobject JNICALL Java_radium_DLib_00024_landmarks0(JNIEnv *jni_env, jobject instance, jstring shape_model_file_path_as_jstring, jint width, jint height, jbyteArray bgr_jbyte_java_array)
{
    jbyte *bgr_jbyte_array = jni_env->GetByteArrayElements(bgr_jbyte_java_array, 0);

    array2d<bgr_pixel> *bgr_pixel_array2d = bgrJByteArrayToBGRPixelArray2d(width, height, bgr_jbyte_array);

    save_bmp((*bgr_pixel_array2d), "/tmp/test.bmp");


    frontal_face_detector detector = get_frontal_face_detector();
    std::vector<rectangle> dets = detector((*bgr_pixel_array2d));

    const char *shape_model_file_path = jni_env->GetStringUTFChars(shape_model_file_path_as_jstring, JNI_FALSE);
    dlib::shape_predictor shape_predictor;
    dlib::deserialize(shape_model_file_path) >> shape_predictor;

    full_object_detection shape = shape_predictor((*bgr_pixel_array2d), dets[0]);

    jclass list_class = jni_env->FindClass("java/util/ArrayList");
    jmethodID list_constructor_method = jni_env->GetMethodID(list_class, "<init>", "()V");
    jobject list_instance = jni_env->NewObject(list_class, list_constructor_method);
    jmethodID list_add_method = jni_env->GetMethodID(list_class, "add", "(Ljava/lang/Object;)Z");
    for (int i = 0; i < shape.num_parts(); i++)
    {
        point dlib_point = shape.part(i);
        Point vm_point(dlib_point);
        jni_env->CallObjectMethod(list_instance, list_add_method, vm_point.to_jobject(jni_env));
    }

    return list_instance;
}
