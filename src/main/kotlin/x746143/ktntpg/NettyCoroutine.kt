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
import io.netty.channel.EventLoop
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class NettyContext(val eventLoop: EventLoop, val ctx: ChannelHandlerContext? = null) :
    AbstractCoroutineContextElement(NettyContextKey) {
    companion object NettyContextKey : CoroutineContext.Key<NettyContext>
}

private open class NettyContinuation(override val context: NettyContext) : Continuation<Unit> {
    override fun resumeWith(result: Result<Unit>) {}
}

private class NettyContinuationWithResume(
    context: NettyContext,
    private val resumeBlock: (result: Result<Unit>) -> Unit
) : NettyContinuation(context) {
    override fun resumeWith(result: Result<Unit>) = resumeBlock(result)
}

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

fun EventLoop.startCoroutine(block: suspend () -> Unit) {
    execute {
        val cont = NettyContinuation(NettyContext(this))
        block.createCoroutineUnintercepted(cont).resume(Unit)
    }
}

fun Bootstrap.runTest(block: suspend () -> Unit) {
    val eventLoop = config().group().next()
    val thread = Thread.currentThread()
    var exception: Throwable? = null
    eventLoop.execute {
        val cont = NettyContinuationWithResume(NettyContext(eventLoop)) {
            exception = it.exceptionOrNull()
            LockSupport.unpark(thread)
        }
        block.createCoroutineUnintercepted(cont).resume(Unit)
    }
    LockSupport.park()
    exception?.let { throw it }
}

suspend inline fun <T> suspendUninterceptedCoroutine(crossinline block: (Continuation<T>) -> Unit): T {
    return suspendCoroutineUninterceptedOrReturn {
        block(it)
        COROUTINE_SUSPENDED
    }
}
