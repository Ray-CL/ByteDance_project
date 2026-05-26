# 方案设计与思考过程

## 需求概述
- 双列图片流与详情页。
- 使用 OkHttp 进行网络请求。
- 本地缓存避免重复下载。
- 推荐、历史记录与收藏 Tab。
- 需要明确的方案描述便于评审。

## 关键决策
1. 图片源
   - 选择 TheCatAPI，画质更好且无需 API Key。

2. UI 结构
   - 使用 ViewPager2 + TabLayout 支持左右滑动切换。
   - 两个 Tab 复用同一列表布局，降低重复代码。

3. 数据层
   - 使用 ImageRepository 统一处理网络与缓存。
   - 使用 HistoryStore + SharedPreferences 快速持久化历史。
   - 使用 FavoriteStore + SharedPreferences 持久化收藏。

4. 缓存策略
   - 磁盘缓存图片文件，避免重复下载。
   - LruCache 缓存 Bitmap，减少重复解码。
   - OkHttp 缓存复用网络响应。

5. 并发策略
   - 协程 + Dispatchers.IO 将网络和磁盘 IO 放在后台线程。

## 权衡点
- SharedPreferences 简单快速，但不适合大规模数据。
- ViewPager2 有一定开销，但满足滑动切换体验。
- 使用内部存储实现磁盘缓存，后续可补充容量控制与淘汰。

## 迭代过程
- 先完成单列表页面，再加入 TabLayout。
- 由仅点击切换升级为 ViewPager2 支持滑动。
- 增加清除缓存与清除历史记录操作。
- 增加收藏与取消收藏入口。

## 风险与应对
- 大图导致内存压力：使用 LruCache 复用 Bitmap。
- 网络不稳定：通过 Toast 反馈错误，列表可继续使用。
- 缓存增长：规划后续引入 Room 管理淘汰策略。

## 后续优化方向
- 按视图尺寸进行图片下采样。
- 增加重试/退避与离线提示。
- 增加收藏与下载管理功能。
- 引入更健壮的分页状态管理。
