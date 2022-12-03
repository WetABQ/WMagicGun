package me.hbj233.wmagicguns.utils

import java.util.*

object RandomUtils {

    @JvmStatic
    fun r(min: Int, max: Int): Int {
        val rand = Random()
        return rand.nextInt(max - min + 1) + min
    }

}