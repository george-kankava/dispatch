package com.azry.dispatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class TeltonikaDecoder extends FrameDecoder {
	
	private final static Logger LOGGER = Logger.getLogger(TeltonikaDecoder.class);
	
	private static ConcurrentMap<Integer, String> imeiMap = null;
	
	
	public static final int INITIAL_ZERO_BYTES = 4;
	public static final int LENGTH_FIELD_OFFSET = 4;
	private static final int FRAME_BYTES_SIZE = 4;
	public static final byte CODEC_BYTE = 1;
	private static final int CRC_BYTES_SIZE = 4;
	
	
	public TeltonikaDecoder(ConcurrentMap<Integer, String> map) {
		super(true);
		imeiMap = map;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buff) throws Exception {
		int frameSize = buff.getInt(LENGTH_FIELD_OFFSET);
		int wholeSize = INITIAL_ZERO_BYTES + FRAME_BYTES_SIZE + frameSize + CRC_BYTES_SIZE;
		int readableBytes = buff.readableBytes();
		if(readableBytes < wholeSize) {
			return null;
		}
		
		return decodeMessageFrame(buff, channel, frameSize, wholeSize);
		
	}
	
	private List<GPSData> decodeMessageFrame(ChannelBuffer buff, Channel channel, int fSize, int wSize) {
		// skip first four zero bytes and frame size zero bytes
		buff.skipBytes(INITIAL_ZERO_BYTES + FRAME_BYTES_SIZE);
		
		// codecId byte is ignored
		buff.skipBytes(CODEC_BYTE);
		
		short numberOfData = buff.readUnsignedByte();
		// response to modem about data reception
		acknowledgeTeltonikaModem(channel, numberOfData);
		List<GPSData> list = new ArrayList<GPSData>();
		String imeiLast7digits = imeiMap.get(channel.getId());
		LOGGER.info("Received package decode start: " + System.currentTimeMillis() + "ms from device: " + imeiLast7digits);
		for (int i = 0; i < numberOfData; i++) {

			GPSData data = new GPSData();

			Date timestamp = new Date(buff.getLong(buff.readerIndex()));
			buff.readerIndex(buff.readerIndex() + 8);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			data.setTimestamp(format.format(timestamp));

			data.setUnitId(imeiMap.get(channel.getId()));

			// priority byte is ignored
			buff.skipBytes(1);

			// X
			StringBuilder longitude = new StringBuilder(String.valueOf(buff
					.readUnsignedInt()));
			longitude.insert(2, '.');
			data.setLongitude(Double.parseDouble(longitude.toString()));

			// Y
			StringBuilder latitude = new StringBuilder(String.valueOf(buff
					.readUnsignedInt()));
			latitude.insert(2, '.');
			data.setLatitude(Double.parseDouble(latitude.toString()));

			// H
			int altitude = buff.readUnsignedShort();
			data.setAltitude((double) altitude);

			int angle = buff.readUnsignedShort();
			data.setAngle(angle);

			short satelite = buff.readUnsignedByte();
			data.setSatelite(satelite);

			int speed = buff.readUnsignedShort();
			data.setSpeed((double) speed);

			// IO INFORMATION START

			// IO ENETS
			//
			// IO element ID of Event generated (in case when 00 data
			// generated not on event)
			// we skip this byte
			
			buff.skipBytes(1);
			
			// could read elements number but not necessary
			// instead we can skip it
			// short elementsNumber = buff.readUnsignedByte();

			buff.skipBytes(1);

			// number of records with value 1 byte length
			short oneByteElementNum = buff.readUnsignedByte();

			for (int j = 0; j < oneByteElementNum; j++) {
				buff.skipBytes(1);
				buff.skipBytes(1);
			}

			// number of records with value 2 byte length
			short twoByteElementNum = buff.readUnsignedByte();

			for (int j = 0; j < twoByteElementNum; j++) {
				buff.skipBytes(1);
				buff.skipBytes(2);
			}

			// number of records with value 4 byte length
			short fourByteElementNum = buff.readUnsignedByte();

			for (int j = 0; j < fourByteElementNum; j++) {
				buff.skipBytes(1);
				buff.skipBytes(4);
			}

			// number of records with value 8 byte length
			short eightByteElementNum = buff.readUnsignedByte();

			for (int j = 0; j < eightByteElementNum; j++) {
				buff.skipBytes(1);
				buff.skipBytes(8);
			}
			// IO INFORMATION END
			list.add(data);
		}
		LOGGER.info("Received package decode end: " + System.currentTimeMillis() + "ms from device: " + imeiLast7digits);
		// records number(1 byte) and CRC(4 bytes) left in the buffer unread
		LOGGER.info("Number of packages decoded: " + numberOfData);
		numberOfData = buff.readByte();
		// skip CRC bytes
		long CRC = buff.readUnsignedInt();
		LOGGER.info("Data CRC: " + CRC);
		
		return list;
	}
	
	/**
	 * acknowledges TELTONIKA modem about data reception
	 */
	private static void acknowledgeTeltonikaModem(Channel ch, int recordsNum) {
		ChannelBuffer buf = ChannelBuffers.buffer(4);
		byte b[] = intToByteArray(recordsNum);
		buf.writeBytes(b);
		LOGGER.info("Response to device about data reception with package amount: " + recordsNum);
		ch.write(buf);
	}
	
	private static byte[] intToByteArray(int value) {
	       byte[] b = new byte[4];
	       for (int i = 0; i < 4; i++) {
	           int offset = (b.length - 1 - i) * 8;
	           b[i] = (byte) ((value >>> offset) & 0xFF);
	       }
	       return b;
	}

}
