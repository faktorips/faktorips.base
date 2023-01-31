## Using a different JDK

There may be up to three different JDKs involved in the excecution of this plugin: 

1. the JDK used to run Maven (should be Java 17 or newer)
2. the JDK used to launch the Eclipse plugins used in the build, called the "Execution Environment"
3. the JDK used as compilation source version for the project(s) being built

The preferable way to use different JDKs in Maven is to use the toolchains mechanism (see [Guide to Using Toolchains](http://maven.apache.org/guides/mini/guide-using-toolchains.html)).

### ExecutionEnvironment

The `executionEnvironment` profile is used to resolve dependencies and run Faktor-IPS. It must be at least JavaSE-11 which is pre-configured. It must match the Eclipse version being used, so JavaSE-11 is good for the default of Eclipse 2020-03, but starting from 2021-12 it should be JavaSE-17. See [Repositories and additional plugins](repositories-additional-plugins.html) for how to configure another Eclipse version.

### JDK for compiling a Faktor-IPS project

Although a JDK 11 or newer is used to start the builder, a Faktor-IPS project might use an older Java version (at least Java 8) as its compile target and dependency. To compile such a project, that JDK must be passed to the build. If neither `jdkId` or `jdkDir` is configured, the ExecutionEnvironment is used as the default compile target and dependency for the Faktor-IPS projects.

For example, use the JDK with the Id `JavaSE-1.8` in your toolchains.xml.

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <jdkId>JavaSE-1.8</jdkId>
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
The other possibility is to use the directory directly in the `jdkDir` parameter and therefore bypassing the toolchains mechanism.

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

### Using Java 17 for the project

If you want to use Java 17, you need to set ExecutionEnvironment and an Eclipse version that can handle Java 17, for example

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <executionEnvironment>JavaSE-17</executionEnvironment>
                    <!-- Eclipse 2021-12 -->
                    <eclipseRepository>https://download.eclipse.org/eclipse/updates/4.22/</eclipseRepository>
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