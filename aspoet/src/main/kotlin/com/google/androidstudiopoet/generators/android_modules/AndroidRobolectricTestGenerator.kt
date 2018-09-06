package com.google.androidstudiopoet.generators.android_modules

import com.google.androidstudiopoet.models.*
import javax.lang.model.element.Modifier
import com.google.androidstudiopoet.writers.FileWriter
import com.squareup.javapoet.*

class AndroidRobolectricTestGenerator(val fileWriter: FileWriter) {

    fun generate(blueprint: ActivityBlueprint) {

        val testClassName = blueprint.className + "Test"

        val filePath = "${blueprint.where.replace("main", "test")}/$testClassName.java"

        val robolectricTestClass = getClazzSpec(testClassName)
            .addMethod(getTestMethod(blueprint))
            .build()

        val JUNIT_TESTCASE_CLASS = ClassName.get("junit.framework", "TestCase")

        val robolectricTestFile = JavaFile
            .builder(blueprint.packageName, robolectricTestClass)
            .addStaticImport(JUNIT_TESTCASE_CLASS, "assertEquals")
            .build()

        fileWriter.writeToFile(robolectricTestFile.toString(), filePath)
    }

    private fun getClazzSpec(activityClassName: String): TypeSpec.Builder {
        val RUN_WITH = ClassName.get("org.junit.runner", "RunWith");
        val ROBOLECTRIC_TEST_RUNNER = ClassName.get("org.robolectric", "RobolectricTestRunner");
        return TypeSpec.classBuilder(activityClassName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec
                    .builder(RUN_WITH)
                    .addMember("value", "\$T.class", ROBOLECTRIC_TEST_RUNNER)
                    .build())
    }

    private fun getTestMethod(blueprint: ActivityBlueprint): MethodSpec {
        val TEST_CLASS = ClassName.get("org.junit", "Test");
        val ROBOLECTRIC_CLASS = ClassName.get("org.robolectric", "Robolectric")
        val classUnderTest = blueprint.className

        val builder = MethodSpec
            .methodBuilder("activityCanBeInstantiated")
            .addAnnotation(TEST_CLASS)
            .addModifiers(Modifier.PUBLIC)
            .returns(Void.TYPE)
            .addStatement("$classUnderTest activity = \$T.setupActivity($classUnderTest.class)", ROBOLECTRIC_CLASS)
            .addStatement("assertEquals(activity, activity)")
        return builder.build()
    }

}
