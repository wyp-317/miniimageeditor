# MiniImageEditor

一款从零搭建的“小型修图 App”，包含首页、相册、编辑器与导出。技术栈覆盖 Kotlin、Jetpack、MediaStore、OpenGL ES 2.0、协程、Room、R8 混淆，并已接入 CameraX 拍照与 ExoPlayer 视频预览。

**核心功能截图**
- 首页（品牌位 + 主按钮 + 工具网格）：
  - `https://trae-api-sg.mchost.guru/api/ide/v1/text_to_image?prompt=%5BUI%20mock%5D%20mobile%20photo%20editor%20home%20screen%2C%20large%20logo%20with%20neon%20green%20glow%2C%20two%20primary%20buttons%20%28import%20photo%20black%20CTA%2C%20mint%20camera%20button%29%2C%20two%20secondary%20tiles%20%28AI%20portrait%2C%20collage%29%2C%20below%20a%204-column%20grid%20of%20tools%2C%20clean%20white%20background%2C%20material%20rounded%20corners%2C%20Chinese%20labels%20similar%20to%20Xingtu&image_size=portrait_16_9`
- 相册（三列网格 + 视频播放标 + 时长角标）：
  - `https://trae-api-sg.mchost.guru/api/ide/v1/text_to_image?prompt=%5BUI%20mock%5D%20gallery%20screen%2C%20dark%20appbar%20title%20%E6%9C%AC%E5%9C%B0%E7%9B%B8%E5%86%8C%2C%203-column%20thumbnail%20grid%2C%20video%20tile%20with%20center%20play%20icon%20and%20duration%20badge%2C%20rounded%20thumbnails%2C%20Chinese%20tabs%20and%20chips%2C%20style%20similar%20to%20Xingtu&image_size=portrait_16_9`
- 编辑器（深色工作区 + 画布 + 底部工具条 + 裁剪框）：
  - `https://trae-api-sg.mchost.guru/api/ide/v1/text_to_image?prompt=%5BUI%20mock%5D%20photo%20editor%20screen%2C%20dark%20workspace%2C%20centered%20canvas%20with%20crop%20overlay%20rectangle%2C%20top%20toolbar%20%E7%BC%96%E8%BE%91%E5%99%A8%2C%20export%20FAB%20at%20bottom-right%2C%20material%20style%2C%20Chinese%20labels&image_size=portrait_16_9`

> 提示：实际运行时请以真机截图替换以上占位图链接，以形成真实交付材料。

## 功能概览
- 首页：原生控件搭建，仿“醒图”布局；自定义扫光视图 `ShimmerImageView`。
- 相册：MediaStore 异步拉取图片/视频缩略图，视频显示播放符号与时长角标。
- 编辑器：OpenGL ES 着色器管线渲染纹理，支持捏合缩放（焦点跟随）与单指平移；裁剪框拖拽与固定比例（自由/1:1/3:4/9:16）；撤销/重做。
- 导出：将编辑操作与裁剪结果应用到原图，保存 PNG 至 `Pictures/MiniEdit`。
- 拍照与视频：CameraX 拍照保存到相册；ExoPlayer 播放相册视频。

## 构建与运行
- 环境准备
  - 安装 Android Studio（2023.1+），SDK Platform 34/36，Build-Tools 34.x+；JDK 17。
  - 导入目录：`android/miniimageeditor`。
  - 连接真机（建议至少两台不同品牌），开启 USB 调试。
- 调试运行
  - 选择 `app` 模块，点击 Run；首页→相册→编辑→导出完整流程可运行。
- 生成构建产物
  - APK：`Build > Build Bundle(s) / APK(s) > Build APK(s)`，输出路径通常为 `app/build/outputs/apk/release/app-release.apk`。
  - AAB：`Build > Build Bundle(s) / APK(s) > Build Bundle(s)`，输出路径通常为 `app/build/outputs/bundle/release/app-release.aab`。
  - 使用向导签名：`Build > Generate Signed Bundle / APK...`，新建或选择密钥库，填写别名与密码后生成签名包；建议使用演示密钥提交评审，正式密钥请自行保管。
  - 命令行（可选）：在 `android/miniimageeditor` 目录执行 `./gradlew assembleRelease` 与 `./gradlew bundleRelease`（需在 `app/build.gradle` 配置 `signingConfigs`）。
- 签名配置（可选）
  - 在 Android Studio 中创建签名密钥并配置 `Build > Generate Signed Bundle / APK...`。
  - 或在 `app/build.gradle` 中加入 `signingConfigs`，并通过 `gradle.properties` 引用敏感信息（避免明文入库）。

## 适配与权限
- Android 13+ 使用 `READ_MEDIA_IMAGES/VIDEO`；Android 12- 使用 `READ_EXTERNAL_STORAGE`。
- 使用 MediaStore 读写，避免直写外部存储；权限失败提供提示与重试（各厂商弹窗差异可通过设置引导增强）。

## 技术实现
- 主页与相册：`RecyclerView` 网格 + `Material` 按钮；Coil 支持 GIF/WebP；视频缩略图播放符号与时长角标。
- OpenGL 编辑器：顶点/片元着色器绘制四边形纹理，MVP 矩阵实现缩放与平移；捏合按焦点缩放并校正平移；裁剪框叠层交互与导出映射；撤销/重做基于状态快照快速恢复。
- 协程与 Jetpack：`ViewModel` + `StateFlow` 驱动异步加载；Room 记录导出历史。
- 混淆：`app/proguard-rules.pro` 保留 OpenGL/Room/Coil 必要类。

## 运行截图采集建议
- 首页、相册、编辑器分别在真机上截屏；将图片存放于 `android/miniimageeditor/docs/screenshots/`，并在本文替换占位链接。

## 排错与性能
- 日志标签：`MediaRepo`、`Album`、`Editor`；使用 Logcat 过滤。
- Profiler：检查编辑器缩放/平移时 CPU 与内存峰值；必要时启用大图采样解码与纹理复用。

## 基础项目报告（摘要）
- 设计取舍：优先打通“选图→编辑→导出”主链路；编辑器着色器管线采用简化 MVP 以保证易用性与可维护性。
- 难点与解决：
  - 权限与 MediaStore 适配：区分 33+ 与旧版权限；统一保存到标准相册路径，降低机型差异。
  - OpenGL 坐标与裁剪映射：屏幕坐标到原图像素反映射，捏合缩放引入焦点 NDC 修正保持手感稳定；保留将裁剪移至 GPU 纹理坐标反变换的升级空间。
  - 视频预览：使用 Media3 ExoPlayer，复用相册跳转逻辑，保证播放链路简单稳定。
- 后续规划：接入 CameraX 高级能力（对焦/曝光/网格）、精确裁剪坐标反变换、完整工具栏与滑杆参数面板、AI 人像优化模块。

## 目录索引
- 主页：`app/src/main/java/com/example/miniimageeditor/MainActivity.kt`
- 相册：`app/src/main/java/com/example/miniimageeditor/AlbumActivity.kt`
- 媒体库：`app/src/main/java/com/example/miniimageeditor/media/MediaStoreRepository.kt`
- 编辑器：`app/src/main/java/com/example/miniimageeditor/EditorActivity.kt`、`app/src/main/java/com/example/miniimageeditor/gl/ImageEditorRenderer.kt`
- 自定义视图：`app/src/main/java/com/example/miniimageeditor/ui/ShimmerImageView.kt`、`ui/CropOverlayView.kt`
- 混淆：`app/proguard-rules.pro`

---
若需要我在本地生成并提交 `app-release.apk` 与 `app-release.aab`（签名可使用演示密钥或你的正式密钥），请在 Android Studio 中执行上述步骤或提供构建环境权限，我也可以为你添加 CI（如 GitHub Actions）在远程自动打包产物。 
## 进阶任务完成报告
- 详见 `android/miniimageeditor/docs/ADVANCED_REPORT.md`。
