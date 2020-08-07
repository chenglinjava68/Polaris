package com.polaris.container.gateway.proxy.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebsocketClientImpl extends WebSocketClient {
    private ChannelHandlerContext ctx;
    
    private static final Logger log = LoggerFactory.getLogger(WebsocketClientImpl.class);
    
    public WebsocketClientImpl(String uri, ChannelHandlerContext ctx) throws URISyntaxException {
        super(new URI(uri));
        this.ctx = ctx;
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        log.debug("------ WebsocketClientImpl onOpen ------");
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        log.debug("------ WebsocketClientImpl onClose ------");
        WsComponent.close(ctx, new CloseWebSocketFrame());
    }

    @Override
    public void onError(Exception arg0) {
        log.debug("------ WebsocketClientImpl onError ------");
        WsComponent.close(ctx, new CloseWebSocketFrame());
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        log.debug("------ WebsocketClientImpl onMessage ByteBuffer ------");
        if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
            int len = bytes.limit() - bytes.position();
            byte[] newBytes = new byte[len];
            bytes.get(newBytes);
            ctx.writeAndFlush(new BinaryWebSocketFrame(io.netty.buffer.Unpooled.copiedBuffer(newBytes)));
        } else {
            WsComponent.close(ctx, new CloseWebSocketFrame());
        }
    }
    
    @Override
    public void onMessage(String message) {
        log.debug("------ WebsocketClientImpl onMessage text ------");
        if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(new TextWebSocketFrame(message));
        } else {
            WsComponent.close(ctx, new CloseWebSocketFrame());
        }
    }
}