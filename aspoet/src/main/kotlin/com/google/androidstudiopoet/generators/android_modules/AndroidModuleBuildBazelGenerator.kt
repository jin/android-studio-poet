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

package com.google.androidstudiopoet.generators.android_modules

import com.google.androidstudiopoet.models.*
import com.google.androidstudiopoet.writers.FileWriter

class AndroidModuleBuildBazelGenerator(val fileWriter: FileWriter) {
    fun generate(bazelBlueprint: AndroidBuildBazelBlueprint) {

        val deps: Set<String> = bazelBlueprint.dependencies.map {
            when (it) {
                is ModuleDependency -> "\"//${it.name}\""
                is MavenBazelDependency -> "artifact(\"${it.name}\")"
                else -> ""
            }
        }.toSet()
        val depsString = """
    deps = [
        ${deps.joinToString(separator = ",\n        ") { it }}
    ],"""
        val multidexString = """
    multidex = "native","""

        val ruleClass = if (bazelBlueprint.isApplication) "android_binary" else "android_library"
        val targetName = bazelBlueprint.name
        val ruleDefinition = """load("@rules_jvm_external//:defs.bzl", "artifact")
$ruleClass(
    name = "$targetName",
    srcs = glob(["src/main/java/**/*.java"]),${if (bazelBlueprint.isApplication) multidexString else ""}
    resource_files = glob(["src/main/res/**/*"]),
    manifest = "src/main/AndroidManifest.xml",
    custom_package = "${bazelBlueprint.packageName}",
    visibility = ["//visibility:public"],${if (deps.isNotEmpty()) depsString else ""}
)"""

        fileWriter.writeToFile(ruleDefinition, bazelBlueprint.path)
    }
}

