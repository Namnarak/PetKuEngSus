package com.petkuengsus.petkuengsus

import com.petkuengsus.petkuengsus.api.PetKuEngSusAPI
import com.petkuengsus.petkuengsus.pets.Pet
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.getPetLevel
import com.petkuengsus.petkuengsus.pets.getPetProgress
import com.petkuengsus.petkuengsus.pets.getPetXP
import com.petkuengsus.petkuengsus.pets.getPetXPRequired
import com.petkuengsus.petkuengsus.pets.givePetExperience
import com.petkuengsus.petkuengsus.pets.hasPet
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

internal object PetKuEngSusAPIImpl : PetKuEngSusAPI {
    override fun hasPet(player: OfflinePlayer, pet: Pet) = player.hasPet(pet)

    override fun getActivePet(player: OfflinePlayer): Pet? = player.activePet

    override fun setActivePet(player: OfflinePlayer, pet: Pet?) {
        player.activePet = pet
    }

    override fun getPetLevel(player: OfflinePlayer, pet: Pet) = player.getPetLevel(pet)

    override fun givePetExperience(player: Player, pet: Pet, amount: Double) =
        player.givePetExperience(pet, amount)

    override fun givePetExperience(player: Player, pet: Pet, amount: Double, applyMultipliers: Boolean) =
        player.givePetExperience(pet, amount, noMultiply = !applyMultipliers)

    override fun getPetProgress(player: OfflinePlayer, pet: Pet) =
        player.getPetProgress(pet)

    override fun getPetXPRequired(player: OfflinePlayer, pet: Pet) =
        player.getPetXPRequired(pet)

    override fun getPetXP(player: OfflinePlayer, pet: Pet) =
        player.getPetXP(pet)
}
