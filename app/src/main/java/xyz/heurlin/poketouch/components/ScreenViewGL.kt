package xyz.heurlin.poketouch.components

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import xyz.heurlin.poketouch.emulator.libretro.IScreenView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private object GBDimensions {
    const val width = 160
    const val height = 144
    const val rowLength = 256
    const val bytesPerPixel = 2
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

        private var data: ByteBuffer = ByteBuffer.allocateDirect(
            GBDimensions.width * GBDimensions.height * GBDimensions.bytesPerPixel
        )
        private val dataMutex = Object()

        private val vertex = makeFloatBuffer(
            -1.0f, +1.0f, 0.0f, // top left
            +1.0f, +1.0f, 0.0f, // top right
            -1.0f, -1.0f, 0.0f, // bottom left
            +1.0f, -1.0f, 0.0f // bottom right
        )

        private val texcoords = makeFloatBuffer(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            GLES10.glEnable(GLES10.GL_TEXTURE_2D);
            GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
            GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);

            GLES10.glGenTextures(1, textures, 0);
            GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, texId);

            GLES10.glTexParameterx(
                GLES10.GL_TEXTURE_2D,
                GLES10.GL_TEXTURE_MAG_FILTER,
                GLES10.GL_NEAREST
            );
            GLES10.glTexParameterx(
                GLES10.GL_TEXTURE_2D,
                GLES10.GL_TEXTURE_MIN_FILTER,
                GLES10.GL_NEAREST
            );

            GLES10.glTexImage2D(
                GLES10.GL_TEXTURE_2D,
                0,
                GLES10.GL_RGB,
                GBDimensions.width,
                GBDimensions.height,
                0,
                GLES10.GL_RGB,
                GLES10.GL_UNSIGNED_SHORT_5_6_5,
                null
            )
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES10.glViewport(0, 0, width, height);
            resizeViewport(width, height)
        }

        private fun copyBuffer(src: ByteBuffer) {
            for (row in 0 until GBDimensions.height) {
                src.position(row * GBDimensions.rowLength * GBDimensions.bytesPerPixel)
                data.position(row * GBDimensions.width * GBDimensions.bytesPerPixel)
                for (pixel in 0 until GBDimensions.width * GBDimensions.bytesPerPixel) {
                    data.put(src.get())
                }
            }
            data.rewind()
        }

        private fun resizeViewport(screenWidth: Int, screenHeight: Int) {
            val nearestIntegerScaleFactor = screenWidth / GBDimensions.width
            val marginXInPixels =
                (screenWidth - ((GBDimensions.width) * nearestIntegerScaleFactor)) / 2
            val marginYInPixels =
                (screenHeight - (GBDimensions.height * nearestIntegerScaleFactor)) / 2

            val quadrantWidth = screenWidth.toFloat() / 2
            val quadrantHeight = screenHeight.toFloat() / 2

            val minX = -1.0f + (marginXInPixels.toFloat() / quadrantWidth)
            val maxX = 1.0f - (marginXInPixels.toFloat() /  quadrantWidth)
            val minY = -1.0f + (marginYInPixels.toFloat() / quadrantHeight)
            val maxY = 1.0f - (marginYInPixels.toFloat() /  quadrantHeight)

            println("minXFraction, $minX")
            println("minYFraction, $minY")
            println("maxXFraction, $maxX")
            println("maxYFraction, $maxY")

            vertex.apply {
                put(0, minX); put(1 , maxY)
                put(3, maxX); put(4 , maxY)
                put(6, minX); put(7 , minY)
                put(9, maxX); put(10, minY)
                rewind()
            }
        }

        override fun onDrawFrame(gl: GL10) {
            synchronized(dataMutex) {
                GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT)

                GLES10.glActiveTexture(GLES10.GL_TEXTURE0);
                GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, texId);

                GLES10.glTexSubImage2D(
                    GLES10.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    GBDimensions.width,
                    GBDimensions.height,
                    GLES10.GL_RGB,
                    GLES10.GL_UNSIGNED_SHORT_5_6_5,
                    data
                );

                GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, vertex);
                GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, texcoords);
                GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
            }
        }

        fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
            synchronized(dataMutex) {
                copyBuffer(buffer)
            }
        }

    }
}

private fun makeFloatBuffer(vararg array: Float): FloatBuffer {
    return ByteBuffer.allocateDirect(array.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(array).apply { rewind() }
}
