# FirOnlineTime
_✨一款轻量化便携性的Bukkit/Paper/Folia在线统计插件✨_

<div align="center">
    <img src="https://img.shields.io/badge/支持版本-1.16 ~ 1.21.8-brightgreen?style=flat-square" alt="minecraft-version">
    <img src="https://img.shields.io/badge/数据库-SQLite%20%7C%20MySQL-blue?style=flat-square" alt="database">
    <img src="https://img.shields.io/badge/占位符-PlaceholderAPI-orange?style=flat-square" alt="placeholderapi">
</div>

## 📌 关于
FirOnlineTime 是一款轻量化便携性的Bukkit在线统计插件。其支持使用SQLite和Mysql来存储玩家的在线时间，并提供了一些占位符来获取玩家的在线时间。
数据缓存会定时从数据库异步刷新，所以它可以很轻松的在群组服务器上使用。  
插件在遇到频繁请求时会采用缓存，不会出现过多的数据库请求，对于在线玩家是完全异步处理，对于离线玩家在第一次请求时会阻塞数据库数据，随后会缓存并异步定时更新。   
<br />

## 🔨 安装
1. 下载/构建最新的版本，将插件放入 `plugins` 文件夹。
2. 启动服务器，生成默认配置文件。
3. 打开 `settings.yml` 文件, 配置您的数据库信息，最后重启服务器。     
   <br />

## 💻 使用方法
命令:
```
 /fironlinetime reload -- 重载插件
```
您可以使用 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245)  插件来获取玩家的在线时间。

### 基础占位符

| 占位符                    | 说明             |
|------------------------|----------------|
| `%fotime_today%`       | 当日玩家的在线时间      |
| `%fotime_week%`        | 本周玩家的在线时间      |
| `%fotime_month%`       | 本月玩家的在线时间      |
| `%fotime_total%`       | 玩家的总共在线时间      |
| `%fotime_today_value%` | 当日玩家的在线时间的毫秒时间 |
| `%fotime_week_value%`  | 本周玩家的在线时间的毫秒时间 |
| `%fotime_month_value%` | 本月玩家的在线时间的毫秒时间 |
| `%fotime_total_value%` | 玩家的总共在线时间的毫秒时间 |
| `%fotime_today_days%`  | 玩家当日的在线日期数     |
| `%fotime_week_days%`   | 玩家本周的在线日期数     |
| `%fotime_month_days%`  | 玩家本月的在线日期数     |
| `%fotime_total_days%`  | 玩家总共的在线日期数     |

### 🔧 自定义格式化占位符

您可以使用独有的格式化器进行自定义格式化结果，语法格式为：
```
%fotime_{时间类型}_format_{格式化模板}%
```

#### 时间类型
- `today` - 当日在线时间
- `week` - 本周在线时间
- `month` - 本月在线时间
- `total` - 总在线时间

#### 格式化符号

| 格式符号 | 说明 | 示例 |
|---------|------|------|
| `{d}` | 天数（整数） | 3 |
| `{h}` | 剩余小时数（0-23） | 12 |
| `{m}` | 分钟数（0-59） | 30 |
| `{s}` | 秒数（0-59） | 15 |
| `{ms}` | 毫秒数（0-999） | 500 |
| `{D}` | 总天数（整数） | 3 |
| `{H}` | 总小时数（整数） | 84 |
| `{M}` | 总分钟数（整数） | 5040 |
| `{S}` | 总秒数（整数） | 302400 |
| `{T}` | 总毫秒数 | 302400000 |
| `{.1d}` | 精确天数（1位小数） | 3.5 |
| `{.2d}` | 精确天数（2位小数） | 3.50 |
| `{.1h}` | 精确小时数（1位小数） | 84.5 |
| `{.2h}` | 精确小时数（2位小数） | 84.50 |
| `{.1m}` | 精确分钟数（1位小数） | 5040.5 |
| `{.2m}` | 精确分钟数（2位小数） | 5040.50 |
| `{.1s}` | 精确秒数（1位小数） | 302400.5 |
| `{.2s}` | 精确秒数（2位小数） | 302400.50 |
| `{HH}` | 两位小时数 | 84 → "84" |
| `{mm}` | 两位分钟数 | 5 → "05" |
| `{ss}` | 两位秒数 | 5 → "05" |

#### 使用示例

**基础格式化：**
- `%fotime_week_format_{d}天{h}小时{m}分钟%` → "3天12小时30分钟"
- `%fotime_total_format_{H}:{mm}:{ss}%` → "84:30:15"

**总时间显示：**
- `%fotime_total_format_总分钟数: {M}%` → "总分钟数: 5070"
- `%fotime_week_format_总天数: {D}%` → "总天数: 3"
- `%fotime_month_format_总秒数: {S}%` → "总秒数: 259200"

**精确小数显示：**
- `%fotime_total_format_{.2d}天%` → "3.50天"
- `%fotime_week_format_{.1h}小时%` → "84.5小时"
- `%fotime_today_format_{.2m}分钟%` → "5070.25分钟"

**时间格式化：**
- `%fotime_week_format_{HH}:{mm}:{ss}%` → "84:30:15"
- `%fotime_total_format_{02d}天{02h}小时{02m}分钟%` → "03天12小时30分钟"

**复杂示例：**
- `%fotime_total_format_在线时间: {d}天{h}小时{m}分钟{s}秒%` → "在线时间: 3天12小时30分钟15秒"
- `%fotime_week_format_进度: {.1h}/24小时%` → "进度: 12.5/24小时"

<br />

## 📚 插件 API
```kotlin
repositories {
    maven("https://repo.catnies.top/releases/")
}
```
```kotlin
dependencies {
    compileOnly("top.catnies:firOnlineTime:1.0.11")
}
```

### API 使用示例
```kotlin
// 获取玩家今日在线时间（毫秒）
val todayTime = FirOnlineTimeAPI.getPlayerOnlineTime(player, QueryType.TODAY)

// 获取玩家本周在线天数
val weekDays = FirOnlineTimeAPI.getPlayerOnlineDays(player, QueryType.WEEK)

// 使用自定义格式化
val formattedTime = TimeUtil.formatTimeByMillis(todayTime)
```

## 🤝 贡献
欢迎提交 Issue 和 Pull Request 来帮助改进这个插件！

## 📄 许可证
本项目采用 MIT 许可证，详情请查看 LICENSE 文件。