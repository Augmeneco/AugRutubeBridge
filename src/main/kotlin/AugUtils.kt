import java.io.FileWriter

class AugUtils {
    companion object {
        fun writeFile(fileName: String, data: String){
            val file = FileWriter(fileName)
            file.write(data)
            file.flush()
            file.close()
        }
    }
}