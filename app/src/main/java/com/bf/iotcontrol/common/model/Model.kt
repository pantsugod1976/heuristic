package com.bf.iotcontrol.common.model

enum class Attribute {
    WAY,
    WALL,
    START,
    GOAL
}

data class Model(
    val attr: Attribute = Attribute.WAY
)