package com.kaizzinho.wildbosses.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen

class WildBossesModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory { parent ->
        if (!FabricLoader.getInstance().isModLoaded(YACL_ID)) {
            MissingYaclScreen(parent)
        } else {
            createYaclScreen(parent)
        }
    }

    private fun createYaclScreen(parent: Screen): Screen = try {
        val screenClass = Class.forName("com.kaizzinho.wildbosses.config.WildBossesConfigScreen")
        screenClass.getMethod("create", Screen::class.java).invoke(null, parent) as Screen
    } catch (_: ReflectiveOperationException) {
        MissingYaclScreen(parent)
    } catch (_: LinkageError) {
        MissingYaclScreen(parent)
    }

    companion object {
        private const val YACL_ID = "yet_another_config_lib_v3"
    }
}
