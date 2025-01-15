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
package x746143.ktntpg.pgclient.integration

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import x746143.ktntpg.pgclient.PgClient
import x746143.ktntpg.pgclient.PgProperties
import x746143.ktntpg.pgclient.test.toSingleLine
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueryTest {

    private val postgres = PostgreSQLContainer("postgres:16")
        .withUsername("test-user")
        .withPassword("test-password")
    private lateinit var client: PgClient
    private val bootstrap = Bootstrap()
        .group(NioEventLoopGroup())
        .channel(NioSocketChannel::class.java)

    @BeforeAll
    fun setUp() {
        postgres.withInitScript("test-tables.sql").start()
        val props = with(postgres) {
            PgProperties(
                port = firstMappedPort,
                username = username,
                password = password,
                database = databaseName,
                minPoolSizePerThread = 1,
                maxPoolSizePerThread = 1
            )
        }
        client = PgClient(props, bootstrap)
        runBlocking {
            client.initPool()
        }
    }

    @AfterAll
    fun tearDown() {
        postgres.stop()
    }

    @Timeout(1)
    @Test
    fun simpleQuery() = runBlocking {
        val sql = """
            select integer_column, varchar_column
            from basic_types_table
            order by integer_column
        """.toSingleLine()
        val rows = client.query(sql)
        assertEquals(2, rows.size)
        assertEquals(0, rows[0].getInt(0))
        assertEquals(1, rows[1].getInt(0))
        assertEquals("varchar_data_0", rows[0].getString(1))
        assertEquals("varchar_data_1", rows[1].getString(1))
    }
}