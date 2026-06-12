package com.petkuengsus.petkuengsus.api.event

import com.petkuengsus.petkuengsus.pets.Pet

interface PetEvent {
    val pet: Pet
}
