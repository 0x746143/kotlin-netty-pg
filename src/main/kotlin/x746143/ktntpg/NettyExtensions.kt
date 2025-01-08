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
package x746143.ktntpg

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponse

inline fun HttpResponse.addHeaders(
    contentLength: Int = 0,
    block: HttpHeaders.() -> Unit = {}
): HttpResponse {
    headers()
        .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
        .set(HttpHeaderNames.CONTENT_LENGTH, contentLength)
        .block()
    return this
}

fun String.toBuffer(): ByteBuf {
    return Unpooled.wrappedBuffer(this.toByteArray())
}