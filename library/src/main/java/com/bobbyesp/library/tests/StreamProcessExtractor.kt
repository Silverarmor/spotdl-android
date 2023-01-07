package com.bobbyesp.library.tests

import android.util.Log
import com.bobbyesp.library.BuildConfig
import com.bobbyesp.library.StreamProcessExtractor
import java.io.BufferedReader
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

    override fun run() {
        try {
            //Read the stream and get the output line by line on real time
            val reader: Reader = InputStreamReader(stream, StandardCharsets.UTF_8)
            val bufferedReader = BufferedReader(reader)
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                //Just read the line, cut that line in it's end and add it to the buffer. Then, the buffer will be read by the UI and after that, it will be cleared
                //clena output
                val matcher: Matcher = cleanOutRegex.matcher(line)
                val cleanLine = matcher.replaceAll("")
                Log.d("StreamProcessExtractor", line.toString())
                callback?.let { it(1f, 1, line.toString()) }
                buffer.setLength(0)
                continue
            }

        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "failed to read stream", e)
        }
    }

    companion object {
        private val TAG = StreamProcessExtractor::class.java.simpleName

        private var ETA: Long = -1
        private var PERCENT = -1.0f
        private var GROUP_PERCENT = 1
        private var GROUP_MINUTES = 2
        private var GROUP_SECONDS = 3
    }
}