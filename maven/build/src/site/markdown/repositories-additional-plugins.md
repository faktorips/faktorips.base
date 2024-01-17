## Repositories and Additional Plugins

The repositories used to find the required Faktor-IPS and Eclipse plugins can be configured as follows: 

### Configured Defaults
Per default the standard Faktor-IPS repositories are used by the plugin. These repositories do not have to be added manually, also the version of Faktor-IPS can be configured by the `faktorips.repository.version` property in your pom file. See [Table of properties](properties.html) for more.

```
    <properties>
        <faktorips.repository.version><!-- The desired Faktor-IPS Version e.g.: -->24.1</faktorips.repository.version>
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
                           <id>eclipse-2022-12</id>
                           <layout>p2</layout>
                           <url>https://download.eclipse.org/eclipse/updates/4.26/</url>
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
Also the parameters `fipsRepository` and `eclipseRepository` can be used to override only the URLs of the default repositories. Alternatively, the user properties `repository.fips` and `repository.eclipse` can be provided in the Maven properties or as command line parameters.

```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <fipsRepository>https://myfipsmirror.firma.de/fips/24.1.1.release</fipsRepository>
                    <eclipseRepository>https://myeclipsemirror.firma.de/eclipse/2022-12</eclipseRepository>
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

#### ⚠ Compatibility with Eclipse Versions

Not all Eclipse versions work with the same Faktor-IPS plugins. Although you can provide the URL to any Eclipse version as `eclipseRepository`, only those listed in the compatibility list on [faktorzehn.org](https://www.faktorzehn.org/en/download/) may work. A known exception is Eclipse 2023-06 that does not work with this maven plugin even though it is listed as compatible with the Faktor-IPS Eclipse plugin.

### Using Additional Plugins

If additional plugins are needed to compile a Faktor-IPS workspace they can be added as `additionalPlugins`.
Also it is possible to add `additionalRepositories` to the Eclipse-Runtime. These additional repositories will be added to the configured repositories in `repositories`.

#### Additionally Using the Productvariant Plugin
```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
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
                            <url>https://update.faktorzehn.org/faktorips/productvariants/24.1</url>
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
### Adding Junit as Integrated Eclipse Library
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