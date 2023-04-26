package xyz.heurlin.poketouch.components

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import xyz.heurlin.poketouch.emulator.libretro.IScreenView
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ScreenViewGL(context: Context) : GLSurfaceView(context), IScreenView {

    init {
        setRenderer(Renderer)
        holder.setFormat(PixelFormat.RGB_565)
    }

    override fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        Renderer.videoRefresh(buffer, width, height, pitch)
    }

    override fun videoRender() {
        requestRender()
    }

    private object Renderer : GLSurfaceView.Renderer {
        val textures = IntBuffer.allocate(1).also { it.put(0, 0) }
        val texId get() = textures[0]

        val vertex = makeFloatBuffer(
            -1.0f, -1.0f, // left-bottom
            -1.0f, 1.0f, // left-top
            1.0f, -1.0f, // right-bottom
            1.0f, 1.0f, // right-top
        )

        val texcoords = makeFloatBuffer(
            0.0f,  1.0f,
            0.0f,  0.0f,
            1.0f,  1.0f,
            1.0f,  0.0f,
        )

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            GLES20.glEnable(GLES20.GL_TEXTURE_2D)
            if (texId != 0) {
                GLES20.glDeleteTextures(1, textures)
            }

            GLES20.glGenTextures(1, textures)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)

            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width, height,
                0,
                GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_SHORT_5_6_5,
                ByteBuffer.allocate(1)
            )

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }

        fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
            GLES20.glTexSubImage2D(
                GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height,
                GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, buffer
            );

        }

        override fun onDrawFrame(gl: GL10) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertex);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texcoords);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        }
    }
}

fun makeFloatBuffer(vararg array: Float): FloatBuffer {
    return ByteBuffer.allocateDirect(array.size * 4).asFloatBuffer().apply {
        array.forEachIndexed { index, item -> put(index, item) }
    }
}