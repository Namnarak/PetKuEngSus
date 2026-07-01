package com.petkuengsus.petkuengsus.internal

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.Base64
import java.util.UUID

object SimpleItems {
    fun getByID(id: String): ItemStack? {
        return try {
            val material = Material.valueOf(id.uppercase())
            ItemStack(material)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun lookup(id: String): ItemStack? {
        if (id.startsWith("player_head texture:")) {
            return SkullBuilder.setSkullTexture(id.removePrefix("player_head texture:"))
        }
        if (id.startsWith("player_head:")) {
            return SkullBuilder.setSkullTexture(id.removePrefix("player_head:"))
        }
        if (id.startsWith("item:")) {
            val matStr = id.removePrefix("item:")
            val mat = try { Material.valueOf(matStr.uppercase()) } catch (e: Exception) { Material.STONE }
            return ItemStack(mat)
        }
        if (id.startsWith("block:")) {
            val matStr = id.removePrefix("block:")
            val mat = try { Material.valueOf(matStr.uppercase()) } catch (e: Exception) { Material.STONE }
            return ItemStack(mat)
        }
        return getByID(id)
    }

    fun builder(item: ItemStack): ItemStackBuilder = ItemStackBuilder(item)
}

class ItemStackBuilder(material: Material) {
    private val itemStack = ItemStack(material)
    private var meta = itemStack.itemMeta ?: throw IllegalStateException("Material $material has no meta")

    constructor(item: ItemStack) : this(item.type) {
        itemStack.setType(item.type)
        itemStack.amount = item.amount
        val clonedMeta = item.itemMeta?.clone()
        if (clonedMeta != null) {
            itemStack.itemMeta = clonedMeta
            meta = clonedMeta
        }
        itemStack.data = item.data?.clone()
    }

    fun setAmount(amount: Int): ItemStackBuilder {
        itemStack.amount = amount
        return this
    }

    fun setDisplayName(name: String): ItemStackBuilder {
        meta.setDisplayName(name)
        return this
    }

    fun setLore(lore: List<String>): ItemStackBuilder {
        meta.lore = lore
        return this
    }

    fun addLore(lines: List<String>): ItemStackBuilder {
        val existing = meta.lore ?: mutableListOf()
        existing.addAll(lines)
        meta.lore = existing
        return this
    }

    fun addLore(line: String): ItemStackBuilder {
        val existing = meta.lore ?: mutableListOf()
        existing.add(line)
        meta.lore = existing
        return this
    }

    fun addLoreLines(lines: List<String>): ItemStackBuilder {
        val existing = meta.lore ?: mutableListOf()
        existing.addAll(lines)
        meta.lore = existing
        return this
    }

    fun addItemFlags(vararg flags: ItemFlag): ItemStackBuilder {
        meta.addItemFlags(*flags)
        return this
    }

    fun setCustomModelData(data: Int): ItemStackBuilder {
        meta.setCustomModelData(data)
        return this
    }

    fun setUnbreakable(unbreakable: Boolean): ItemStackBuilder {
        meta.isUnbreakable = unbreakable
        return this
    }

    fun setEnchantmentGlintOverride(glint: Boolean): ItemStackBuilder {
        meta.setEnchantmentGlintOverride(glint)
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = meta
        return itemStack
    }
}

object SkullBuilder {
    fun setSkullTexture(texture: String): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD)
        val meta = skull.itemMeta as? SkullMeta ?: return skull

        val profile: PlayerProfile = try {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            Bukkit.createProfile(UUID.randomUUID(), null)
        } catch (e: Exception) {
            try {
                val spigotProfile = Bukkit.createProfileExact(UUID.randomUUID(), null)
                spigotProfile
            } catch (e2: Exception) {
                Bukkit.createProfile(UUID.randomUUID(), null)
            }
        }

        val decodedData = Base64.getDecoder().decode(texture)
        val textureString = String(decodedData, Charsets.UTF_8)
        profile.setProperty(ProfileProperty("textures", texture))

        meta.playerProfile = profile
        skull.itemMeta = meta
        return skull
    }
}

val ItemStack.savedDisplayName: String
    get() = itemMeta?.displayName ?: type.name.lowercase().replace('_', ' ')
