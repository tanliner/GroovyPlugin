package com.ltan.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

/**
 * ref: https://www.jianshu.com/p/804d79415258
 */
class InjectClass {
    // 初始化类池
    private final static ClassPool pool = ClassPool.getDefault()

    static void inject(String path, Project project, String injectCode) {
        println("filePath = " + path)
        // 将当前路径加入类池,不然找不到这个类
        pool.appendClassPath(path)
        // project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())

        File dir = new File(path)
        if (dir.isDirectory()) {
            // 遍历文件夹
            dir.eachFileRecurse { File file ->
                if (file.getName().equals("MainActivity.class")) {
                    // 获取 MainActivity.class
                    CtClass ctClass = pool.getCtClass("com.ltan.groovyplugin.MainActivity")
                    println("ctClass = " + ctClass)
                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }
                    // 获取到 onCreate 方法
                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")
                    println("方法名 = " + ctMethod)
                    println "injectCode = " + injectCode
                    // 在方法开始注入代码
                    ctMethod.insertBefore(injectCode)
                    ctClass.writeFile(path)
                    // 释放
                    ctClass.detach()
                }
            }
        }
    }
}