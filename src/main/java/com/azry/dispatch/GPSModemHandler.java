package com.azry.dispatch;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Handler class that stores data GPS/GPRS modems in (1)file on the file system and into the (2)database.
 * <pre> 1.When storing in the file system it creates directories for files if they 
 * already do not exist. Top dir is named as a current year(yyyy), sub dir is called 
 * as a current month(MM) and files are called as current days of the month.</pre>
 * <pre> 2.When storing in the database in transfers data after parsing it into the 
 * corresponding table of the database.</pre>
 * Parse of data is accomplished by decoder classes and transfered to this class. 
 * </pre>
 * 
 * @author  George Kankava
 * @version 1.0
 */
public class GPSModemHandler extends SimpleChannelHandler {
	
	private final static Logger LOGGER = Logger.getLogger(GPSModemHandler.class);
	
	private static final int CORE_NTHREADS = 10;
	private static final int MAX_NTHREADS = 25;
	private static final long THREAD_LIFECYCLE_MILLIS = 60000;
	
	private static final Executor exec =  new ThreadPoolExecutor(CORE_NTHREADS, MAX_NTHREADS, THREAD_LIFECYCLE_MILLIS, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	private String folder = null;
	private String dbUser = null;
	private String dbPassword = null;

	public GPSModemHandler(String folder, String dbUser, String dbPassword) {
		this.folder = folder;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		final GPSData gpsData = (GPSData) e.getMessage();
		try {
			// acknowledge modem about data reception
			sendCommandToModem(e.getChannel(), gpsData.getUnitId());
		} catch (SQLException e3) {
			LOGGER.error(e3.getMessage());
		}
		// building record info
		StringBuilder sb = new StringBuilder();
		if (gpsData.getTimestamp() != null) {
			sb.append(gpsData.getTimestamp()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getUnitId() != null) {
			sb.append(gpsData.getUnitId()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getLongitude() != null) {
			sb.append(gpsData.getLongitude()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getLatitude() != null) {
			sb.append(gpsData.getLatitude()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAltitude() != null) {
			sb.append(gpsData.getAltitude()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getSpeed() != null) {
			sb.append(gpsData.getSpeed()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAngle() != null) {
			sb.append(gpsData.getAngle()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getSatelite() != null) {
			sb.append(gpsData.getSatelite()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getReasonCode() != null) {
			sb.append(gpsData.getReasonCode()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getRunMode() != null) {
			sb.append(gpsData.getRunMode()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAc() != null) {
			sb.append(gpsData.getAc()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAd0() != null) {
			sb.append(gpsData.getAd0()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAd1() != null) {
			sb.append(gpsData.getAd1()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAd2() != null) {
			sb.append(gpsData.getAd2()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getAd3() != null) {
			sb.append(gpsData.getAd3()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getRs() != null) {
			sb.append(gpsData.getRs()).append(",");
		} else {
			sb.append(",");
		}
		if (gpsData.getSystemFlag() != null) {
			sb.append(gpsData.getSystemFlag()).append(",");
		} else {
			sb.append(",");
		}
		sb.append("\n");

		// write data in file
		Calendar c = Calendar.getInstance();
		Path path = FileSystems.getDefault().getPath(folder, String.valueOf(c.get(Calendar.YEAR)), String.valueOf((c.get(Calendar.MONTH) + 1)));
		try {
			Files.createDirectories(path);
			Files.write(FileSystems.getDefault().getPath(path.toString(), String.valueOf(c.get(Calendar.DATE) + ".txt")), 
					sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			Runnable task = new Runnable() {
				public void run() {
					Connection con;
					try {
						con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dispatcher", dbUser, dbPassword);
						String statement = "INSERT INTO `tracker_log`(tl_timestamp, tl_unit, tl_x, tl_y, tl_altitude, tl_direction, tl_speed,"
								+ " tl_satellite, tl_reacon_code, tl_system_flag, tl_ad0, tl_ad1, tl_ad2,"
								+ " tl_ad3, tl_ac, tl_rs) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement ps = con.prepareStatement(statement);
						if (gpsData.getTimestamp() != null) {
							ps.setString(1, gpsData.getTimestamp());
						} else {
							throw new IllegalArgumentException("timestamp cannot be null");
						}
						if (gpsData.getUnitId() != null) {
							ps.setString(2, gpsData.getUnitId());
						} else {
							throw new IllegalArgumentException("vehicle unit number cannot be null");
						}
						if (gpsData.getLongitude() != null) {
							ps.setDouble(3, gpsData.getLongitude());
						} else {
							throw new IllegalArgumentException("longitude cannot be null");
						}
						if (gpsData.getLatitude() != null) {
							ps.setDouble(4, gpsData.getLatitude());
						} else {
							throw new IllegalArgumentException("latitude cannot be null");
						}
						if (gpsData.getAltitude() != null) {
							ps.setDouble(5, gpsData.getAltitude());
						} else {
							ps.setNull(5, Types.NULL);
						}
						if (gpsData.getAngle() != null) {
							ps.setInt(6, gpsData.getAngle());
						} else {
							ps.setNull(6, Types.NULL);
						}
						if (gpsData.getSpeed() != null) {
							ps.setDouble(7, gpsData.getSpeed());
						} else {
							ps.setNull(7, Types.NULL);
						}
						if (gpsData.getSatelite() != null) {
							ps.setShort(8, gpsData.getSatelite());
						} else {
							ps.setNull(8, Types.NULL);
						}
						if (gpsData.getReasonCode() != null) {
							ps.setInt(9, gpsData.getReasonCode());
						} else {
							ps.setNull(9, Types.NULL);
						}
						if (gpsData.getSystemFlag() != null) {
							ps.setString(10, gpsData.getSystemFlag());
						} else {
							ps.setNull(10, Types.NULL);
						}
						if (gpsData.getAd0() != null) {
							ps.setString(11, gpsData.getAd0());
						} else {
							ps.setNull(11, Types.NULL);
						}
						if (gpsData.getAd1() != null) {
							ps.setString(12, gpsData.getAd1());
						} else {
							ps.setNull(12, Types.NULL);
						}
						if (gpsData.getAd2() != null) {
							ps.setString(13, gpsData.getAd2());
						} else {
							ps.setNull(13, Types.NULL);
						}
						if (gpsData.getAd3() != null) {
							ps.setString(14, gpsData.getAd3());
						} else {
							ps.setNull(14, Types.NULL);
						}
						if (gpsData.getAc() != null) {
							ps.setInt(15, gpsData.getAc());
						} else {
							ps.setNull(15, Types.NULL);
						}
						if (gpsData.getRs() != null) {
							ps.setByte(16, gpsData.getRs());
						} else {
							ps.setNull(16, Types.NULL);
						}
						// writing information to the database
						ps.executeUpdate();
					} catch (SQLException e) {
						LOGGER.error("SQL exception: " + e.getMessage());
					} catch (IllegalArgumentException e) {
						LOGGER.error(e.getMessage());
					}
				}
			};
			exec.execute(task);
		} catch (IOException e2) {
			LOGGER.error("IO Exception: " + e2.getMessage());
		}
		}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		LOGGER.error(e.getCause().getMessage());
		Channel ch = e.getChannel();
		ch.close();
	}
	
	/**
	 * Send AT command to a modem.
	 * 
	 * @param channel channel object to write response with
	 * @throws SQLException 
	 * 
	 */
	private boolean sendCommandToModem(Channel ch, String modemId) throws SQLException  {
	 	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/dispatcher", dbUser, dbPassword);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT modem_command FROM modem_commands WHERE modem_id LIKE " + modemId + " AND send_status = " + 0);
		if(rs.next()) {
			String c = "";
			try {
				// command
				c = rs.getString(1);
				ChannelBuffer buff = ChannelBuffers.buffer(c.length());
				buff.writeBytes(c.getBytes());
				// writing command on destination output
				ch.write(buff);
				stmt.executeUpdate("UPDATE modem_commands SET send_status=1 WHERE modem_id LIKE " + modemId);
				return true;
			} finally {
				LOGGER.info("AT command \"" + c + "\"sent to modem with ID :" + modemId);
			}
		} 
	
		return false;
	}
}
