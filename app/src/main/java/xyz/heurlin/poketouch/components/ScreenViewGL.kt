package xyz.heurlin.poketouch.components

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import xyz.heurlin.poketouch.emulator.libretro.IScreenView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ScreenViewGL(private val context: Context) : GLSurfaceView(context), IScreenView {
    private val renderer = Renderer(context)

    init {
        setEGLContextClientVersion(1);
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        holder.setFormat(PixelFormat.RGB_565)
    }

    override fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        renderer.videoRefresh(buffer, width, height, pitch)
    }

    override fun videoRender() {
        requestRender()
    }

    class Renderer(val context: Context) : GLSurfaceView.Renderer {
        private val textures = intArrayOf(0)
        private val texId get() = textures[0]

        private val data = ByteBuffer.allocateDirect(144 * 256 * 2)

        private val vertex = makeFloatBuffer(
            -1.0f, +1.0f, 0.0f,
            +1.0f, +1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            +1.0f, -1.0f, 0.0f
        )

        private val texcoords = makeFloatBuffer(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            gl.glGenTextures(1, textures, 0);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texId);

            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

//            gl.glPixelStorei(GL10.GL_UNPACK_ALIGNMENT, 4)

            gl.glTexImage2D(
                GL10.GL_TEXTURE_2D,
                0,
                GL10.GL_RGB,
                160,
                144,
                0,
                GL10.GL_RGB,
                GL10.GL_UNSIGNED_SHORT_5_6_5,
                null
            )
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            gl.glViewport(0, 0, width, height);
        }

        override fun onDrawFrame(gl: GL10) {
            gl.glActiveTexture(GL10.GL_TEXTURE0);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texId);

            gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, 160, 144,
                GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5, data);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texcoords);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        }

        fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
            data.rewind()
            data.put(buffer)
            data.rewind()
        }

    }
}

private fun makeFloatBuffer(vararg array: Float): FloatBuffer {
    return ByteBuffer.allocateDirect(array.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(array).apply { rewind() }
}
