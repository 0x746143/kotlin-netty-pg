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

data class PgProperties(
    val host: String = "localhost",
    val port: Int = 54321,
    override val username: String = "postgres",
    override val password: String = "postgres1234",
    override val database: String = username,
    override val appName: String = "kotlin-netty-pg",
    val minPoolSizePerThread: Int = 1,
    val maxPoolSizePerThread: Int = 10,
//    val timeout: Duration = 5.seconds TODO: implement timeout using coroutines and netty
) : PgAuthProperties

internal interface PgAuthProperties {
    val username: String
    val password: String
    val database: String
    val appName: String
}