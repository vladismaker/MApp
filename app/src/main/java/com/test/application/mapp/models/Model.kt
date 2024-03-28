package com.test.application.mapp.models

interface Model {
    suspend fun startRequest():String?
    suspend fun checkInternet():Boolean
    //suspend fun startPostRequest(geoJsonObjectDto: GeoJsonObjectDto):String?
}