package com.petkuengsus.petkuengsus.pets

import com.petkuengsus.petkuengsus.internal.SimpleEffectList
import com.petkuengsus.petkuengsus.internal.SimpleConditionList

class PetLevel(
    val pet: Pet,
    val level: Int,
    val effectList: SimpleEffectList,
    val conditionList: SimpleConditionList
)
