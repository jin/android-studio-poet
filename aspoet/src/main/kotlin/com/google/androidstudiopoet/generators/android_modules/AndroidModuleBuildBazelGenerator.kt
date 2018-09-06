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

        val deps: Set<String> = bazelBlueprint.dependencies.mapNotNull {
            when (it) {
                is ModuleDependency -> "\"//${it.name}\""
                is GmavenBazelDependency -> "gmaven_artifact(\"${it.name}\")"
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
        val targetName = bazelBlueprint.packageName.split(".")[1]
        var ruleDefinition = """load("@gmaven_rules//:defs.bzl", "gmaven_artifact")

$ruleClass(
    name = "$targetName",
    srcs = glob(["src/main/java/**/*.java"]),${if (bazelBlueprint.isApplication) multidexString else ""}
    resource_files = glob(["src/main/res/**/*"]),
    manifest = "src/main/AndroidManifest.xml",
    custom_package = "${bazelBlueprint.packageName}",
    visibility = ["//visibility:public"],${if (deps.isNotEmpty()) depsString else ""}
)"""

        if (bazelBlueprint.generateTests && !bazelBlueprint.isApplication) {
            var testManifestContent = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.$targetName">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-sdk
        android:minSdkVersion="${bazelBlueprint.androidBuildConfig.minSdkVersion}"
        android:targetSdkVersion="${bazelBlueprint.androidBuildConfig.targetSdkVersion}" />
    <application>"""

            for (activityIndex in 0 until bazelBlueprint.numOfActivities) {
                ruleDefinition += """
android_local_test(
  name = "Activity${activityIndex}Test",
  srcs = ["src/test/java/com/${targetName}/Activity${activityIndex}Test.java"],
  manifest = "TestManifest.xml",
  custom_package = "com.${targetName}",
  deps = [
    ":$targetName",
    "@robolectric//bazel:robolectric",
  ],
)"""
                testManifestContent += """
        <activity android:name=".Activity$activityIndex" />
"""
            }

            testManifestContent += """
    </application>
</manifest>"""


            fileWriter.writeToFile(
                testManifestContent,
                bazelBlueprint.path.replaceAfterLast("/", "TestManifest.xml"))
        }

        fileWriter.writeToFile(ruleDefinition, bazelBlueprint.path)
    }
}

