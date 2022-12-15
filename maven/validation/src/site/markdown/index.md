The faktorips-validation-maven-plugin can be used to validate a Faktor-IPS project in a maven build.  

See [here](plugin-info.html) for configuration.

Include
```
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.faktorips</groupId>
                    <artifactId>faktorips-validation-maven-plugin</artifactId>
                    <version>${faktorips-version}</version>
                </plugin>
                ...
            </plugins>
        </pluginManagement>
        <plugins>
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
            </plugin>
            ...
        </plugins>
```
in your pom to use it.