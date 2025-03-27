The faktorips-maven-plugin can be used to build a Faktor-IPS project in a maven build.  

It requires *Java 21* to run. Other JDKs may be used in the project(s) being built, see ["Using 
a different JDK"](using-different-jdk.html).

If you use Eclipse for development, we recommend configuring the same version for use 
in this plugin, to avoid discrepancies in code formatter settings, see ["Repositories and additional plugins"](repositories-additional-plugins.html).

See [here](plugin-info.html) for configuration.

Include
```
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.faktorips</groupId>
                    <artifactId>faktorips-maven-plugin</artifactId>
                    <version>${faktorips-version}</version>
                </plugin>
                ...
            </plugins>
        </pluginManagement>
        <plugins>
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
            ...
        </plugins>
```
in your pom to use it.