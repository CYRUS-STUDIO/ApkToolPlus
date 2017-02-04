#include "ApkToolPlus.h"
#include "android_log.h"
#include <string.h>
#include "xxtea.h"

static const char* TAG = "ApkToolPlus";

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jbyteArray JNICALL Java_com_linchaolong_apktoolplus_jiagu_utils_ApkToolPlus_encrypt
  (JNIEnv *env, jclass clazz, jbyteArray data)
{
	// 把jbyteArray转换为byte*
	 jboolean isCopy = false;
	 jbyte* dataBuff = env->GetByteArrayElements(data, &isCopy); //获取jbyteArray的数据，返回byte*
	 jsize dataSize = env->GetArrayLength(data);

    LOGE(TAG,"data size %d",dataSize);

    // encrypt
    const char *key = "lcl_apktoolplus";
	size_t len;
	jbyte* encrypt_data = (jbyte*)xxtea_encrypt(dataBuff, dataSize, key, &len);
	env->ReleaseByteArrayElements(data, dataBuff, JNI_ABORT); //JNI_ABORT表示不拷贝数据回jbyteArray

    LOGE(TAG,"encrypt_data size %d",len);

    // char * to jbyteArray
    jbyteArray array = env->NewByteArray (len);
    env->SetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(encrypt_data));
    return array;
}

JNIEXPORT jbyteArray JNICALL Java_com_linchaolong_apktoolplus_jiagu_utils_ApkToolPlus_decrypt
  (JNIEnv *env, jclass clazz, jbyteArray data)
{
	// jbyteArray to jbyte*
	jboolean isCopy = false;
	jbyte* dataBuff = env->GetByteArrayElements(data, &isCopy);
    jsize dataSize = env->GetArrayLength(data);

    LOGE(TAG,"data size %d",dataSize);

    // decrypt
    const char *key = "lcl_apktoolplus";
	size_t len;
	jbyte* decrypt_data = (jbyte*)xxtea_decrypt(dataBuff, dataSize, key, &len);
	env->ReleaseByteArrayElements(data, dataBuff, JNI_ABORT); //JNI_ABORT表示不拷贝数据回jbyteArray

    LOGE(TAG,"decrypt_data size %d",len);

	 // char * to jbyteArray
	 jbyteArray array = env->NewByteArray (len);
	 env->SetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(decrypt_data));
	 return array;
}

#ifdef __cplusplus
}
#endif
