package woojin.projects.weatheractivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import woojin.projects.weatheractivity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                updateLocation()
            }
            else -> {
                Toast.makeText(this, "위치권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
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

    private fun updateLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            val retrofit = Retrofit.Builder().baseUrl("http://apis.data.go.kr")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val baseDateTime = BaseDateTime.getBaseDateTime()
            val converter = GeoPointConverter()
            val point = converter.convert(lat = it.latitude, lon = it.longitude)
            val service = retrofit.create(WeatherService::class.java)
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
                        Log.e("Forecast", forecastDateTimeMap.toString())

                        val list = forecastDateTimeMap.values.toMutableList()
                        list.sortWith { f1, f2 ->
                            val f1DateTime = "${f1.forecastDate}${f1.forecastTime}"
                            val f2DateTime = "${f2.forecastDate}${f2.forecastTime}"
                            return@sortWith f1DateTime.compareTo(f2DateTime)
                        }
                        Log.e("list", list.toString())

                        val currentForecast = list.first()
                        binding.temperatureTextView.text =
                            getString(R.string.temperature_text, currentForecast.temperature)
                        binding.skyTextView.text = currentForecast.weather
                        binding.precipitationTextView.text =
                            getString(R.string.precipitation_text, currentForecast.precipitation)
                    }

                }

                override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {

                }
            })
        }
    }
}