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

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.util.concurrent.FastThreadLocal
import x746143.ktntpg.*
import x746143.ktntpg.channel.ContinuationChannel
import x746143.ktntpg.channel.NettyOutputChannel
import x746143.ktntpg.pgclient.wire3.StartupAuthHandler
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

class PgClient(
    private val props: PgProperties,
    private val bootstrap: Bootstrap
) : PgConnection {
    private val _connectionCounter = AtomicInteger()
    private val initPoolLatch = CountDownLatch(props.minPoolSizePerThread)
    val connectionCounter get() = _connectionCounter.get()

    // TODO: analyze io.netty.channel.pool.FixedChannelPool and io.netty.channel.pool.SimpleChannelPool
    private val connectionPool = object : FastThreadLocal<ConnectionPool>() {
        override fun initialValue(): ConnectionPool {
            return ConnectionPool(0, 0, Array(props.maxPoolSizePerThread) { null }, ArrayDeque())
        }
    }

    fun initPool(): PgClient {
        var exception: Exception? = null
        repeat(props.minPoolSizePerThread) {
            val eventLoop = bootstrap.config().group().next()
            eventLoop.startCoroutine {
                try {
                    with(connectionPool.get()) {
                        connections[size++] = createConnection(eventLoop)
                        capacity++
                        _connectionCounter.incrementAndGet()
                    }
                } catch (e: Exception) {
                    exception = e
                } finally {
                    initPoolLatch.countDown()
                }
            }
        }
        initPoolLatch.await()
        exception?.let { throw it }
        return this
    }

    // TODO: add timeout
    private suspend fun createConnection(eventLoop: EventLoop): PgConnection {
        val inputChannel = ContinuationChannel()
        val bs = bootstrap.clone(eventLoop).handler(object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                ch.pipeline()
                    .addLast(LengthFieldBasedFrameDecoder(0xFFFF, 1, 4, -4, 0))
                    .addLast(object : SimpleChannelInboundHandler<ByteBuf>() {
                        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                            inputChannel.send(msg.retain())
                        }
                    })
            }
        })

        val channel = bs.connectAwait(props.host, props.port)
        val connection = PgConnectionImpl(inputChannel, NettyOutputChannel(channel)) {
            releaseConnection(it)
        }
        StartupAuthHandler(connection.input, connection.output, props)
            .sendStartupMessage()
            .authenticate()
        return connection
    }

    // TODO: add timeout
    private suspend fun acquireConnection(): PgConnection {
        val nettyContext = coroutineContext[NettyContext]
            ?: throw PgException("The coroutine is running outside of Netty EventLoop")
        val pool = connectionPool.get()
        return if (pool.size == 0) {
            if (pool.capacity == props.maxPoolSizePerThread) {
                return suspendUninterceptedCoroutine {
                    pool.pendingQueue.addLast(it)
                }
            } else {
                pool.capacity++
                _connectionCounter.incrementAndGet()
                return createConnection(nettyContext.eventLoop)
            }
        } else {
            pool.connections[--pool.size]!!
        }
    }

    private fun releaseConnection(connection: PgConnectionImpl) {
        with(connectionPool.get()) {
            if (pendingQueue.isEmpty()) {
                connections[size++] = connection
            } else {
                pendingQueue.removeFirst().resume(connection)
            }
        }
    }

    override suspend fun query(sql: String): List<Row> {
        return acquireConnection().use {
            it.query(sql)
        }
    }

    override suspend fun preparedQuery(sql: String): List<Row> {
        return acquireConnection().use {
            it.preparedQuery(sql)
        }
    }

    suspend fun transaction(block: (PgConnection) -> Unit) {
        return acquireConnection().use {
            block(it)
        }
    }

    override suspend fun close() {
        TODO()
    }

    private class ConnectionPool(
        var size: Int,
        var capacity: Int,
        val connections: Array<PgConnection?>,
        val pendingQueue: ArrayDeque<Continuation<PgConnection>>
    )
}