<div align="center">  

# FirOnlineTime

_✨一款轻量化便携性的Bukkit在线统计插件✨_
</div>

<p align="center">
    <img src="https://img.shields.io/badge/支持版本-1.18 ~ 1.21.4-brightgreen?style=flat-square" alt="minecraft-version">
</p>

## 📌 关于 
***
FirOnlineTime 是一款轻量化便携性的Bukkit在线统计插件。其支持使用Mysql数据库来存储玩家的在线时间，并提供了一些占位符来获取玩家的在线时间。
数据缓存会定时从数据库异步刷新，所以它可以很轻松的在群组服务器上使用。  
插件在遇到频繁请求时会采用缓存，不会出现过多的数据库请求，对于在线玩家是完全异步处理，对于离线玩家在第一次请求时会缓存数据。
<br />

## 🔨 安装
***
1. 下载/构建最新的版本，将插件放入 `plugins` 文件夹。
2. 启动服务器，生成默认配置文件。
3. 打开 `settings.yml` 文件, 配置您的 Mysql 数据库信息，最后重启服务器。     
<br />
  
## 💻 使用方法
***
命令: 
```access transformers
 /fironlinetime reload -- 重载插件
```
您可以使用 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245)  插件来获取玩家的在线时间。

| 占位符 | 说明 |
| --- | --- |
| `%fotime_daily%` | 当日玩家的在线时间 |
| `%fotime_weekly%` | 本周玩家的在线时间 |
| `%fotime_month%` | 本月玩家的在线时间 |
| `%fotime_total%` | 玩家的总在线时间 |
| `%fotime_daily_value%` | 当日玩家的在线时间的毫秒时间戳 |
| `%fotime_weekly_value%` | 本周玩家的在线时间的毫秒时间戳 |
| `%fotime_month_value%` | 本月玩家的在线时间的毫秒时间戳 |
| `%fotime_total_value%` | 玩家的总在线时间的毫秒时间戳 |
