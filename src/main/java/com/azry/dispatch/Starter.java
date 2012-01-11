package com.azry.dispatch;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class Starter {
	private final static Logger LOGGER = Logger.getLogger(Starter.class);
	public static void main(String[] args) {
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		final String folder = args[0];
		String server = args[1];
		String port = args[2];
		final String dbUser = args[3];
		final String dbPassword = args[4];

		// starts TELTONIKA server
		if (server.equals("teltonika")) {
			ServerBootstrap bootstrap = new ServerBootstrap(factory);
			startTeltonika(bootstrap, port, folder, dbUser, dbPassword);
			bootstrap.bind(new InetSocketAddress(Integer.parseInt(port)));
		// start G200/G300 server
		} else if (server.equals("g200/g300")) {
			ServerBootstrap bootstrap = new ServerBootstrap(factory);
			startG200_300(bootstrap, port, folder, dbUser, dbPassword);
			bootstrap.bind(new InetSocketAddress(Integer.parseInt(port)));
		// starts all registered servers
		} else if (server.equals("all")) {
			ServerBootstrap bootstrap1 = new ServerBootstrap(factory);
			ServerBootstrap bootstrap2 = new ServerBootstrap(factory);
			startTeltonika(bootstrap1, port.split(",")[0], folder, dbUser, dbPassword);
			startG200_300(bootstrap2, port.split(",")[1], folder, dbUser, dbPassword);
			bootstrap1.bind(new InetSocketAddress(Integer.parseInt(port.split(",")[0])));
			bootstrap2.bind(new InetSocketAddress(Integer.parseInt(port.split(",")[1])));
		}
		System.out.println("Application started. Please see log file for more information");
		LOGGER.info("Data will be logged in the file \"" + folder + File.separator + "yyyy" + File.separator + "MM" + File.separator + "dd\"\n\tWhere \"yyyy\" is the current year \"MM\" is the current month and \"dd\" is the current day");
	}

	public static void startG200_300(ServerBootstrap bootstrap, String port,
			final String folder, final String dbUser, final String dbPassword) {
		LOGGER.info("Starting g200/g300 server on port: " + port);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new G200_300Decoder(2048, false,
						ChannelBuffers.wrappedBuffer(new byte[]{'>'})),
						new GPSModemHandler(folder, dbUser, dbPassword));
			}
		});
	}

	public static void startTeltonika(ServerBootstrap bootstrap, String port,
			final String folder, final String dbUser, final String dbPassword) {
		LOGGER.info("Starting teltonika server on port: " + port);
		final ConcurrentMap<Integer, String> imeiMap = new ConcurrentHashMap<Integer, String>();
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new TeltonikaIMEIDecoder(imeiMap),
						new TeltonikaDecoder(imeiMap), new GPSModemHandler(
								folder, dbUser, dbPassword));
			}
		});
	}

}
