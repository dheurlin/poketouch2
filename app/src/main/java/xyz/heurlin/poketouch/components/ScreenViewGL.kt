package xyz.heurlin.poketouch.components

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES10
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import xyz.heurlin.poketouch.emulator.libretro.IScreenView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ScreenViewGL(context: Context) : GLSurfaceView(context), IScreenView {

    init {
        setRenderer(Renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        holder.setFormat(PixelFormat.RGB_565)
    }

    override fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        queueEvent {
            Renderer.videoRefresh(buffer, width, height, pitch)
        }
    }

    override fun videoRender() {
        requestRender()
    }

    private object Renderer : GLSurfaceView.Renderer {
        val textures: IntBuffer = IntBuffer.allocate(1).also { it.put(0, 0) }
        val texId get() = textures[0]

        val someBuf = ByteBuffer.allocateDirect(144 * 256 * 2)
        val data = ByteBuffer.allocateDirect(144 * 256 * 2)

//        val vertex = makeFloatBuffer(
//            -1.0f, -1.0f, // left-bottom
//            -1.0f, 1.0f, // left-top
//            1.0f, -1.0f, // right-bottom
//            1.0f, 1.0f, // right-top
//        )

        val vertex = makeFloatBuffer(
            -1.0f, +1.0f, 0.0f,
            +1.0f, +1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            +1.0f, -1.0f, 0.0f
        )

        val texcoords = makeFloatBuffer(
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            gl.glEnable(GL10.GL_TEXTURE_2D)
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

            if (texId != 0) {
                gl.glDeleteTextures(1, textures)
            }

            gl.glGenTextures(1, textures)
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texId)
            gl.glTexParameterx(
                GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST
            )
            gl.glTexParameterx(
                GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_NEAREST
            )

            gl.glTexImage2D(
                GL10.GL_TEXTURE_2D,
                0,
                GL10.GL_RGBA,
//                width, height,
                160, 144,
                0,
                GL10.GL_RGB,
                GL10.GL_UNSIGNED_SHORT_5_6_5,
                someBuf
            )

            gl.glBindTexture(GL10.GL_TEXTURE_2D, 0)
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texId)

//            gl.glTexImage2D(
//                GL10.GL_TEXTURE_2D,
//                0,
//                GL10.GL_RGBA,
////                width, height,
//                160, 144,
//                0,
//                GL10.GL_RGB,
//                GL10.GL_UNSIGNED_SHORT_5_6_5,
//                someBuf
//            )

            gl.glViewport(0, 0, width, height)
        }

        fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
//            GLES10.glBindTexture(GL10.GL_TEXTURE_2D, texId)
//            GLES10.glTexSubImage2D(
//                GL10.GL_TEXTURE_2D, 0, 0, 0, width, height,
//                GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5, buffer
//            );

            data.rewind()
            data.put(buffer)
            data.rewind()
        }

        override fun onDrawFrame(gl: GL10) {
            gl.glActiveTexture(GL10.GL_TEXTURE0)
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texId)

            gl.glTexSubImage2D(
                GL10.GL_TEXTURE_2D, 0, 0, 0, 160, 144,
                GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5, data
            );

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texcoords);

            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)

        }
    }
}

private fun makeFloatBuffer(vararg array: Float): FloatBuffer {
    return ByteBuffer.allocateDirect(array.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(array).apply { rewind() }
}
