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

import io.netty.buffer.ByteBuf
import x746143.ktntpg.channel.InputMessageChannel
import x746143.ktntpg.channel.OutputMessageChannel
import x746143.ktntpg.pgclient.PgException
import x746143.ktntpg.pgclient.Row

internal class QueryHandler(
    private val input: InputMessageChannel,
    private val output: OutputMessageChannel
) {
    suspend fun query(sql: String): List<Row> {
        val result = mutableListOf<Row>()
        output.writePgMessage(FrontendMessage.QUERY) {
            writeCString(sql)
        }
        while (true) {
            val message = input.readMessage()
            val messageType = message.readByte()
            message.readInt() // skip message length
            when (messageType) {
                BackendMessage.COMMAND_COMPLETE -> {} // TODO: handle
                BackendMessage.ROW_DESCRIPTION -> {} // TODO: handle
                BackendMessage.DATA_ROW -> handleDataRow(message, result)
                BackendMessage.EMPTY_QUERY_RESPONSE -> {} // TODO: handle
                BackendMessage.ERROR_RESPONSE -> handleErrorResponse(message)
                BackendMessage.READY_FOR_QUERY -> return result
                else -> throw PgException("Unsupported message type: ${messageType.toInt().toChar()}")
            }
        }
    }

    private fun handleDataRow(message: ByteBuf, result: MutableList<Row>) {
        val values = Array<Any>(message.readShort().toInt()) {}
        for (i in values.indices) {
            values[i] = message.readString(message.readInt())
        }
        result.add(Row(values))
    }
}