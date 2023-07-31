import java.io.FileWriter

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.*

class AugUtils {
    companion object {
        fun writeFile(fileName: String, data: String){
            val file = FileWriter(fileName)
            file.write(data)
            file.flush()
            file.close()
        }

        class Requests{
            var httpClient = OkHttpClient()

            companion object {
                fun buildProxy(domain: String, port: Int): Proxy{
                    return Proxy(Proxy.Type.SOCKS, InetSocketAddress(domain, port))
                }
            }

            fun protoRequest(
                type: String,
                url: String,
                params: MutableMap<Any, Any> = mutableMapOf(),
                proxy: MutableList<Any> = mutableListOf<Any>()
            ): RequestResponse{
                val formBodyBuilder = FormBody.Builder()

                for ((key, value) in params) {
                    formBodyBuilder.add(key.toString(), value.toString())
                }
                val requestBody = formBodyBuilder.build()
                var request = Request.Builder()
                    .url(url)
                if (type == "post")
                    request = request.post(requestBody)


                val requestResponse: RequestResponse = RequestResponse()

                var client: OkHttpClient.Builder = httpClient.newBuilder()
                if (proxy.isNotEmpty())
                    client = client.proxy(buildProxy(
                        proxy[0] as String, proxy[1] as Int
                    ))

                client.build().newCall(request.build()).execute().use { response ->
                    val responseBody = response.body

                    requestResponse.content = responseBody!!.bytes()
                    requestResponse.text = String(requestResponse.content)
                    requestResponse.status = response.code
                }

                return requestResponse
            }

            fun post(
                url: String,
                params: MutableMap<Any, Any> = mutableMapOf(),
                proxy: MutableList<Any> = mutableListOf<Any>()
            ): RequestResponse{
                return protoRequest("post", url, params, proxy)
            }

            fun get(
                url: String,
                params: MutableMap<Any, Any> = mutableMapOf(),
                proxy: MutableList<Any> = mutableListOf<Any>()
            ): RequestResponse{
                return protoRequest("get", url, params, proxy)
            }

            class RequestResponse{
                lateinit var content: ByteArray
                lateinit var text: String
                var status: Int = 0

                fun json(): Any{
                    return try {
                        JSONObject(text)
                    } catch (e: Exception) {
                        JSONArray(text)
                    }
                }
            }
        }

    }
}