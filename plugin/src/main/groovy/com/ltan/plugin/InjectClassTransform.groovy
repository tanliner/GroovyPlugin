package com.ltan.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.Format
import com.android.build.gradle.internal.pipeline.TransformManager

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * 用来向每一个 calss 文件中注入指定代码
 * ref: https://www.jianshu.com/p/804d79415258
 */
class InjectClassTransform extends Transform {

    Project project
    String injectCode

    InjectClassTransform(Project project) {
        this.project = project
    }

    /**
     * 设置我们自定义的Transform对应的Task名称, 类似：TransformClassesWithPreDexInjectCodeForXXX
     * @return
     */
    @Override
    String getName() {
        return "PreDexInjectCode"
    }

    /**
     * 需要处理的数据类型，CONTENT_CLASS代表处理class文件
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定Transform的作用范围
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 指明当前Transform是否支持增量编译
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        transformInvocation.inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //注入代码
                InjectClass.inject(directoryInput.file.absolutePath, project, injectCode)
                // 获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //遍历jar文件 对jar不操作，但是要输出到out路径
            input.jarInputs.each { JarInput jarInput ->
                // 重命名输出文件（同名文件copyFile会冲突）
                def jarName = jarInput.name
                // println("jar = " + jarInput.file.getAbsolutePath())
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}