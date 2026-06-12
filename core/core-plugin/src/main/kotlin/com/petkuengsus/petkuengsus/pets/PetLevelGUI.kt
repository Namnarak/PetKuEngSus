package com.petkuengsus.petkuengsus.pets

import com.petkuengsus.petkuengsus.internal.ItemStackBuilder
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class PetLevelGUI(private val pet: Pet) {
    private val progressionSlots: List<Int> by lazy { parseProgressionPattern() }
    private val slotsPerPage: Int get() = progressionSlots.size

    fun open(player: Player, page: Int = 0) {
        val rows = plugin.configYml.getInt("level-gui.rows")
        val title = StringUtils.format(
            plugin.langYml.getMessage("level-gui.title")
                .replace("%pet%", pet.name)
        )
        val menu = SimpleMenu(title, rows)

        val maskMaterials = plugin.configYml.getStringList("level-gui.mask.materials")
        if (maskMaterials.isNotEmpty()) {
            val fillerItem = buildSimpleItem(maskMaterials.first())
            if (fillerItem.type != Material.AIR) {
                menu.fillEmpty(fillerItem)
            }
        }

        val maxLevel = pet.maxLevel
        val playerLevel = player.getPetLevel(pet)
        val totalPages = ceil(maxLevel.toDouble() / slotsPerPage).toInt()
        val safePage = page.coerceIn(0, totalPages - 1)

        val startLevel = safePage * slotsPerPage + 1
        val endLevel = minOf(startLevel + slotsPerPage - 1, maxLevel)

        for (absoluteLevel in startLevel..endLevel) {
            val localIndex = absoluteLevel - startLevel
            val slot = progressionSlots.getOrNull(localIndex) ?: continue
            val item = buildProgressionItem(absoluteLevel, playerLevel, maxLevel, player)
            menu.setItem(slot, item)
        }

        buildBackButton(menu, player)
        buildCloseButton(menu)
        buildLevelPageSwitcher(menu, safePage, totalPages, player)

        SimpleMenu.open(player, menu)
    }

    private fun buildProgressionItem(levelNum: Int, playerLevel: Int, maxLevel: Int, player: Player): ItemStack {
        return when {
            levelNum <= playerLevel -> buildUnlockedItem(levelNum, player)
            levelNum == playerLevel + 1 -> buildInProgressItem(levelNum, player)
            playerLevel >= maxLevel -> buildMaxLevelItem(levelNum, player)
            else -> buildLockedItem(levelNum, player)
        }
    }

    private fun buildUnlockedItem(levelNum: Int, player: Player): ItemStack {
        val raw = plugin.configYml.getString("level-gui.progression-slots.unlocked.item") ?: "lime_stained_glass_pane"
        val rawName = plugin.configYml.getString("level-gui.progression-slots.unlocked.name") ?: "&aLevel %level% &7(&f&lUNLOCKED&7)"
        val rawLore = plugin.configYml.getStringList("level-gui.progression-slots.unlocked.lore")
        val name = StringUtils.format(pet.injectPlaceholdersInto(
            listOf(rawName), player, levelNum
        ).first())
        val lore = pet.injectPlaceholdersInto(rawLore.map { StringUtils.format(it) }, player, levelNum)
        val amount = getLevelAsAmount(levelNum)
        return buildNamedItem(raw, name, lore, amount)
    }

    private fun buildInProgressItem(levelNum: Int, player: Player): ItemStack {
        val raw = plugin.configYml.getString("level-gui.progression-slots.in-progress.item") ?: "yellow_stained_glass_pane"
        val rawName = plugin.configYml.getString("level-gui.progression-slots.in-progress.name") ?: "&eLevel %level% &7(&f&lIN PROGRESS&7)"
        val rawLore = plugin.configYml.getStringList("level-gui.progression-slots.in-progress.lore")
        val name = StringUtils.format(pet.injectPlaceholdersInto(
            listOf(rawName), player, levelNum
        ).first())
        val lore = pet.injectPlaceholdersInto(rawLore.map { StringUtils.format(it) }, player, levelNum)
        val amount = getLevelAsAmount(levelNum)
        return buildNamedItem(raw, name, lore, amount)
    }

    private fun buildMaxLevelItem(levelNum: Int, player: Player): ItemStack {
        val raw = plugin.configYml.getString("level-gui.progression-slots.in-progress.item") ?: "yellow_stained_glass_pane"
        val rawName = plugin.configYml.getString("level-gui.progression-slots.in-progress.name") ?: "&eLevel %level%"
        val maxLore = plugin.configYml.getStringList("level-gui.progression-slots.in-progress.max-level-lore")
        val name = StringUtils.format(pet.injectPlaceholdersInto(
            listOf(rawName), player, levelNum
        ).first())
        val lore = pet.injectPlaceholdersInto(maxLore.map { StringUtils.format(it) }, player, levelNum)
        val amount = getLevelAsAmount(levelNum)
        return buildNamedItem(raw, name, lore, amount)
    }

    private fun buildLockedItem(levelNum: Int, player: Player): ItemStack {
        val raw = plugin.configYml.getString("level-gui.progression-slots.locked.item") ?: "red_stained_glass_pane"
        val rawName = plugin.configYml.getString("level-gui.progression-slots.locked.name") ?: "&cLevel %level% &7(&8&lLOCKED&7)"
        val rawLore = plugin.configYml.getStringList("level-gui.progression-slots.locked.lore")
        val name = StringUtils.format(pet.injectPlaceholdersInto(
            listOf(rawName), player, levelNum
        ).first())
        val lore = pet.injectPlaceholdersInto(rawLore.map { StringUtils.format(it) }, player, levelNum)
        val amount = getLevelAsAmount(levelNum)
        return buildNamedItem(raw, name, lore, amount)
    }

    private fun buildNamedItem(raw: String, name: String, lore: List<String>, amount: Int = 1): ItemStack {
        val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { Material.GRAY_STAINED_GLASS_PANE }
        return ItemStackBuilder(mat).setDisplayName(name).setLore(lore).setAmount(amount).build()
    }

    private fun buildSimpleItem(raw: String): ItemStack {
        val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { return ItemStack(Material.AIR) }
        return ItemStackBuilder(mat).setDisplayName(" ").build()
    }

    private fun getLevelAsAmount(levelNum: Int): Int {
        val levelAsAmount = if (plugin.configYml.contains("level-gui.progression-slots.level-as-amount")) {
            plugin.configYml.getBool("level-gui.progression-slots.level-as-amount")
        } else true
        return if (levelAsAmount) levelNum.coerceIn(1, 64) else 1
    }

    private fun buildBackButton(menu: SimpleMenu, player: Player) {
        if (!plugin.configYml.contains("level-gui.back")) return
        val enabled = if (plugin.configYml.contains("level-gui.back.enabled")) {
            plugin.configYml.getBool("level-gui.back.enabled")
        } else true
        if (!enabled) return
        val row = plugin.configYml.getInt("level-gui.back.location.row")
        val col = plugin.configYml.getInt("level-gui.back.location.column")
        val slot = (row - 1) * 9 + (col - 1)
        val rawName = plugin.configYml.getString("level-gui.back.name") ?: "&7Back"
        val name = StringUtils.format(rawName)
        val raw = plugin.configYml.getString("level-gui.back.item") ?: "arrow"
        val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { Material.ARROW }
        val item = ItemStackBuilder(mat).setDisplayName(name).build()
        menu.setItem(slot, item) { p, _ ->
            Bukkit.getScheduler().runTask(plugin, Runnable { PetsGUI.open(p) })
        }
    }

    private fun buildCloseButton(menu: SimpleMenu) {
        val enabled = if (plugin.configYml.contains("level-gui.close.enabled")) {
            plugin.configYml.getBool("level-gui.close.enabled")
        } else true
        if (!enabled) return
        val row = plugin.configYml.getInt("level-gui.close.location.row")
        val col = plugin.configYml.getInt("level-gui.close.location.column")
        val slot = (row - 1) * 9 + (col - 1)
        val rawName = plugin.configYml.getString("level-gui.close.name") ?: "&cClose"
        val name = StringUtils.format(rawName)
        val raw = plugin.configYml.getString("level-gui.close.material") ?: "barrier"
        val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { Material.BARRIER }
        val item = ItemStackBuilder(mat).setDisplayName(name).build()
        menu.setItem(slot, item) { p, _ -> p.closeInventory() }
    }

    private fun buildLevelPageSwitcher(menu: SimpleMenu, currentPage: Int, totalPages: Int, player: Player) {
        if (currentPage > 0) {
            val row = plugin.configYml.getInt("level-gui.progression-slots.prev-page.location.row", 6)
            val col = plugin.configYml.getInt("level-gui.progression-slots.prev-page.location.column", 3)
            val slot = (row - 1) * 9 + (col - 1)
            val rawName = plugin.configYml.getString("level-gui.progression-slots.prev-page.name") ?: "&7← &fPage %page%"
            val name = StringUtils.format(rawName.replace("%page%", (currentPage).toString()))
            val raw = plugin.configYml.getString("level-gui.progression-slots.prev-page.material") ?: "arrow"
            val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { Material.ARROW }
            val item = ItemStackBuilder(mat).setDisplayName(name).build()
            menu.setItem(slot, item) { p, _ ->
                Bukkit.getScheduler().runTask(plugin, Runnable { open(p, currentPage - 1) })
            }
        }
        if (currentPage < totalPages - 1) {
            val row = plugin.configYml.getInt("level-gui.progression-slots.next-page.location.row", 6)
            val col = plugin.configYml.getInt("level-gui.progression-slots.next-page.location.column", 7)
            val slot = (row - 1) * 9 + (col - 1)
            val rawName = plugin.configYml.getString("level-gui.progression-slots.next-page.name") ?: "&fPage %page% &7→"
            val name = StringUtils.format(rawName.replace("%page%", (currentPage + 2).toString()))
            val raw = plugin.configYml.getString("level-gui.progression-slots.next-page.material") ?: "arrow"
            val mat = try { Material.valueOf(raw.uppercase()) } catch (_: Exception) { Material.ARROW }
            val item = ItemStackBuilder(mat).setDisplayName(name).build()
            menu.setItem(slot, item) { p, _ ->
                Bukkit.getScheduler().runTask(plugin, Runnable { open(p, currentPage + 1) })
            }
        }
    }

    private fun parseProgressionPattern(): List<Int> {
        val pattern = plugin.configYml.getStringList("level-gui.progression-slots.pattern")
        val slotsByTrack = mutableMapOf<Int, Int>()
        for ((row, line) in pattern.withIndex()) {
            for ((col, c) in line.withIndex()) {
                if (c == '0') continue
                val order = charToTrackOrder(c)
                slotsByTrack[order] = row * 9 + col
            }
        }
        return slotsByTrack.entries.sortedBy { it.key }.map { it.value }
    }

    private fun charToTrackOrder(c: Char): Int = when {
        c in '1'..'9' -> c - '0'
        c in 'a'..'z' -> c - 'a' + 10
        else -> 0
    }
}
