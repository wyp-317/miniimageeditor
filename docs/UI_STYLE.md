# UI 样式规范（2025-12 更新）

## 全局按钮
- 背景色：`#FFFFFF`
- 文字颜色：`#000000`
- 圆角：`@dimen/corner_sm`
- 字号：`14sp`
- 内边距：水平 `12dp` / 垂直 `8dp`
- Ripple（hover/active）：`@color/button_ripple`（黑色 10% 透明）
- 样式资源：`@style/AppButton`，切换组使用 `@style/AppButton.Toggle`

## 编辑页面（裁剪）
- 按钮统一使用 `@style/AppButton`，高度 `40dp`。
- 撤销/重做按钮与比例按钮尺寸一致。

## 滤镜页面
- 四个滤镜按钮统一尺寸：等分宽度（`layout_weight=1`），固定高度 `48dp`。
- 文本最多两行，超出省略：`android:maxLines="2"`、`android:ellipsize="end"`。
- 选中/未选中状态尺寸不变。

## 调色页面
- 文本颜色统一为黑色（`@color/black`），在浅色背景下达到 WCAG AA（对比度≥4.5:1）。
- 面板背景：`@drawable/panel_bg`（白底、圆角、带内边距）。
- 移除右下角“保存到本地”按钮，保留顶部统一导出按钮。

## 面板背景与可读性
- 面板背景白色（`@drawable/panel_bg`），底栏透明，确保黑字在白底具备高对比度与可读性。

## 文件位置
- 样式资源：`res/values/styles.xml`
- 颜色资源：`res/values/colors.xml`（新增 `button_ripple`）
- 面板背景：`res/drawable/panel_bg.xml`

## 兼容与验证
- Android：通过布局与样式资源统一验证；预览界面与交互保持原逻辑。
- iOS：此仓库为 Android 项目，若需 iOS 同步样式，请参照本规范在 iOS 工程中创建对应样式（白底黑字、统一圆角与内边距、Ripple/Pressed 对应为 iOS 的 Highlight 状态），并进行视觉验证。

## 变更记录
- 统一按钮样式与尺寸。
- 调色面板可读性提升，去除局部保存按钮。
- 滤镜按钮文本换行/省略处理与尺寸一致化。
