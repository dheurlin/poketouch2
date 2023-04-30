#include <jni.h>
#include <android/log.h>
#include <cstdio>
#include <cstring>
#include "headers/libretro.h"


JavaVM *jvm;
jobject my_frontend;

#define ROM_START      0x0000
#define ZEROPAGE_START 0xFF80
#define WRAM_START     0xC000
#define WRAM_PAGE_SIZE 0x1000
#define ROM_PAGE_SIZE  0x4000


unsigned char *zeropage_ptr = nullptr;
unsigned char *wram_ptr = nullptr;
unsigned char *rom_ptr = nullptr;

int16_t g_joy[RETRO_DEVICE_ID_JOYPAD_R3 + 1] = {0};

void set_memory_map_pointers(struct retro_memory_map *desc) {
    int i;
    for (i = 0; i < desc->num_descriptors; i++) {
        switch (desc->descriptors[i].start) {
            case ZEROPAGE_START:
                zeropage_ptr = static_cast<unsigned char *>(desc->descriptors[i].ptr);
                break;
            case WRAM_START:
                wram_ptr = static_cast<unsigned char *>(desc->descriptors[i].ptr);
                break;
            case ROM_START:
                rom_ptr = static_cast<unsigned char *>(desc->descriptors[i].ptr);
                break;
        }
    }
}

static void core_log(enum retro_log_level level, const char *fmt, ...) {
    char buffer[4096] = {0};
    static const enum android_LogPriority level_android[] = {
            ANDROID_LOG_DEBUG,
            ANDROID_LOG_INFO,
            ANDROID_LOG_WARN,
            ANDROID_LOG_ERROR
    };
    va_list va;

    va_start(va, fmt);
    vsnprintf(buffer, sizeof(buffer), fmt, va);
    va_end(va);

    if (level == 0)
        return;

    __android_log_write(level_android[level], "Gambatte Core", buffer);
}

static bool core_environment(unsigned cmd, void *data) {
    bool *bval;

    switch (cmd) {
        case RETRO_ENVIRONMENT_GET_LOG_INTERFACE: {
            auto *cb = (struct retro_log_callback *) data;
            cb->log = core_log;
            break;
        }
        case RETRO_ENVIRONMENT_GET_CAN_DUPE:
            bval = (bool *) data;
            *bval = true;
            break;
        case RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY:
        case RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY:
            *(const char **) data = ".";
            return true;

        case RETRO_ENVIRONMENT_SET_PIXEL_FORMAT:
            return true;

        case RETRO_ENVIRONMENT_SET_MEMORY_MAPS: {
            auto *desc = (retro_memory_map *) data;
            set_memory_map_pointers(desc);
            return true;
        }

        default:
            core_log(RETRO_LOG_DEBUG, "Unhandled env #%u", cmd);
            return false;
    }

    return true;
}

static void core_input_poll() {
    core_log(RETRO_LOG_DEBUG, "Calling core_input_poll");
    // TODO Implement
}

static int16_t core_input_state(unsigned port, unsigned device, unsigned index, unsigned id) {
    if (port || index || device != RETRO_DEVICE_JOYPAD)
        return 0;

    return g_joy[id];
}

static void core_audio_sample(int16_t left, int16_t right) {
    core_log(RETRO_LOG_INFO, "Calling audio_sample_sample\n");
}

#define SOUND_SAMPLES_PER_FRAME   35112

static size_t core_audio_sample_batch(const int16_t *data, size_t frames) {
    JNIEnv *env = nullptr;
    jvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);

    auto bb = env->NewDirectByteBuffer((void *) data, SOUND_SAMPLES_PER_FRAME * frames * 2 * 2);

    auto cls = env->GetObjectClass(my_frontend);
    auto mid = env->GetMethodID(cls, "audioBatchCallback", "(Ljava/nio/ByteBuffer;J)J");
    auto res = env->CallLongMethod(my_frontend, mid, bb, (jlong) frames);

    return res;
}

#define VIDEO_HEIGHT 144
#define VIDEO_BUFF_SIZE (256 * VIDEO_HEIGHT * sizeof(uint16_t))

static void core_video_refresh(const void *data, unsigned width, unsigned height, size_t pitch) {
    if (!data) return;

    JNIEnv *env = nullptr;
    jvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);

    auto bb = env->NewDirectByteBuffer((void *) data, VIDEO_BUFF_SIZE);
    auto cls = env->GetObjectClass(my_frontend);
    auto mid = env->GetMethodID(cls, "videoRefreshCallback", "(Ljava/nio/ByteBuffer;IIJ)V");
    env->CallVoidMethod(my_frontend, mid, bb, (jint) width, (jint) height, (jlong) pitch);
}


unsigned char *game_addr_to_real_addr(
        unsigned short bank,
        unsigned short game_addr,
        unsigned char *base_ptr,
        unsigned short section_start,
        unsigned short section_length
) {
    unsigned char *result = base_ptr + (game_addr - section_start);
    if (bank > 0) {
        result += (bank - 1) * section_length;
    }
    return result;
}

void read_zeropage(unsigned short address, unsigned short num_bytes, void *dest) {
    unsigned char *base_address = zeropage_ptr + (address - ZEROPAGE_START);
    memcpy(dest, base_address, num_bytes);
}

void read_wram(unsigned char bank, unsigned short address, unsigned short num_bytes, void *dest) {
    unsigned char *base_address = game_addr_to_real_addr(bank, address, wram_ptr, WRAM_START,
                                                         WRAM_PAGE_SIZE);
    memcpy(dest, base_address, num_bytes);
}

void write_wram_byte(unsigned char bank, unsigned short address, unsigned char byte) {
    unsigned char *base_address = game_addr_to_real_addr(bank, address, wram_ptr, WRAM_START,
                                                         WRAM_PAGE_SIZE);
    *base_address = byte;
}

void read_rom(unsigned char bank, unsigned short address, unsigned short num_bytes, void *dest) {
    unsigned char *base_address = game_addr_to_real_addr(bank, address, rom_ptr, ROM_START,
                                                         ROM_PAGE_SIZE);
    memcpy(dest, base_address, num_bytes);
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
extern "C"
JNIEXPORT void JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_retroInit(JNIEnv *env, jobject thiz) {
    env->GetJavaVM(&jvm);
    my_frontend = env->NewGlobalRef(thiz);

    retro_set_environment(core_environment);
    retro_set_video_refresh(core_video_refresh);
    retro_set_input_poll(core_input_poll);
    retro_set_input_state(core_input_state);
    retro_set_audio_sample(core_audio_sample);
    retro_set_audio_sample_batch(core_audio_sample_batch);
    retro_init();
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_coreLoadGame(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jbyteArray bytes) {
    jboolean isCopy;
    jbyte *b = env->GetByteArrayElements(bytes, &isCopy);

    struct retro_game_info info = {"pokecrystal.gbc", 0};
    info.size = env->GetArrayLength(bytes);
    info.data = b;

    auto res = retro_load_game(&info);
    env->ReleaseByteArrayElements(bytes, b, 0);
    return res;
}
extern "C"
JNIEXPORT void JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_readRomBytes_1(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jbyte bank,
                                                                           jint game_address,
                                                                           jbyteArray dest) {
    auto num_bytes = env->GetArrayLength(dest);
    unsigned char local_dest[num_bytes];
    read_rom(bank, game_address, num_bytes, local_dest);
    env->SetByteArrayRegion(dest, 0, num_bytes, reinterpret_cast<const jbyte *>(local_dest));
}
extern "C"
JNIEXPORT jint JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_retroRun(JNIEnv *env, jobject thiz) {
    return retro_run();
}
extern "C"
JNIEXPORT void JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_setInput(JNIEnv *env, jobject thiz,
                                                                     jboolean a, jboolean b,
                                                                     jboolean start,
                                                                     jboolean select, jboolean up,
                                                                     jboolean down, jboolean left,
                                                                     jboolean right) {
    for (auto &btn: g_joy) {
        btn = 0;
    }
    if (a) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_A] = 1;
    }
    if (b) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_B] = 1;
    }
    if (start) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_START] = 1;
    }
    if (select) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_SELECT] = 1;
    }
    if (up) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_UP] = 1;
    }
    if (down) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_DOWN] = 1;
    }
    if (left) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_LEFT] = 1;
    }
    if (right) {
        g_joy[RETRO_DEVICE_ID_JOYPAD_RIGHT] = 1;
    }
}
extern "C"
JNIEXPORT jlong JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_serializeSize(JNIEnv *env,
                                                                          jobject thiz) {
    return retro_serialize_size();
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_serializeState(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jbyteArray dest) {
    auto save_size = retro_serialize_size();
    unsigned char saveblob[save_size];
    if (!retro_serialize(saveblob, save_size)) {
        core_log(RETRO_LOG_ERROR, "Could not generate savestate, core returned error");
        return false;
    }

    env->SetByteArrayRegion(dest, 0, save_size, reinterpret_cast<const jbyte *>(saveblob));
    return true;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_xyz_heurlin_poketouch_emulator_libretro_LibretroBridge_deserializeState(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jbyteArray data) {
    jboolean isCopy;
    auto bytes = env->GetByteArrayElements(data, &isCopy);
    if (!retro_unserialize(bytes, env->GetArrayLength(data))) {
        core_log(RETRO_LOG_ERROR, "Could not load savestate, core returned error");
        return false;
    }
    return true;
}