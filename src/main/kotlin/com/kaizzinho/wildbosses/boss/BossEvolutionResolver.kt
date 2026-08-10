package com.kaizzinho.wildbosses.boss

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace

object BossEvolutionResolver {

    data class ResolvedForm(val species: Species, val form: FormData)


    fun resolveFinalForm(startingSpecies: Species, startingForm: FormData): ResolvedForm {
        var currentSpecies = startingSpecies
        var currentForm = startingForm
        val visited = mutableSetOf<String>()

        while (true) {
            val evolutions = currentForm.evolutions
            if (evolutions.isEmpty()) return ResolvedForm(currentSpecies, currentForm)

            val nextOptions = evolutions.mapNotNull { evolution ->
                val speciesName = evolution.result.species ?: return@mapNotNull null
                val targetSpecies = PokemonSpecies.getByIdentifier(speciesName.asIdentifierDefaultingNamespace())
                    ?: return@mapNotNull null
                val formName = evolution.result.form
                val targetForm = if (formName != null) {
                    targetSpecies.forms.find { it.name.equals(formName, ignoreCase = true) }
                        ?: targetSpecies.standardForm
                } else {
                    targetSpecies.standardForm
                }
                targetSpecies to targetForm
            }

            if (nextOptions.isEmpty()) return ResolvedForm(currentSpecies, currentForm)

            val (nextSpecies, nextForm) = nextOptions.random()
            val key = "${nextSpecies.name}:${nextForm.name}"
            if (key in visited) return ResolvedForm(currentSpecies, currentForm)
            visited.add(key)
            currentSpecies = nextSpecies
            currentForm = nextForm
        }
    }
}
