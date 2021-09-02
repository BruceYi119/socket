package com.netty.socket;

import java.util.ArrayList;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

@Sharable
public class InitHandler extends ChannelInitializer<SocketChannel> {

	private ArrayList<ChannelHandler> hansdlers = null;

	public InitHandler(ArrayList<ChannelHandler> hansdlers) {
		this.hansdlers = hansdlers;
	}

	@Override
	protected void initChannel(SocketChannel sc) throws Exception {
		ChannelPipeline pipe = sc.pipeline();

		for (ChannelHandler handler : hansdlers)
			pipe.addLast(handler);
	}

}