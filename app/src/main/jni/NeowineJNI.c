#include <jni.h>
#include<android/log.h>
#include <stdio.h>
#include <stdlib.h>
//#include <linux/mmc/ioctl.h>
#include <sys/ioctl.h>
#include <string.h> /* memset */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <dirent.h>
#include "openssl/aes.h"
#include "openssl/ssl.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#define LOG_TAG1		"JNI_SPI_NEOWINE"
#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO,LOG_TAG1,__VA_ARGS__)
#define LOGE(...)	__android_log_print(ANDROID_LOG_ERROR,LOG_TAG1,__VA_ARGS__)
#define for_each_item(item, list) for(T * item = list->head; item != NULL; item = item->next)
#include "openssl/aes.h"
struct timeval t0, t1;

JNIEXPORT jint JNICALL
Java_com_neowine_fmanager_JniBrige_OPENSSLENC(JNIEnv* env, jobject clazz, jstring src_file, jstring dest_file, jstring key, jstring device){
	const char* nativeSrcString = (*env)->GetStringUTFChars(env, src_file, 0);
	const char* nativeDestString = (*env)->GetStringUTFChars(env, dest_file, 0);
	const char* nativeKeyString = (*env)->GetStringUTFChars(env, key, 0);
	const char* nativeDeviceString = (*env)->GetStringUTFChars(env, device, 0);
    int time = 0;

	time = SSL_ENCRYPT(nativeSrcString, nativeDestString, nativeKeyString);
    LOGE("ENCRYPT KEY JNI  0 log: %02s",nativeKeyString);

	(*env)->ReleaseStringUTFChars(env, src_file, nativeSrcString);
	(*env)->ReleaseStringUTFChars(env, dest_file, nativeDestString);
	(*env)->ReleaseStringUTFChars(env, key, nativeKeyString);
	(*env)->ReleaseStringUTFChars(env, device, nativeDeviceString);
	return time;
}
JNIEXPORT jint JNICALL
Java_com_neowine_fmanager_JniBrige_OPENSSLDEC(JNIEnv* env, jobject clazz, jstring src_file, jstring dest_file, jstring key, jstring device){
	const char* nativeSrcString = (*env)->GetStringUTFChars(env, src_file, 0);
	const char* nativeDestString = (*env)->GetStringUTFChars(env, dest_file, 0);
	const char* nativeKeyString = (*env)->GetStringUTFChars(env, key, 0);
	const char* nativeDeviceString = (*env)->GetStringUTFChars(env, device, 0);
	int time = 0;
	time = SSL_DECRYPT(nativeSrcString, nativeDestString, nativeKeyString);
    LOGE("DECRYPT KEY JNI  0 log: %02s",nativeKeyString);

	(*env)->ReleaseStringUTFChars(env, src_file, nativeSrcString);
	(*env)->ReleaseStringUTFChars(env, dest_file, nativeDestString);
	(*env)->ReleaseStringUTFChars(env, key, nativeKeyString);
	(*env)->ReleaseStringUTFChars(env, device, nativeDeviceString);
	return time;
}

JNIEXPORT void JNICALL Java_com_neowine_fmanager_JniBrige_AES_1CIPHER
		(JNIEnv * env, jobject obj, jbyteArray input, jbyteArray output, jbyteArray key)
{
	jbyte *jbinput = (*env)->GetByteArrayElements(env,input,NULL);
	jbyte *jboutput = (*env)->GetByteArrayElements(env,output,NULL);
	jbyte *jbkey = (*env)->GetByteArrayElements(env,key,NULL);
	AES_KEY stKey;
	int i = 0;
	memset(&stKey, 0, sizeof(AES_KEY));

	AES_set_encrypt_key(jbkey, 256, &stKey);
	// return false;

	AES_ecb_encrypt(jbinput, jboutput, &stKey, AES_ENCRYPT);

	for( i = 0; i < 16; i++)
		LOGE("JNI  0 log: %02x",jboutput[i]);

	(*env)->ReleaseByteArrayElements(env,input,jbinput,JNI_COMMIT);
	(*env)->ReleaseByteArrayElements(env,output,jboutput,JNI_COMMIT);
	(*env)->ReleaseByteArrayElements(env,key,jbkey,JNI_COMMIT);


}
