### how to build a plugin?

1. crate a new library module, and remove the auto generated java code.

2. Edit the build.gradle file, like below

```
apply plugin: 'java'
apply plugin: 'groovy'
apply plugin: 'maven'

dependencies {
    implementation gradleApi() // gradle sdk
    implementation localGroovy() // groovy sdk
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.tools.build:gradle:3.5.0'
    // open source, for class file modify
    implementation 'javassist:javassist:3.12.1.GA'
    // open source, handle IO
    implementation 'commons-io:commons-io:2.6'
}

// publish to local Maven
repositories {
    mavenCentral()
}

// Be same as the file `properties` under the directory gradle-plugins/com.ltan.plugin.properties
group = 'com.ltan.plugin'
archivesBaseName = 'injectclassplugin' // optional
version = '1.0.0'

uploadArchives {
    repositories {
        mavenDeployer {
            // upload to ../repo, local maven
            repository(url: uri('../repo'))
        }
    }
}
``` 
3. create directory as source code for groovy, `src/main/groovy/`
![](https://raw.githubusercontent.com/tanliner/GroovyPlugin/master/images/plugin_structure.png)

4. write the groovy plugin code
[Download-InjectClassPlugin](https://raw.githubusercontent.com/tanliner/GroovyPlugin/master/plugin/src/main/groovy/com/ltan/plugin/InjectClassPlugin.groovy)
[Download-InjectClass](https://raw.githubusercontent.com/tanliner/GroovyPlugin/master/plugin/src/main/groovy/com/ltan/plugin/InjectClass.groovy)
[Download-InjectClassTransform](https://raw.githubusercontent.com/tanliner/GroovyPlugin/master/plugin/src/main/groovy/com/ltan/plugin/InjectClassTransform.groovy)
[Download-InjectClassExtension](https://raw.githubusercontent.com/tanliner/GroovyPlugin/master/plugin/src/main/groovy/com/ltan/plugin/InjectClassExtension.groovy)

5. tell the build system who is the root class
```
// file:resource/META-INF/gradle-plugins/com.ltan.plugin.properties
implementation-class=com.ltan.plugin.InjectClassPlugin
```

6. upload the archive to maven local center `../repo` specified by build.gradle

7. add local maven center to build path
```
// rootProject/build.gradle
buildscript {
    repositories {
        maven {
            url uri('./repo')
        }
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.5.0'
        // the package in the local maven, specified by url uri('./repo')
        classpath 'com.ltan.plugin:plugin:1.0.0'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
```

8. use the plugin in you app module
```
// `com.ltan.plugin` is the name of the properties
apply plugin: 'com.ltan.plugin'

android {}

dependencies {}

// @see InjectClassPlugin.groovy
InjectClassCode {
    // this name must be same as property of `InjectClassExtension.groovy`
    injectCode = "android.widget.Toast.makeText(this, \"Toast代码注入测试\", android.widget.Toast.LENGTH_SHORT).show();"
}
```

9. build app with plugin
```
./gradlew -p app clean build -stacktrace
```

10. check it
![](https://raw.githubusercontent.com/tanliner/GroovyPlugin/master/images/done_with_succeed.png)