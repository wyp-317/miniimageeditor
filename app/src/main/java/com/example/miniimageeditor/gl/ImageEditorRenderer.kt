package com.example.miniimageeditor.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageEditorRenderer : GLSurfaceView.Renderer {
    var bitmap: Bitmap? = null
    private var textureId = 0
    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var mvpHandle = 0
    private var filterModeHandle = 0
    private var brightnessHandle = 0
    private var contrastHandle = 0
    private var exposureHandle = 0

    // MVP matrix (only scale/translate here)
    private val mvp = FloatArray(16) { i -> if (i % 5 == 0) 1f else 0f }
    private var scale = 1f
    private var tx = 0f
    private var ty = 0f

    private var filterMode = 0
    private var brightness = 0f
    private var contrast = 0f
    private var exposure = 0f

    fun setTranslation(dx: Float, dy: Float) {
        tx += dx
        ty += dy
        mvp[12] = tx
        mvp[13] = ty
    }

    fun applyScale(factor: Float) {
        scale = (scale * factor).coerceIn(0.5f, 3.0f)
        mvp[0] = scale
        mvp[5] = scale
    }

    fun applyScaleAt(factor: Float, focusNdcX: Float, focusNdcY: Float) {
        val old = scale
        val next = (scale * factor).coerceIn(0.4f, 5.0f)
        val dx = (old - next) * focusNdcX
        val dy = (old - next) * focusNdcY
        scale = next
        tx += dx
        ty += dy
        mvp[0] = scale
        mvp[5] = scale
        mvp[12] = tx
        mvp[13] = ty
    }

    fun setTransform(newScale: Float, newTx: Float, newTy: Float) {
        scale = newScale.coerceIn(0.5f, 3.0f)
        tx = newTx
        ty = newTy
        mvp[0] = scale
        mvp[5] = scale
        mvp[12] = tx
        mvp[13] = ty
    }

    fun resetTransform() {
        scale = 1f
        tx = 0f
        ty = 0f
        mvp[0] = 1f
        mvp[5] = 1f
        mvp[12] = 0f
        mvp[13] = 0f
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        filterModeHandle = GLES20.glGetUniformLocation(program, "uFilterMode")
        brightnessHandle = GLES20.glGetUniformLocation(program, "uBrightness")
        contrastHandle = GLES20.glGetUniformLocation(program, "uContrast")
        exposureHandle = GLES20.glGetUniformLocation(program, "uExposure")
        textureId = createTexture()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        uploadBitmap()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)
        GLES20.glUniform1i(filterModeHandle, filterMode)
        GLES20.glUniform1f(brightnessHandle, brightness)
        GLES20.glUniform1f(contrastHandle, contrast)
        GLES20.glUniform1f(exposureHandle, exposure)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, quadVertices)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, quadTex)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun createTexture(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        return tex[0]
    }

    private fun uploadBitmap() {
        val bmp = bitmap ?: return
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
    }

    private fun createProgram(vs: String, fs: String): Int {
        val v = compileShader(GLES20.GL_VERTEX_SHADER, vs)
        val f = compileShader(GLES20.GL_FRAGMENT_SHADER, fs)
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, v)
        GLES20.glAttachShader(p, f)
        GLES20.glLinkProgram(p)
        val status = IntArray(1)
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e("Renderer", "Link error: ${GLES20.glGetProgramInfoLog(p)}")
            GLES20.glDeleteProgram(p)
            throw RuntimeException("Program link error")
        }
        return p
    }

    private fun compileShader(type: Int, code: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, code)
        GLES20.glCompileShader(s)
        val status = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e("Renderer", "Compile error: ${GLES20.glGetShaderInfoLog(s)}")
            GLES20.glDeleteShader(s)
            throw RuntimeException("Shader compile error")
        }
        return s
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec2 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMVPMatrix;
            varying vec2 vTex;
            void main() {
                vec4 pos = vec4(aPosition, 0.0, 1.0);
                gl_Position = uMVPMatrix * pos;
                vTex = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTex;
            uniform sampler2D sTexture;
            uniform int uFilterMode;
            uniform float uBrightness;
            uniform float uContrast;
            uniform float uExposure;
            void main() {
                vec4 c = texture2D(sTexture, vTex);
                float avg = (c.r + c.g + c.b) / 3.0;
                // Exposure gain (approximate linear)
                c.rgb = c.rgb * (1.0 + uExposure);
                // Contrast around 0.5
                c.rgb = (c.rgb - 0.5) * (1.0 + uContrast) + 0.5;
                // Brightness offset
                c.rgb += vec3(uBrightness);
                float lum = dot(c.rgb, vec3(0.299, 0.587, 0.114));
                if (uFilterMode == 1) {
                    // 去灰增白：提升高亮并适度增饱和
                    float w = smoothstep(0.6, 1.0, lum);
                    c.rgb = mix(c.rgb, c.rgb + vec3(0.12), w);
                    float satFactor = 1.15;
                    c.rgb = mix(vec3(avg), c.rgb, satFactor);
                } else if (uFilterMode == 2) {
                    // 暗调增彩：暗部提升饱和度，轻微加对比
                    float d = 1.0 - smoothstep(0.0, 0.5, lum);
                    float satFactor = 1.0 + 0.6 * d;
                    c.rgb = mix(vec3(avg), c.rgb, satFactor);
                    c.rgb = (c.rgb - 0.5) * 1.05 + 0.5;
                } else if (uFilterMode == 3) {
                    // 暗光提亮：暗部加亮
                    float lift = (1.0 - smoothstep(0.0, 0.5, lum)) * 0.22;
                    c.rgb += vec3(lift);
                } else if (uFilterMode == 4) {
                    // 黑白
                    c.rgb = vec3(lum);
                }
                c.rgb = clamp(c.rgb, 0.0, 1.0);
                gl_FragColor = c;
            }
        """

        private val quadVertices = java.nio.ByteBuffer.allocateDirect(4 * 2 * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f))
                position(0)
            }

        private val quadTex = java.nio.ByteBuffer.allocateDirect(4 * 2 * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f))
                position(0)
            }
    }

    fun currentScale(): Float = scale
    fun currentTranslation(): Pair<Float, Float> = tx to ty

    fun setFilterMode(mode: Int) { filterMode = mode }
    fun setBrightness(v: Float) { brightness = v }
    fun setContrast(v: Float) { contrast = v }
    fun setExposure(v: Float) { exposure = v }
}
