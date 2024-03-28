package com.test.application.mapp.presenters

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.test.application.mapp.GeoJsonObjectDto
import com.test.application.mapp.views.ViewMap
import com.test.application.mapp.models.Model
import com.test.application.mapp.models.TextModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MapPresenter(private val view: ViewMap.View, private val model: Model) {
    private lateinit var coroutine: Job
    private var initCoroutine:Boolean = false
    private var textModel = mutableStateOf(TextModel(""))
    private val arrayPoints: MutableList<GeoJsonObjectDto> = mutableListOf()
    private var currentStyle: String by mutableStateOf("https://api.maptiler.com/maps/streets/style.json?key=onNivG1D5OeMY7l7WTYQ")
    private val _mapPoints = mutableStateOf<MutableList<GeoJsonObjectDto>>(mutableListOf())
    val mapPoints: State<MutableList<GeoJsonObjectDto>> = _mapPoints
    private val _mapCircle = mutableStateOf(true)
    val mapCircle: State<Boolean> = _mapCircle
    private val _featureCollection = mutableStateOf(FeatureCollection.fromFeatures(arrayOf()))
    val featureCollection: State<FeatureCollection> = _featureCollection

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun getCurrentStyleMy(): String {
        return currentStyle
    }

    fun setStyle(styleId: String) {
        currentStyle = styleId
    }

    fun toggleLayerVisibility(b:Boolean){
        _mapCircle.value = b
    }

    fun loadGeoJsonData(): MutableList<GeoJsonObjectDto> {
        getDataFromServer()
        return arrayPoints
    }

    private fun getDataFromServer(){
        coroutine = CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            try {
                initCoroutine = true

                val internet = async {model.checkInternet()}.await()

                if(!internet) {
                    withContext(Dispatchers.Main) {
                        view.showToastText("Нет интернета")
                        textModel.value = textModel.value.copy(text = "Интернета нет")
                    }
                }else {
                    val data2  =  async {model.startRequest().toString()}.await()

                    updateView(data2)
                }
            }catch (e:Throwable){

            }
        }
    }

    private fun updateView(data:String){

        val features = mutableListOf<Feature>()
        val jsonArray = JSONArray(data)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val feature = Feature.fromJson(jsonObject.toString())
            features.add(feature)
        }

        val featureCollection = FeatureCollection.fromFeatures(features)

        _featureCollection.value = featureCollection
        _isLoading.value = false
    }

    fun onDestroy(){
        if(initCoroutine){
            coroutine.cancel()
        }
    }
}