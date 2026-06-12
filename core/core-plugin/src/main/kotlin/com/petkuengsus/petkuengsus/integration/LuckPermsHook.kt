package com.petkuengsus.petkuengsus.integration

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

object LuckPermsHook {
    private var luckPerms: LuckPerms? = null
    var enabled: Boolean = false
        private set

    fun init() {
        luckPerms = try {
            LuckPermsProvider.get()
        } catch (e: Exception) {
            null
        }
        enabled = luckPerms != null
    }

    fun getUser(player: Player): User? {
        val lp = luckPerms ?: return null
        return lp.userManager.getUser(player.uniqueId)
    }

    fun getCachedUser(player: Player): User? {
        val lp = luckPerms ?: return null
        return lp.userManager.getUser(player.uniqueId)
    }

    fun addPermission(player: Player, permission: String): CompletableFuture<Void>? {
        val lp = luckPerms ?: return null
        val user = lp.userManager.getUser(player.uniqueId) ?: return null
        val data = user.data()
        val node = Node.builder(permission).build()
        data.add(node)
        return lp.userManager.saveUser(user)
    }

    fun removePermission(player: Player, permission: String): CompletableFuture<Void>? {
        val lp = luckPerms ?: return null
        val user = lp.userManager.getUser(player.uniqueId) ?: return null
        val data = user.data()
        val node = Node.builder(permission).build()
        data.remove(node)
        return lp.userManager.saveUser(user)
    }

    fun getGroups(player: Player): List<String> {
        val user = getUser(player) ?: return emptyList()
        return user.getNodes(NodeType.INHERITANCE).map { it.groupName }
    }

    fun getPrimaryGroup(player: Player): String {
        val user = getUser(player) ?: return ""
        return user.primaryGroup
    }
}
