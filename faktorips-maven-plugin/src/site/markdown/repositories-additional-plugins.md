## Repositories and additional plugins

The repositories used to find the required Faktor-IPS and Eclipse plugins can be configured as follows: 

### Configured defaults
Per default the standard Faktor-IPS repositories are used by the plugin. These repositories do not have to be added manually, also the version of Faktor-IPS can be configured by the `faktorips.repository.version` property in your pom file. See [Table of properties](properties.html) for more.

```
    <properties>
        <faktorips.repository.version><!-- The desired Faktor-IPS Version e.g.: -->22.12.0-rfinal</faktorips.repository.version>
    </properties>
...
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <!-- default configured repositories
                    <repositories>
                        <repository>
                           <id>faktor-ips</id>
                           <layout>p2</layout>
                           <url>https://update.faktorzehn.org/faktorips/${faktorips.repository.version}</url>
                        </repository>
                        <repository>
                           <id>eclipse-2022-03</id>
                           <layout>p2</layout>
                           <url>https://download.eclipse.org/eclipse/updates/4.23/</url>
                        </repository>
                    </repositories>
                    -->
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
Also the parameters `fipsRepository` and `eclipseRepository` can be used to override only the URLs of the default repositories.

```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <fipsRepository>https://myfipsmirror.firma.de/fips/22.12.0-rfinal</fipsRepository>
                    <eclipseRepository>https://myeclipsemirror.firma.de/eclipse/2022-03</eclipseRepository>
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

### Using additional plugins

If additional plugins are needed to compile a Faktor-IPS workspace they can be added as `additionalPlugins`.
Also it is possible to add `additionalRepositories` to the Eclipse-Runtime. These additional repositories will be added to the configured repositories in `repositories`.

#### Additionally using the Productvariant Plugin
```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <jdkId>JavaSE-11</jdkId>
                    <additionalPlugins>
                        <dependency>
                            <artifactId>org.faktorips.productvariant.core</artifactId>
                            <type>eclipse-plugin</type>
                        </dependency>
                    </additionalPlugins>
                    <additionalRepositories>
                        <repository>
                            <id>productvariants</id>
                            <layout>p2</layout>
                            <url>https://update.faktorzehn.org/faktorips/productvariants/22.12</url>
                        </repository>
                    </additionalRepositories>
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
### Adding Junit as integrated Eclipse library
```
    <additionalPlugins>
        <dependency>
            <artifactId>org.eclipse.jdt.junit</artifactId>
            <type>eclipse-plugin</type>
        </dependency>
    </additionalPlugins>
<!-- or Junit 5 -->
    <additionalPlugins>
        <dependency>
            <artifactId>org.eclipse.jdt.junit5.runtime</artifactId>
            <type>eclipse-plugin</type>
        </dependency>
    </additionalPlugins>
```