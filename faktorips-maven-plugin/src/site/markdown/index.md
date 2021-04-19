The faktorips-maven-plugin can be used to build a Faktor-IPS project in a maven build. 

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
                            <goal>faktorips-build</goal>
                        </goals>
                        <phase>process-resources</phase>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
```
in your pom to use it.