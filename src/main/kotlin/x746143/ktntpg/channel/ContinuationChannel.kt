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
package x746143.ktntpg.channel

import io.netty.buffer.ByteBuf
import x746143.ktntpg.suspendUninterceptedCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ContinuationChannel : InputMessageChannel {
    private var continuation: Continuation<ByteBuf>? = null
    private var buffer = ArrayDeque<ByteBuf>(64)

    override suspend fun readMessage(): ByteBuf {
        return if (buffer.isEmpty()) {
            suspendUninterceptedCoroutine {
                continuation = it
            }
        } else {
            buffer.removeFirst()
        }
    }

    fun send(msg: ByteBuf) {
        val cont = continuation
        if (cont == null) {
            buffer.addLast(msg)
        } else {
            continuation = null
            cont.resume(msg)
        }
    }
}