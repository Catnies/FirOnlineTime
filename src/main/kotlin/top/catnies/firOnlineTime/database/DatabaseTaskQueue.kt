package top.catnies.firOnlineTime.database

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger

/**
 * 全局数据库任务队列，确保所有数据库操作在单个线程中顺序执行
 * 提供自动重连机制和错误处理
 */
object DatabaseTaskQueue {
    private val logger: Logger = FirOnlineTime.instance.logger
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val isRunning = AtomicBoolean(true)
    
    // 关闭任务队列
    fun shutdown() {
        isRunning.set(false)
        executor.shutdownNow()
        logger.info("数据库任务队列已关闭")
    }
    
    // 提交数据库任务
    fun <T> submitTask(taskName: String, task: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        
        if (!isRunning.get()) {
            future.completeExceptionally(IllegalStateException("数据库任务队列已关闭"))
            return future
        }
        
        executor.submit {
            try {
                val result = task()
                future.complete(result)
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "执行数据库任务[$taskName]时发生错误", e)
                future.completeExceptionally(e)
            }
        }
        
        return future
    }
}
