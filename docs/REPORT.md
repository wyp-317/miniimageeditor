# 基础项目报告

## 整体设计
- 模块划分：主页、相册、编辑器、导出、拍照、数据层（Room 历史记录）。
- 技术栈：Kotlin + Jetpack（ViewBinding、Lifecycle、协程）、MediaStore 读取与写入、OpenGL ES 2.0 纹理渲染、CameraX 拍照、Media3 ExoPlayer 视频预览、Coil 图片/视频缩略图、Room 持久化。
- 交互主线：选图/拍照 → 编辑（缩放、平移、裁剪） → 导出到相册；相册页支持图片与视频混排、视频时长与播放标识。

## 开发遇到的困难
- 构建与网络：Gradle/AGP 版本要求高，国内仓库与 SDK 下载不稳定。
- 资源与图标：自适应图标前景缺失、占位资源编译错误、PNG 规范问题导致 AAPT2 报错。
- 编辑器交互：缩放/平移手感不佳、裁剪比例约束与越界控制、坐标反映射到原图切图。
- 相机预览：返回编辑器后复到拍照页出现黑屏、权限处理时序问题。
- 媒体缩略图：视频封面未显示，需要统一加载首帧。
- 签名与产物：签名文件创建失败、路径与权限、生成 APK/AAB 的规范化交付。

## 解决思路
- 构建与网络：在 `settings.gradle` 配置镜像源，Gradle Wrapper 升级；Android Studio 使用内置 JDK 17；必要时走命令行与离线缓存。
- 资源与图标：自适应图标引用统一到前景资源名，提供占位 XML；异常图片转移到 `drawable-nodpi` 或重新导出为规范 PNG；清理并重建确保通过。
- 编辑器交互：
  - 缩放/平移：`gl/ImageEditorRenderer.kt` 维护 MVP 矩阵与状态；`EditorActivity.kt` 使用 `ScaleGestureDetector` 与 `GestureDetector`，并增加双击复位与速度系数调优。
  - 裁剪比例：`ui/CropOverlayView.kt` 维护 `aspectRatio` 并在角/边拖拽时强制比，`constrain()` 校正最小尺寸与越界。
  - 坐标映射：导出时读取当前缩放与平移，将视图坐标反映射为原图像素坐标后切图。
- 相机预览：`CameraActivity.kt` 在 `onResume()` 重新 `startCamera()`，`onStop()` 解绑，保存后 `EditorActivity` `finish()` 返回上页立即恢复预览。
- 媒体缩略图：相册 `AlbumActivity.kt` 使用 Coil 的视频帧支持，加载第 0ms 首帧作为封面，统一视觉体验。
- 签名与产物：通过 Android Studio 向导创建 `.jks` 于可写目录（用户文档路径），记录别名与密码；生成签名 APK/AAB，放入 `release/` 并在 README 提供下载入口。
