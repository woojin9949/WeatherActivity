package woojin.projects.weatheractivity

import android.util.Log
import kotlin.math.*

class GeoPointConverter {


    //기상청 제공 api 자료에 있는 위도 경도 변환 프로그램 참고
    //C프로그래밍 과정 변경

    private val NX = 149
    private val NY = 253

    private val RE = 6371.00877 // 지도반경
    private val GRID = 5.0 // 격자간격 (km)
    private val SLAT1 = 30.0 // 표준위도 1
    private val SLAT2 = 60.0 // 표준위도 2
    private val OLON = 126.0 // 기준점 경도
    private val OLAT = 38.0 // 기준점 위도
    private val XO = 210 / GRID //기준점 X좌표
    private val YO = 675 / GRID //기준점 Y좌표

    private val DEGRAD = PI / 180.0
    private val RADDEG = 180.0 / PI

    private val re = RE / GRID
    private val slat1 = SLAT1 * DEGRAD
    private val slat2 = SLAT2 * DEGRAD
    private val olon = OLON * DEGRAD
    private val olat = OLAT * DEGRAD

    data class Point(val nx: Int, val ny: Int)

    fun convert(lon: Double, lat: Double): Point {

        var sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
        sn = log2(cos(slat1) / cos(slat2)) / log2(sn)
        var sf = tan(PI * 0.25 + slat1 * 0.5)
        sf = sf.pow(sn) * cos(slat1) / sn
        var ro = tan(PI * 0.25 + olat * 0.5)
        ro = re * sf / ro.pow(sn)

        var ra = tan(PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / ra.pow(sn)

        var theta = lon * DEGRAD - olon

        if (theta > PI)
            theta -= 2 * PI
        if (theta < -PI)
            theta += 2 * PI

        theta *= sn

        val nx = ra * sin(theta) + XO + 1.5
        val ny = ro - ra * cos(theta) + YO + 1.5

        Log.e("converter", "$nx $ny")
        return Point(nx.toInt(), ny.toInt())
    }

}

