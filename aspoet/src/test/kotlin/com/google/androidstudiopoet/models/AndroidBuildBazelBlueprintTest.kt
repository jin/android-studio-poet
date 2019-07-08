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

package com.google.androidstudiopoet.models

import com.google.androidstudiopoet.testutils.assertEquals
import com.google.androidstudiopoet.testutils.assertOn
import com.google.androidstudiopoet.utils.joinPath
import org.junit.Test

class AndroidBuildBazelBlueprintTest {

    @Test
    fun `libraries contain default set of libraries`() {
        val blueprint = createAndroidBuildBazelBlueprint()

        assertOn(blueprint) {
            libraries.assertEquals(setOf(
                MavenBazelDependency("com.android.support:appcompat-v7:aar:26.1.0"),
                MavenBazelDependency("com.android.support.constraint:constraint-layout:aar:1.0.2"),
                MavenBazelDependency("com.android.support:multidex:aar:1.0.1"),
                MavenBazelDependency("com.android.support.test:runner:aar:1.0.1"),
                MavenBazelDependency("com.android.support.test.espresso:espresso-core:aar:3.0.1")))
        }
    }

    @Test
    fun `path is module root joined with bazel build file`() {
        val blueprint = createAndroidBuildBazelBlueprint(moduleRoot = "moduleRoot")

        assertOn(blueprint) {
            path.assertEquals("moduleRoot".joinPath("BUILD.bazel"))
        }
    }

    private fun createAndroidBuildBazelBlueprint(isApplication: Boolean = false,
                                                 moduleRoot: String = "",
                                                 packageName: String = "com.example",
                                                 dependencies: Set<ModuleDependency> = setOf(),
                                                 name: String = "targetName"
    ) = AndroidBuildBazelBlueprint(isApplication, moduleRoot, packageName, dependencies, name)

}
