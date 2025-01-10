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
package x746143.ktntpg.pgclient.test

import io.netty.buffer.ByteBuf
import x746143.ktntpg.channel.InputMessageChannel
import x746143.ktntpg.pgclient.wire3.toStringUtf8
import x746143.ktntpg.suspendUninterceptedCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class UnbufferedContChannel : InputMessageChannel {
    private var continuation: Continuation<ByteBuf>? = null

    override suspend fun readMessage(): ByteBuf {
        return suspendUninterceptedCoroutine {
            continuation = it
        }
    }

    fun send(msg: ByteBuf) {
        val cont = continuation
        if (cont == null) {
            throw UnhandledMessageException(msg.toStringUtf8())
        } else {
            continuation = null
            cont.resume(msg)
        }
    }

    class UnhandledMessageException(message: String) : Exception(message)
}