用例：图片美化跳转验证

前置：安装到设备；授予照片权限（或仅选择单张模式）。

1. 首页点击“图片美化”→相册→选择一张图片
   - 期望：进入 `BeautyActivity`，显示裁剪/调色/滤镜面板；Logcat 有 `Main/Album/Beauty` 结构化日志。

2. 相册权限拒绝后选择单张图片（系统选择器）
   - 期望：直接进入 `BeautyActivity`；无跳首页；日志包含 `pickVisual uri`。

3. 选择损坏图片或不可读 URI
   - 期望：页面留在 `BeautyActivity`，显示示例图与吐司提示；`BeautyActivity` 输出异常日志但不导航。

4. 连续切换编辑/调色/滤镜并保存
   - 期望：保存成功吐司；MediaStore 生成图片；日志包含 `saveToAlbum ok=true`。

5. 相册网格选择视频
   - 期望：进入 `VideoPlayerActivity`；与美化流程互不影响。

6. 返回键行为
   - 期望：从美化页返回至相册；不存在自动跳首页。

注意观察：若意外跳首页，检查是否出现进程崩溃（Logcat fatal），并记录最后一条 `Beauty/Album` 日志定位原因。
