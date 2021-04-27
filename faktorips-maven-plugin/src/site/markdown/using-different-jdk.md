## Using a different JDK

The preferable way to use a different JDK is to use the toolchains mechanism ([Guide to Using Toolchains](http://maven.apache.org/guides/mini/guide-using-toolchains.html)).

### ExecutionEnvironment

The `executionEnvironment` profile is used to resolve dependencies and run Faktor-IPS. It must be at least JavaSE-11 and is pre configured. If neither `jdkId` or `jdkDir` is configured it is used as the default compile target and dependency for the Faktor-IPS projects.

### JDK for compiling a Faktor-IPS project

Although a JDK 11 or newer is used to start the builder, a Faktor-IPS project might use an older Java version (at least Java 8) as its compile target and dependency. To compile such a project, that JDK must be passed to the build.

For example, use the JDK with the Id `JavaSE-1.8` in your toolchains.xml.

```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <jdkId>JavaSE-1.8</jdkId>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>faktorips-build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```
The other possibility is to use the directory directly in the `jdkDir` parameter and therefore bypassing the toolchains mechanism.
```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <jdkDir>/usr/lib/jvm/java-8-openjdk-amd64/</jdkDir>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>faktorips-build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```