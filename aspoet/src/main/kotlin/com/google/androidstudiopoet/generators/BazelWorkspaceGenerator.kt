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

import com.google.androidstudiopoet.generators.bazel.AssignmentStatement
import com.google.androidstudiopoet.generators.bazel.Comment
import com.google.androidstudiopoet.generators.bazel.LoadStatement
import com.google.androidstudiopoet.generators.bazel.RawAttribute
import com.google.androidstudiopoet.generators.bazel.StringAttribute
import com.google.androidstudiopoet.generators.bazel.Target
import com.google.androidstudiopoet.models.BazelWorkspaceBlueprint
import com.google.androidstudiopoet.writers.FileWriter

class BazelWorkspaceGenerator(private val fileWriter: FileWriter) {

    fun generate(bazelWorkspaceBlueprint: BazelWorkspaceBlueprint) {
        fileWriter.writeToFile(
                getBazelWorkspaceContent(bazelWorkspaceBlueprint),
                bazelWorkspaceBlueprint.workspacePath)
        fileWriter.mkdir(bazelWorkspaceBlueprint.aswbDir)

        fileWriter.writeToFile("""
build --incompatible_depset_is_not_iterable=false

build --experimental_strict_action_env

build --experimental_generate_json_trace_profile
build --profile profile.json.gz
build --experimental_json_trace_compression

build --spawn_strategy=standalone # disable sandboxing
build --genrule_strategy=standalone # disable sandboxing for genrules
build --android_persistent_workers
build --strategy=ManifestMerger=worker
build --android_aapt=aapt2
build --nojava_header_compilation # this will definitely affect incremental build performance, this is here only for a proof of concept. Ideally we can turn the Java header compiler into a persistent worker for lower execution cost.
build --experimental_multi_threaded_digest # great for SSDs""".trimIndent(),
                bazelWorkspaceBlueprint.bazelRcFile)

        fileWriter.writeToFile(
                """
directories:
  # Add the directories you want added as source here
  # By default, we've added your entire workspace ('.')
  .

targets:
  # Add targets that reach the source code that you want to resolve here
  # By default, we've added all targets in your workspace
  //...

additional_languages:
  kotlin


# Please uncomment an android-SDK platform. Available SDKs are:
android_sdk_platform: android-28""".trimIndent(),
                bazelWorkspaceBlueprint.bazelprojectFile)
    }

    fun getBazelWorkspaceContent(blueprint: BazelWorkspaceBlueprint) =
            """android_sdk_repository(name = "androidsdk")
                    
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "2.2"
RULES_JVM_EXTERNAL_SHA = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "com.android.support:appcompat-v7:aar:26.1.0",
        "com.android.support.constraint:constraint-layout:aar:1.0.2",
        "com.android.support:multidex:aar:1.0.1",
        "com.android.support.test:runner:aar:1.0.1",
        "com.android.support.test.espresso:espresso-core:aar:3.0.2",
    ],
    repositories = [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)
"""

}