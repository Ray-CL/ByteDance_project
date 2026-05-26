# ImageFlow

ImageFlow 是一个 Kotlin Android 应用，展示双列图片流与详情页，使用 OkHttp 从 TheCatAPI 拉取图片并进行本地缓存，避免重复下载。

## 功能
- 双列图片网格（RecyclerView + GridLayoutManager）
- 详情页大图与元数据（日期、大小、路径）
- 推荐 / 历史记录 / 收藏 Tab
- OkHttp 网络请求
- 磁盘缓存（内部文件）+ 内存缓存（LruCache）
- 下拉刷新与无限滚动
- 清除缓存
- 清除历史记录
- 收藏与取消收藏

## 构建与运行
1. 使用 Android Studio 打开项目。
2. 同步 Gradle。
3. 运行到设备或模拟器（minSdk 21）。

## 说明
- 图片文件已存在时直接从本地加载，不会重复下载。
- 缓存存放于应用内部 files 目录。

## 交付物
- 技术方案：docs/technical-plan.md
- 方案设计与思考过程：docs/design-process.md
- 学习总结：docs/learning-summary.md
- 演示视频：单独录制并提交
## 演示视频
[点击观看演示视频](homework/Screen_recording.mp4)
