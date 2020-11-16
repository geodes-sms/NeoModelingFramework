package geodes.sms.nmf.railway

import geodes.sms.nmf.editor.railway.Region
import geodes.sms.nmf.editor.railway.Segment
import geodes.sms.nmf.editor.railway.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnitTest {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = ModelManagerImpl(dbUri, username, password)
    private val resDirectory = File("../TestResults/railway/")
    //private val sizes = listOf(1000, 10000)
    private val sizes = listOf(
        10, 100, 1000, 5000, 10000, 15000, 20000, 25000, 50000, 60000, 70000,
        80000, 90000, 100000, 120000, 140000, 160000, 180000, 200000
    ) //sum = 1126010
    private val maxSize = sizes.maxOrNull()!!

    init {
        resDirectory.createNewFile()
    }

    @BeforeEach fun clear() {
        manager.clearDB()
        manager.clearCache()
    }

    @Test fun createTest() {
        val resWriter = File(resDirectory,"Create.csv").bufferedWriter()

        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..20) {    //repeat 20 times to calibrate
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    manager.createSwitch()
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i; ${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun createContainmentsTest() {
        val resWriter = File(resDirectory,"CreateContainments.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..20) {    //repeat 20 times to calibrate
                val container = manager.createRailwayContainer()
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    container.addRegions()
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                //clear db
                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i; ${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun updateTest() {
        val resWriter = File(resDirectory,"Update.csv").bufferedWriter()

        //preparation step
        val segments = LinkedList<Segment>()
        for (i in 1..maxSize) {
            val segment = manager.createSegment()
            segment.setLength(5)
            segments.add(segment)
        }
        manager.saveChanges()

        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..20) {
                val startTime = System.currentTimeMillis()
                for (segment in segments) {
                    segment.setLength(999)
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i; ${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    fun readTest() {
        //simple read by label
    }

    @Test fun readContainmentsTest() {
        val resWriter = File(resDirectory,"ReadContainments.csv").bufferedWriter()
        val container = manager.createRailwayContainer()
        for (i in 1..maxSize) {
            val region = container.addRegions()
            manager.unload(region)
        }
        manager.saveChanges()

        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..20) {    //calibrate
                val startTime = System.currentTimeMillis()
                /*val regions = */container.loadRegions(i)

                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i; ${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }


//    //fails ( removes only first containment
//    @Test fun removeContainmentsTest() {
//        val resWriter = File(resDirectory,"RemoveContainments.csv").bufferedWriter()
//        val calibration = 3 //20
//
//        // preparation step
//        val regions = LinkedList<Region>()
//        val container = manager.createRailwayContainer()
//        for (i in 1..(sizes.sum() * calibration)) {
//            regions.add(container.addRegions())
//        }
//        manager.saveChanges()
//
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {    //repeat 20 times to calibrate
//                val startTime = System.currentTimeMillis()
//                for (j in 1..i) {
//                    container.removeRegions(regions.pop())
//                }
//                manager.saveChanges()
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//            }
//            resWriter.write("$i; ${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }

    @AfterAll fun close() {
        manager.close()
    }
}