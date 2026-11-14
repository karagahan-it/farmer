package ir.topograde.app

import ir.topograde.app.gnss.GnssTimeSync
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GnssTimeSyncTest {
    @Test fun syncsCloseReceivers(){
        val s=GnssTimeSync.evaluate("123519.100","123519.220",250)
        assertTrue(s.synchronized)
        assertEquals(120L,s.differenceMs)
    }
    @Test fun handlesMidnightWrap(){
        val s=GnssTimeSync.evaluate("235959.950","000000.050",250)
        assertTrue(s.synchronized)
        assertEquals(100L,s.differenceMs)
    }
    @Test fun rejectsLargeDifference(){
        assertFalse(GnssTimeSync.evaluate("120000.000","120001.000",250).synchronized)
    }
}