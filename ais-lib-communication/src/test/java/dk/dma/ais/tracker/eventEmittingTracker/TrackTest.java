package dk.dma.ais.tracker.eventEmittingTracker;

import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.model.geometry.Position;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static dk.dma.commons.util.DateTimeUtil.MILLIS_TO_LOCALDATETIME_UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TrackTest {

    Track track;

    // GatehouseSourceTag [baseMmsi=2190067, country=DK, region=, timestamp=Thu Apr 10 15:30:29 CEST 2014]
    // [msgId=5, repeat=0, userId=219000606, callsign=OWNM@@@, dest=BOEJDEN-FYNSHAV@@@@@, dimBow=12, dimPort=8, dimStarboard=4, dimStern=58, draught=30, dte=0, eta=67584, imo=8222824, name=FRIGG SYDFYEN@@@@@@@, posType=1, shipType=61, spare=0, version=0]
    AisPacket msg5 = AisPacket.from(
            "$PGHP,1,2014,4,10,13,30,29,165,219,,2190067,1,28*22\r\n" +
                    "!BSVDM,2,1,1,A,53@ng7P1uN6PuLpl000I8TLN1=T@ITDp0000000u1Pr844@P07PSiBQ1,0*7B\r\n" +
                    "!BSVDM,2,2,1,A,CcAVCTj0EP00000,2*53");

    @Before
    public void setUp() throws Exception {
        track = new Track(2345);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetMmsi() throws Exception {
        assertEquals(2345, track.getMmsi());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotUpdateTrackWithWrongMMSI() throws Exception {
        Track track = new Track(2191000);
        track.update(msg5);
    }

    @Test
    public void canGetStaticInformationFromTrack() throws Exception {
        Track track = new Track(219000606);

        assertNull(track.getCallsign());
        assertNull(track.getShipName());
        assertNull(track.getIMO());
        assertNull(track.getShipDimensionBow());
        assertNull(track.getShipDimensionStern());
        assertNull(track.getShipDimensionPort());
        assertNull(track.getShipDimensionStarboard());
        assertNull(track.getVesselLength());
        assertNull(track.getVesselBeam());
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC), track.getTimeOfLastUpdate());

        track.update(msg5);

        assertEquals("OWNM@@@", track.getCallsign());
        assertEquals("FRIGG SYDFYEN@@@@@@@", track.getShipName());
        assertEquals(8222824, track.getIMO().intValue());
        assertEquals(12, track.getShipDimensionBow().intValue());
        assertEquals(58, track.getShipDimensionStern().intValue());
        assertEquals(8, track.getShipDimensionPort().intValue());
        assertEquals(4, track.getShipDimensionStarboard().intValue());
        assertEquals(12 + 58, track.getVesselLength().intValue());
        assertEquals(8 + 4, track.getVesselBeam().intValue());
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1397136629165L), track.getTimeOfLastUpdate());
    }

    @Test
    public void testGetNewestTrackingReport() throws Exception {
        assertNull(track.getNewestTrackingReport());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000000000L), Position.create(56, 12), 45.0f, 10.1f, 10.1f);
        assertNotNull(track.getNewestTrackingReport());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(1000000000), ZoneOffset.UTC), track.getNewestTrackingReport().getTimestamp());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000010000L), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertNotNull(track.getNewestTrackingReport());
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000010000L), track.getNewestTrackingReport().getTimestamp());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000020000L), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertNotNull(track.getNewestTrackingReport());
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000020000L), track.getNewestTrackingReport().getTimestamp());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000L), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertNotNull(track.getNewestTrackingReport());
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000020000L), track.getNewestTrackingReport().getTimestamp());
    }

    @Test
    public void testGetTimeOfLastUpdate() throws Exception {
        // Avoid NPEs elsewhere in code
        assertNotNull(track.getTimeOfLastUpdate());  // Avoid NPEs elsewhere in code
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC), track.getTimeOfLastUpdate());
    }

    @Test
    public void testGetTimeOfLastPositionReport() throws Exception {
        //assertEquals(null, track.getTimeOfLastPositionReport());
        assertNotNull(track.getTimeOfLastPositionReport());  // Avoid NPEs elsewhere in code
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC), track.getTimeOfLastPositionReport());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000000000L), Position.create(56, 12), 45.0f, 10.1f, 10.1f);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000000000L), track.getTimeOfLastPositionReport());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000010000L), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000010000L), track.getTimeOfLastPositionReport());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000020000L), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000020000L), track.getTimeOfLastPositionReport());

        track.update(MILLIS_TO_LOCALDATETIME_UTC.apply(1000L), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(1000020000L), track.getTimeOfLastPositionReport());
    }

    @Test
    public void testPurgeOldPositionReports() {
        assertEquals(0, track.getTrackingReports().size());
        assertEquals(20, track.MAX_AGE_POSITION_REPORTS_MINUTES);

        track.update(LocalDateTime.of(2014, 02, 11, 12, 32, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(1, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 34, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(2, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 36, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(3, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 38, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(4, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 38, 59), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(5, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 40, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(6, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 42, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(7, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 44, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(8, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 46, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(9, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 48, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(10, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 50, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(11, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 52, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(12, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 54, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(12, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 56, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(12, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 12, 58, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(12, track.getTrackingReports().size());
        track.update(LocalDateTime.of(2014, 02, 11, 13, 00, 00), Position.create(56.01, 12.01), 45.0f, 10.1f, 10.1f);
        assertEquals(11, track.getTrackingReports().size());

        List<TrackingReport> trackingReports = track.getTrackingReports();
        assertEquals(11, trackingReports.size());
        LocalDateTime oldestKept = LocalDateTime.of(2014, 02, 11, 12, 50).minus(track.MAX_AGE_POSITION_REPORTS_MINUTES, ChronoUnit.MINUTES);
        trackingReports.forEach(p -> assertTrue(p.getTimestamp().isAfter(oldestKept)));
    }

    @Test
    public void testPredictEast() {
        track.update(LocalDateTime.of(2014, 02, 11, 12, 32, 00), Position.create(56.00, 12.00), 90.0f, 1.0f, 1.0f);
        track.predict(LocalDateTime.of(2014, 02, 11, 12, 33, 00));
        assertEquals(56.000000, track.getNewestTrackingReport().getPosition().getLatitude(), 1e-6);
        assertEquals(12.000496, track.getNewestTrackingReport().getPosition().getLongitude(), 1e-6);
        assertEquals(LocalDateTime.of(2014, 02, 11, 12, 33, 00), track.getTimeOfLastPositionReport());
    }

    @Test
    public void testPredictNorth() {
        track.update(LocalDateTime.of(2014, 02, 11, 12, 32, 00), Position.create(56.00, 12.00), 0.0f, 1.0f, 1.0f);
        track.predict(LocalDateTime.of(2014, 02, 11, 12, 33, 00));
        assertEquals(56.000277, track.getNewestTrackingReport().getPosition().getLatitude(), 1e-6);
        assertEquals(12.000000, track.getNewestTrackingReport().getPosition().getLongitude(), 1e-6);
        assertEquals(LocalDateTime.of(2014, 02, 11, 12, 33, 00), track.getTimeOfLastPositionReport());
    }

    @Test
    public void testPredictLongAndFastNE() {
        track.update(LocalDateTime.of(2014, 02, 11, 12, 32, 00), Position.create(56.00, 12.00), 45.0f, 20.0f, 20.0f);
        track.predict(LocalDateTime.of(2014, 02, 11, 12, 42, 00));
        assertEquals(56.039237, track.getNewestTrackingReport().getPosition().getLatitude(), 1e-6);
        assertEquals(12.070274, track.getNewestTrackingReport().getPosition().getLongitude(), 1e-6);
        assertEquals(LocalDateTime.of(2014, 02, 11, 12, 42, 00), track.getTimeOfLastPositionReport());
    }

    @Test
    public void testPredictLongAndFastSW() {
        track.update(LocalDateTime.of(2014, 02, 11, 12, 32, 00), Position.create(56.00, 12.00), 180.0f+45.0f, 20.0f, 20.0f);
        track.predict(LocalDateTime.of(2014, 02, 11, 12, 42, 00));
        assertEquals(55.960722, track.getNewestTrackingReport().getPosition().getLatitude(), 1e-6);
        assertEquals(11.929867, track.getNewestTrackingReport().getPosition().getLongitude(), 1e-6);
        assertEquals(LocalDateTime.of(2014, 02, 11, 12, 42, 00), track.getTimeOfLastPositionReport());
    }

    @Test
    public void testGetTimeOfLastAisTrackingReport() throws Exception {
        final String[] NMEA_TEST_STRINGS = {
                // GatehouseSourceTag [baseMmsi=2190067, country=DK, region=, timestamp=Thu Apr 10 15:30:28 CEST 2014]
                // [msgId=1, repeat=0, userId=219000606, cog=2010, navStatus=0, pos=(33024811,6011092) = (33024811,6011092), posAcc=1, raim=0, specialManIndicator=0, rot=0, sog=108, spare=0, syncState=1, trueHeading=200, utcSec=60, slotTimeout=6, subMessage=1063]
                "$PGHP,1,2014,4,10,13,30,28,385,219,,2190067,1,12*26\r\n" +
                        "!BSVDM,1,1,,A,13@ng7P01dPeo6`OOc:onVAp0p@W,0*12",

                // GatehouseSourceTag [baseMmsi=2190067, country=DK, region=, timestamp=Thu Apr 10 15:30:38 CEST 2014]
                // [msgId=1, repeat=0, userId=219000606, cog=2010, navStatus=0, pos=(33024530,6010902) = (33024530,6010902), posAcc=1, raim=0, specialManIndicator=0, rot=0, sog=108, spare=0, syncState=1, trueHeading=200, utcSec=60, slotTimeout=2, subMessage=1427]
                "$PGHP,1,2014,4,10,13,30,38,88,219,,2190067,1,26*1E\r\n" +
                        "!BSVDM,1,1,,B,13@ng7P01dPeo0dOOb4WnVAp0`FC,0*26",

                // GatehouseSourceTag [baseMmsi=2190075, country=DK, region=, timestamp=Thu Apr 10 15:30:49 CEST 2014]
                // [msgId=1, repeat=0, userId=219000606, cog=2010, navStatus=0, pos=(33024198,6010667) = (33024198,6010667), posAcc=1, raim=0, specialManIndicator=0, rot=0, sog=108, spare=0, syncState=1, trueHeading=200, utcSec=60, slotTimeout=4, subMessage=1852]
                "$PGHP,1,2014,4,10,13,30,49,429,219,,2190075,1,2D*56\r\n" +
                        "!BSVDM,1,1,,A,13@ng7P01dPenqFOO`iWnVAp0hLt,0*2D",

                // GatehouseSourceTag [baseMmsi=2190067, country=DK, region=, timestamp=Thu Apr 10 15:30:59 CEST 2014]
                // [msgId=1, repeat=0, userId=219000606, cog=2010, navStatus=0, pos=(33023918,6010471) = (33023918,6010471), posAcc=1, raim=0, specialManIndicator=0, rot=0, sog=108, spare=0, syncState=1, trueHeading=200, utcSec=60, slotTimeout=1, subMessage=12528]
                "$PGHP,1,2014,4,10,13,30,59,428,219,,2190067,1,2D*55\r\n" +
                        "!BSVDM,1,1,,B,13@ng7P01dPenk>OOWcWnVAp0W3h,0*2D",

                // GatehouseSourceTag [baseMmsi=2190067, country=DK, region=, timestamp=Thu Apr 10 15:31:09 CEST 2014]
                // [msgId=1, repeat=0, userId=219000606, cog=2020, navStatus=0, pos=(33023639,6010274) = (33023639,6010274), posAcc=1, raim=0, specialManIndicator=0, rot=0, sog=108, spare=0, syncState=1, trueHeading=200, utcSec=60, slotTimeout=2, subMessage=348]
                "$PGHP,1,2014,4,10,13,31,9,318,219,,2190067,1,4F*61\r\n" +
                        "!BSVDM,1,1,,A,13@ng7P01dPene4OOVUoq6Ap0`5L,0*4F",

                // GatehouseSourceTag [baseMmsi=2190067, country=DK, region=, timestamp=Thu Apr 10 15:31:18 CEST 2014]
                // [msgId=1, repeat=0, userId=219000606, cog=2010, navStatus=0, pos=(33023417,6010117) = (33023417,6010117), posAcc=1, raim=0, specialManIndicator=0, rot=0, sog=108, spare=0, syncState=1, trueHeading=199, utcSec=60, slotTimeout=0, subMessage=2263]
                "$PGHP,1,2014,4,10,13,31,18,678,219,,2190067,1,03*23\r\n" +
                        "!BSVDM,1,1,,B,13@ng7P01dPen`:OOUfGnV?p0PSG,0*03"
        };

        AisPacket[] packets = new AisPacket[NMEA_TEST_STRINGS.length];
        for (int i = 0; i < NMEA_TEST_STRINGS.length; i++) {
            packets[i] = AisPacket.from(NMEA_TEST_STRINGS[i]);
        }

        // ---

        // No updates yet
        Track track = new Track(219000606);
        assertEquals(null, track.getTimeOfLastAisTrackingReport());

        // AIS update
        track.update(packets[0]);
        assertEquals(LocalDateTime.of(2014, 4, 10, 13, 30, 28, 385000000), track.getTimeOfLastAisTrackingReport());

        // Predicted update
        track.update(packets[0].getBestTimestamp() + 5000, Position.create(55, 11), 22f, 2.4f, 25f);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(packets[0].getTimestamp().getTime()), track.getTimeOfLastAisTrackingReport());

        // AIS update
        track.update(packets[1]);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(packets[1].getTimestamp().getTime()), track.getTimeOfLastAisTrackingReport());

        // AIS update
        track.update(packets[2]);
        assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(packets[2].getTimestamp().getTime()), track.getTimeOfLastAisTrackingReport());

        // Predicted updates
        for (int i = 0; i < 100; i++) {
            track.update(packets[2].getBestTimestamp() + i*5000, Position.create(55, 11), 22f, 2.4f, 25f);
            assertEquals(MILLIS_TO_LOCALDATETIME_UTC.apply(packets[2].getTimestamp().getTime()), track.getTimeOfLastAisTrackingReport());
        }
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        Track track = new Track(219000606);
        track.update(msg5);
        track.update(1000000000, Position.create(56, 12), 45.0f, 10.1f, 10.1f);

        Track clone = track.clone();

        assertNotSame(clone, track);
        assertEquals(clone.toString(), track.toString());
        assertEquals(clone.getTrackingReports().size(), track.getTrackingReports().size());
        assertEquals(clone.getNewestTrackingReport(), track.getNewestTrackingReport());

        track.update(1000001000, Position.create(56.1, 12.1), 45.1f, 10.2f, 10.2f);
        assertNotEquals(clone.toString(), track.toString());
        assertEquals(clone.getTrackingReports().size() + 1, track.getTrackingReports().size());
        assertNotEquals(clone.getNewestTrackingReport(), track.getNewestTrackingReport());
    }

}
