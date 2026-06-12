package com.petkuengsus.petkuengsus.internal

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class SimpleMenu(
    private val title: String,
    private val rows: Int
) {
    internal val inventory: Inventory = Bukkit.createInventory(null, rows * 9, title)
    private val clickActions = mutableMapOf<Int, (Player, InventoryClickEvent) -> Unit>()
    private var closeAction: ((Player, InventoryCloseEvent) -> Unit)? = null
    private var filler: ItemStack? = null
    private val openPlayers = mutableSetOf<Player>()

    fun setItem(slot: Int, item: ItemStack, action: ((Player, InventoryClickEvent) -> Unit)? = null) {
        inventory.setItem(slot, item)
        if (action != null) {
            clickActions[slot] = action
        }
    }

    fun fillEmpty(item: ItemStack) {
        filler = item
    }

    fun onClose(action: (Player, InventoryCloseEvent) -> Unit) {
        closeAction = action
    }

    fun open(player: Player) {
        // Apply filler to empty slots
        if (filler != null) {
            for (i in 0 until inventory.size) {
                val existing = inventory.getItem(i)
                if (existing == null || existing.type.isAir) {
                    inventory.setItem(i, filler)
                }
            }
        }
        openPlayers.add(player)
        player.openInventory(inventory)
    }

    fun isOpen(player: Player): Boolean = player in openPlayers

    fun handleClick(player: Player, slot: Int, event: InventoryClickEvent) {
        val action = clickActions[slot] ?: return
        action(player, event)
    }

    fun handleClose(player: Player, event: InventoryCloseEvent) {
        openPlayers.remove(player)
        closeAction?.invoke(player, event)
    }

    companion object {
        private val openMenus = mutableMapOf<Player, SimpleMenu>()

        fun registerListener(plugin: JavaPlugin) {
            plugin.server.pluginManager.registerEvents(object : Listener {
                @EventHandler
                fun onClick(event: InventoryClickEvent) {
                    val player = event.whoClicked as? Player ?: return
                    val menu = openMenus[player] ?: return
                    event.isCancelled = true
                    menu.handleClick(player, event.rawSlot, event)
                }

                @EventHandler
                fun onClose(event: InventoryCloseEvent) {
                    val player = event.player as? Player ?: return
                    val menu = openMenus[player] ?: return
                    if (menu.inventory !== event.inventory) return
                    menu.handleClose(player, event)
                    openMenus.remove(player)
                }
            }, plugin)
        }

        fun open(player: Player, menu: SimpleMenu) {
            openMenus[player] = menu
            menu.open(player)
        }
    }
}

class SimplePaginatedMenu(
    private val baseTitle: String,
    private val rows: Int,
    private val items: List<Pair<ItemStack, (Player, InventoryClickEvent) -> Unit>>,
    private val itemsPerPage: Int = (rows - 1) * 9
) {
    fun open(player: Player, page: Int = 0) {
        val menu = SimpleMenu("$baseTitle - Page ${page + 1}", rows)
        val startIndex = page * itemsPerPage
        val pageItems = items.drop(startIndex).take(itemsPerPage)

        for ((index, pair) in pageItems.withIndex()) {
            menu.setItem(index, pair.first, pair.second)
        }

        // Navigation
        if (page > 0) {
            val prevItem = ItemStackBuilder(org.bukkit.Material.ARROW)
                .setDisplayName("§aPrevious Page")
                .build()
            menu.setItem((rows - 1) * 9, prevItem) { p, _ -> open(p, page - 1) }
        }

        if (items.size > (page + 1) * itemsPerPage) {
            val nextItem = ItemStackBuilder(org.bukkit.Material.ARROW)
                .setDisplayName("§aNext Page")
                .build()
            menu.setItem((rows - 1) * 9 + 8, nextItem) { p, _ -> open(p, page + 1) }
        }

        SimpleMenu.open(player, menu)
    }
}
