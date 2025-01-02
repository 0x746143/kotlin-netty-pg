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
package com.github.x746143.knpg

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec

class NettyHttpServer(private val port: Int) {

    companion object {
        private const val MAX_CONTENT_LENGTH = 1024 * 1024
    }

    private val bossGroup: EventLoopGroup
    private val workerGroup: EventLoopGroup
    private val serverSocketChannelClass: Class<out ServerSocketChannel>

    init {
        if (Epoll.isAvailable()) {
            bossGroup = EpollEventLoopGroup(1)
            workerGroup = EpollEventLoopGroup()
            serverSocketChannelClass = EpollServerSocketChannel::class.java
        } else if (KQueue.isAvailable()) {
            bossGroup = KQueueEventLoopGroup(1)
            workerGroup = KQueueEventLoopGroup()
            serverSocketChannelClass = KQueueServerSocketChannel::class.java
        } else {
            bossGroup = NioEventLoopGroup(1)
            workerGroup = NioEventLoopGroup()
            serverSocketChannelClass = NioServerSocketChannel::class.java
        }
    }

    private class HttpServerInitializer : ChannelInitializer<SocketChannel>() {
        override fun initChannel(ch: SocketChannel) {
            with(ch.pipeline()) {
                addLast(HttpServerCodec())
                addLast(HttpObjectAggregator(MAX_CONTENT_LENGTH))
                addLast(HttpServerHandler())
            }
        }
    }

    fun start() {
        try {
            ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(serverSocketChannelClass)
                .childHandler(HttpServerInitializer())
                .bind(port).sync()
                .also { println("Server started.") } // TODO: replace to Logger
                .channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully().sync()
            bossGroup.shutdownGracefully().sync()
        }
    }
}