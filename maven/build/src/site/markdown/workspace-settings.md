## Faktor-IPS workspace settings

### Example
```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <work>${integrationtest.workspace}/${project.name}</work>
                    <importAsMavenProject>false</importAsMavenProject>
                    <exportHtml>true</exportHtml>
                    <debug>true</debug>
                    <debugPort>4711</debugPort>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>faktorips-clean</goal>
                            <goal>faktorips-build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

### buildIpsOnly
With the parameter `buildIpsOnly` Faktor-IPS will only generate the source code, which is then compiled by the “maven-compile” plugin in the lifecycle phase compile. If this parameter is set to `false` the Faktor-IPS build will generate the source code and compile it in Eclipse just before the “maven-compiler” compiles it again.
Therefore the default value is `true`.

### work
The parameter `work` can be used to overwrite the location of the workspace used to compile the Faktor-IPS project.
It must not be set to a directory inside the project's own directory, such as the project's target directory, as Eclipse can not import a project that contains the workspace directory.
The default value is `${java.io.tmpdir}/${project.name}/eclipserun-work`.

### importAsMavenProject
By default a Faktor-IPS project will be imported as Maven project, therefore it must have a pom.xml file.
It can also be imported as a Eclipse project if a .project file exists. This behavior must be activated by setting `<importAsMavenProject>false</importAsMavenProject>`.

### exportHtml
With this setting `<exportHtml>true</exportHtml>` the Faktor-IPS build will export the model with the **Faktor-IPS Html Export** Wizard.
To package the generated HTML export in its own JAR, configure the maven-jar-plugin like

```
 <plugin>
   <artifactId>maven-jar-plugin</artifactId>
   <executions>
     <execution>
       <configuration>
         <classifier>html</classifier>
         <classesDirectory>${project.build.directory}/html</classesDirectory>
         <includes>**/*</includes>
       </configuration>
       <id>pack-html</id>
       <phase>package</phase>
       <goals>
         <goal>jar</goal>
       </goals>
     </execution>
   </executions>
 </plugin>
```
Keep in mind that UI-libraries are required for the HTML export to work, so if you run this build on a CI server like Jenkins you will need a server with a desktop environment. For a pure CLI Linux server you can use the Xvfb fake X server for example.

### Debugging the build
The parameter `debug` starts the build in debug mode and pauses it until a remote debugger has been connected. The default debug port is 8000 but a different port can be configured with the parameter `debugPort`.

If Maven is started with the debug option (-X or --debug) Eclipse will be started with a platform debug tracing facility file. If for some reason the standard trace settings are not suitable, a separate tracing options file can be specified with the parameter `<debugLogOptions>/path/to/trace_file</debugLogOptions>`. For more information how to write your own trace file see: [FAQ How do I use the platform debug tracing facility](https://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility)


### m2eIgnorePlugins

During parallel Maven builds (`-T`), m2e can run plugin executions as build participants when Eclipse imports or refreshes a project. This can cause failures when dependency JARs are still being written by a concurrent job.

By default, the plugin suppresses the **SpotBugs** execution in the embedded m2e runtime, because SpotBugs requires Groovy classes that are not available in the m2e ClassRealm.

To suppress additional plugin executions, configure `<m2eIgnorePlugins>`. **Important:** configuring this parameter _replaces_ the SpotBugs default entirely. Include the SpotBugs entry explicitly if you want to keep it.

Each `<m2eIgnorePlugin>` entry requires:

| Element | Description | Example |
|---|---|---|
| `groupId` | Maven group ID of the plugin | `com.github.spotbugs` |
| `artifactId` | Maven artifact ID of the plugin | `spotbugs-maven-plugin` |
| `versionRange` | Maven version range (see below) | `[4,)` |
| `goals` | Comma-separated list of goals to suppress | `spotbugs,check` |

The `versionRange` follows standard [Maven version range syntax](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html), e.g. `[1.0,)`, `(,2.0)`, `[1.0,2.0]`.

#### Example: keep SpotBugs and suppress an additional plugin

```xml
<plugin>
    <groupId>org.faktorips</groupId>
    <artifactId>faktorips-maven-plugin</artifactId>
    <configuration>
        <m2eIgnorePlugins>
            <m2eIgnorePlugin>
                <groupId>com.github.spotbugs</groupId>
                <artifactId>spotbugs-maven-plugin</artifactId>
                <versionRange>[4,)</versionRange>
                <goals>spotbugs,check</goals>
            </m2eIgnorePlugin>
            <m2eIgnorePlugin>
                <groupId>com.example</groupId>
                <artifactId>my-maven-plugin</artifactId>
                <versionRange>[1,)</versionRange>
                <goals>generate</goals>
            </m2eIgnorePlugin>
        </m2eIgnorePlugins>
    </configuration>
</plugin>
```

### antScriptPath
To correctly import and build the Faktor-IPS project in Eclipse, the plugin uses a Ant script, performing the necessary steps. If no path is specified, a new script is generated based on the configuration.

If a custom ant script is used the parameters `exportHtml`, `importAsMavenProject` and the JDK settings will be ignored from the maven plugin. The default target is import but can be configured with the parameter `antTarget`.

```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <antScriptPath>${project.basedir}/build/importProjects.xml</antScriptPath>
                    <antTarget>doBuild</antTarget>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>faktorips-clean</goal>
                            <goal>faktorips-build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```
An example for a custom ant script:
```

    <project name="importProjects">
        <taskdef name="faktorips.import" classname="org.faktorips.devtools.ant.ProjectImportTask" />
        <taskdef name="faktorips.fullBuild" classname="org.faktorips.devtools.ant.FullBuildTask" />
        <taskdef name="faktorips.exportHtml" classname="org.faktorips.devtools.ant.ExportHtmlTask" />
        <taskdef name="faktorips.configureJdk" classname="org.faktorips.devtools.ant.ConfigureJdkTask" />
        <!-- <taskdef name="faktorips.mavenRefresh" classname="org.faktorips.devtools.ant.MavenProjectRefreshTask" /> -->

        <target name="doBuild">
            <echo message="executing faktorips import now" />
            <faktorips.configureJdk dir="${jdk.dir}" />
            <faktorips.import dir="${sourcedir}" copy="false" />
            <!--
            <faktorips.mavenImport dir="${sourcedir}" />
            <faktorips.mavenRefresh updateSnapshots="true" />
            -->
            <faktorips.fullBuild />
            <faktorips.exportHtml ipsProjectName="my.awesome.fips.project" showValidationErrors="true" showInheritedObjectPartsInTable="true" locale="de" destination="${sourcedir}/target/html" ipsObjectTypes="ALL" />
        </target>
    </project>
```
