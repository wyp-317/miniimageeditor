package com.example.miniimageeditor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\r\u001a\u00020\u000eH\u0002J\u0012\u0010\u000f\u001a\u00020\u000e2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0014J\u0010\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/example/miniimageeditor/EditorActivity;", "Landroidx/activity/ComponentActivity;", "()V", "binding", "Lcom/example/miniimageeditor/databinding/ActivityEditorBinding;", "gestureDetector", "Landroid/view/GestureDetector;", "renderer", "Lcom/example/miniimageeditor/gl/ImageEditorRenderer;", "scaleDetector", "Landroid/view/ScaleGestureDetector;", "vm", "Lcom/example/miniimageeditor/viewmodel/EditorViewModel;", "exportResult", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "saveToAlbum", "bitmap", "Landroid/graphics/Bitmap;", "app_debug"})
public final class EditorActivity extends androidx.activity.ComponentActivity {
    private com.example.miniimageeditor.databinding.ActivityEditorBinding binding;
    private com.example.miniimageeditor.viewmodel.EditorViewModel vm;
    @org.jetbrains.annotations.NotNull()
    private final com.example.miniimageeditor.gl.ImageEditorRenderer renderer = null;
    private android.view.ScaleGestureDetector scaleDetector;
    private android.view.GestureDetector gestureDetector;
    
    public EditorActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void exportResult() {
    }
    
    private final void saveToAlbum(android.graphics.Bitmap bitmap) {
    }
}