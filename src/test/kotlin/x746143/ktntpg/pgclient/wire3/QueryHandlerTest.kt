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
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.test.assertEquals

class QueryHandlerTest : AbstractHandlerTest() {

    @Timeout(1)
    @Test
    fun simpleQuery() = runBlocking {
        val deferred = async(Unconfined) {
            val sql = "select integer_column, varchar_column from basic_types_table"
            QueryHandler(input, output).query(sql)
        }

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-QUERY
        val query = "Q[00000041]select integer_column, varchar_column from basic_types_table[00]"
        output.verifyMessage(query)

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-ROWDESCRIPTION
        // TODO: verify a describe message

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-ROWDESCRIPTION
        // TODO: write a row description message

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-DATAROW
        val dataRow0 = "D[0000001d][0002][00000001]0[0000000e]varchar_data_0"
        val dataRow1 = "D[0000001d][0002][00000001]1[0000000e]varchar_data_1"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-COMMANDCOMPLETE
        val commandComplete = "C[0000000d]SELECT 2[00]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-READYFORQUERY
        val readyForQuery = "Z[00000005]I"

        input.send(dataRow0, dataRow1, commandComplete, readyForQuery)

        val rows = deferred.await()
        assertEquals(2, rows.size)
        assertEquals(0, rows[0].getInt(0))
        assertEquals("varchar_data_0", rows[0].getString(1))
        assertEquals(1, rows[1].getInt(0))
        assertEquals("varchar_data_1", rows[1].getString(1))
    }

    @Timeout(1)
    @Test
    fun preparedQueryWithoutParams() = runBlocking {
        val deferred = async(Unconfined) {
            val sql = "select integer_column, varchar_column from basic_types_table"
            QueryHandler(input, output).preparedQuery(sql)
        }

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-PARSE
        val parse = "P[00000044][00]select integer_column, varchar_column from basic_types_table[00][0000]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-BIND
        val bind = "B[0000000c][00][00][0000][0000][0000]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-DESCRIBE
        // TODO: verify describe message

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-EXECUTE
        val execute = "E[00000009][00][00000000]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-SYNC
        val sync = "S[00000004]"

        output.verifyMessages(parse, bind, execute, sync)

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-PARSECOMPLETE
        val parseComplete = "1[00000004]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-BINDCOMPLETE
        val bindComplete = "2[00000004]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-ROWDESCRIPTION
        // TODO: write row description message

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-DATAROW
        val dataRow0 = "D[0000001d][0002][00000001]4[0000000e]varchar_data_4"
        val dataRow1 = "D[0000001d][0002][00000001]5[0000000e]varchar_data_5"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-COMMANDCOMPLETE
        val commandComplete = "C[0000000d]SELECT 2[00]"

        // https://www.postgresql.org/docs/16/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-READYFORQUERY
        val readyForQuery = "Z[00000005]I"

        input.send(parseComplete, bindComplete, dataRow0, dataRow1, commandComplete, readyForQuery)

        val rows = deferred.await()
        assertEquals(2, rows.size)
        assertEquals(4, rows[0].getInt(0))
        assertEquals("varchar_data_4", rows[0].getString(1))
        assertEquals(5, rows[1].getInt(0))
        assertEquals("varchar_data_5", rows[1].getString(1))
    }

    // TODO: add tests for prepared queries with parameters

    // TODO: add tests for cached prepared queries
}