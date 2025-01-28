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
package x746143.ktntpg.pgclient

import x746143.ktntpg.channel.InputMessageChannel
import x746143.ktntpg.channel.OutputMessageChannel
import x746143.ktntpg.pgclient.wire3.QueryHandler

class PgConnectionImpl(
    val input: InputMessageChannel,
    val output: OutputMessageChannel,
    private val onClose: (suspend (PgConnectionImpl) -> Unit)?
) : PgConnection {

    private val queryHandler = QueryHandler(input, output)

    override suspend fun query(sql: String): List<Row> {
        return queryHandler.query(sql)
    }

    override suspend fun preparedQuery(sql: String): List<Row> {
        return queryHandler.preparedQuery(sql)
    }

    private suspend fun closeConnection() {
        TODO()
    }

    override suspend fun close() {
        onClose?.invoke(this) ?: closeConnection()
    }
}
