# 技术方案

## 目标
- 实现双列图片流与详情页。
- 使用 OkHttp 获取图片元数据与图片文件。
- 进行本地缓存，避免重复下载。
- 提供可左右滑动的推荐 / 历史记录 / 收藏 Tab。

## 架构
- UI：单 Activity + ViewPager2 + TabLayout。
- 页面：RecommendFragment、HistoryFragment、FavoriteFragment 复用同一列表布局。
- 详情：DetailActivity 展示大图与元数据。
- 数据：ImageRepository 统一处理网络与磁盘缓存。
- 历史：HistoryStore 使用 SharedPreferences 持久化浏览记录。
- 收藏：FavoriteStore 使用 SharedPreferences 持久化收藏记录。

## 数据流
1. RecommendFragment 从 TheCatAPI 拉取图片元数据。
2. RecyclerView 绑定时调用 ImageRepository 加载图片。
3. Repository 先查本地文件，缺失则用 OkHttp 下载并落盘。
4. Adapter 更新图片与描述信息。
5. 点击后写入历史并进入详情页。
6. HistoryFragment 读取历史并复用同一图片加载逻辑。
7. FavoriteFragment 读取收藏列表并复用同一图片加载逻辑。

## 缓存策略
- 磁盘：filesDir/images/image_<id>.jpg
- 内存：LruCache 缓存解码后的 Bitmap
- HTTP：OkHttp 缓存复用响应

## 并发
- UI 使用 viewLifecycleOwner.lifecycleScope。
- IO 任务运行在 Dispatchers.IO。
- UI 更新在主线程完成。

## 交互行为
- 推荐页支持下拉刷新。
- 推荐页支持分页加载。
- Tab 重新点击可刷新内容。
- 菜单支持清除缓存与清除历史记录。
- 列表与详情页均支持收藏/取消收藏。

## 可扩展方向
- 引入 Room 记录缓存大小与淘汰策略。
- 增加收藏、下载管理功能。
- 增加离线提示与重试策略。
