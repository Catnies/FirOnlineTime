package top.catnies.firOnlineTime.managers



class DataCacheManager private constructor() {

    companion object {
        val instance: DataCacheManager by lazy { DataCacheManager().apply {
        } }
    }


}