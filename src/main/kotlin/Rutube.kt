import khttp.get
import org.json.JSONArray
import org.json.JSONObject

class Rutube{
    var knownVideos: JSONArray = JSONArray()
    var channelId: Int = 0

    val videosRegex = Regex("wdp-card-description-module__descriptionWrapper wdp-card-description-module__fullwidthDescription\">(.+?)<\\/a>")
    val urlRegex = Regex("href=\"(.+?)\" title")
    val nameRegex = Regex("title=\"(.+?)\" class")
    val idRegex = Regex("video/(.+?)/")

    fun getNewVideos(): MutableList<Map<String, String>>{
        val newVideos = mutableListOf<Map<String, String>>()

        val response = get("https://rutube.ru/channel/$channelId/videos/").text

        val videos = videosRegex.findAll(response)
        for (video in videos.toList()){
            val url = urlRegex.find(video.value.toString())?.groups?.get(1)?.value.toString()
            val name = nameRegex.find(video.value.toString())?.groups?.get(1)?.value.toString()
            val id = idRegex.find(video.value.toString())?.groups?.get(1)?.value.toString()

            if (knownVideos.contains(id)) break
            else{
                knownVideos.put(id)
                newVideos.add(mapOf(
                    "url" to url,
                    "name" to name,
                    "id" to id
                ))
            }
        }

        return newVideos
    }
}