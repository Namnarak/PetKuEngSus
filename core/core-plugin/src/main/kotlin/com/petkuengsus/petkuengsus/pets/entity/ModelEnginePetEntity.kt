package com.petkuengsus.petkuengsus.pets.entity

import com.petkuengsus.petkuengsus.pets.Pet
import com.petkuengsus.petkuengsus.plugin
import org.bukkit.Location
import org.bukkit.entity.Entity

class ModelEnginePetEntity(
    pet: Pet,
    private val modelID: String
) : PetEntity(pet) {
    override fun spawn(location: Location): Entity {
        try {
            val apiClass = Class.forName("com.ticxo.modelengine.api.ModelEngineAPI")
            val getApi = apiClass.getMethod("getAPI")
            val api = getApi.invoke(null)
            val modelManager = api::class.java.getMethod("getModelManager").invoke(api)

            try {
                val createModel = modelManager::class.java.getMethod(
                    "createActiveModel", String::class.java, Location::class.java, Float::class.javaPrimitiveType
                )
                val activeModel = createModel.invoke(modelManager, modelID, location, 0f)
                val entity = activeModel::class.java.getMethod("getEntity").invoke(activeModel) as? Entity
                if (entity != null) return entity
            } catch (_: NoSuchMethodException) {
                val createEntity = modelManager::class.java.getMethod(
                    "createModelEntity", Location::class.java, String::class.java
                )
                val modelEntity = createEntity.invoke(modelManager, location, modelID)
                val entity = modelEntity::class.java.getMethod("getEntity").invoke(modelEntity) as? Entity
                if (entity != null) return entity
            }
        } catch (e: Exception) {
            plugin.logger.warning("ModelEngine not available, falling back to armor stand for '$modelID': ${e.message}")
        }
        return emptyArmorStandAt(location, pet, isSkull = false)
    }
}
