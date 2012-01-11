package com.azry.dispatch;

import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class TeltonikaIMEIDecoder extends FrameDecoder {
	
	private final static Logger LOGGER = Logger.getLogger(TeltonikaIMEIDecoder.class);
	
	public static final int IMEI_LENGTH_BYTES = 2;
	
	// sent to modem in case we want to receive information from this modem with 
	// specified IMEI
	// in the current version we accept all incoming calls from TELTONIKA modems
	private static final byte ACCEPT_IMEI_BYTE = 1;
	
	private ConcurrentMap<Integer, String> imeiMap = null;
	
	public TeltonikaIMEIDecoder(ConcurrentMap<Integer, String> imeiMap) {
		this.imeiMap = imeiMap;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buff) throws Exception {
		short imeiLength = buff.getShort(0);
		if(imeiLength == (buff.readableBytes() - IMEI_LENGTH_BYTES)) {
			byte imeiBytes[] = new byte[imeiLength];
			buff.skipBytes(IMEI_LENGTH_BYTES);
			buff.readBytes(imeiBytes);
			byte dest[] = new byte[7];
			System.arraycopy(imeiBytes, imeiBytes.length - 7, dest, 0, dest.length);
			String imei = new String(dest);
			imeiMap.put(channel.getId(), imei);
			LOGGER.info("IMEI last 7 digits: " + imei + " from device with IP: " + channel.getRemoteAddress() + " from channel with ID: " + channel.getId());
			ChannelBuffer buf = ChannelBuffers.buffer(ACCEPT_IMEI_BYTE);
			buf.writeByte(1);
			channel.write(buf);
			return null;
		}
		Channels.fireMessageReceived(ctx, buff);
		return null;
	}

}
