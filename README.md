# MiniImageEditor

一款从零搭建的“小型修图 App”，包含首页、相册、编辑器与导出。技术栈覆盖 Kotlin、Jetpack、MediaStore、OpenGL ES 2.0、协程、Room、R8 混淆，并已接入 CameraX 拍照与 ExoPlayer 视频预览。

**核心功能截图**
- 首页（品牌位 + 主按钮 + 工具网格）：
  - ![首页](./docs/images/home.png)
- 编辑器（深色工作区 + 画布 + 底部工具条 + 裁剪框 + 滤镜/调色面板）：
  - ![编辑器](./docs/images/editor.png)


## 功能概览
- 首页：原生控件搭建；自定义扫光视图 `ShimmerImageView`。
- 相册：MediaStore 异步拉取图片/视频缩略图，视频显示播放符号与时长角标。
- 编辑器：OpenGL ES 着色器管线渲染纹理，支持捏合缩放（焦点跟随）与单指平移；裁剪框拖拽与固定比例（自由/1:1/3:4/9:16）；撤销/重做；模式切换（编辑/滤镜/调色）。
- 滤镜：四种滤镜实时预览（去灰增白、暗调增彩、暗光提亮、黑白）。
- 调色：亮度/对比度/曝光三项滑条，居中为 0，左右滑动实时生效。
- 导出：将编辑操作与裁剪结果应用到原图，保存 PNG 至 `Pictures/MiniEdit`。
- 拍照与视频：CameraX 拍照保存到相册；ExoPlayer 播放相册视频。

### 拼图模块（新增）
- 入口：首页“拼图”按钮进入选择页 `CollageSelectActivity`。
- 选择页：
  - 顶部系统风格导航；提示“请选择照片，并选择拼图方式”。
  - 拼图方式单选行：横拼、竖拼、四宫格（2×2）、九宫格（3×3）。
  - 底部状态栏增高，右下角垂直按钮：导入画布（系统相册）与开始拼图。
  - “导入画布”使用 Android 13+ 系统照片选择器，仅图片；选择后弹出可缩放预览确认再设为背景。
  - 选择了图片与方式后，“开始拼图”可用并直接进入预览保存页。
- 预览页：
  - 横/竖拼 `CollageLinearActivity`：统一输出基准 2048 像素高度/宽度，按方向居中排列并支持背景绘制。
  - 宫格 `CollageGridActivity`：正方形 2048×2048，按中心裁剪缩放铺满 2×2 或 3×3 网格，支持背景绘制。
  - 自由拼 `CollageFreeActivity`：白底画布，可拖拽/缩放/旋转已选图，支持从素材库更换背景；底部“保存到本地”。
  - 顶部返回栏样式与相册一致；预览页标题置空，仅保留返回箭头。

## 构建与运行
- 环境准备
  - 安装 Android Studio（2023.1+），SDK Platform 34/36，Build-Tools 34.x+；JDK 17。
  - 导入目录：`android/miniimageeditor`。
- 调试运行
  - 选择 `app` 模块，点击 Run；首页→相册→编辑→滤镜/调色→导出完整流程可运行。
  - 拼图流程：首页→拼图→选择图片与方式→开始拼图→预览→保存。


## 适配与权限
- Android 13+ 使用 `READ_MEDIA_IMAGES/VIDEO`；Android 12- 使用 `READ_EXTERNAL_STORAGE`。
- 使用 MediaStore 读写，避免直写外部存储；权限失败提供提示与重试（各厂商弹窗差异可通过设置引导增强）。
- 系统照片选择器（Android 13+）：UI包含“Photos/Albums”，为系统固定设计，仅能限制类型为图片；无法隐藏“Albums”。

## 技术实现
- 主页与相册：`RecyclerView` 网格 + `Material` 按钮；Coil 支持 GIF/WebP；视频缩略图播放符号与时长角标。
- OpenGL 编辑器：顶点/片元着色器绘制四边形纹理，MVP 矩阵实现缩放与平移；捏合按焦点缩放并校正平移；裁剪框叠层交互与导出映射；撤销/重做基于状态快照快速恢复。
- 滤镜与调色（实时预览）：在片段着色器中通过 Uniform 注入 `uFilterMode/uBrightness/uContrast/uExposure` 实时处理；滤镜包含高亮提升、暗部增彩、暗光提亮与黑白。
- 导出一致性：CPU 端 `BitmapUtils.applyEffects()` 以同样参数与近似公式应用到裁剪后的位图，保证保存图与预览一致。
- 协程与 Jetpack：`ViewModel` + `StateFlow` 驱动异步加载；Room 记录导出历史。
- 混淆：`app/proguard-rules.pro` 保留 OpenGL/Room/Coil 必要类。

### 拼图实现细节
- 选择页：`CollageSelectActivity.kt` 使用 `MaterialButtonToggleGroup` 管理方式选择；“导入画布”走 `PickVisualMedia.ImageOnly` 并弹出缩放预览确认。
- 横/竖拼：`CollageLinearActivity.kt` 对各图片按方向统一尺寸缩放，先绘制背景位图（可选）再拼接。
- 宫格：`CollageGridActivity.kt` 计算每格尺寸，中心裁剪再缩放铺满；输出 2048×2048。
- 自由拼：`ui/TransformableImageView.kt` 支持拖拽/缩放/旋转与越界约束；`BackgroundPickerDialog.kt` 提供素材背景选择。

## 排错与性能
- 日志标签：`MediaRepo`、`Album`、`Editor`；使用 Logcat 过滤。
- Profiler：检查编辑器缩放/平移时 CPU 与内存峰值；必要时启用大图采样解码与纹理复用。
 - 拼图输出统一 2048 基准，兼顾清晰度与内存占用；背景绘制前优先中心裁剪，避免超大位图带来额外分配。

## 项目报告（摘要）
- 设计取舍：优先打通“选图→编辑→导出”主链路；编辑器着色器管线采用简化 MVP 以保证易用性与可维护性。
- 难点与解决：
  - 权限与 MediaStore 适配：区分 33+ 与旧版权限；统一保存到标准相册路径，降低机型差异。
  - OpenGL 坐标与裁剪映射：屏幕坐标到原图像素反映射，捏合缩放引入焦点 NDC 修正保持手感稳定；保留将裁剪移至 GPU 纹理坐标反变换的升级空间。
  - 视频预览：使用 Media3 ExoPlayer，复用相册跳转逻辑，保证播放链路简单稳定。
- 过滤与调色：预览在 GPU 实时，导出在 CPU 端按参数一致实现；统一 UI 风格与可读性，面板白底黑字（详见 `docs/UI_STYLE.md`）。
- 后续规划：接入 CameraX 高级能力（对焦/曝光/网格）、精确裁剪坐标反变换、完整工具栏与滑杆参数面板、AI 人像优化模块。

## 目录索引
- 主页：`app/src/main/java/com/example/miniimageeditor/MainActivity.kt`
- 相册：`app/src/main/java/com/example/miniimageeditor/AlbumActivity.kt`
- 媒体库：`app/src/main/java/com/example/miniimageeditor/media/MediaStoreRepository.kt`
- 编辑器：`app/src/main/java/com/example/miniimageeditor/EditorActivity.kt`、`app/src/main/java/com/example/miniimageeditor/gl/ImageEditorRenderer.kt`
- 拼图：`CollageSelectActivity.kt`、`CollageModeActivity.kt`、`CollageLinearActivity.kt`、`CollageGridActivity.kt`、`CollageFreeActivity.kt`
- 自定义视图：`app/src/main/java/com/example/miniimageeditor/ui/ShimmerImageView.kt`、`ui/CropOverlayView.kt`
- 样式与可读性：`app/src/main/res/values/styles.xml`、`docs/UI_STYLE.md`
- 混淆：`app/proguard-rules.pro`


