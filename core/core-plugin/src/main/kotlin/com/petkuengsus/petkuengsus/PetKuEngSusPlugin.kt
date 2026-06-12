package com.petkuengsus.petkuengsus

import com.petkuengsus.petkuengsus.breakthrough.BreakthroughTier
import com.petkuengsus.petkuengsus.commands.CommandBreakthrough
import com.petkuengsus.petkuengsus.commands.CommandPets
import com.petkuengsus.petkuengsus.commands.CommandPlugin
import com.petkuengsus.petkuengsus.commands.CommandPrestige
import com.petkuengsus.petkuengsus.commands.CommandSkillTree
import com.petkuengsus.petkuengsus.commands.CommandTalents
import com.petkuengsus.petkuengsus.combat.CombatEffect
import com.petkuengsus.petkuengsus.combat.CombatListener
import com.petkuengsus.petkuengsus.integration.IntegrationManager
import com.petkuengsus.petkuengsus.internal.LangYml
import com.petkuengsus.petkuengsus.internal.PetCommand
import com.petkuengsus.petkuengsus.internal.PlayerPlaceholder
import com.petkuengsus.petkuengsus.internal.PlayerStaticPlaceholder
import com.petkuengsus.petkuengsus.internal.SimpleConfig
import com.petkuengsus.petkuengsus.internal.SimpleMenu
import com.petkuengsus.petkuengsus.internal.StringUtils
import com.petkuengsus.petkuengsus.internal.registerCommand
import com.petkuengsus.petkuengsus.pets.DiscoverRecipeListener
import com.petkuengsus.petkuengsus.pets.PetDisplay
import com.petkuengsus.petkuengsus.pets.PetLevelListener
import com.petkuengsus.petkuengsus.pets.Pets
import com.petkuengsus.petkuengsus.pets.PetsGUI
import com.petkuengsus.petkuengsus.pets.SpawnEggHandler
import com.petkuengsus.petkuengsus.pets.activePet
import com.petkuengsus.petkuengsus.pets.entity.ModelEnginePetEntity
import com.petkuengsus.petkuengsus.pets.mount.MountManager
import com.petkuengsus.petkuengsus.pets.entity.PetEntity
import com.petkuengsus.petkuengsus.pets.hasPet
import com.petkuengsus.petkuengsus.prestige.PrestigeTier
import com.petkuengsus.petkuengsus.skilltree.SkillTree
import com.petkuengsus.petkuengsus.storage.JsonStorage
import com.petkuengsus.petkuengsus.storage.StorageProvider
import com.petkuengsus.petkuengsus.synergy.SynergyPair
import com.petkuengsus.petkuengsus.talents.Talent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

internal lateinit var plugin: PetKuEngSusPlugin
    private set

class PetKuEngSusPlugin : JavaPlugin() {
    private val petDisplay = PetDisplay

    lateinit var configYml: SimpleConfig
        private set
    lateinit var langYml: LangYml
        private set

    var breakthroughConfig: List<BreakthroughTier>? = null
        private set
    var prestigeConfig: List<PrestigeTier>? = null
        private set
    var talentsConfig: List<Talent>? = null
        private set
    var skillTreeConfig: List<SkillTree>? = null
        private set
    var combatConfig: List<CombatEffect>? = null
        private set
    var synergyConfig: List<SynergyPair>? = null
        private set
    var storageProvider: StorageProvider? = null
        private set

    val placeholderRegistry = mutableListOf<PlayerPlaceholder>()

    init {
        plugin = this
    }

    override fun onEnable() {
        saveDefaultConfig()
        configYml = SimpleConfig(File(dataFolder, "config.yml"))

        val langFile = File(dataFolder, "lang.yml")
        if (!langFile.exists()) {
            saveResource("lang.yml", false)
        }
        LangYml.load(this)
        langYml = LangYml

        SimpleMenu.registerListener(this)

        registerPluginCommands()
        registerListeners()
        registerPlaceholders()

        Pets.reload()

        loadPhase2Configs()

        registerPhase2Commands()
        registerPhase2Listeners()

        IntegrationManager(this).initIntegrations()

        PetEntity.registerPetEntity("modelengine") { pet, arg ->
            ModelEnginePetEntity(pet, arg)
        }

        handleReload()
    }

    private fun registerPluginCommands() {
        val ecoPetsCmd = CommandPlugin
        registerCommand(this, ecoPetsCmd, "petkuengsus")

        val petsCmd = CommandPets
        registerCommand(this, petsCmd, "pets")
    }

    private fun registerPhase2Commands() {
        registerCommand(this, CommandBreakthrough, "breakthrough")
        registerCommand(this, CommandPrestige, "prestige")
        registerCommand(this, CommandSkillTree, "skilltree")
        registerCommand(this, CommandTalents, "talents")
    }

    private fun registerListeners() {
        listOf(
            PetLevelListener,
            SpawnEggHandler,
            petDisplay,
            DiscoverRecipeListener,
            MountManager
        ).forEach { server.pluginManager.registerEvents(it, this) }
    }

    private fun registerPhase2Listeners() {
        server.pluginManager.registerEvents(CombatListener(), this)
    }

    private fun loadPhase2Configs() {
        val btFile = File(dataFolder, "breakthroughs.yml")
        if (!btFile.exists()) saveResource("breakthroughs.yml", false)
        val btCfg = SimpleConfig(btFile)
        breakthroughConfig = btCfg.keys().mapNotNull { key ->
            val sec = btCfg.getSubsection(key) ?: return@mapNotNull null
            val tn = key.toIntOrNull() ?: return@mapNotNull null
            BreakthroughTier.fromConfig(sec, tn)
        }.sortedBy { it.tier }

        val prFile = File(dataFolder, "prestige.yml")
        if (!prFile.exists()) saveResource("prestige.yml", false)
        val prCfg = SimpleConfig(prFile)
        prestigeConfig = prCfg.keys().mapNotNull { key ->
            val sec = prCfg.getSubsection(key) ?: return@mapNotNull null
            val tn = key.toIntOrNull() ?: return@mapNotNull null
            PrestigeTier.fromConfig(sec, tn)
        }.sortedBy { it.tier }

        val taFile = File(dataFolder, "talents.yml")
        if (!taFile.exists()) saveResource("talents.yml", false)
        val taCfg = SimpleConfig(taFile)
        talentsConfig = taCfg.keys().mapNotNull { key ->
            val sec = taCfg.getSubsection(key) ?: return@mapNotNull null
            Talent.fromConfig(key, sec)
        }

        val stFile = File(dataFolder, "skilltrees.yml")
        if (!stFile.exists()) saveResource("skilltrees.yml", false)
        val stCfg = SimpleConfig(stFile)
        skillTreeConfig = stCfg.keys().mapNotNull { key ->
            val sec = stCfg.getSubsection(key) ?: return@mapNotNull null
            SkillTree.fromConfig(key, sec)
        }

        val coFile = File(dataFolder, "combat.yml")
        if (!coFile.exists()) saveResource("combat.yml", false)
        val coCfg = SimpleConfig(coFile)
        combatConfig = coCfg.keys().mapNotNull { key ->
            val sec = coCfg.getSubsection(key) ?: return@mapNotNull null
            CombatEffect.fromConfig(key, sec)
        }

        val syFile = File(dataFolder, "synergies.yml")
        if (!syFile.exists()) saveResource("synergies.yml", false)
        val syCfg = SimpleConfig(syFile)
        synergyConfig = syCfg.keys().mapNotNull { key ->
            val sec = syCfg.getSubsection(key) ?: return@mapNotNull null
            SynergyPair.fromConfig(key, sec)
        }

        storageProvider = JsonStorage(File(dataFolder, "data"))
    }

    private fun registerPlaceholders() {
        placeholderRegistry.add(
            PlayerStaticPlaceholder("pet") { it.activePet?.name ?: "" }
        )
        placeholderRegistry.add(
            PlayerStaticPlaceholder("pet_id") { it.activePet?.id ?: "" }
        )
        placeholderRegistry.add(
            PlayerStaticPlaceholder("total_pets") {
                var count = 0
                for (pet in Pets.values()) {
                    if (it.hasPet(pet)) count++
                }
                count.toString()
            }
        )
    }

    fun handleReload() {
        configYml = SimpleConfig(File(dataFolder, "config.yml"))
        PetsGUI.update()

        server.scheduler.runTaskTimer(this, Runnable {
            if (configYml.getBool("auto-deactivate-on-condition-fail")) {
                for (player in Bukkit.getOnlinePlayers()) {
                    val activePet = player.activePet ?: continue
                    if (!activePet.canActivate(player)) {
                        player.activePet = null
                        player.sendMessage(
                            langYml.getMessage("pet-auto-deactivated")
                                .replace("%pet%", activePet.name)
                        )
                    }
                }
            }
        }, 20, 20)

        if (!configYml.getBool("pet-entity.enabled")) {
            return
        }

        server.scheduler.runTaskTimer(this, Runnable {
            petDisplay.tickAll()
        }, 1, 1)
    }

    override fun onDisable() {
        petDisplay.shutdown()
        storageProvider?.shutdown()
    }
}
