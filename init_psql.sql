-- 创建数据库
--CREATE DATABASE FirOnlineTime;

-- 切换到 minecraft 数据库
--\c minecraft;

-- 创建玩家在线时间表
CREATE TABLE IF NOT EXISTS fir_online_time (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    onlineTime BIGINT NOT NULL,
    CONSTRAINT unique_player_date UNIQUE (uuid, date)
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_fir_online_time_uuid ON fir_online_time(uuid);
CREATE INDEX IF NOT EXISTS idx_fir_online_time_date ON fir_online_time(date);
CREATE INDEX IF NOT EXISTS idx_fir_online_time_uuid_date ON fir_online_time(uuid, date);

-- 创建用户并授予权限（根据实际情况修改）
CREATE USER minecraft_user WITH PASSWORD 'fir_online_time';
GRANT ALL PRIVILEGES ON DATABASE minecraft TO fir_online_time;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fir_online_time;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fir_online_time;
