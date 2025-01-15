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
import io.netty.buffer.Unpooled
import x746143.ktntpg.channel.OutputMessageChannel
import x746143.ktntpg.pgclient.wire3.toByteArray
import kotlin.test.assertContentEquals

class TestOutputChannel : OutputMessageChannel {
    private var message = Unpooled.EMPTY_BUFFER

    override fun writeMessage(message: ByteBuf) {
        this.message = message
    }

    fun verifyMessage(expectedMsg: String) {
        assertContentEquals(expectedMsg.mixedHexToByteBuf().toByteArray(), message.toByteArray())
    }
}