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
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*

class HttpServerHandler : SimpleChannelInboundHandler<FullHttpRequest>() {
    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val response = when (request.uri()) {
            "/" -> redirect("/hello")
            "/hello" -> responseString("Hello, world")
            else -> notFound()
        }
        ctx.writeAndFlush(response)
    }

    @Suppress("SameParameterValue")
    private fun responseString(content: String): HttpResponse {
        return httpResponse(content.toBuffer()).addHeaders(content.length)
    }

    @Suppress("SameParameterValue")
    private fun redirect(url: String): HttpResponse {
        return httpResponse(HttpResponseStatus.FOUND).addHeaders {
            set(HttpHeaderNames.LOCATION, url)
        }
    }

    private fun notFound(): HttpResponse {
        return httpResponse(HttpResponseStatus.NOT_FOUND).addHeaders()
    }

    private fun httpResponse(status: HttpResponseStatus = HttpResponseStatus.OK): HttpResponse {
        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status)
    }

    private fun httpResponse(content: ByteBuf = Unpooled.buffer(0)): HttpResponse {
        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace() // TODO: replace to Logger
        ctx.close()
    }
}