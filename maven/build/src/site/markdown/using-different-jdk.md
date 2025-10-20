## Using a different JDK

There may be up to three different JDKs involved in the excecution of this plugin: 

1. the JDK used to run Maven (should be Java 21 or newer)
2. the JDK used to launch the Eclipse plugins used in the build, called the "Execution Environment"
3. the JDK used as compilation source version for the project(s) being built

The preferable way to use different JDKs in Maven is to use the toolchains mechanism (see [Guide to Using Toolchains](http://maven.apache.org/guides/mini/guide-using-toolchains.html)).

### Execution Environment

The `executionEnvironment` profile is used to resolve dependencies and run Faktor-IPS. It must be at least JavaSE-21 which is pre-configured. Therefore Maven must be started with an jdk21 and the configured Eclipse version must also support Java 21 or newer. See [Repositories and additional plugins](repositories-additional-plugins.html) for configuring a different Eclipse version.

### JDK for compiling a Faktor-IPS project

Although a JDK 21 or newer is used to start the builder, a Faktor-IPS project might use an older Java version (e.g.: The runtime of Faktor-IPS 25.1 could be Java 17 or 21) as its compile target and dependency. To compile such a project, the desired Compile JDK must be passed to the build.

If neither `jdkId` nor `jdkDir` is configured, the JDK from Maven is used as the default compile target and dependency for the Faktor-IPS projects.

The other possibility is to use the directory directly in the `jdkDir` parameter and therefore bypassing the toolchains mechanism.

#### Configuration Overview 

|Mechanism                            |When to Use                        |Notes                                |
|-------------------------------------|-----------------------------------|-------------------------------------|
|`jdkDir` Variable                      |One-off or fixed JDK path          |Highest priority, bypasses everything|
|toolchains.xml + `jdkId` Variable      |Recommended for multi-JDK builds   |Build fails if `jdkId` is not found    |
|`jdkId` Variable without toolchains.xml|Never                              |Only valid if it matches the Maven JDK, otherwise build fails        |
|No config                            |Default Configuration              |Maven JDK is used                    |

Some examples:

 - use the Maven JDK for the Faktor-IPS build, best if no toolchains.xml is used.

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
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

 - use the JDK with the Id `JavaSE-24` in your toolchains.xml.

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <jdkId>JavaSE-24</jdkId>
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
 - using a direct JDK directory (bypassing toolchains.xml).

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <jdkDir>/usr/lib/jvm/java-8-openjdk-amd64/</jdkDir>
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
