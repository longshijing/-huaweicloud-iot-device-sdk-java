package com.huaweicloud.sdk.iot.device.devicelog;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DeviceLogService extends AbstractService {

    private static final String LOG_CONFIG = "log_config";

    private boolean logSwitch = true;

    private String endTime;

    private Map<String, String> connectLostMap;

    private Map<String, String> connectFailedMap;

    public Map<String, String> getConnectLostMap() {
        return connectLostMap;
    }

    public void setConnectLostMap(Map<String, String> connectLostMap) {
        this.connectLostMap = connectLostMap;
    }

    public Map<String, String> getConnectFailedMap() {
        return connectFailedMap;
    }

    public void setConnectFailedMap(Map<String, String> connectFailedMap) {
        this.connectFailedMap = connectFailedMap;
    }

    public boolean isLogSwitch() {
        return logSwitch;
    }

    public void setLogSwitch(boolean logSwitch) {
        this.logSwitch = logSwitch;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (LOG_CONFIG.equals(deviceEvent.getEventType())) {

            ObjectNode objectNode = JsonUtil.convertMap2Object(deviceEvent.getParas(), ObjectNode.class);

            String aSwitch = objectNode.get("switch").asText();
            String endTime = objectNode.get("end_time").asText();

            if ("on".equals(aSwitch)) {
                logSwitch = true;
            } else if ("off".equals(aSwitch)) {
                logSwitch = false;
            }

            setEndTime(endTime);
        }

    }

    /**
     * ????????????????????????
     *
     * @param timestamp ????????????????????????????????????
     * @param type      ???????????????????????????????????????
     *                  DEVICE_STATUS ???????????????
     *                  DEVICE_PROPERTY ???????????????
     *                  DEVICE_MESSAGE ???????????????
     *                  DEVICE_COMMAND???????????????
     * @param content   ????????????
     */
    public void reportDeviceLog(String timestamp, String type, String content) {

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("type", type);
        map.put("content", content);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("log_report");
        deviceEvent.setServiceId("$log");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setParas(map);

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportEvent");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);

    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @return true??????????????????  false???????????????????????????
     */
    public boolean canReportLog() {
        String endTime = this.getEndTime();
        if (endTime != null) {
            endTime = endTime.replace("T", "");
            endTime = endTime.replace("Z", "");
        }

        String timeStampFormat = "yyyyMMddHHmmss";
        SimpleDateFormat df = new SimpleDateFormat(timeStampFormat);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = df.format(new Date(System.currentTimeMillis()));

        if (this.isLogSwitch() && (endTime == null || currentTime.compareTo(endTime) < 0)) {
            return true;
        }

        return false;
    }

}
