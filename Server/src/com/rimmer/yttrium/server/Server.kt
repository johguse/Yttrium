package com.rimmer.yttrium.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

/**
 * Contains common data used through the server.
 */
class ServerContext(val acceptorGroup: EventLoopGroup, val handlerGroup: EventLoopGroup)

/**
 * Runs a server by creating a server context and sending it to the provided starter callback.
 * This function only returns when the server shuts down.
 */
inline fun runServer(f: (ServerContext) -> Unit) {
    // Create the server thread pools to use for every module.
    // Use native Epoll if possible, since it gives much better performance for small packets.
    val handlerThreads = Runtime.getRuntime().availableProcessors()
    var acceptorGroup: EventLoopGroup
    var handlerGroup: EventLoopGroup

    if(Epoll.isAvailable()) {
        acceptorGroup = EpollEventLoopGroup(1)
        handlerGroup = EpollEventLoopGroup(handlerThreads)
    } else {
        acceptorGroup = NioEventLoopGroup(1)
        handlerGroup = NioEventLoopGroup(handlerThreads)
    }

    // Start the server.
    f(ServerContext(acceptorGroup, handlerGroup))

    // Wait for everything to finish.
    acceptorGroup.terminationFuture().awaitUninterruptibly()
    handlerGroup.terminationFuture().awaitUninterruptibly()
}

/**
 * Binds a connection listener to the provided port and calls the provided pipeline builder.
 * The server then listens for requests using the built pipeline.
 * Note that the pipeline builder is called for each connection, and thus should cache any common data.
 */
inline fun listen(context: ServerContext, port: Int, crossinline pipeline: ChannelPipeline.() -> Unit): ChannelFuture {
    val channel = if(context.handlerGroup is EpollEventLoopGroup) {
        EpollServerSocketChannel::class.java as Class<out ServerChannel>
    } else {
        NioServerSocketChannel::class.java
    }
    
    val child = object: ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) { pipeline(channel.pipeline()) }
    }

    return ServerBootstrap()
        .group(context.acceptorGroup, context.handlerGroup)
        .channel(channel)
        .childHandler(child)
        .bind(port).sync().channel().closeFuture()
}