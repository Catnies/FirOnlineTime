<div align="center">  

# FirOnlineTime
_✨一款轻量化便携性的Bukkit/Paper/Folia在线统计插件✨_
</div>

<p align="center">
    <img src="https://img.shields.io/badge/支持版本-1.16 ~ 1.21.8-brightgreen?style=flat-square" alt="minecraft-version">
</p>

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
<br />
      
## 📚 插件 API
```kotlin
repositories {
    maven("https://repo.catnies.top/releases/")
}
```
```kotlin
dependencies {
    compileOnly("top.catnies:firOnlineTime:1.0.9")
}
```
