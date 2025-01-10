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

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class NettyContext(val ctx: ChannelHandlerContext) : AbstractCoroutineContextElement(NettyContextKey)

object NettyContextKey : CoroutineContext.Key<NettyContext>

suspend fun Bootstrap.connectAwait(host: String, port: Int): Channel {
    return suspendUninterceptedCoroutine { cont ->
        val future: ChannelFuture = connect(host, port)
        future.addListener { f ->
            if (f.isSuccess) {
                cont.resume(future.channel())
            } else {
                cont.resumeWithException(f.cause())
            }
        }
    }
}

suspend inline fun <T> suspendUninterceptedCoroutine(crossinline block: (Continuation<T>) -> Unit): T {
    return suspendCoroutineUninterceptedOrReturn {
        block(it)
        COROUTINE_SUSPENDED
    }
}
