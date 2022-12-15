## Using Git to check for modifications after compiling

### gitStatusPorcelain

With this setting the faktorips-maven-plugin will check the local workspace for any modifications. This can be especially useful to check for unexpected behavior after version updates or source-code changes.


```
        <plugins>
            <plugin>
                <groupId>org.faktorips</groupId>
                <artifactId>faktorips-maven-plugin</artifactId>
                <configuration>
                    <gitStatusPorcelain>
                        <failBuild>true</failBuild>
                        <verbosity>diff</verbosity>
                    </gitStatusPorcelain>
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

#### failBuild

Whether the build should fail if local modifications are detected. The value of `failBuild`can be overridden with the `git.fail.build` property.

#### verbosity

There are three different settings: diff, verbose and quiet.

##### diff

With this setting the faktorips-maven-plugin will log the changes in the git patch format. Please note, the diff output will ignore new files, but if `failBuild` is true, the build will still fail.

```
diff --git a/org.faktorips.integrationtest.java11/src/main/resources/org/faktorips/integrationtest/internal/toc/toc.xml b/org.faktorips.integrationtest.java11/src/main/resources/org/faktorips/integrationtest/internal/toc/toc.xml
index eca3c8e..b948629 100644
--- a/org.faktorips.integrationtest.java11/src/main/resources/org/faktorips/integrationtest/internal/toc/toc.xml
+++ b/org.faktorips.integrationtest.java11/src/main/resources/org/faktorips/integrationtest/internal/toc/toc.xml
@@ -1,5 +1,5 @@
 <?xml version="1.0" encoding="UTF-8" standalone="no"?>
-<FaktorIps-TableOfContents productDataVersion="21.6.0" xmlversion="3.0">
+<FaktorIps-TableOfContents productDataVersion="21.12.0" xmlversion="3.0">
```
##### verbose

With this setting all modified, deleted or added files are logged.

##### quiet

With this setting the plugin will not log any modifications, but if `failBuild` is true, the build will still fail.

