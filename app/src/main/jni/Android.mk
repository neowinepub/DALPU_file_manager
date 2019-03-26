LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NeowineNative
LOCAL_SRC_FILES := NeowineJNI.c dorca.c aes128_cipher.c aes128_decipher.c neowineDorca.c 

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)
