load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/cgruber/rules_kotlin/archive/ecc895796f503f43a2f2fb2a120ee54fa597cd34.tar.gz"],
    strip_prefix = "rules_kotlin-ecc895796f503f43a2f2fb2a120ee54fa597cd34",
    sha256 = "97b5aa8f112de30120e0370370233308dfe211613806ce44d128ef054853216a",
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories")

KOTLIN_VERSION = "1.3.31"
KOTLINC_RELEASE_SHA = ""

KOTLINC_RELEASE = {
    "urls": [
        "https://github.com/JetBrains/kotlin/releases/download/v{v}/kotlin-compiler-{v}.zip".format(v = KOTLIN_VERSION),
    ],
    "sha256": KOTLINC_RELEASE_SHA,
}

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories")
kotlin_repositories(compiler_release = KOTLINC_RELEASE)
register_toolchains("//:kotlin_toolchain")

RULES_JVM_EXTERNAL_TAG = "2.3"
RULES_JVM_EXTERNAL_SHA = "375b1592e3f4e0a46e6236e19fc30c8020c438803d4d49b13b40aaacd2703c30"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.31",
        "com.google.code.gson:gson:2.8.2",
        "com.squareup:javapoet:1.10.0",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:0.22.5",
        "junit:junit:4.12",
        "commons-io:commons-io:2.6",
        "junit:junit:4.12",
        "org.mockito:mockito-inline:2.15.0",
        "com.nhaarman:mockito-kotlin-kt1.1:1.5.0",
        "org.jetbrains.kotlin:kotlin-reflect:1.2.31",
    ],
    repositories = [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
    maven_install_json = "//:maven_install.json",
    fetch_sources = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")
pinned_maven_install()

