package geodes.sms.nmf.functional

import java.lang.NumberFormatException
import java.lang.management.ManagementFactory

fun main() {
    val name = ManagementFactory.getRuntimeMXBean().name
    //println(name)
    try {
        val pid = name.split("@")[0].toLong()
        println(pid)
    } catch (e: NumberFormatException) {
        println("cannot get process id")
    }
}