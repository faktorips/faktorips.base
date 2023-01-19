In order to be able to validate objects that use data types defined in extensions, those extensions/plugins must be available as Maven bundles.
They must be added to the POM of the project(s) to be validated as follows.

First, add a dependency to the project containing the datatypes and extension to the configuration of the faktorips-validation-maven-plugin:

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-validation-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>faktorips-validate</goal>
                        </goals>
                    </execution>
                </executions>
                <dependencies>
                    <dependency>
                        <groupId>org.foo</groupId>
                        <artifactId>org.foo.project</artifactId>
                        <version>${project.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
```

If your plugin has dependencies to Eclipse projects, it may be necessary to exclude 
some or all of them, because the POMs of Eclipse plugins unfortunately are often not 
well maintained.

```
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-validation-maven-plugin</artifactId>
                ...
                <dependencies>
                    <dependency>
                        <groupId>org.foo</groupId>
                        <artifactId>org.foo.project</artifactId>
                        <version>version</version>
                        <exclusions>
                            <exclusion>
                                <groupId>org.eclipse.platform</groupId>
                                <artifactId>*</artifactId>
                            </exclusion>
                        </exclusions>
                    </dependency>
                </dependencies>
            </plugin>
```

If the plugin has been built with Tycho and has the packaging type 'eclipse-plugin', to ensure that the required artifacts are loaded correctly, the Tycho extension must be added.

```
<build>
        <extensions>
            <extension>
                <groupId>org.eclipse.tycho</groupId>
                <artifactId>tycho-build</artifactId>
                <version>${tycho.version}</version>
            </extension>
        </extensions>
</build>
```
