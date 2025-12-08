package com.example.conecta4.data.stats

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object StatsRepo {
    private var dao: StatsDao? = null

    fun init(context: Context) {
        if (dao == null) dao = StatsDatabase.get(context).statsDao()
    }

    fun observeAll(): Flow<List<GameStat>> = dao!!.observeAll()

    suspend fun insert(stat: GameStat) {
        withContext(Dispatchers.IO) { dao!!.insert(stat) }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) { dao!!.clear() }
    }
}
