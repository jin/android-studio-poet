/*
Copyright 2018 Google Inc.

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

package com.google.androidstudiopoet.generators

import com.google.androidstudiopoet.generators.bazel.*
import com.google.androidstudiopoet.generators.bazel.Target
import com.google.androidstudiopoet.models.Dependency
import com.google.androidstudiopoet.models.ModuleBuildBazelBlueprint
import com.google.androidstudiopoet.models.ModuleDependency
import com.google.androidstudiopoet.writers.FileWriter

class ModuleBuildBazelGenerator(private val fileWriter: FileWriter) {

    fun generate(blueprint: ModuleBuildBazelBlueprint) {
        fileWriter.writeToFile(
                getJavaLibraryTarget(blueprint).toString(),
                blueprint.path)
    }

    private fun getJavaLibraryTarget(blueprint: ModuleBuildBazelBlueprint): Target {
        var attributes = listOf(
                StringAttribute("name", blueprint.targetName),
                RawAttribute("srcs", "glob([\"src/main/java/**/*.java\"])"),
                StringListAttribute("visibility", listOf("//visibility:public")))

        if (blueprint.dependencies.isNotEmpty()) {
            attributes += StringListAttribute("deps", getDependencyTargetLabels(blueprint.dependencies))
        }

        return Target("java_library", attributes)
    }

    private fun getDependencyTargetLabels(dependencies: Set<Dependency>): List<String> {
        return dependencies.mapNotNull {
            when (it) {
                is ModuleDependency -> "//${it.name}" // `//module42` == `//module42:module42`
                else -> null
            }
        }
    }

}
