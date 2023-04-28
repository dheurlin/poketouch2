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

private object GameBoyDimensions {
    const val width = 160
    const val height = 144
    const val rowLength = 256
    val rowLengthDiff get() = rowLength - width
}

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

        private var data: ByteBuffer? = null

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

            gl.glTexImage2D(
                GL10.GL_TEXTURE_2D,
                0,
                GL10.GL_RGB,
                GameBoyDimensions.rowLength,
                GameBoyDimensions.height,
                0,
                GL10.GL_RGB,
                GL10.GL_UNSIGNED_SHORT_5_6_5,
                null
            )
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            gl.glViewport(0, 0, width, height);
            setTextureCordinates(width, height)
        }

        private fun setTextureCordinates(screenWidth: Int, screenHeight: Int) {
            val nearestIntegerScaleFactor = screenWidth / GameBoyDimensions.width
            val marginXInPixels = (screenWidth - ((GameBoyDimensions.rowLengthDiff) * nearestIntegerScaleFactor)) / 2
            val marginYInPixels = (screenHeight - (GameBoyDimensions.height * nearestIntegerScaleFactor)) / 2

            val minX = 0.0f - (marginXInPixels.toFloat() / screenWidth .toFloat())
            val maxX = 1.0f + (marginXInPixels.toFloat() / screenWidth .toFloat())
            val minY = 0.0f - (marginYInPixels.toFloat() / screenHeight.toFloat())
            val maxY = 1.0f + (marginYInPixels.toFloat() / screenHeight.toFloat())

            println("minXFraction, $minX")
            println("minYFraction, $minY")
            println("maxXFraction, $maxX")
            println("maxYFraction, $maxY")

            texcoords.apply {
                put(0, minX); put(1, minY)
                put(2, maxX); put(3, minY)
                put(4, minX); put(5, maxY)
                put(6, maxX); put(7, maxY)
                rewind()
            }
        }

        override fun onDrawFrame(gl: GL10) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT)

            gl.glActiveTexture(GL10.GL_TEXTURE0);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texId);

            gl.glTexSubImage2D(
                GL10.GL_TEXTURE_2D,
                0,
                0,
                0,
                GameBoyDimensions.rowLength,
                GameBoyDimensions.height,
                GL10.GL_RGB,
                GL10.GL_UNSIGNED_SHORT_5_6_5,
                data
            );

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texcoords);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        }

        fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
            data = buffer
        }

    }
}

private fun makeFloatBuffer(vararg array: Float): FloatBuffer {
    return ByteBuffer.allocateDirect(array.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(array).apply { rewind() }
}
