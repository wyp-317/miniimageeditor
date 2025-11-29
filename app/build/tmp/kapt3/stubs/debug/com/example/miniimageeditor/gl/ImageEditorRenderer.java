package com.example.miniimageeditor.gl;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0014\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\u0018\u0000 *2\u00020\u0001:\u0001*B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0018\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u0014H\u0002J\b\u0010\u0018\u001a\u00020\fH\u0002J\u0012\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0016J\"\u0010\u001d\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\u0006\u0010\u001e\u001a\u00020\f2\u0006\u0010\u001f\u001a\u00020\fH\u0016J\u001c\u0010 \u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0016J\u000e\u0010#\u001a\u00020\u001a2\u0006\u0010$\u001a\u00020%J\u0016\u0010&\u001a\u00020\u001a2\u0006\u0010\'\u001a\u00020%2\u0006\u0010(\u001a\u00020%J\b\u0010)\u001a\u00020\u001aH\u0002R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/example/miniimageeditor/gl/ImageEditorRenderer;", "Landroid/opengl/GLSurfaceView$Renderer;", "()V", "bitmap", "Landroid/graphics/Bitmap;", "getBitmap", "()Landroid/graphics/Bitmap;", "setBitmap", "(Landroid/graphics/Bitmap;)V", "mvp", "", "mvpHandle", "", "positionHandle", "program", "texCoordHandle", "textureId", "compileShader", "type", "code", "", "createProgram", "vs", "fs", "createTexture", "onDrawFrame", "", "gl", "Ljavax/microedition/khronos/opengles/GL10;", "onSurfaceChanged", "width", "height", "onSurfaceCreated", "config", "Ljavax/microedition/khronos/egl/EGLConfig;", "setScale", "scale", "", "setTranslation", "dx", "dy", "uploadBitmap", "Companion", "app_debug"})
public final class ImageEditorRenderer implements android.opengl.GLSurfaceView.Renderer {
    @org.jetbrains.annotations.Nullable()
    private android.graphics.Bitmap bitmap;
    private int textureId = 0;
    private int program = 0;
    private int positionHandle = 0;
    private int texCoordHandle = 0;
    private int mvpHandle = 0;
    @org.jetbrains.annotations.NotNull()
    private final float[] mvp = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String VERTEX_SHADER = "\n            attribute vec2 aPosition;\n            attribute vec2 aTexCoord;\n            uniform mat4 uMVPMatrix;\n            varying vec2 vTex;\n            void main() {\n                vec4 pos = vec4(aPosition, 0.0, 1.0);\n                gl_Position = uMVPMatrix * pos;\n                vTex = aTexCoord;\n            }\n        ";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String FRAGMENT_SHADER = "\n            precision mediump float;\n            varying vec2 vTex;\n            uniform sampler2D sTexture;\n            void main() {\n                gl_FragColor = texture2D(sTexture, vTex);\n            }\n        ";
    private static final java.nio.FloatBuffer quadVertices = null;
    private static final java.nio.FloatBuffer quadTex = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.miniimageeditor.gl.ImageEditorRenderer.Companion Companion = null;
    
    public ImageEditorRenderer() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.graphics.Bitmap getBitmap() {
        return null;
    }
    
    public final void setBitmap(@org.jetbrains.annotations.Nullable()
    android.graphics.Bitmap p0) {
    }
    
    public final void setTranslation(float dx, float dy) {
    }
    
    public final void setScale(float scale) {
    }
    
    @java.lang.Override()
    public void onSurfaceCreated(@org.jetbrains.annotations.Nullable()
    javax.microedition.khronos.opengles.GL10 gl, @org.jetbrains.annotations.Nullable()
    javax.microedition.khronos.egl.EGLConfig config) {
    }
    
    @java.lang.Override()
    public void onSurfaceChanged(@org.jetbrains.annotations.Nullable()
    javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
    }
    
    @java.lang.Override()
    public void onDrawFrame(@org.jetbrains.annotations.Nullable()
    javax.microedition.khronos.opengles.GL10 gl) {
    }
    
    private final int createTexture() {
        return 0;
    }
    
    private final void uploadBitmap() {
    }
    
    private final int createProgram(java.lang.String vs, java.lang.String fs) {
        return 0;
    }
    
    private final int compileShader(int type, java.lang.String code) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0006\u001a\n \b*\u0004\u0018\u00010\u00070\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n \b*\u0004\u0018\u00010\u00070\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/example/miniimageeditor/gl/ImageEditorRenderer$Companion;", "", "()V", "FRAGMENT_SHADER", "", "VERTEX_SHADER", "quadTex", "Ljava/nio/FloatBuffer;", "kotlin.jvm.PlatformType", "quadVertices", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}