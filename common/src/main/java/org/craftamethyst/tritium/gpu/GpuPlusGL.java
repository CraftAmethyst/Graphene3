package org.craftamethyst.tritium.gpu;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import org.craftamethyst.tritium.TritiumCommon;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public final class GpuPlusGL {

    // track immutable storage usage per GL buffer id for safe updates
    private static final Int2BooleanOpenHashMap bufferImmutable = new Int2BooleanOpenHashMap();
    private static final Int2LongOpenHashMap bufferSizes = new Int2LongOpenHashMap();
    private static boolean initialized = false;
    private static boolean supportsBufferStorage = false;
    private static boolean supportsDSA = false;

    private GpuPlusGL() {
    }

    private static void ensureCaps() {
        if (initialized) {
            return;
        }

        GLCapabilities caps = GL.getCapabilities();
        if (caps == null) {
            TritiumCommon.LOG.warn("[GPU Plus] GL capabilities queried before context; disabling advanced GL features.");
            initialized = true;
            return;
        }

        supportsBufferStorage = caps.GL_ARB_buffer_storage;
        // For DSA we rely on core 4.5, which includes the modern DSA entry points.
        supportsDSA = caps.OpenGL45;

        TritiumCommon.LOG.info("[GPU Plus] Caps - BufferStorage: {} DSA: {}",
                supportsBufferStorage, supportsDSA);
        initialized = true;
    }

    // capability queries

    public static boolean supportsBufferStorage() {
        ensureCaps();
        return supportsBufferStorage;
    }

    public static boolean supportsDSA() {
        ensureCaps();
        return supportsDSA;
    }

    // buffer storage

    public static void bufferStorageOrData(int target, long size, int usage, int storageFlags) {
        ensureCaps();
        if (supportsBufferStorage) {
            ARBBufferStorage.glBufferStorage(target, size, storageFlags);
        } else {
            GL15.glBufferData(target, size, usage);
        }
    }

    public static void bufferStorageOrData(int target, ByteBuffer data, int usage, int storageFlags) {
        ensureCaps();
        if (supportsBufferStorage) {
            ARBBufferStorage.glBufferStorage(target, data, storageFlags);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    public static void uploadBoundBuffer(int target, ByteBuffer data, int usage, int storageFlags) {
        ensureCaps();
        if (data == null) {
            return;
        }

        int bindingEnum = switch (target) {
            case GL15.GL_ARRAY_BUFFER -> GL15.GL_ARRAY_BUFFER_BINDING;
            case GL15.GL_ELEMENT_ARRAY_BUFFER -> GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING;
            default -> GL15.GL_ARRAY_BUFFER_BINDING;
        };

        int buffer = GL11.glGetInteger(bindingEnum);
        if (buffer == 0) {
            // No buffer bound; fall back to classic glBufferData.
            GL15.glBufferData(target, data, usage);
            return;
        }

        uploadBuffer(target, buffer, data, usage, storageFlags);
    }

    public static void uploadBuffer(int target, int buffer, ByteBuffer data, int usage, int storageFlags) {
        ensureCaps();
        if (data == null) {
            return;
        }

        int size = data.remaining();

        GL15.glBindBuffer(target, buffer);
        GL15.glBufferData(target, data, usage);

        bufferSizes.put(buffer, size);
        bufferImmutable.put(buffer, false);
    }

    // direct State Access helpers (minimal subset)

    public static int createTexture(int target) {
        ensureCaps();
        if (supportsDSA) {
            // GL45: use glCreateTextures(target, n) and return the generated id
            int[] tmp = new int[1];
            GL45.glCreateTextures(target, tmp);
            return tmp[0];
        }

        // No DSA: fall back to classic glGenTextures
        return GL11.glGenTextures();
    }

    public static void textureStorage2D(int texture, int target, int levels, int internalFormat, int width, int height) {
        ensureCaps();

        if (supportsDSA) {
            // Core GL 4.5 DSA: target is encoded in the texture object, so we don't need it here
            GL45.glTextureStorage2D(texture, levels, internalFormat, width, height);
            return;
        }

        // fallback: emulate with binding + glTexImage2D for each level
        GL11.glBindTexture(target, texture);
        for (int level = 0; level < levels; level++) {
            int w = Math.max(1, width >> level);
            int h = Math.max(1, height >> level);
            GL11.glTexImage2D(target, level, internalFormat, w, h, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
    }

    public static void textureParameteri(int texture, int target, int pname, int param) {
        ensureCaps();

        if (supportsDSA) {
            GL45.glTextureParameteri(texture, pname, param);
            return;
        }

        GL11.glBindTexture(target, texture);
        GL11.glTexParameteri(target, pname, param);
    }

    public static void namedBufferStorage(int buffer, long size, int storageFlags, int usageFallback) {
        ensureCaps();

        if (supportsDSA) {
            GL45.glNamedBufferStorage(buffer, size, storageFlags);
            return;
        }

        // fall back to bind + bufferStorage/bufferData.
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
        if (supportsBufferStorage) {
            ARBBufferStorage.glBufferStorage(GL15.GL_ARRAY_BUFFER, size, storageFlags);
        } else {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, usageFallback);
        }
    }
}
