package com.ltan.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectClassPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        // AppExtension 对应build.gradle中android{...}
        def android = project.extensions.getByType(AppExtension)
        // 注册一个Transform
        def classTransform = new InjectClassTransform(project)
        android.registerTransform(classTransform)

        // 通过Extension的方式传递将要被注入的自定义代码
        def extension = project.extensions.create("InjectClassCode", InjectClassExtension)
        project.afterEvaluate {
            classTransform.injectCode = extension.injectCode
        }
    }
}