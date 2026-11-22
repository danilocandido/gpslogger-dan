package com.mendhak.gpslogger.common;

import com.mendhak.gpslogger.GpsLoggingService;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DanCustomizationsTest {

    @Test
    public void isOnAllowedWifi_MatchesCaseInsensitiveAndTrimmed() {
        assertThat(GpsLoggingService.isOnAllowedWifi("HomeWifi, OfficeWifi", "homewifi"), is(true));
        assertThat(GpsLoggingService.isOnAllowedWifi("HomeWifi, OfficeWifi", "OtherWifi"), is(false));
        assertThat(GpsLoggingService.isOnAllowedWifi("", "HomeWifi"), is(false));
        assertThat(GpsLoggingService.isOnAllowedWifi("HomeWifi", ""), is(false));
    }

    @Test
    public void getEffectiveMinimumDistance_StationaryOnAllowedWifi_ReturnsConfigured100() {
        int dist = GpsLoggingService.getEffectiveMinimumDistance(100, true, 0.2f, "HomeWifi", "HomeWifi");
        assertThat(dist, is(100));
    }

    @Test
    public void getEffectiveMinimumDistance_MovingOnAllowedWifi_Returns30() {
        int dist = GpsLoggingService.getEffectiveMinimumDistance(100, true, 1.5f, "HomeWifi", "HomeWifi");
        assertThat(dist, is(30));
    }

    @Test
    public void getEffectiveMinimumDistance_OutsideAllowedWifi_Returns30() {
        int dist = GpsLoggingService.getEffectiveMinimumDistance(100, false, 0f, "HomeWifi", "StreetWifi");
        assertThat(dist, is(30));
    }

    @Test
    public void getEffectiveMinimumDistance_ConfiguredBelow30_ReturnsConfigured() {
        int dist = GpsLoggingService.getEffectiveMinimumDistance(15, true, 2.0f, "HomeWifi", "StreetWifi");
        assertThat(dist, is(15));
    }
}
