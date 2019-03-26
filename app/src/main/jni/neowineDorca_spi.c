/* Neowine Dorca Library
Format for Neowine data encryption/decryption application(1:0 mode)

We will create/open a “file” to let SD card recognize the following request is a normal read/write request or a smart card (Neowine) request at beginning. When access this “file” the actual behavior is communicate with the dorca chip.
You can get related code in the functions named Dorca20* in neowineDorca.c
By the pre-defined pattern, SD controller will know which actions need to be doing.

The pre-defined pattern is a 512bytes array begin by 0x0b 0xad 0xca 0xfe used as the smart card mark.
The byte offset 15 is used for data encrypt/decrypt application (1:0 mode).
Set as 0xa is FW mode. All data calculated by FW (i.e. Dorca chip).
Set as 0xb is SW mode. Only first 16 bytes calculated by FW (i.e. Dorca chip).
The byte offset 16 is defined as the command index.
0x0c: init dorca
0x10: key exchange
0x01: cipher data
0x02: decipher data
The data followed by offset 32 is the data portion.

When SD controller received the request by call functions neoWrite(), it will parse the pattern and send relative SPI commands to the dorca chip. */

/* DEFAULT HEADER */
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <time.h>
#include <stdbool.h>

/* SPI FUNCTION */

#include "openssl/aes.h"

#include <sys/system_properties.h>
//#include "aes.h"
// For Hybrid encryption

//==> neowine android debug
#include <android/log.h>
#include <unistd.h>

#define LOG_TAG1        "JNI_SPI_NEOWINE"
#define LOGI(...)    __android_log_print(ANDROID_LOG_INFO,LOG_TAG1,__VA_ARGS__)
#define LOGE(...)    __android_log_print(ANDROID_LOG_ERROR,LOG_TAG1,__VA_ARGS__)
#define WHEREAMI() LOGI("%s %d",__FILE__,__LINE__)
#define COM_SPEED 1000*1000*12 //12Mhz
#define AES_ONLY  0
#define FAST_MODE  1
#define device "/dev/spidev0.0" // Fix after test
//#define ENCODE_ONCE


/*
 * 1) AES through SPI device
 * all data are process by SPI communication
 * */
int GetTimeMS(clock_t start, clock_t end) {
    int time = ((end - start) * 1000 / CLOCKS_PER_SEC);//
    LOGI("Start %ld End %ld  total %ld", start, end, end - start);

    return time;
}

int SSL_ENCRYPT(const char *src_file, char *dest_file, char *key) {
    int chunk_size = 16; //valid data
    LOGI("*** Dorca20FASTEncrypt start fw (FAST MODE) 20181130");
    //encrypt_file(src_file, dest_file);
    int src_file_size = 0;
    unsigned char src_file_size_arr[4];
    int align_size = 0;
    unsigned char *src_mem, *dest_mem;
    FILE *srcF, *destF;
    int val, i = 0, j = 0, cnt_byte = 0, temp_int = 0;
    int fd = 0;
    int k = 0;
    clock_t start = 0;
    clock_t finish = 0;
    int time = 0;
    AES_KEY encKey;
    AES_set_encrypt_key(key, 128, &encKey);
    //get the size of src file
    srcF = fopen(src_file, "r");
    while ((val = fgetc(srcF)) != EOF) {
        src_file_size++;
    }
    src_file_size_arr[0] = src_file_size;
    src_file_size_arr[1] = src_file_size >> 8;
    src_file_size_arr[2] = src_file_size >> 16;
    src_file_size_arr[3] = src_file_size >> 24;

    align_size = src_file_size / chunk_size;
    if (src_file_size % chunk_size != 0)
        align_size += 1;
    //malloc src_mem for store the src file
    src_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (src_mem < 0) {
        LOGE("malloc src_mem errno: %s\n", strerror(errno));
    }
    memset(src_mem, 0, align_size * chunk_size);
    //malloc dest_mem for the result
    dest_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (dest_mem < 0) {
        LOGE("malloc dest_mem errno: %s\n", strerror(errno));
    }
    memset(dest_mem, 0, align_size * chunk_size);
    //malloc src_ctr_mem for store the src ctr
    fseek(srcF, 0, SEEK_SET);//move srcF to the begin of src file
    while ((val = fgetc(srcF)) != EOF) {
        src_mem[i++] = val;
    }
    fclose(srcF);
    LOGI("Dorca20FASTEncrypt read src file done");
    start = clock();
    //fd = Dorca20_SPI_Open(device, FAST_MODE, COM_SPEED);
    LOGI("fd %d", fd);
    //Dorca20_SPI_Reset(fd);
    //Dorca20_SPI_KeyTrans(fd, key);
#ifdef ENCODE_ONCE
    //Dorca20_SPI_CipherBlock(fd, src_mem, dest_mem, align_size * chunk_size);
#else
    for(i = 0; i < align_size*chunk_size; i+= 16) {
     //  Dorca20_SPI_CipherBlock(fd,src_mem+i*chunk_size,dest_mem+i*chunk_size,chunk_size);
    AES_ecb_encrypt(&src_mem[i], &dest_mem[i], &encKey, AES_ENCRYPT);
    }
#endif
    finish = clock();
    time = GetTimeMS(start, finish);
    LOGI("Dorca20FASTEncrypt encrypt done");
    //<== FW AES method
    //save(write) dest_mem as dest file
    destF = fopen(dest_file, "wb");
    if (destF) {
        //record (ori. file size) info.
        fwrite(src_file_size_arr, 4, 1, destF);
        fwrite(dest_mem, align_size * chunk_size, 1, destF);
//        fsync(destF);
        fclose(destF);
        LOGI("dest_mem saved to %s\n", dest_file);
    }
    else {
        LOGE("save dest_mem fopen errno: %s\n", strerror(errno));
    }
    //free src_mem and dest_mem
    free(src_mem);
    free(dest_mem);
    //Doraca20_SPI_Close(fd);
    LOGI("*** Dorca20FASTEncrypt finished");
    return time;
}

int SSL_DECRYPT(char *src_file, char *dest_file, char *key) {
    int chunk_size = 16; //valid data
    LOGI("*** Dorca20FASTDecrypt start fw 20181130");
    //decrypt_file(src_file, dest_file);
    int src_file_size = 0;
    int ori_src_file_size = 0;
    unsigned char ori_src_file_size_arr[4];
    int align_size = 0;
    unsigned char *src_mem, *dest_mem;
    FILE *srcF, *destF;
    int val, i = 0, j = 0, cnt_byte = 0, temp_int = 0;;
    int k = 0;
    int fd = 0;
    clock_t start = 0;
    clock_t finish = 0;
    int time = 0;
    AES_KEY decKey;
    AES_set_decrypt_key(key, 128, &decKey);
    //get the size of src file
    srcF = fopen(src_file, "r");
    while ((val = fgetc(srcF)) != EOF) {
        src_file_size++;
    }
    src_file_size -= 4;
    LOGI("src_file_size: %d\n", src_file_size);
    align_size = src_file_size / chunk_size;
    if (src_file_size % chunk_size != 0)
        align_size += 1;
    LOGI("align_size: %d\n", align_size);
    //malloc src_mem for store the src file
    src_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (src_mem < 0) {
        LOGE("malloc src_mem errno: %s\n", strerror(errno));
    }
    memset(src_mem, 0, align_size * chunk_size);
    //LOGI("memset src_mem: %d\n", src_mem[align_size*chunk_size-1]);
    //malloc dest_mem for the result
    dest_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (dest_mem < 0) {
        LOGE("malloc dest_mem errno: %s\n", strerror(errno));
    }
    memset(dest_mem, 0, align_size * chunk_size);
    LOGI("memset dest_mem: %d\n", dest_mem[align_size * chunk_size - 1]);
    //malloc src_ctr_mem for store the src ctr
    //read the content of src file to src_mem
    fseek(srcF, 0, SEEK_SET);//move srcF to the begin of src file
    while ((val = fgetc(srcF)) != EOF) {
        if (i > 3) {
            src_mem[j++] = val;
        } else {
            ori_src_file_size_arr[i] = val;
        }
        i++;
    }
    fclose(srcF);
    LOGI("Dorca20FASTDecrypt read src file done");
    ori_src_file_size = (ori_src_file_size_arr[0]) | (ori_src_file_size_arr[1] << 8) |
                        (ori_src_file_size_arr[2] << 16) | (ori_src_file_size_arr[3] << 24);
    //==> FW AES method
//	Dorca20Init();
//	Dorca20KeyTrans(key);
    start = clock();
    //fd = Dorca20_SPI_Open(device, FAST_MODE, COM_SPEED);
    //Dorca20_SPI_Reset(fd);
    //Dorca20_SPI_KeyTrans(fd, key);
#ifdef ENCODE_ONCE
    //Dorca20_SPI_DecipherFast(fd, src_mem, dest_mem, align_size * chunk_size);
#else
    for(i = 0; i < align_size*chunk_size; i+= chunk_size){
    //Dorca20_SPI_DecipherFast(fd,src_mem+i*chunk_size,dest_mem+i*chunk_size,chunk_size);
        AES_ecb_encrypt(&src_mem[i], &dest_mem[i], &decKey, AES_DECRYPT);
    }
#endif
    finish = clock();
    time = GetTimeMS(start, finish);

    LOGI("Dorca20FASTDecrypt decrypt done");
    //<== FW AES method

    //save(write) dest_mem as dest file
    destF = fopen(dest_file, "wb");
    if (destF) {
        fwrite(dest_mem, ori_src_file_size, 1, destF);
        //fsync();
        fclose(destF);
        LOGI("dest_mem saved to %s\n", dest_file);
    }
    else {
        LOGE("save dest_mem fopen errno: %s\n", strerror(errno));
    }
    //free src_mem and dest_mem
    free(src_mem);
    free(dest_mem);
    close(fd);
    return time;
    LOGI("*** Dorca20Decryptfile finished");
    //Doraca20_SPI_Close(fd);
}

int Dorca20SPIEncrypt(const char *src_file, char *dest_file, char *key) {
    int chunk_size = 16; //valid data
    LOGI("*** Dorca20SPIEncryptfile start fw");
    //encrypt_file(src_file, dest_file);
    int src_file_size = 0;
    unsigned char src_file_size_arr[4];
    int align_size = 0;
    unsigned char *src_mem, *dest_mem, *temp;
    FILE *srcF, *destF;
    int val, i = 0, cnt_byte = 0, temp_int = 0;
    int fd = 0;
    clock_t start = 0;
    clock_t finish = 0;
    int time = 0;



    WHEREAMI();
    LOGE("src_file %s\n   dest_file %s", src_file, dest_file);
    //get the size of src file
    srcF = fopen(src_file, "r");
    while ((val = fgetc(srcF)) != EOF) {
        src_file_size++;
    }

    LOGE("src_file_size %d\n", src_file_size);

    src_file_size_arr[0] = src_file_size & 0xFF;
    src_file_size_arr[1] = src_file_size >> 8 & 0xFF;
    src_file_size_arr[2] = src_file_size >> 16 & 0xFF;
    src_file_size_arr[3] = src_file_size >> 24 & 0xFF;
    WHEREAMI();
    align_size = src_file_size / chunk_size;
    LOGE("chunk_size %d\n", chunk_size);

    if (src_file_size % chunk_size != 0)
        align_size += 1;
    //malloc src_mem for store the src file
    LOGE("align_size %d\n", align_size);
    src_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (src_mem < 0) {
        LOGE("malloc src_mem errno: %s\n", strerror(errno));
    }
    memset(src_mem, 0, align_size * chunk_size);
    //malloc dest_mem for the result
    dest_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (dest_mem < 0) {
        LOGE("malloc dest_mem errno: %s\n", strerror(errno));
    }
    memset(dest_mem, 0, align_size * chunk_size);

    temp = malloc((chunk_size) * sizeof(char));
    if (dest_mem < 0) {
        LOGE("malloc chunk errno: %s\n", strerror(errno));
    }
    memset(dest_mem, 0, align_size * chunk_size);

    //read the content of src file to src_mem
    fseek(srcF, 0, SEEK_SET);//move srcF to the begin of src file
    while ((val = fgetc(srcF)) != EOF) {
        src_mem[i++] = val;
    }
    fclose(srcF);
//	for(i = 0; i < src_file_size ;i++)
//	{
//		LOGI("SRC %c",src_mem[i]);
//	}
    LOGI("Dorca20SPIEncryptfile read src file done");
    //==> FW AES method
    start = clock();
   // fd = Dorca20_SPI_Open(device, AES_ONLY, COM_SPEED);
    WHEREAMI();
    LOGI("fd %d", fd);
    //Dorca20_SPI_Reset(fd);
    //Dorca20_SPI_KeyTrans(fd, key);
    LOGI("Dorca20SPIEncryptfile KeyTrans done");
    //create_file();
#ifdef ENCODE_ONCE
    //Dorca20_SPI_CipherBlock(fd, src_mem, dest_mem, align_size * chunk_size);
#else
    for(i= 0;i<align_size;i++){
   // Dorca20_SPI_CipherBlock(fd,src_mem+i*chunk_size,dest_mem+i*chunk_size,chunk_size);
    }
#endif
    LOGI("Dorca20SPIEncryptfile encrypt done");

    //<== SPI AES method

//----------------------------------------------------------------
    // Dorca20_SPI_Cipher(device, dest_mem, rdata);
//----------------------------------------------------------------

    //save(write) dest_mem as dest file
    destF = fopen(dest_file, "wb");

    LOGI("dest_file %s", dest_file);
    if (destF) {
        //record (ori. file size) info.
        fwrite(src_file_size_arr, 4, 1, destF);
        fwrite(dest_mem, align_size * chunk_size, 1, destF);
//        fsync();
        fclose(destF);
        LOGI("dest_mem saved to %s\n", dest_file);
    }
    else {
        LOGE("save dest_mem fopen errno: %s\n", strerror(errno));
    }
    //free src_mem and dest_mem
    free(src_mem);
    free(dest_mem);
   // Doraca20_SPI_Close(fd);
    //Dorca20Done();
    LOGI("*** Dorca20SPIEncryptfile file finished");
    return time;
}

int Dorca20SPIDecrypt(const char *src_file, char *dest_file, char *key) {
    int chunk_size = 16; //valid data
    LOGI("*** Dorca20_SPI_Decrypt file start");
    //decrypt_file(src_file, dest_file);
    int src_file_size = 0;
    int ori_src_file_size = 0;
    unsigned char ori_src_file_size_arr[4];
    int align_size = 0;
    unsigned char *src_mem, *dest_mem, *temp;
    FILE *srcF, *destF;
    int val, i = 0, j = 0, cnt_byte = 0, temp_int = 0;
    int fd = 0;
    clock_t start = 0;
    clock_t finish = 0;
    int time = 0;
    //get the size of src file
    srcF = fopen(src_file, "r");
    while ((val = fgetc(srcF)) != EOF) {
        src_file_size++;
    }
    src_file_size -= 4; // reduce header size
    //LOGI("src_file_size: %d\n", src_file_size);
    align_size = src_file_size / chunk_size;
    if (src_file_size % chunk_size != 0)
        align_size += 1;
    //LOGI("align_size: %d\n", align_size);
    //malloc src_mem for store the src file
    src_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (src_mem < 0) {
        LOGE("malloc src_mem errno: %s\n", strerror(errno));
    }
    memset(src_mem, 0, align_size * chunk_size);
    //LOGI("memset src_mem: %d\n", src_mem[align_size*chunk_size-1]);
    //malloc dest_mem for the result
    dest_mem = malloc((align_size * chunk_size) * sizeof(char));
    if (dest_mem < 0) {
        LOGE("malloc dest_mem errno: %s\n", strerror(errno));
    }
    memset(dest_mem, 0, align_size * chunk_size);
    LOGI("memset dest_mem: %d\n", dest_mem[align_size * chunk_size - 1]);
    //read the content of src file to src_mem
    fseek(srcF, 0, SEEK_SET);//move srcF to the begin of src file
    while ((val = fgetc(srcF)) != EOF) {
        if (i > 3) {
            src_mem[j++] = val;
        } else {
            ori_src_file_size_arr[i] = val;
        }
        i++;
    }

    fclose(srcF);
    LOGI("Dorca20SPIDecryptfile read src file done");
    ori_src_file_size = (ori_src_file_size_arr[0]) | (ori_src_file_size_arr[1] << 8) |
                        (ori_src_file_size_arr[2] << 16) | (ori_src_file_size_arr[3] << 24);
    //==> FW AES method
    //fd = Dorca20_SPI_Open(device, AES_ONLY, COM_SPEED);
    //Dorca20_SPI_Reset(fd);
    //Dorca20_SPI_KeyTrans(fd, key);
    LOGI("Dorca20SPIDecryptfile szie %d", align_size * chunk_size);
#ifdef ENCODE_ONCE
    //Dorca20_SPI_DecipherBlock(fd, src_mem, dest_mem, align_size * chunk_size);
#else
    for(i = 0; i < align_size; i++) {
    //    Dorca20_SPI_DecipherBlock(fd,src_mem+i*chunk_size,dest_mem+i*chunk_size,chunk_size);
    }
#endif
    LOGI("Dorca20SPIDecryptfile decrypt done");
    destF = fopen(dest_file, "wb");
    if (destF) {
        fwrite(dest_mem, ori_src_file_size, 1, destF);
        fsync(destF);
        fclose(destF);
        LOGI("dest_mem saved to %s\n", dest_file);
    }
    else {
        LOGE("save dest_mem fopen errno: %s\n", strerror(errno));
    }
    free(src_mem);
    free(dest_mem);
    //Doraca20_SPI_Close(fd);
//Dorca20Done();
    LOGI("*** Dorca20Decryptfile finished");
    return time;
}