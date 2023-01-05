import khttp.post
import khttp.get
import org.json.*

class VKAPI {
    var accessToken: String = ""
    val version: String = "5.131"

    fun call(method: String, params: MutableMap<String, Any>): Any{
        //println("Call $method")

        params["v"] = version
        params["access_token"] = accessToken

        val response = post(
            "https://api.vk.com/method/$method",
            data = params
        ).jsonObject

        if (response.has("error")){
            val errorObject = response.getJSONObject("error")
            val errorString = "VK ERROR ${errorObject.getInt("error_code")}: \"" +
                    "${errorObject.getString("error_msg")}\"\n" +
                    "Params: ${errorObject.getJSONObject("requests_params").toMap().toString()}"

            throw Exception(errorString)
        }

        return response.get("response")
    }

    fun parseToken(tokenData: String): String{
        val tokenRegex = Regex("access_token=(.+)\\&expires_in")

        val matchResult = tokenRegex.find(tokenData)
        if (matchResult != null){
            return matchResult.groups.toList()[1]?.value.toString()
        }else{
            if (tokenData.contains("access_token")) return tokenData
            else throw Exception("Вставленна какая-то фигня вместо токена")
        }

    }
}