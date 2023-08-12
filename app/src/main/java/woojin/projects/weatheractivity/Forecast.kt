package woojin.projects.weatheractivity

data class Forecast(
    val forecastDate: String,
    val forecastTime: String,

    //Category에 맞게 설정
    var temperature: Double = 0.0, //기온 
    var sky: String = "", //하늘상태
    var precipitation: Int = 0, //강수확률
    var precipitationType: String = "", //강수형태
) {
    //이 작업은 그저 귀찮아서 묶어버렸다고 함
    val weather: String
        get() {
            return if (precipitationType == "" || precipitationType == "없음") {
                sky
            } else {
                precipitationType
            }
        }
}