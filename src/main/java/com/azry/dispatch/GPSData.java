package com.azry.dispatch;

public class GPSData {
	String timestamp;
	String unitId;
	// X
	Double longitude;
	// Y
	Double latitude;
	// Height
	Double altitude;
	Integer angle;
	Double speed;
	Short satelite;
	Integer reasonCode;
	Integer runMode;
	String systemFlag;
	String ad0;
	String ad1;
	String ad2;
	String ad3;
	Integer ac;
	Byte rs;
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getUnitId() {
		return unitId;
	}
	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getAltitude() {
		return altitude;
	}
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}
	public Integer getAngle() {
		return angle;
	}
	public void setAngle(Integer angle) {
		this.angle = angle;
	}
	public Double getSpeed() {
		return speed;
	}
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	public Short getSatelite() {
		return satelite;
	}
	public void setSatelite(Short satelite) {
		this.satelite = satelite;
	}
	public Integer getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(Integer reasonCode) {
		this.reasonCode = reasonCode;
	}
	public Integer getRunMode() {
		return runMode;
	}
	public void setRunMode(Integer runMode) {
		this.runMode = runMode;
	}
	public String getSystemFlag() {
		return systemFlag;
	}
	public void setSystemFlag(String systemFlag) {
		this.systemFlag = systemFlag;
	}
	public String getAd0() {
		return ad0;
	}
	public void setAd0(String ad0) {
		this.ad0 = ad0;
	}
	public String getAd1() {
		return ad1;
	}
	public void setAd1(String ad1) {
		this.ad1 = ad1;
	}
	public String getAd2() {
		return ad2;
	}
	public void setAd2(String ad2) {
		this.ad2 = ad2;
	}
	public String getAd3() {
		return ad3;
	}
	public void setAd3(String ad3) {
		this.ad3 = ad3;
	}
	public Integer getAc() {
		return ac;
	}
	public void setAc(Integer ac) {
		this.ac = ac;
	}
	public Byte getRs() {
		return rs;
	}
	public void setRs(Byte rs) {
		this.rs = rs;
	}
	
	@Override
	public String toString() {
		return "GPSData [timestamp=" + timestamp + ", unitId=" + unitId
				+ ", longitude=" + longitude + ", latitude=" + latitude
				+ ", altitude=" + altitude + ", angle=" + angle + ", speed="
				+ speed + ", satelite=" + satelite + ", reasonCode="
				+ reasonCode + ", runMode=" + runMode + ", systemFlag="
				+ systemFlag + ", ad0=" + ad0 + ", ad1=" + ad1 + ", ad2=" + ad2
				+ ", ad3=" + ad3 + ", ac=" + ac + ", rs=" + rs + "]";
	}
}
