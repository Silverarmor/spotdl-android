package com.bobbyesp.library

import android.os.Environment
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import java.util.regex.Pattern

internal class StreamProcessExtractor(
    private val buffer: StringBuffer,
    private val stream: InputStream,
    private val callback: ((Float, Long, String) -> Unit)? = null
) : Thread() {

    private val cleanOutRegex: Pattern =
        Pattern.compile("(?:\\x1B[@-Z\\\\-_]|[\\x80-\\x9A\\x9C-\\x9F]|(?:\\x1B\\[|\\x9B)[0-?]*[ -/]*[@-~])")

    init {
        start()
    }

    //Based on this output that changes every second: Alan Walker - Shut Up                    Converting         ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━╺━━━━━━━━━━━━━━━  66% 0:00:02 //HAVING ISSUES WITH THIS. INPUT STREAM DON'T READ THE LINE
    //NF - The Search                          Converting         ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━╸━━━━━━━━━━  78% 0:00:01

    override fun run() {
        try {
            //Read the stream and get the output line by line on real time
            val reader: Reader = InputStreamReader(stream, StandardCharsets.UTF_8)
            val bufferedReader = BufferedReader(reader)
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                //Just read the line, cut that line in it's end and add it to the buffer. Then, the buffer will be read by the UI and after that, it will be cleared
                //clean output
                val matcher: Matcher = cleanOutRegex.matcher(line)
                val cleanLine = matcher.replaceAll("")
                if (cleanLine != "") {
                    processOutputLine(cleanLine)
                    //change the line length to 0, not the buffer
                    buffer.setLength(0)
                }
                continue
            }

            //TEST CODE

            /*var nextChar: Int
            var arrayOfChars = ArrayList<String>()
            val currentLine = StringBuilder()

            while (reader.read().also { nextChar = it } != -1) {
                arrayOfChars.add(nextChar.toChar().toString())
                Log.d("StreamProcessExtractor", arrayOfChars.toString())
                if (callback != null) {
                    val line = currentLine.toString()
                    processOutputLine(line)
                    currentLine.setLength(0)
                    continue
                }
                currentLine.append(nextChar.toChar())

            }*/

        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "failed to read stream", e)
        }
    }

    private fun processOutputLine(line: String) {
        //if is debug, print the line
        if (BuildConfig.DEBUG) Log.d(TAG, line)
        callback?.let { it(getProgress(line), getEta(line), line) }
    }

    private fun getProgress(line: String): Float {
        return 1f
    }

    private fun getEta(line: String): Long {
        return 1
    }


    //TESTS FIELD

    companion object {
        private val TAG = StreamProcessExtractor::class.java.simpleName

        private var ETA: Long = -1
        private var PERCENT = -1.0f
        private var GROUP_PERCENT = 1
        private var GROUP_MINUTES = 2
        private var GROUP_SECONDS = 3
    }

    //TESTS FIELD
    //1 - Read the stream and get put it in a binary file
    /*override fun run() {
        try {
            //Option 1: Read the stream directly
            val reader: Reader = InputStreamReader(stream, StandardCharsets.UTF_8)
            val bytes = mutableListOf<Byte>()
            var c = reader.read()
            while (c != -1) {
                bytes.add(c.toByte())
                c = reader.read()
            }
            reader.close()

            //Option 2: Read the stream (but as Buffered)
            /*val buffer = BufferedInputStream(stream)
            val bytes = mutableListOf<Byte>()
            var c = buffer.read()
            while (c != -1) {
                bytes.add(c.toByte())
                c = buffer.read()
            }
            buffer.close()*/

            //file output stream to write file in the files directory

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "output.bin"
            )
            val fileOutputStream = FileOutputStream(file)
            //Log the file location
            Log.d("StreamProcessExtractor", fileOutputStream.toString())
            fileOutputStream.write(bytes.toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "failed to read stream", e)
        }
    }*/

}