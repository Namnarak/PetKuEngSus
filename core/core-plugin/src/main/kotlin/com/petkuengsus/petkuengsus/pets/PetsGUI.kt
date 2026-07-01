package com.petkuengsus.petkuengsus.pets

import com.petkuengsus.petkuengsus.internal.ItemStackBuilder
import com.petkuengsus.petkuengsus.internal.PlayableSound
import com.petkuengsus.petkuengsus.internal.SimpleItems
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.internal.SkullBuilder
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.pets.mount.MountManager
import com.petkuengsus.petkuengsus.pets.transform.TransformationManager
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

object PetsGUI {
    private val petAreaSlots = mutableListOf<Int>()

    internal fun update() {
        petAreaSlots.clear()
        val topLeftRow = plugin.configYml.getInt("gui.pet-area.top-left.row")
        val topLeftColumn = plugin.configYml.getInt("gui.pet-area.top-left.column")
        val bottomRightRow = plugin.configYml.getInt("gui.pet-area.bottom-right.row")
        val bottomRightColumn = plugin.configYml.getInt("gui.pet-area.bottom-right.column")
        for (row in topLeftRow..bottomRightRow) {
            for (column in topLeftColumn..bottomRightColumn) {
                petAreaSlots.add((row - 1) * 9 + (column - 1))
            }
        }
    }

    fun open(player: Player, page: Int = 0) {
        val rows = plugin.configYml.getInt("gui.rows")
        val menu = SimpleMenu(
            StringUtils.format(plugin.langYml.getMessage("menu.title")),
            rows
        )

        val maskMaterials = plugin.configYml.getStringList("gui.mask.materials")
        if (maskMaterials.isNotEmpty()) {
            val fillerItem = buildSimpleItem(maskMaterials.first())
            if (fillerItem.type != Material.AIR) {
                menu.fillEmpty(fillerItem)
            }
        }

        val unlockedPets = Pets.values()
            .sortedByDescending { player.getPetLevel(it) }
            .filter { player.getPetLevel(it) > 0 }

        val slotsPerPage = petAreaSlots.size
        val maxPage = if (unlockedPets.isEmpty()) 0 else ceil(unlockedPets.size.toDouble() / slotsPerPage).toInt() - 1
        val safePage = page.coerceIn(0, maxPage.coerceAtLeast(0))
        val startIndex = safePage * slotsPerPage
        val pagePets = unlockedPets.drop(startIndex).take(slotsPerPage)

        for ((index, slotIndex) in petAreaSlots.withIndex()) {
            val pet = pagePets.getOrNull(index) ?: continue
            val icon = pet.getIcon(player)
            menu.setItem(slotIndex, icon) { p, _ ->
                if (p.activePet != pet) {
                    if (!pet.canActivate(p)) {
                        p.sendMessage(
                            StringUtils.format(
                                plugin.langYml.getMessage("cannot-activate-pet")
                                    .replace("%pet%", pet.name)
                            )
                        )
                        return@setItem
                    }
                    p.activePet = pet
                }
                PlayableSound.create(plugin.configYml.getSubsection("gui.pet-icon.click"))?.playTo(p)
            }
        }

        buildPageSwitcher(menu, safePage, maxPage)
        buildPetInfoSlot(menu, player)
        buildCloseButton(menu)
        buildDeactivateButton(menu)
        buildMountButton(menu, player)
        buildModelButton(menu, player)
        buildTransformButton(menu, player)
        buildToggleButton(menu, player)

        SimpleMenu.open(player, menu)
    }

    private fun buildPageSwitcher(menu: SimpleMenu, currentPage: Int, maxPage: Int) {
        if (currentPage > 0) {
            val row = plugin.configYml.getInt("gui.prev-page.location.row")
            val col = plugin.configYml.getInt("gui.prev-page.location.column")
            val slot = (row - 1) * 9 + (col - 1)
            val itemName = StringUtils.format(plugin.configYml.getString("gui.prev-page.name") ?: "&fPrevious Page")
            val item = simpleConfigItem("gui.prev-page.item", itemName)
            menu.setItem(slot, item) { p, _ ->
                Bukkit.getScheduler().runTask(plugin, Runnable { open(p, currentPage - 1) })
            }
        }

        if (currentPage < maxPage) {
            val row = plugin.configYml.getInt("gui.next-page.location.row")
            val col = plugin.configYml.getInt("gui.next-page.location.column")
            val slot = (row - 1) * 9 + (col - 1)
            val itemName = StringUtils.format(plugin.configYml.getString("gui.next-page.name") ?: "&fNext Page")
            val item = simpleConfigItem("gui.next-page.item", itemName)
            menu.setItem(slot, item) { p, _ ->
                Bukkit.getScheduler().runTask(plugin, Runnable { open(p, currentPage + 1) })
            }
        }
    }

    private fun buildPetInfoSlot(menu: SimpleMenu, player: Player) {
        val row = plugin.configYml.getInt("gui.pet-info.row")
        val col = plugin.configYml.getInt("gui.pet-info.column")
        val slot = (row - 1) * 9 + (col - 1)

        val active = player.activePet
        if (active != null) {
            menu.setItem(slot, active.getPetInfoIcon(player)) { p, _ ->
                PetLevelGUI(active).open(p)
            }
        } else {
            menu.setItem(slot, buildNoActivePetItem()) { _, _ -> }
        }
    }

    private fun buildNoActivePetItem(): ItemStack {
        val raw = plugin.configYml.getString("gui.pet-info.no-active.item") ?: ""
        val name = StringUtils.format(plugin.configYml.getString("gui.pet-info.no-active.name") ?: "&cNo Active Pet")
        val lore = plugin.configYml.getStringList("gui.pet-info.no-active.lore").map { StringUtils.format(it) }
        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.PLAYER_HEAD)
        return ItemStackBuilder(itemStack).setDisplayName(name).setLore(lore).build()
    }

    private fun buildCloseButton(menu: SimpleMenu) {
        val enabled = if (plugin.configYml.contains("gui.close.enabled")) {
            plugin.configYml.getBool("gui.close.enabled")
        } else true
        if (!enabled) return
        val row = plugin.configYml.getInt("gui.close.location.row")
        val col = plugin.configYml.getInt("gui.close.location.column")
        val slot = (row - 1) * 9 + (col - 1)
        val itemName = StringUtils.format(plugin.configYml.getString("gui.close.name") ?: "&cClose")
        val item = simpleConfigItem("gui.close.item", itemName)
        menu.setItem(slot, item) { p, _ -> p.closeInventory() }
    }

    private fun buildDeactivateButton(menu: SimpleMenu) {
        val raw = plugin.configYml.getString("gui.deactivate-pet.item") ?: return
        val row = plugin.configYml.getInt("gui.deactivate-pet.location.row")
        val col = plugin.configYml.getInt("gui.deactivate-pet.location.column")
        val slot = (row - 1) * 9 + (col - 1)
        val itemName = StringUtils.format(plugin.configYml.getString("gui.deactivate-pet.name") ?: "&cDeactivate Pet")

        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.BARRIER)
        val item = ItemStackBuilder(itemStack).setDisplayName(itemName).build()

        menu.setItem(slot, item) { p, _ ->
            p.activePet = null
        }
    }

    private fun buildToggleButton(menu: SimpleMenu, player: Player) {
        val toggleRow = plugin.configYml.getInt("gui.toggle.location.row")
        val toggleCol = plugin.configYml.getInt("gui.toggle.location.column")
        val toggleSlot = (toggleRow - 1) * 9 + (toggleCol - 1)
        val isPetVisible = !player.shouldHidePet

        val (itemKey, nameKey, loreKey) = if (isPetVisible) {
            Triple("gui.toggle.hide-pet.item", "gui.toggle.hide-pet.name", "gui.toggle.hide-pet.lore")
        } else {
            Triple("gui.toggle.show-pet.item", "gui.toggle.show-pet.name", "gui.toggle.show-pet.lore")
        }

        val raw = plugin.configYml.getString(itemKey) ?: return
        val itemName = StringUtils.format(plugin.configYml.getString(nameKey) ?: "")
        val lore = plugin.configYml.getStringList(loreKey).map { StringUtils.format(it) }

        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.PLAYER_HEAD)
        val item = ItemStackBuilder(itemStack).setDisplayName(itemName).setLore(lore).build()

        menu.setItem(toggleSlot, item) { p, _ ->
            p.shouldHidePet = !p.shouldHidePet
            Bukkit.getScheduler().runTask(plugin, Runnable { open(p, 0) })
        }
    }

    private fun buildMountButton(menu: SimpleMenu, player: Player) {
        val mountCfg = plugin.configYml.getSubsection("gui.mount") ?: return
        val locCfg = mountCfg.getSubsection("location") ?: return
        val row = locCfg.getInt("row")
        val col = locCfg.getInt("column")
        val slot = (row - 1) * 9 + (col - 1)

        val isMounted = MountManager.isMounted(player)
        val section = if (isMounted) mountCfg.getSubsection("mounted") else mountCfg

        val raw = section?.getString("item") ?: return
        val itemName = StringUtils.format(section?.getString("name") ?: "")
        val lore = section?.getStringList("lore")?.map { StringUtils.format(it) } ?: emptyList()

        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.SADDLE)
        val item = ItemStackBuilder(itemStack).setDisplayName(itemName).setLore(lore).build()

        menu.setItem(slot, item) { p, _ ->
            if (MountManager.isMounted(p)) {
                MountManager.dismount(p)
            } else {
                MountManager.mount(p)
            }
            Bukkit.getScheduler().runTask(plugin, Runnable { open(p, 0) })
        }
    }

    private fun buildModelButton(menu: SimpleMenu, player: Player) {
        val cfg = plugin.configYml.getSubsection("gui.model") ?: return
        val locCfg = cfg.getSubsection("location") ?: return
        val row = locCfg.getInt("row")
        val col = locCfg.getInt("column")
        val slot = (row - 1) * 9 + (col - 1)

        val currentType = TransformationManager.getModelType(player)
        val typeName = plugin.langYml.getMessage("transform.types.$currentType") ?: currentType
        val raw = cfg.getString("item") ?: return
        val itemName = StringUtils.format((cfg.getString("name") ?: "").replace("%model_type%", typeName))
        val lore = cfg.getStringList("lore").map { StringUtils.format(it.replace("%model_type%", typeName)) }

        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.ITEM_FRAME)
        val item = ItemStackBuilder(itemStack).setDisplayName(itemName).setLore(lore).build()

        menu.setItem(slot, item) { p, _ ->
            val next = TransformationManager.cycleModelType(p)
            val nextName = plugin.langYml.getMessage("transform.types.$next") ?: next
            p.sendMessage(StringUtils.format(
                plugin.langYml.getMessage("transform.model-changed")?.replace("%type%", nextName) ?: "Model: $next"
            ))
            Bukkit.getScheduler().runTask(plugin, Runnable { open(p, 0) })
        }
    }

    private fun buildTransformButton(menu: SimpleMenu, player: Player) {
        val cfg = plugin.configYml.getSubsection("gui.transform") ?: return
        val locCfg = cfg.getSubsection("location") ?: return
        val row = locCfg.getInt("row")
        val col = locCfg.getInt("column")
        val slot = (row - 1) * 9 + (col - 1)

        val isActive = TransformationManager.hasTransformationMode(player)
        val status = if (isActive) "&aON" else "&cOFF"
        val raw = cfg.getString("item") ?: return
        val itemName = StringUtils.format(
            (cfg.getString("name") ?: "").replace("%transform_status%", status)
        )
        val lore = cfg.getStringList("lore").map {
            StringUtils.format(it.replace("%transform_status%", status))
        }

        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.ENDER_EYE)
        val item = ItemStackBuilder(itemStack).setDisplayName(itemName).setLore(lore).build()

        menu.setItem(slot, item) { p, _ ->
            TransformationManager.toggleTransformationMode(p)
            Bukkit.getScheduler().runTask(plugin, Runnable { open(p, 0) })
        }
    }

    private fun simpleConfigItem(path: String, displayName: String): ItemStack {
        val raw = plugin.configYml.getString(path) ?: return ItemStack(Material.AIR)
        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.AIR)
        if (itemStack.type == Material.AIR) return itemStack
        return ItemStackBuilder(itemStack).setDisplayName(displayName).build()
    }

    private fun buildSimpleItem(raw: String): ItemStack {
        val itemStack = SimpleItems.lookup(raw) ?: ItemStack(Material.AIR)
        if (itemStack.type == Material.AIR) return itemStack
        return ItemStackBuilder(itemStack).setDisplayName(" ").build()
    }
}
