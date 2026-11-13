package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadingResolverTest {
    @Test fun usesReceiverHeadingForDualGnss(){
        val o=HeadingResolver.resolve(Orientation(10.0,2.0,3.0),123.0,HeadingConfig(HeadingSource.DUAL_GNSS,0.0,2.0,1.0,1.5))
        assertEquals(125.0,o.headingDeg,1e-9)
        assertEquals(1.0,o.rollDeg,1e-9)
        assertEquals(1.5,o.pitchDeg,1e-9)
    }
    @Test fun fallsBackToImuWhenHdtMissing(){
        val o=HeadingResolver.resolve(Orientation(20.0,0.0,0.0),null,HeadingConfig(HeadingSource.DUAL_GNSS))
        assertEquals(20.0,o.headingDeg,1e-9)
    }
}