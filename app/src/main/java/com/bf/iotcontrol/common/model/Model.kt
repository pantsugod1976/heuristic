package com.bf.iotcontrol.common.model

enum class Attribute {
    WAY,
    WALL,
    START,
    GOAL
}

data class Model(
    var attr: Attribute = Attribute.WAY
)