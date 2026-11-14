package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadingFusionTest {
    @Test fun fusesAcrossNorthWrap(){
        val h=HeadingFusion.fuse(350.0,10.0,HeadingFusionConfig(0.5,45.0))
        assertEquals(0.0,h,1e-9)
    }
    @Test fun trustsGnssOnLargeInnovation(){
        val h=HeadingFusion.fuse(10.0,200.0,HeadingFusionConfig(0.5,45.0))
        assertEquals(200.0,h,1e-9)
    }
}