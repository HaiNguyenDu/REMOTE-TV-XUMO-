package com.example.remote_xumo_tv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer

class XumoGrpcException(val status: Int, override val message: String) :
    Exception("gRPC status=$status: $message")

internal object Proto {

    private fun Buffer.writeVarint(value: Long) {
        var v = value
        while (true) {
            val b = (v and 0x7F).toInt()
            v = v ushr 7
            if (v == 0L) {
                writeByte(b)
                return
            }
            writeByte(b or 0x80)
        }
    }

    private fun tag(field: Int, wire: Int): Long = ((field shl 3) or wire).toLong()

    class Writer {
        private val buf = Buffer()

        fun string(field: Int, value: String): Writer {
            if (value.isNotEmpty()) {
                val bytes = value.encodeToByteArray()
                buf.writeVarint(tag(field, 2))
                buf.writeVarint(bytes.size.toLong())
                buf.write(bytes)
            }
            return this
        }

        fun message(field: Int, body: ByteArray): Writer {
            buf.writeVarint(tag(field, 2))
            buf.writeVarint(body.size.toLong())
            buf.write(body)
            return this
        }

        fun toByteArray(): ByteArray = buf.readByteArray()
    }

    private class Reader(bytes: ByteArray) {
        private val buf = Buffer().write(bytes)
        fun exhausted() = buf.exhausted()

        fun readVarint(): Long {
            var result = 0L
            var shift = 0
            while (true) {
                val b = buf.readByte().toInt() and 0xFF
                result = result or ((b.toLong() and 0x7F) shl shift)
                if (b and 0x80 == 0) return result
                shift += 7
            }
        }

        fun readTag(): Pair<Int, Int> {
            val t = readVarint().toInt()
            return (t ushr 3) to (t and 0x7)
        }

        fun readLenBytes(): ByteArray = buf.readByteArray(readVarint())
        fun readString(): String = String(readLenBytes(), Charsets.UTF_8)

        fun skip(wire: Int) {
            when (wire) {
                0 -> readVarint()
                1 -> buf.skip(8)
                2 -> buf.skip(readVarint())
                5 -> buf.skip(4)
                else -> error("unknown wire type $wire")
            }
        }
    }

    fun firstString(bytes: ByteArray, field: Int): String {
        val r = Reader(bytes)
        while (!r.exhausted()) {
            val (f, w) = r.readTag()
            if (f == field && w == 2) return r.readString()
            r.skip(w)
        }
        return ""
    }

    fun firstVarint(bytes: ByteArray, field: Int): Long {
        val r = Reader(bytes)
        while (!r.exhausted()) {
            val (f, w) = r.readTag()
            if (f == field && w == 0) return r.readVarint()
            r.skip(w)
        }
        return 0L
    }

    fun firstMessage(bytes: ByteArray, field: Int): ByteArray {
        val r = Reader(bytes)
        while (!r.exhausted()) {
            val (f, w) = r.readTag()
            if (f == field && w == 2) return r.readLenBytes()
            r.skip(w)
        }
        return ByteArray(0)
    }
}

internal object Grpc {
    private val MEDIA = "application/grpc".toMediaType()

    suspend fun unary(
        client: OkHttpClient,
        baseUrl: String,
        method: String,
        authorization: String,
        requestMessage: ByteArray
    ): ByteArray = withContext(Dispatchers.IO) {
        val framed = Buffer().apply {
            writeByte(0)
            writeInt(requestMessage.size)
            write(requestMessage)
        }
        val request = Request.Builder()
            .url(baseUrl + XumoConstants.SERVICE + "/" + method)
            .addHeader("te", "trailers")
            .addHeader(XumoConstants.AUTH_HEADER, authorization)
            .post(framed.readByteArray().toRequestBody(MEDIA))
            .build()

        client.newCall(request).execute().use { resp ->
            val bytes = resp.body?.bytes() ?: ByteArray(0)
            val trailers = runCatching { resp.trailers() }.getOrNull()
            val status = trailers?.get("grpc-status") ?: resp.header("grpc-status") ?: "0"
            if (status != "0") {
                val msg = trailers?.get("grpc-message")
                    ?: resp.header("grpc-message")
                    ?: "gRPC error (HTTP ${resp.code})"
                throw XumoGrpcException(status.toIntOrNull() ?: -1, msg)
            }
            if (bytes.size < 5) {
                ByteArray(0)
            } else {
                val len = ((bytes[1].toInt() and 0xFF) shl 24) or
                        ((bytes[2].toInt() and 0xFF) shl 16) or
                        ((bytes[3].toInt() and 0xFF) shl 8) or
                        (bytes[4].toInt() and 0xFF)
                bytes.copyOfRange(5, minOf(5 + len, bytes.size))
            }
        }
    }
}
