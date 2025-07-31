package top.catnies.firOnlineTime.utils

import cn.chengzhiya.mhdfscheduler.scheduler.MHDFScheduler
import cn.chengzhiya.mhdfscheduler.task.MHDFTask
import top.catnies.firOnlineTime.FirOnlineTime
import java.util.concurrent.atomic.AtomicInteger

object TaskUtils {

    val plugin = FirOnlineTime.instance

    // 运行异步任务
    fun runAsyncTask(task: () -> Unit) {
        MHDFScheduler.getAsyncScheduler().runTask(plugin, task)
    }

    // 运行异步定时任务
    fun runTimerAsyncTask(task: () -> Unit, delay: Long = 0, period: Long): MHDFTask {
        return MHDFScheduler.getAsyncScheduler().runTaskTimer(
            plugin, task, delay, period
        )
    }

    // 运行异步任务并同步执行回调
    fun runAsyncTaskWithSyncCallback(async:() -> Unit, callback: () -> Unit, delay: Long = 0) {
        MHDFScheduler.getAsyncScheduler().runTaskLater(plugin, {
            async()
            MHDFScheduler.getGlobalRegionScheduler().runTask(plugin) {
                callback()
            }
        }, delay)
    }

    // 运行异步并行任务
    fun runAsyncTasksLater(vararg tasks: () -> Unit, delay: Long = 0) {
        tasks.forEach { MHDFScheduler.getAsyncScheduler().runTaskLater(plugin, it, delay) }
    }

    // 运行多个异步任务并在全部完成后执行同步回调
    fun runAsyncTasksWithSyncCallback(vararg tasks: () -> Unit, callback: () -> Unit, delay: Long = 0) {
        if (tasks.isEmpty()) {
            // 没任务时直接回调
            MHDFScheduler.getGlobalRegionScheduler().runTask(plugin, callback)
            return
        }

        val counter = AtomicInteger(tasks.size)
        tasks.forEach { task ->
            MHDFScheduler.getAsyncScheduler().runTaskLater(plugin, {
                try {
                    task()
                }
                finally {
                    // 计数器减一并检查是否所有任务完成
                    if (counter.decrementAndGet() == 0) {
                        MHDFScheduler.getGlobalRegionScheduler().runTask(plugin, callback)
                    }
                }
            }, delay)
        }
    }

}