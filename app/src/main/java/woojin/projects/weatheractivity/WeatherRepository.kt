package woojin.projects.weatheractivity

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import woojin.projects.weatheractivity.databinding.ItemForecastBinding

object WeatherRepository {

    private val retrofit = Retrofit.Builder().baseUrl("http://apis.data.go.kr")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(WeatherService::class.java)


    fun getVillageForecast(
        longitude: Double,
        latitude: Double,
        successCallback: (List<Forecast>) -> Unit,
        failureCallback: (Throwable) -> Unit
    ) {
        val baseDateTime = BaseDateTime.getBaseDateTime()
        val converter = GeoPointConverter()
        val point = converter.convert(lat = latitude, lon = longitude)
        service.getVillageForecast(
            serviceKey = "aF41gh0T7v4J4uDWyh6lfYEORxLlD7Y76PkIMHk3DAERo4z5r2m1oyqczvM+B2bxKFy7xExRdZiYyfc2AROBzA==",
            baseDate = baseDateTime.baseDate,
            baseTime = baseDateTime.baseTime,
            nx = point.nx,
            ny = point.ny
        ).enqueue(object : Callback<WeatherEntity> {
            override fun onResponse(
                call: Call<WeatherEntity>,
                response: Response<WeatherEntity>
            ) {
                if (response.isSuccessful) {
                    val forecastDateTimeMap = mutableMapOf<String, Forecast>()
                    //WeatherEntity로 받은 객체의 ForecastList형식으로 받아오는 아이템값 받기
                    val forecastList =
                        response.body()?.response?.body?.items?.forecastEntities.orEmpty()
                    forecastList.forEach { fc ->   //forecastEntity
                        //기본값이 없을 경우 초기화
                        if (forecastDateTimeMap["${fc.forecastDate}/${fc.forecastTime}"] == null) {
                            forecastDateTimeMap["${fc.forecastDate}/${fc.forecastTime}"] =
                                Forecast(fc.forecastDate, fc.forecastTime)
                            //Log.d("testt", forecastDateTimeMap.toString())
                        }
                        //카테고리
                        forecastDateTimeMap["${fc.forecastDate}/${fc.forecastTime}"]?.apply {
                            when (fc.category) {
                                //강수확률 표기
                                Category.POP -> precipitation = fc.forecastValue.toInt()
                                //강수형태 표기
                                Category.PTY -> precipitationType = transformRainType(fc)
                                //하늘상태 표기
                                Category.SKY -> sky = transformSky(fc)
                                //1시간 기온 표기
                                Category.TMP -> temperature = fc.forecastValue.toDouble()
                                else -> {}
                            }
                        }
                    }
                    //Log.e("Forecast", forecastDateTimeMap.toString())

                    val list = forecastDateTimeMap.values.toMutableList()
                    list.sortWith { f1, f2 ->
                        val f1DateTime = "${f1.forecastDate}${f1.forecastTime}"
                        val f2DateTime = "${f2.forecastDate}${f2.forecastTime}"
                        return@sortWith f1DateTime.compareTo(f2DateTime)
                    }
                    //Log.e("list", list.toString())
                    if (list.isEmpty()) {
                        failureCallback(NullPointerException())
                    } else {
                        successCallback(list)
                    }
                }
            }

            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                failureCallback(t)
            }
        })
    }

    private fun transformRainType(forecast: ForecastEntity): String {
        return when (forecast.forecastValue.toInt()) {
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }
    }

    private fun transformSky(forecast: ForecastEntity): String {
        return when (forecast.forecastValue.toInt()) {
            1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> ""
        }
    }
}