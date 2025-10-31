import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    // Import KMM plugin
    kotlin("multiplatform")
}

kotlin {
    // Build JS output for h5App
    js(IR) {
        // Build output supports browser
        browser {
            webpackTask {
                // Final output executable JS filename
                outputFileName = "h5App.js"
            }

            commonWebpackConfig {
                // Do not export global objects, only export necessary entry methods
                output?.library = null
            }
        }
        // Package render code and h5App code together and execute directly
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                // Import web render, for specific business should use actual version, for example:
                implementation("com.tencent.kuikly-open.core-render-web:base:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open.core-render-web:h5:${Version.getKuiklyVersion()}")
            }
        }
    }
}

// Business project path name
val businessPathName = "shared"

/**
 * Copy locally built unified JS result to h5App's build/distributions/page directory
 */
fun copyLocalJSBundle() {
    // Output target path
    val destDir = Paths.get(
        project.buildDir.absolutePath,
        "distributions", "page"
    ).toFile()
    if (!destDir.exists()) {
        // Create directory if it doesn't exist
        destDir.mkdirs()
    } else {
        // Remove original files if directory exists
        destDir.deleteRecursively()
    }

    // Input target path, in shared/outputs/kuikly/js/release/local/nativevue2.zip
    val sourceDir = Paths.get(project.buildDir.absolutePath, "distributions", "kotlin2js").toFile()

    // File to be decompressed
    val zipFile = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build", "outputs", "kuikly", "js", "release", "local", "nativevue2.zip"
    ).toFile()
    // Compressed file directory
    val zipDir = Paths.get(project.buildDir.absolutePath, "distributions/kotlin2js").toFile()
    if (!zipDir.exists()) {
        zipDir.mkdirs()
    } else {
        zipDir.deleteRecursively()
    }
    // Decompress
    project.copy {
        from(zipTree(zipFile))
        into(zipDir)
    }
    // Copy files
    project.copy {
        // Copy js files from business build result
        from(sourceDir) {
            include("nativevue2.js")
        }
        into(destDir)
    }
    // Remove redundant decompressed directory kotlin2js
    delete(sourceDir)

}

/**
 * Copy business built page JS result to h5App's build/distributions/page directory
 */
fun copySplitJSBundle() {
    // Output target path
    val destDir = Paths.get(
        project.buildDir.absolutePath,
        "distributions", "page"
    ).toFile()
    if (!destDir.exists()) {
        // Directory does not exist, create it
        destDir.mkdirs()
    } else {
        // Remove original files if directory exists
        destDir.deleteRecursively()
    }
    // Input target path, in shared/outputs/kuikly/js/release/split/page
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build", "outputs", "kuikly", "js", "release", "split", "page"
    ).toFile()

    // Copy files
    project.copy {
        // Copy js files from business build result
        from(sourceDir) {
            include("*.js")
        }
        into(destDir)
    }
}

/**
 * Generate unified build page html file
 */
fun generateLocalHtml() {
    // File path to be processed
    val filePath = Paths.get(
        project.buildDir.absolutePath,
        "distributions", "index.html"
    )
    val fileContent = Files.readString(filePath)
    // Placeholder to be replaced
    val placeText = "http://127.0.0.1:8083/nativevue2.js"
    // Replace development environment JSBundle link with production environment link
    val updatedContent = fileContent.replace(placeText, "page/nativevue2.js")
    // Write new file content
    Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8)
    // Write success
    println("generate local html file success.")
}

/**
 * Generate page build html file
 */
fun generateSplitHtml() {
    // File path to be processed
    val htmlFilePath = Paths.get(
        project.buildDir.absolutePath,
        "distributions", "index.html"
    )
    val fileContent = Files.readString(htmlFilePath)
    // Placeholder to be replaced
    val placeText = "http://127.0.0.1:8083/nativevue2.js"
    // Need to read all js files in page, get file names, then modify business js in index.html to corresponding
    val pagePath = Paths.get(
        project.buildDir.absolutePath,
        "distributions", "page"
    )
    val pageDir = file(pagePath)
    if (pageDir.exists()) {
        // File names, and change new html file name to page name
        val files = pageDir.listFiles()
        files?.forEach { file ->
            if (file.isFile) {
                val fileName = file.name
                // Replace development environment JSBundle link with production environment link
                val updatedContent = fileContent.replace(placeText, "page/$fileName")
                // File path to be written
                val filePath = Paths.get(
                    project.buildDir.absolutePath,
                    "distributions", "${file.nameWithoutExtension}.html"
                )
                // Write new file content
                Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8)
            }
        }
        // Remove index.html
        htmlFilePath.toFile().delete()
        // Write success
        println("generate local html file success.")
    } else {
        // Write failure
        println("generate local html file failure, no such files.")
    }
}

/**
 * Copy business assets resources to h5App's build/distributions/page directory
 */
fun copyAssetsResource() {
    // Source target path
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build",
        "outputs",
        "kuikly",
        "assets"
    )

    if (sourceDir.toFile().exists()) {
        // If directory does not exist, do not process
        // Output target path, in h5App
        val destDir = Paths.get(
            project.rootDir.absolutePath,
            "h5App",
            "build",
            "distributions",
            "assets"
        )

        // Copy files
        project.copy {
            // Copy assets resources from business build result to publish directory
            from(sourceDir)
            into(destDir)
        }
    } else {
        print("dest directory not exist")
    }
}

/**
 * Copy assets resources to webpack dev server static directory
 */
fun copyAssetsFileToWebpackDevServer() {
    // Source target path
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "src",
        "commonMain",
        "assets"
    )

    if (sourceDir.toFile().exists()) {
        // If directory does not exist, do not process
        // Output target path, in h5App
        val destDir = Paths.get(
            project.rootDir.absolutePath,
            "h5App",
            "build", "processedResources", "js", "main", "assets"
        )

        // Copy files
        project.copy {
            // Copy assets resources from business build result to deServer directory
            from(sourceDir)
            into(destDir)
        }
    } else {
        print("dest directory not exist")
    }
}

project.afterEvaluate {
    // At this point, project configuration is complete,
    // register build h5 page release version related build methods here

    // Register Release unified packaging processing task
    tasks.register("publishLocalJSBundle") {
        group = "kuikly"

        // First execute h5App build task
        dependsOn("jsBrowserDistribution")
        // Then copy corresponding nativevue2.zip from business build result and copy nativevue2.js
        // to h5App's release directory
        copyLocalJSBundle()
        // Copy assets resources
        copyAssetsResource()
        doLast {
            // Finally modify html file page.js reference
            generateLocalHtml()
        }
    }

    // Register Release page packaging processing task
    tasks.register("publishSplitJSBundle") {
        group = "kuikly"

        // First execute h5App build task
        dependsOn("jsBrowserDistribution")
        // Then copy corresponding page js from business build result to h5App's release directory
        copySplitJSBundle()
        // Copy assets resources
        copyAssetsResource()
        doLast {
            // Finally modify html file page.js reference
            generateSplitHtml()
        }
    }

    // Copy assets resources to devServer directory when using webpack for development debugging
    tasks.register("copyAssetsToWebpackDevServer") {
        copyAssetsFileToWebpackDevServer()
    }
}