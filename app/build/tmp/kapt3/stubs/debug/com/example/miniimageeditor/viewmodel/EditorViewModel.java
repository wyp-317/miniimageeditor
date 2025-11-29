package com.example.miniimageeditor.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0019\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\tR\u0019\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\t\u00a8\u0006\u0010"}, d2 = {"Lcom/example/miniimageeditor/viewmodel/EditorViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "app", "Landroid/app/Application;", "(Landroid/app/Application;)V", "currentMatrix", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Landroid/graphics/Matrix;", "getCurrentMatrix", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "exportBitmap", "Landroid/graphics/Bitmap;", "getExportBitmap", "sourceUri", "Landroid/net/Uri;", "getSourceUri", "app_debug"})
public final class EditorViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<android.net.Uri> sourceUri = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<android.graphics.Bitmap> exportBitmap = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<android.graphics.Matrix> currentMatrix = null;
    
    public EditorViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application app) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableStateFlow<android.net.Uri> getSourceUri() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableStateFlow<android.graphics.Bitmap> getExportBitmap() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableStateFlow<android.graphics.Matrix> getCurrentMatrix() {
        return null;
    }
}