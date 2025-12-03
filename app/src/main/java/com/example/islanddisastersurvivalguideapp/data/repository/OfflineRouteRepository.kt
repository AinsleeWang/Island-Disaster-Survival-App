package com.example.islanddisastersurvivalguideapp.data.repository


import com.example.islanddisastersurvivalguideapp.data.local.dao.OfflineRouteDao
import com.example.islanddisastersurvivalguideapp.data.model.PrecomputedRoute
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRouteRepository @Inject constructor(
    private val routeDao: OfflineRouteDao,
    private val gson: Gson
) {
    suspend fun getRoutesForLocation(locationId: String): List<PrecomputedRoute> =
        withContext(Dispatchers.IO) {
            routeDao.getRoutesForLocation(locationId).map { it.toDomainModel(gson) }
        }

    suspend fun saveRoute(route: PrecomputedRoute) =
        withContext(Dispatchers.IO) {
            routeDao.insertRoute(route.toEntity(gson))
        }

    suspend fun cleanOldRoutes(timestamp: Long) =
        withContext(Dispatchers.IO) {
            routeDao.deleteOldRoutes(timestamp)
        }
}