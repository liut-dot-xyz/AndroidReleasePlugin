package xyz.liut.releaseplugin.task

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import xyz.liut.logcat.L

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Consumer

/**
 * release task
 */
class ReleaseTask extends BaseTask {

    /**
     * 待处理的数据
     */
    Set<ApplicationVariant> inputVariants

    /**
     * 文件名模板
     */
    String fileNameTemplate

    /**
     * 输出路径
     */
    String outputDir

    /**
     * 所有输出的文件
     */
    Set<File> outputFiles

    @TaskAction
    def release() {
        if (!inputVariants) {
            throw new IllegalArgumentException("variants 为空")
        }
        if (!fileNameTemplate) {
            throw new IllegalArgumentException("fileNameTemplate 为空")
        }
        if (!outputDir) {
            throw new IllegalArgumentException("outputDir 为空")
        }

        inputVariants.forEach(new Consumer<ApplicationVariant>() {
            @Override
            void accept(ApplicationVariant applicationVariant) {

                def outputs = applicationVariant.outputs

                if (outputs.size() != 1) {
                    throw new IllegalArgumentException("outputs.size = $outputs.size()")
                }

                def output = applicationVariant.outputs[0]

                def outputFile = output.outputFile
                if (!outputFile.exists()) {
                    return
                }
                L.d outputFile


                // 根据模板生成文件名
                String finalFileName = fileNameTemplate
                        .replace('$app', project.name)
                        .replace('$b', applicationVariant.buildType.name)
                        .replace('$f', applicationVariant.flavorName)
                        .replace('$vn', applicationVariant.versionName)
                        .replace('$vc', applicationVariant.versionCode.toString())

                // 判断是否已签名
                if (applicationVariant.signingReady) {
                    finalFileName = finalFileName + ".apk"
                } else {
                    finalFileName = finalFileName + "-unsigned.apk"
                }

                File resultFile = new File(outputDir + File.separator + finalFileName).absoluteFile

                Files.copy(outputFile.toPath(),
                        resultFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING)

                outputFiles.add(resultFile)
            }
        })

        success = true

        L.i "-------${name} end, ${outputFiles.size()}---------"
    }

}
