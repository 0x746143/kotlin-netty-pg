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
import x746143.ktntpg.pgclient.wire3.BackendMessage.BIND_COMPLETE
import x746143.ktntpg.pgclient.wire3.BackendMessage.COMMAND_COMPLETE
import x746143.ktntpg.pgclient.wire3.BackendMessage.DATA_ROW
import x746143.ktntpg.pgclient.wire3.BackendMessage.EMPTY_QUERY_RESPONSE
import x746143.ktntpg.pgclient.wire3.BackendMessage.ERROR_RESPONSE
import x746143.ktntpg.pgclient.wire3.BackendMessage.PARSE_COMPLETE
import x746143.ktntpg.pgclient.wire3.BackendMessage.READY_FOR_QUERY
import x746143.ktntpg.pgclient.wire3.BackendMessage.ROW_DESCRIPTION

internal class QueryHandler(
    private val input: InputMessageChannel,
    private val output: OutputMessageChannel
) {
    // TODO: Cache prepared statements

    suspend fun query(sql: String): List<Row> {
        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-QUERY
        output.writePgMessage(FrontendMessage.QUERY) {
            writeCString(sql)
        }
        return handleBackendMessages()
    }

    suspend fun preparedQuery(sql: String): List<Row> {
        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-PARSE
        output.writePgMessages {
            writePgMessage(FrontendMessage.PARSE) {
                // TODO: add named prepared statement
                writeCString("") // The name of the destination prepared statement (an empty string selects the unnamed prepared statement).
                writeCString(sql) // The query string to be parsed.
                // TODO: add parameters
                writeShort(0) // The number of parameter data types specified (can be zero)
            }
            // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-BIND
            writePgMessage(FrontendMessage.BIND) {
                // TODO: add named portal
                writeCString("") // The name of the destination portal (an empty string selects the unnamed portal).
                // TODO: add named prepared statement
                writeCString("") // The name of the source prepared statement (an empty string selects the unnamed prepared statement).
                // TODO: add parameters
                writeShort(0) // The number of parameter format codes
                writeShort(0) // The number of parameter values
                writeShort(0) // The number of result-column format codes
            }
            // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-EXECUTE
            writePgMessage(FrontendMessage.EXECUTE) {
                // TODO: add named portal
                writeCString("") // The name of the portal to execute (an empty string selects the unnamed portal).
                // TODO: add row limit
                writeInt(0) // Maximum number of rows to return, if portal contains a query that returns rows (ignored otherwise). Zero denotes “no limit”.
            }
            // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-SYNC
            writePgMessage(FrontendMessage.SYNC)
        }
        return handleBackendMessages()
    }

    private suspend fun handleBackendMessages(): MutableList<Row> {
        val result = mutableListOf<Row>()
        while (true) {
            val message = input.readMessage()
            val messageType = message.readByte()
            message.readInt() // skip message length
            when (messageType) {
                COMMAND_COMPLETE -> {} // TODO: handle
                PARSE_COMPLETE -> {} // TODO: handle
                BIND_COMPLETE -> {} // TODO: handle
                ROW_DESCRIPTION -> {} // TODO: handle
                DATA_ROW -> handleDataRow(message, result)
                EMPTY_QUERY_RESPONSE -> {} // TODO: handle
                ERROR_RESPONSE -> handleErrorResponse(message)
                READY_FOR_QUERY -> return result
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