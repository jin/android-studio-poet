/*
Copyright 2017 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.google.androidstudiopoet.models

import com.google.androidstudiopoet.ModuleBlueprintFactory
import com.google.androidstudiopoet.input.AndroidModuleConfig
import com.google.androidstudiopoet.input.ModuleConfig
import com.google.androidstudiopoet.utils.joinPath
import kotlinx.coroutines.experimental.*
import kotlin.system.measureTimeMillis

class ProjectBlueprint(val configPOJO: ConfigPOJO) {

    val projectName = configPOJO.projectName

    val projectRoot = configPOJO.root.joinPath(projectName)


    private val pureModulesConfigs = (0 until configPOJO.numModules).map { ModuleConfig(it, configPOJO) }

    val androidGradlePluginVersion = configPOJO.androidGradlePluginVersion
    val kotlinVersion = configPOJO.kotlinVersion
    val useKotlin = configPOJO.useKotlin
    val gradleVersion = configPOJO.gradleVersion!!

    val dependencies = configPOJO.dependencies ?: listOf()

    val moduleCount = configPOJO.numModules

    val moduleBlueprints : List<ModuleBlueprint>
    val androidModuleBlueprints : List<AndroidModuleBlueprint>
    val allModulesNames : List<String>

    init  {
        var temporaryModuleBlueprints : List<ModuleBlueprint> = listOf()
        val timeModels = measureTimeMillis {
            ModuleBlueprintFactory.initCache(configPOJO.numModules)
            runBlocking {
                val deferred = pureModulesConfigs.map {
                    async {
                        ModuleBlueprintFactory.create(it, projectRoot)
                    }
                }
                temporaryModuleBlueprints = deferred.map {it.await()}
            }
        }
        moduleBlueprints = temporaryModuleBlueprints
        println("Time to create model blueprints: $timeModels")

        var temporayAndroidBlueprints : List<AndroidModuleBlueprint> = listOf()
        val timeAndroidModels = measureTimeMillis {
            temporayAndroidBlueprints = (0 until configPOJO.androidModules!!.toInt()).map { i ->
                ModuleBlueprintFactory.createAndroidModule(projectRoot, pureModulesConfigs.map { it.index },
                        AndroidModuleConfig(i, configPOJO),
                        (i + 1 until configPOJO.androidModules.toInt()).toList().map { AndroidModuleConfig(it, configPOJO) })
            }
        }
        androidModuleBlueprints = temporayAndroidBlueprints
        println("Time to create Android model blueprints: $timeAndroidModels")

        allModulesNames = moduleBlueprints.map { it.name } + androidModuleBlueprints.map { it.name }
    }

    fun printDependencies() {
        println("digraph $projectName {")
        for (module in pureModulesConfigs) {
            val list = if (module.dependencies.size > 0) " -> ${module.dependencies.sorted().joinToString()}" else ""
            println("  ${module.index}$list;")
        }
        println("}")
    }

    fun hasCircularDependencies() : Boolean {
        // Try to find a topological order (it will be stored in withZero)
        val dependencyCounter : MutableList<Int> = mutableListOf()
        pureModulesConfigs.mapTo(dependencyCounter, {_ -> 0})
        // Count how many modules depend on each
        pureModulesConfigs.flatMap { it.dependencies }.forEach { dependencyCounter[it]++ }
        // Try to generate a topological order
        val topologicalOrder = dependencyCounter.withIndex().filter { pair -> pair.value == 0 }.map { it -> it.index }.toMutableList()
        var index = 0
        while (index < topologicalOrder.size && topologicalOrder.size < pureModulesConfigs.size) {
            val from = topologicalOrder[index]
            for (to in pureModulesConfigs[from].dependencies) {
                dependencyCounter[to]--
                if (dependencyCounter[to] == 0) {
                    topologicalOrder.add(to)
                }
            }
            index++
        }
        // No circular dependencies if and olny if all modules can be sorted
        return topologicalOrder.size != pureModulesConfigs.size
    }
}
