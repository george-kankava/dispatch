package com.azry.dispatch;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;

public class G200_300Decoder extends DelimiterBasedFrameDecoder {

	private final static Logger LOGGER = Logger.getLogger(G200_300Decoder.class);
	
	public G200_300Decoder(int maxFrameLength, boolean stripDelimiter, ChannelBuffer delimiter) {
		super(maxFrameLength, stripDelimiter, delimiter);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		ChannelBuffer buff = (ChannelBuffer)super.decode(ctx, channel, buffer);
		if(buff == null) {
			return null;
		}
		GPSData gpsData = new GPSData();
		
		byte dst [] = new byte[buff.readableBytes()];
		buff.readBytes(dst);
		StringBuilder data = new StringBuilder(new String(dst));
		if(!data.substring(0, 4).startsWith("<POS")) {
			return null;
		}
		data.deleteCharAt(0);
		data.deleteCharAt(data.length() - 1);
		data.append(" ");
		
		try {
			LOGGER.info("Received package decode start: " + System.currentTimeMillis() + "ms from device: " + parseData(data, "N="));

			String n = parseData(data, "N=");
			gpsData.setUnitId(n);

			String x = parseData(data, "X=");
			if (x != null) {
				gpsData.setLongitude(Double.parseDouble(x));
			}

			String y = parseData(data, "Y=");
			if (y != null) {
				gpsData.setLatitude(Double.parseDouble(y));
			}

			String h = parseData(data, "H=");
			if (h != null) {
				gpsData.setAltitude(Double.parseDouble(h));
			}

			String s = parseData(data, "S=");
			if (s != null) {
				gpsData.setSpeed(Double.parseDouble(s));
			}

			String d = parseData(data, "D=");
			if (d != null) {
				gpsData.setAngle(Integer.parseInt(d));
			}

			// IO is ignored. we currently don't use it
			// index = data.indexOf("IO=");
			// String io = data.substring(index + 3, data.indexOf(" ", index));

			String e = parseData(data, "E=");
			if (e != null) {
				gpsData.setReasonCode(Integer.parseInt(e));
			}

			String r = parseData(data, "R=");
			if (r != null) {
				gpsData.setRunMode(Integer.parseInt(r));
			}

			String ac = parseData(data, "AC=");
			if (ac != null) {
				LOGGER.info("Sending AC to device: " + n);
				gpsData.setAc(Integer.parseInt(s));
				String resp = "<ACK.SERVER=" + ac + ">";
				ChannelBuffer b = ChannelBuffers.buffer(resp.length());
				buff.writeBytes(resp.getBytes());
				// write response about data reception
				channel.write(b);
			}

			String adString = parseData(data, "AD=");
			String ad[] = null;
			if (adString != null) {
				ad = adString.split(",");
				gpsData.setAd0(ad[0]);
				gpsData.setAd1(ad[1]);
				gpsData.setAd2(ad[2]);
				gpsData.setAd3(ad[3]);
			}

			String rs = parseData(data, "RS=");
			if (rs != null) {
				gpsData.setRs(Byte.parseByte(rs));
			}

			String sv = parseData(data, "SV=");
			if (sv != null) {
				gpsData.setRs(Byte.parseByte(sv));
			}

			String sf = parseData(data, "SF=");
			if (sf != null) {
				gpsData.setSystemFlag(sf);
			}

			String a = parseData(data, "A=");

			String t = parseData(data, "T=");

			if (t != null && a != null) {
				StringBuilder tValue = new StringBuilder(String.valueOf(Integer.parseInt(t, 16)));
				if (tValue.charAt(0) != '0' && tValue.length() == 5) {
					tValue.insert(0, '0');
				}
				tValue.insert(2, ':');
				tValue.insert(5, ':');

				StringBuilder dValue = new StringBuilder(String.valueOf(Integer.parseInt(a, 16)));

				dValue.insert(4, '-');
				dValue.insert(7, '-');
				dValue.insert(10, ' ');

				String timestamp = dValue.toString() + tValue.toString();
				gpsData.setTimestamp(timestamp);

			}
			return gpsData;
		} finally {
			LOGGER.info("Received package decode end: " + System.currentTimeMillis() + "ms from device: " + parseData(data, "N="));
		}
	}
	
	private static String parseData(StringBuilder data, String code) {
		int index = data.indexOf(code);
		if(index != -1) {
			String codeValue = data.substring(index + code.length(), data.indexOf(" ", index));
			return codeValue;
		}
		return null;
	}
	
}
