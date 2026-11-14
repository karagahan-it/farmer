package ir.topograde.app.guidance

import kotlin.math.*

object BladeGeometryEngine {
    fun compute(
        antennaX:Double, antennaY:Double, antennaZ:Double,
        orientation:Orientation, geometry:MachineGeometry
    ):BladePose {
        val h=Math.toRadians(orientation.headingDeg)
        val roll=Math.toRadians(orientation.rollDeg)
        val pitch=Math.toRadians(orientation.pitchDeg)
        val forward=geometry.antennaToBladeForwardM
        val right=geometry.antennaToBladeRightM
        val dx=forward*sin(h)+right*cos(h)
        val dy=forward*cos(h)-right*sin(h)
        val verticalForward=forward*tan(pitch)
        val centerZ=antennaZ-geometry.antennaToBladeDownM-verticalForward
        val half=geometry.bladeWidthM/2.0
        val dzHalf=half*tan(roll)
        val leftZ=centerZ-dzHalf
        val rightZ=centerZ+dzHalf
        val slope=if(geometry.bladeWidthM<=1e-9)0.0 else (rightZ-leftZ)/geometry.bladeWidthM*100.0
        return BladePose(antennaX+dx,antennaY+dy,centerZ,leftZ,rightZ,slope)
    }
}