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
import x746143.ktntpg.pgclient.PgException
import x746143.ktntpg.pgclient.PgProperties
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationTest {

    private val postgres = PostgreSQLContainer("postgres:16")
        .withUsername("test-user")
        .withPassword("test-password")
    private lateinit var props: PgProperties
    private val bootstrap = Bootstrap()
        .group(NioEventLoopGroup())
        .channel(NioSocketChannel::class.java)

    @BeforeAll
    fun setUp() {
        postgres.start()
        props = with(postgres) {
            PgProperties(
                port = firstMappedPort,
                username = username,
                password = password,
                database = databaseName,
                minPoolSizePerThread = 1,
                maxPoolSizePerThread = 1,
            )
        }
    }

    @AfterAll
    fun tearDown() {
        postgres.stop()
    }

    @Timeout(1)
    @Test
    fun testSuccessfulAuthentication() = runBlocking {
        val pgClient = PgClient(props, bootstrap).initPool()
        assertEquals(1, pgClient.connectionCounter)
    }

    @Timeout(1)
    @Test
    fun testFailedAuthentication() = runBlocking {
        val ex = assertThrows<PgException> {
            PgClient(props.copy(password = "incorrect_password"), bootstrap).initPool()
        }
        assertEquals("FATAL", ex.severity)
        assertEquals("28P01", ex.code)
        assertContains(ex.message, "password authentication failed")
    }
}