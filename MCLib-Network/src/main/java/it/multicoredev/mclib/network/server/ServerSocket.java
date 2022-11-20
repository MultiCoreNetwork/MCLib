package it.multicoredev.mclib.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import it.multicoredev.mclib.network.NetworkHandler;
import it.multicoredev.mclib.network.PacketDecoder;
import it.multicoredev.mclib.network.PacketEncoder;
import it.multicoredev.mclib.network.protocol.PacketListener;
import org.jetbrains.annotations.Nullable;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of MCLib-network.
 * MCLib-network is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class ServerSocket {
    private final int port;
    private final Class<? extends PacketListener> packetListener;
    private final Class<? extends NetworkHandler> networkHandlerClass;
    private LogLevel logLevel = LogLevel.INFO;

    private EventLoopGroup parent;
    private EventLoopGroup child;

    public ServerSocket(int port, Class<? extends PacketListener> packetListener, Class<? extends NetworkHandler> networkHandlerClass) {
        this.port = port;
        this.packetListener = packetListener;
        this.networkHandlerClass = networkHandlerClass;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void startServer() throws InterruptedException {
        parent = new NioEventLoopGroup();
        child = new NioEventLoopGroup();

        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                channel.pipeline()
                        .addLast(new PacketDecoder(), new PacketEncoder(), createNetworkHandler());
            }
        };

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(parent, child);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.handler(new LoggingHandler(logLevel));
        bootstrap.option(ChannelOption.SO_BACKLOG, 128);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childHandler(channelInitializer);

        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            parent.shutdownGracefully();
            child.shutdownGracefully();
        }
    }

    public void stopServer() {
        if (parent != null) parent.shutdownGracefully();
        if (child != null) child.shutdownGracefully();
    }

    @Nullable
    private NetworkHandler createNetworkHandler() {
        try {
            NetworkHandler handler = networkHandlerClass.getDeclaredConstructor().newInstance();
            PacketListener listener = packetListener.getDeclaredConstructor().newInstance();

            listener.setNetworkHandler(handler);
            handler.setPacketListener(listener);
            return handler;
        } catch (Exception ignored) {
            return null;
        }
    }
}
