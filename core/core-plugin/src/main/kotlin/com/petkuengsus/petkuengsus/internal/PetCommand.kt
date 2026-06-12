package com.petkuengsus.petkuengsus.internal

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

abstract class PetCommand(
    name: String,
    permission: String = ""
) : CommandExecutor, TabCompleter {

    private val subcommands = mutableMapOf<String, PetSubcommand>()

    fun registerSubcommand(subcommand: PetSubcommand) {
        subcommands[subcommand.name] = subcommand
        for (alias in subcommand.aliases) {
            subcommands[alias] = subcommand
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val sub = subcommands[args[0].lowercase()]
            if (sub != null) {
                if (sub.permission.isNotEmpty() && !sender.hasPermission(sub.permission)) {
                    sender.sendMessage("§cYou don't have permission!")
                    return true
                }
                return sub.onCommand(sender, args.drop(1).toTypedArray())
            }
        }
        return handle(sender, args)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return subcommands.keys.filter { it.startsWith(args[0].lowercase()) }
        }
        if (args.size > 1) {
            val sub = subcommands[args[0].lowercase()]
            if (sub != null) {
                return sub.onTabComplete(sender, args.drop(1).toTypedArray())
            }
        }
        return emptyList()
    }

    abstract fun handle(sender: CommandSender, args: Array<out String>): Boolean
}

abstract class PetSubcommand(
    val name: String,
    val permission: String = "",
    val aliases: List<String> = emptyList()
) {
    abstract fun onCommand(sender: CommandSender, args: Array<out String>): Boolean
    open fun onTabComplete(sender: CommandSender, args: Array<out String>): List<String> = emptyList()
}

fun registerCommand(plugin: JavaPlugin, command: PetCommand, label: String) {
    plugin.getCommand(label)?.let {
        it.setExecutor(command)
        it.tabCompleter = command
    }
}

fun CommandSender.getPlayer(): Player? {
    return if (this is Player) this else null
}
