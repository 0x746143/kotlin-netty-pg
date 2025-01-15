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

import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import x746143.ktntpg.pgclient.test.TestInputChannel
import x746143.ktntpg.pgclient.test.TestOutputChannel

class QueryHandlerTest {

    private var input = TestInputChannel()
    private var output = TestOutputChannel()

    @Timeout(1)
    @Test
    fun simpleQuery() = runBlocking {
        val job = launch(Unconfined) {
            val sql = "select integer_column, varchar_column from basic_types_table"
            QueryHandler(input, output).query(sql)
        }

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-QUERY
        val query = "Q[00000041]select integer_column, varchar_column from basic_types_table[00]"
        output.verifyMessage(query)

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-ROWDESCRIPTION
        // TODO: handle row description message

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-READYFORQUERY
        val readyForQuery = "Z[00000005]I"
        input.send(readyForQuery)

        job.join()
    }
}