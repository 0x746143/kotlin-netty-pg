/*
 * Copyright 2025 0x746143
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package x746143.ktntpg.pgclient.wire3

import x746143.ktntpg.channel.OutputMessageChannel
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.nio.charset.StandardCharsets.UTF_8

internal fun ByteBuf.writeString(str: String) {
    writeCharSequence(str, UTF_8)
}

internal fun ByteBuf.writeCString(str: String) {
    writeCharSequence(str, UTF_8)
    writeByte(0)
}

internal fun ByteBuf.writeCString(arr: ByteArray) {
    writeBytes(arr)
    writeByte(0)
}

internal inline fun ByteBuf.writeList(block: ByteBuf.() -> Unit) {
    block()
    writeByte(0)
}

internal fun ByteBuf.param(name: ByteArray, value: String) {
    writeCString(name)
    writeCString(value)
}

internal fun ByteBuf.param(name: ByteArray, value: ByteArray) {
    writeCString(name)
    writeCString(value)
}

internal fun ByteBuf.readString(): String {
    return readCharSequence(readableBytes(), UTF_8).toString()
}

internal fun ByteBuf.readCString(): String {
    val zeroIndex = indexOf(readerIndex(), readerIndex() + readableBytes(), 0)
    val result = readCharSequence(zeroIndex - readerIndex(), UTF_8)
    readByte() // ignore zero byte
    return result.toString()
}

internal inline fun OutputMessageChannel.writePgMessage(
    identifier: Int,
    block: ByteBuf.() -> Unit
) {
    val buffer = Unpooled.buffer()
    buffer.writeByte(identifier)
    buffer.writeInt(0)
    buffer.block()
    buffer.setInt(1, buffer.writerIndex() - 1)
    writeMessage(buffer)
}

internal inline fun OutputMessageChannel.writePgMessage(
    block: ByteBuf.() -> Unit
) {
    val buffer = Unpooled.buffer()
    buffer.writeInt(0)
    buffer.block()
    buffer.setInt(0, buffer.writerIndex())
    writeMessage(buffer)
}

internal fun ByteBuf.toByteArray(): ByteArray {
    val result = ByteArray(writerIndex())
    readBytes(result)
    return result
}

internal fun ByteBuf.toStringUtf8(): String {
    return toString(UTF_8)
}