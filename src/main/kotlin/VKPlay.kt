import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VKPlay {
    var requests: AugUtils.Companion.Requests = AugUtils.Companion.Requests()
    val vkapi = VKAPI()
    var config: JSONObject = JSONObject()

    init {
        config = JSONObject(File("config.json").readText())
        vkapi.accessToken = config.getString("vk_token")
    }

    fun startCheck(){
        println("Запущена проверка наличия стримов")
        val url = "https://api.vkplay.live/v1/blog/${config.getString("vkplay")}/public_video_stream"
        while(true){try {
            val channelData = requests.get(url).json() as JSONObject
            if (channelData.getBoolean("isOnline") && !config.getBoolean("vkplay_isOnline")) {
                config.put("vkplay_isOnline", true)
                AugUtils.writeFile("config.json", config.toString())

                vkapi.call("wall.post", mutableMapOf(
                    "owner_id" to config.getInt("vk_group_id") * -1,
                    "attachments" to "https://vkplay.live/${config.getString("vkplay")}",
                    "message" to "Начался стрим!\n\n${channelData.getString("title")}",
                    "from_group" to 1
                ))
                println("Обнаружено начало стрима")

            }
            if (!channelData.getBoolean("isOnline") && config.getBoolean("vkplay_isOnline")){
                config.put("vkplay_isOnline", false)
                AugUtils.writeFile("config.json", config.toString())
                println("Стрим закончился")
            }

            Thread.sleep(60000 * config.getLong("update_timer"))


            } catch(e: Exception){
                e.printStackTrace()
                Thread.sleep(1000)
            }
        }
    }
}