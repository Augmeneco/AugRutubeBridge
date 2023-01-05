import khttp.post
import khttp.get
import VKAPI
import Rutube
import org.json.*
import java.io.File
import java.io.FileWriter
import AugUtils

fun main(args: Array<String>) {
    val rutube = Rutube()
    val vkapi = VKAPI()
    var config: JSONObject = JSONObject()
    var knownVideos: JSONArray = JSONArray()
    var firstInit: Boolean = false

    val file = File("config.json")
    if (file.exists()){
        config = JSONObject(file.readText())

        vkapi.accessToken = config.getString("vk_token")
        rutube.channelId = config.getInt("rutube_channel_id")
    }else{
        println("Файл конфигурации не обнаружен, создаётся новый\n")

        print("Ид канала rutube\n$ ")
        config.put("rutube_channel_id", readln().toInt())
        rutube.channelId = config.getInt("rutube_channel_id")

        print("Ид группы VK\n$ ")
        config.put("vk_group_id", readln().toInt())

        while (true){
            println("Вставь ссылку с токеном\nПолучить её можно по адресу " +
                    "\"https://oauth.vk.com/authorize?client_id=2685278&scope=1073737727&redirect_uri=https://oauth.vk.com/blank.html&display=page&response_type=token&revoke=1\"")

            try {
                print("$ ")
                vkapi.accessToken = readln()
                println("Проверка токена...")
                vkapi.accessToken = vkapi.parseToken(vkapi.accessToken)


                vkapi.call("users.get", mutableMapOf())
            } catch (e: Exception){
                println("\nТокен не работает, пробуй ещё раз")
                continue
            }

            config.put("vk_token", vkapi.accessToken)
            break
        }
        print("Раз в сколько минут обновлять канал?\n$ ")
        config.put("update_timer", readln().toFloat())

        AugUtils.writeFile("config.json", config.toString())
    }

    val knownVideosFile = File("knownVideos.json")
    if (knownVideosFile.exists()){
        knownVideos = JSONArray(knownVideosFile.readText())
    }else{
        AugUtils.writeFile("knownVideos.json", knownVideos.toString())
        firstInit = true
    }
    rutube.knownVideos = knownVideos

    //rutube.channelId = 25735130

    println("")
    while (true){
        val newVideos = rutube.getNewVideos()

        for (video in newVideos){
            if (firstInit) {
                println("Обнаружены видео, но поститься будут лишь новые начиная с этого момента")
                break
            }

            vkapi.call("wall.post", mutableMapOf(
                "owner_id" to config.getInt("vk_group_id") * -1,
                "attachments" to "${video["url"]}",
                "message" to "${video["name"]}",
                "from_group" to 1
            ))
            println("Запощено видео | id: ${video["id"]}, name: ${video["name"]}, url: ${video["url"]}")
            Thread.sleep(5000)
        }

        if (newVideos.isNotEmpty())
            AugUtils.writeFile("knownVideos.json", rutube.knownVideos.toString())

        Thread.sleep((config.getFloat("update_timer") * 60000).toLong())
    }
}