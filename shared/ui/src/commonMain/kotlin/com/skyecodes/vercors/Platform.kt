package com.skyecodes.vercors

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform