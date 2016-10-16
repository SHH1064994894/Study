LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE    := libserial_port
LOCAL_SRC_FILES := com_skyworth_splicing_SerialPort.cpp
LOCAL_LDLIBS  := -L$(SYSROOT)/usr/lib -llog
LOCAL_SHARED_LIBRARIES:=liblog

include $(BUILD_SHARED_LIBRARY)

