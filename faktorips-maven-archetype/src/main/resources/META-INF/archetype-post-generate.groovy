/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

import java.nio.file.*
import java.util.regex.Matcher

//////////////////////////////////////////////////////////////
// Maven-Archetype Postprocessor: Main Script
//////////////////////////////////////////////////////////////

try {
    executePostprocessor()
    LogUtil.printInfoMessage("Successfully finished postprocessor")
} catch (Exception e) {
    LogUtil.printSeparator(3)
    LogUtil.printErrorMessage("Due to errors in the postprocessor, "
            + "the generated Maven project might be misconfigured.")
    LogUtil.printErrorMessage("Message: " + e.getMessage())
    LogUtil.printSeparator(3)
    throw new RuntimeException()
}

/**
 * Executes the post-processor in order to adjust
 * generated files based on the user input.
 */
private void executePostprocessor() {
    Path projectPath = Paths.get(request.outputDirectory, request.artifactId)
    String lineSeparator = System.getProperty("line.separator")

    Properties properties = request.properties

    String language = properties.get("IPS-Language")
    LanguageSupport languageSupport = new LanguageSupport(projectPath)
    languageSupport.setLanguage(language)

    String ipsVersion = request.getArchetypeVersion()
    VersionSupport versionSupport = new VersionSupport(projectPath)
    versionSupport.setMinVersion(ipsVersion)

    String configureIpsBuild = properties.get("IPS-ConfigureIpsBuild")
    FaktoripsMavenPluginSupport ipsMavenPluginSupport = new FaktoripsMavenPluginSupport(projectPath, lineSeparator)
    ipsMavenPluginSupport.configureIpsBuild(configureIpsBuild)

    String isGroovySupport = properties.get("IPS-IsGroovySupport")
    GroovySupport groovySupport = new GroovySupport(projectPath, lineSeparator)
    groovySupport.setGroovySupport(isGroovySupport)

    String isPersistenceSupport = properties.get("IPS-IsPersistentProject")
    PersistenceSupport persistenceSupport = new PersistenceSupport(projectPath, lineSeparator)
    persistenceSupport.setPersistenceSupport(isPersistenceSupport)
    
    String isJaxbSupport = properties.get("IPS-IsJaxbSupport")
    JaxbSupport jaxbSupport = new JaxbSupport(projectPath, lineSeparator)
    jaxbSupport.setJaxbSupport(isJaxbSupport)
    
    String isModelProject = properties.get("IPS-IsModelProject")
    String isProductDefinitionProject = properties.get("IPS-IsProductDefinitionProject")
    String packageName = properties.get("package")
    IpsObjectPathUtil ipsObjectPathUtil = new IpsObjectPathUtil(projectPath, isModelProject, isProductDefinitionProject, packageName)
    ipsObjectPathUtil.setOutputFolderMergableSources()
}

//////////////////////////////////////////////////////////////
// Language support
//////////////////////////////////////////////////////////////
/**
 * Loads the required .ipsproject file based on the selected language.
 * <p>
 * This has to be executed first in order to make further changes to the .ipsproject file.
 */
class LanguageSupport {

    private final String LANGUAGE_EN = Locale.ENGLISH.getLanguage()
    private final String LANGUAGE_DE = Locale.GERMAN.getLanguage()

    private final String IPS_PROJECT = ".ipsproject"
    private final String IPS_PROJECT_EN = ".ipsprojectEN"
    private final String IPS_PROJECT_DE = ".ipsprojectDE"

    private final Path projectPath

    LanguageSupport(Path projectPath) {
        this.projectPath = projectPath
    }

    /**
     * Deletes unnecessary .ipsproject files and renames the
     * file with the required language to ".ipsproject".
     *
     * @param language The selected language
     */
    void setLanguage(String language) {
        String requiredFile
        String unnecessaryFile
        if (language.equalsIgnoreCase(LANGUAGE_EN)) {
            requiredFile = IPS_PROJECT_EN
            unnecessaryFile = IPS_PROJECT_DE
        } else if (language.equalsIgnoreCase(LANGUAGE_DE)) {
            requiredFile = IPS_PROJECT_DE
            unnecessaryFile = IPS_PROJECT_EN
        } else {
            throw new RuntimeException("An invalid language has been selected")
        }
        Files.deleteIfExists(projectPath.resolve(unnecessaryFile))
        File file = new File(projectPath.resolve(requiredFile).toString())
        File fileRenamed = new File(projectPath.resolve(IPS_PROJECT).toString())
        file.renameTo(fileRenamed)
    }
}

/**
 * Sets the minimal required version for Faktor-IPS.
 */
class VersionSupport {

    private final String IPS_PROJECT = '.ipsproject'

    private final String MIN_VERSION_REGEX = '([0-9]+[.][0-9]+)[.][0-9]+'

    private final String MIN_VERSION_TEMPLATE = '$IPS-MinVersion$'

    private final Path projectPath

    VersionSupport(Path projectPath) {
        this.projectPath = projectPath
    }

    /**
     * Sets the minimal required version within the .ipsproject file.
     * <p>
     * The minimal version must match with {link #MIN_VERSION_REGEX}.
     *
     * @param version The Faktor-IPS Archetype build version
     */
    void setMinVersion(String version) {
        File ipsProject = new File(projectPath.resolve(IPS_PROJECT).toString())
        Matcher filteredVersion = (version =~ MIN_VERSION_REGEX)
        if (filteredVersion != null && filteredVersion.find()) {
            String minVersion = filteredVersion.group(1)
            if (version.startsWith(minVersion)) {
                String dependency = ipsProject.text.replace(MIN_VERSION_TEMPLATE, minVersion)
                ipsProject.text = dependency
                return
            }
        }
        String empty = ipsProject.text.replace(MIN_VERSION_TEMPLATE, "")
        ipsProject.text = empty
        // This should never be the case since the version is provided by the defined archetype version
        throw new RuntimeException("The Faktor-IPS version is not valid. " +
                "It must start with: \"d.d.d\".")
    }
}

//////////////////////////////////////////////////////////////
// Faktor-IPS Maven Build
//////////////////////////////////////////////////////////////
/**
 * Adds the Faktor-IPS Maven Plugin if required.
 */
class FaktoripsMavenPluginSupport {

    // The template to be replaced in order to set the dependency
    private final String MAVEN_PLUGIN_TEMPLATE = '<!-- $IpsMavenPluginTemplate$ -->'
    private final String MAVEN_PLUGIN_MANAGEMENT_TEMPLATE = '<!-- $IpsMavenPluginManagementTemplate$ -->'

    // Using the version provided by the generated pom.xml
    // Single quotes are used here because interpolation is not required
    private final String VERSION = '\${faktorips.version}'

    // ATTENTION: this dependency uses the version specified within the existing pom.xml
    // Single quotes are necessary to escape the dollar sign
    // Using the system specific line separators (Groovy uses LF only)
    // The indentation fits to the maven dependencies
    private final String PLUGIN_CONFIGURATION = """<plugin>$lineSeparator\
                <groupId>org.faktorips</groupId>$lineSeparator\
                <artifactId>faktorips-maven-plugin</artifactId>$lineSeparator\
                <executions>$lineSeparator\
                    <execution>$lineSeparator\
                        <goals>$lineSeparator\
                            <goal>faktorips-clean</goal>$lineSeparator\
                            <goal>faktorips-build</goal>$lineSeparator\
                        </goals>$lineSeparator\
                    </execution>$lineSeparator\
                </executions>$lineSeparator\
            </plugin>"""
    private final String PLUGIN_MANAGEMENT = """<plugin>$lineSeparator\
                    <groupId>org.faktorips</groupId>$lineSeparator\
                    <artifactId>faktorips-maven-plugin</artifactId>$lineSeparator\
                    <version>$VERSION</version>$lineSeparator\
                </plugin>"""

    private final String POM = "pom.xml"

    private final Path projectPath
    private final String lineSeparator

    FaktoripsMavenPluginSupport(Path projectPath, String lineSeparator) {
        this.projectPath = projectPath
        this.lineSeparator = lineSeparator
    }

    /**
     * Adds the FaktorIPS Groovy Maven dependency to the pom.xml if required.
     *
     * @param configureIpsBuild Is true when Faktor-IPS Maven Build should be configured, otherwise false
     */
    void configureIpsBuild(String configureIpsBuild) {
        File ipsProject = new File(projectPath.resolve(POM).toString())
        if (!Boolean.valueOf(configureIpsBuild)) {
            String empty = ipsProject.text.replace(MAVEN_PLUGIN_TEMPLATE, "").replace(MAVEN_PLUGIN_MANAGEMENT_TEMPLATE, "")
            ipsProject.text = empty
        } else {
            String configured = ipsProject.text.replace(MAVEN_PLUGIN_TEMPLATE, PLUGIN_CONFIGURATION).replace(MAVEN_PLUGIN_MANAGEMENT_TEMPLATE, PLUGIN_MANAGEMENT)
            ipsProject.text = configured
        }
    }
}

//////////////////////////////////////////////////////////////
// Faktor-IPS Groovy support
//////////////////////////////////////////////////////////////
/**
 * Adds the Faktor-IPS Groovy Maven dependency if required.
 */
class GroovySupport {

    // The template to be replaced in order to set the dependency
    private final String MAVEN_DEPENDENCY_TEMPLATE = '<!-- $GroovyDependencyTemplate$ -->'

    // Using the version provided by the generated pom.xml
    // Single quotes are used here because interpolation is not required
    private final String VERSION = '\${faktorips.version}'

    // ATTENTION: this dependency uses the version specified within the existing pom.xml
    // Single quotes are necessary to escape the dollar sign
    // Using the system specific line separators (Groovy uses LF only)
    // The indentation fits to the maven dependencies
    private final String GROOVY_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.faktorips</groupId>$lineSeparator\
            <artifactId>faktorips-runtime-groovy</artifactId>$lineSeparator\
            <version>$VERSION</version>$lineSeparator\
        </dependency>"""

    private final String POM = "pom.xml"

    private final Path projectPath
    private final String lineSeparator

    GroovySupport(Path projectPath, String lineSeparator) {
        this.projectPath = projectPath
        this.lineSeparator = lineSeparator
    }

    /**
     * Adds the FaktorIPS Groovy Maven dependency to the pom.xml if required.
     *
     * @param isGroovySupportSelected Is true when Groovy support is selected, otherwise false
     */
    void setGroovySupport(String isGroovySupportSelected) {
        File ipsProject = new File(projectPath.resolve(POM).toString())
        if (Boolean.valueOf(isGroovySupportSelected)) {
            String dependency = ipsProject.text.replace(MAVEN_DEPENDENCY_TEMPLATE, GROOVY_DEPENDENCY)
            ipsProject.text = dependency
        } else {
            String empty = ipsProject.text.replace(MAVEN_DEPENDENCY_TEMPLATE, "")
            ipsProject.text = empty
        }
    }
}

//////////////////////////////////////////////////////////////
// Persistence support
//////////////////////////////////////////////////////////////
/**
 * Adjusts the maven dependencies and the .ipsproject file
 * in order to add persistence support, if required.
 *
 * This requires another user input.
 */
class PersistenceSupport {

    // Names of required files
    private final String POM = "pom.xml"
    private final String IPS_PROJECT = ".ipsproject"
    private final String MANIFEST = "META-INF/MANIFEST.MF"

    // Names of possible persistence supports
    private final String ECLIPSE_LINK_25 = "EclipseLink 2.5"
    private final String ECLIPSE_LINK_30 = "EclipseLink 3.0"
    private final String GENERIC_JPA_20 = "Generic JPA 2.0"
    private final String GENERIC_JPA_21 = "Generic JPA 2.1"
    private final String JAKARTA_PERSISTENCE_2_2 = "Jakarta Persistence 2.2"
    private final String JAKARTA_PERSISTENCE_3_0 = "Jakarta Persistence 3.0"

    // Templates to be replaced
    private final String IPS_PROJECT_TEMPLATE = '$IPS-PersistenceDatabase$'
    private final String MAVEN_DEPENDENCY_TEMPLATE = '<!-- $JPADependencyTemplate$ -->'

    // Using the system specific line separators (Groovy uses LF only)
    // The indentation fits to the maven dependencies
    private final String ECLIPSE_LINK_25_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.eclipse.persistence</groupId>$lineSeparator\
            <artifactId>eclipselink</artifactId>$lineSeparator\
            <version>2.5.0</version>$lineSeparator\
        </dependency>"""
    private final String ECLIPSE_LINK_30_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.eclipse.persistence</groupId>$lineSeparator\
            <artifactId>eclipselink</artifactId>$lineSeparator\
            <version>3.0.2</version>$lineSeparator\
        </dependency>$lineSeparator\
        <dependency>$lineSeparator\
            <groupId>jakarta.persistence</groupId>$lineSeparator\
            <artifactId>jakarta.persistence-api</artifactId>$lineSeparator\
            <version>3.0.0</version>$lineSeparator\
        </dependency>"""
    private final String JPA_20_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.eclipse.persistence</groupId>$lineSeparator\
            <artifactId>javax.persistence</artifactId>$lineSeparator\
            <version>2.0.0</version>$lineSeparator\
        </dependency>"""
    private final String JPA_21_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.eclipse.persistence</groupId>$lineSeparator\
            <artifactId>javax.persistence</artifactId>$lineSeparator\
            <version>2.1.0</version>$lineSeparator\
        </dependency>"""
    private final String JAKARTA_22_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>jakarta.persistence</groupId>$lineSeparator\
            <artifactId>jakarta.persistence-api</artifactId>$lineSeparator\
            <version>2.2.3</version>$lineSeparator\
        </dependency>"""
    private final String JAKARTA_30_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>jakarta.persistence</groupId>$lineSeparator\
            <artifactId>jakarta.persistence-api</artifactId>$lineSeparator\
            <version>3.0.0</version>$lineSeparator\
        </dependency>"""

    private final Path projectPath
    private final String lineSeparator

    PersistenceSupport(Path projectPath, String lineSeparator) {
        this.projectPath = projectPath
        this.lineSeparator = lineSeparator
    }

    /**
     * Sets the required values in the .ipsproject file and
     * the maven dependencies to the pom.xml if persistence support is enabled.
     * Otherwise, removes the templates from these files.
     *
     * @param isPersistenceSupport Is true whether persistence support is enabled, else false
     */
    void setPersistenceSupport(String isPersistenceSupport) {
        File pom = new File(projectPath.resolve(POM).toString())
        File ipsProject = new File(projectPath.resolve(IPS_PROJECT).toString())
        File manifest = new File(projectPath.resolve(MANIFEST).toString())
        if (Boolean.valueOf(isPersistenceSupport)) {
            PersistenceApi persistenceApi = getPersistenceApi()
            
            // Adjusting the .ipsproject
            String newName = ipsProject.text.replace(IPS_PROJECT_TEMPLATE, persistenceApi.name)
            ipsProject.text = newName
            
            // Adjusting the mainfest
            String newNameInManifest = manifest.text.replace(IPS_PROJECT_TEMPLATE, persistenceApi.name)
            manifest.text = newNameInManifest

            // Adjusting the maven dependencies
            String newDependency = pom.text.replace(MAVEN_DEPENDENCY_TEMPLATE, persistenceApi.dependency)
            pom.text = newDependency
        } else {
            // Has to be set in order to have a valid .ipsproject
            String newName = ipsProject.text.replace(IPS_PROJECT_TEMPLATE, GENERIC_JPA_21)
            ipsProject.text = newName
            String newNameInManifest = manifest.text.replace(IPS_PROJECT_TEMPLATE, GENERIC_JPA_21)
            manifest.text = newNameInManifest
            String newDependency = pom.text.replace(MAVEN_DEPENDENCY_TEMPLATE, "")
            pom.text = newDependency
        }
    }

    /**
     * Asks the user for the required persistence technology.
     *
     * @return the matching persistence API
     */
    private PersistenceApi getPersistenceApi() {
        String possibilities = "Select index for choosing persistence support:" +
                "$lineSeparator(1) $ECLIPSE_LINK_25," +
                "$lineSeparator(2) $ECLIPSE_LINK_30," +
                "$lineSeparator(3) $GENERIC_JPA_20," +
                "$lineSeparator(4) $GENERIC_JPA_21," +
                "$lineSeparator(5) $JAKARTA_PERSISTENCE_2_2," +
                "$lineSeparator(6) $JAKARTA_PERSISTENCE_3_0:"

        LogUtil.printSeparator(2)

        PersistenceApi persistenceApi

        while (persistenceApi == null) {
            LogUtil.printMessage(possibilities)
            String index = System.in.newReader().readLine()
            switch (index) {
                case "1":
                    persistenceApi = new PersistenceApi(ECLIPSE_LINK_25, ECLIPSE_LINK_25_DEPENDENCY)
                    break
                case "2":
                    persistenceApi = new PersistenceApi(ECLIPSE_LINK_30, ECLIPSE_LINK_30_DEPENDENCY)
                    break
                case "3":
                    persistenceApi = new PersistenceApi(GENERIC_JPA_20, JPA_20_DEPENDENCY)
                    break
                case "4":
                    persistenceApi = new PersistenceApi(GENERIC_JPA_21, JPA_21_DEPENDENCY)
                    break
                case "5":
                    persistenceApi = new PersistenceApi(JAKARTA_PERSISTENCE_2_2, JAKARTA_22_DEPENDENCY)
                    break
                case "6":
                    persistenceApi = new PersistenceApi(JAKARTA_PERSISTENCE_3_0, JAKARTA_30_DEPENDENCY)
                    break
                default:
                    LogUtil.printErrorMessage("Invalid index, please try again...")
            }
        }

        LogUtil.printInfoMessage("Selected persistence API: " + persistenceApi.getName())
        LogUtil.printSeparator(2)

        return persistenceApi
    }

    /**
     * Contains information about the selected persistence API.
     */
    private class PersistenceApi {

        private final String name
        private final String dependency

        private PersistenceApi(String name, String dependency) {
            this.name = name
            this.dependency = dependency
        }

        private String getName() {
            return name
        }

        private String getDependency() {
            return dependency
        }
    }
}

/**
 * Provides utility functions for logging.
 */
class LogUtil {

    private static final String SEPARATOR = "***************************************************"

    private static final String POSTPROCESSOR_PREFIX = "[Postprocessor] "

    private static final String POSTPROCESSOR_INFO_PREFIX = POSTPROCESSOR_PREFIX + "[INFO] "

    private static final String POSTPROCESSOR_ERROR_PREFIX = POSTPROCESSOR_PREFIX + "[ERROR] "

    /**
     * Formats a message and prints it to the console.
     */
    static void printMessage(String message) {
        println(POSTPROCESSOR_PREFIX + message)
    }

    /**
     * Formats an info message and prints it to the console.
     */
    static void printInfoMessage(String message) {
        println(POSTPROCESSOR_INFO_PREFIX + message)
    }

    /**
     * Formats an error message and prints it to the console.
     */
    static void printErrorMessage(String message) {
        println(POSTPROCESSOR_ERROR_PREFIX + message)
    }


    /**
     * Prints a defined separator to improve the overview.
     *
     * @param number The number of printed lines containing separators.
     */
    static void printSeparator(int number) {
        for (int i = 0; i < number; i++) {
            println(SEPARATOR)
        }
    }
}

//////////////////////////////////////////////////////////////
// Jaxb support
//////////////////////////////////////////////////////////////
/**
 * Adjusts the maven dependencies and the .ipsproject file
 * in order to add persistence support, if required.
 *
 * This requires another user input.
 */
class JaxbSupport {

    // Names of required files
    private final String POM = "pom.xml"
    private final String IPS_PROJECT = ".ipsproject"
    private final String VERSION = '\${faktorips.version}'

    // Names of possible jaxb support variants
    private final String NONE = "None"
    private final String CLASSIC_JAXB= "ClassicJAXB"
    private final String JAKARTA_XML_BINDING = "JakartaXmlBinding"

    // Templates to be replaced
    private final String IPS_PROJECT_TEMPLATE = '$IPS-JaxbSupport$'
    private final String MAVEN_DEPENDENCY_TEMPLATE = '<!-- $JaxbDependencyTemplate$ -->'

    // Using the system specific line separators (Groovy uses LF only)
    // The indentation fits to the maven dependencies
    private final String CLASSIC_JAXB_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.faktorips</groupId>$lineSeparator\
            <artifactId>faktorips-runtime-javax-xml</artifactId>$lineSeparator\
            <version>$VERSION</version>$lineSeparator\
        </dependency>"""
    private final String JAKARTA_XML_BINDING_DEPENDENCY = """<dependency>$lineSeparator\
            <groupId>org.faktorips</groupId>$lineSeparator\
            <artifactId>faktorips-runtime-jakarta-xml</artifactId>$lineSeparator\
            <version>$VERSION</version>$lineSeparator\
        </dependency>"""

    private final Path projectPath
    private final String lineSeparator

    JaxbSupport(Path projectPath, String lineSeparator) {
        this.projectPath = projectPath
        this.lineSeparator = lineSeparator
    }

    /**
     * Sets the required values in the .ipsproject file and
     * the maven dependencies to the pom.xml if jaxb support is enabled.
     * Otherwise, removes the templates from these files.
     *
     * @param isJaxbSupport Is true whether jaxb support is enabled, else false
     */
    void setJaxbSupport(String isJaxbSupport) {
        File pom = new File(projectPath.resolve(POM).toString())
        File ipsProject = new File(projectPath.resolve(IPS_PROJECT).toString())
        if (Boolean.valueOf(isJaxbSupport)) {
            JaxbApi jaxbApi = getJaxbApi()

            // Adjusting the .ipsproject
            String newName = ipsProject.text.replace(IPS_PROJECT_TEMPLATE, jaxbApi.name)
            ipsProject.text = newName

            // Adjusting the maven dependencies
            String newDependency = pom.text.replace(MAVEN_DEPENDENCY_TEMPLATE, jaxbApi.dependency)
            pom.text = newDependency
        } else {
            // Has to be set in order to have a valid .ipsproject
            String newName = ipsProject.text.replace(IPS_PROJECT_TEMPLATE, NONE)
            ipsProject.text = newName
            String newDependency = pom.text.replace(MAVEN_DEPENDENCY_TEMPLATE, "")
            pom.text = newDependency
        }
    }

    /**
     * Asks the user for the required persistence technology.
     *
     * @return the matching persistence API
     */
    private JaxbApi getJaxbApi() {
        String possibilities = "Select index for choosing jaxb support:" +
                "$lineSeparator(1) $CLASSIC_JAXB," +
                "$lineSeparator(2) $JAKARTA_XML_BINDING:"

        LogUtil.printSeparator(2)

        JaxbApi jaxbApi

        while (jaxbApi == null) {
            LogUtil.printMessage(possibilities)
            String index = System.in.newReader().readLine()
            switch (index) {
                case "1":
                    jaxbApi = new JaxbApi(CLASSIC_JAXB, CLASSIC_JAXB_DEPENDENCY)
                    break
                case "2":
                    jaxbApi = new JaxbApi(JAKARTA_XML_BINDING, JAKARTA_XML_BINDING_DEPENDENCY)
                    break
                default:
                    LogUtil.printErrorMessage("Invalid index, please try again...")
            }
        }

        LogUtil.printInfoMessage("Selected Jaxb API: " + jaxbApi.getName())
        LogUtil.printSeparator(2)

        return jaxbApi
    }

    /**
     * Contains information about the selected Jaxb API.
     */
    private class JaxbApi {

        private final String name
        private final String dependency

        private JaxbApi(String name, String dependency) {
            this.name = name
            this.dependency = dependency
        }

        private String getName() {
            return name
        }

        private String getDependency() {
            return dependency
        }
    }
}

//////////////////////////////////////////////////////////////
// Ips Object Path Util
//////////////////////////////////////////////////////////////
/**
 * Sets the attribute outputFolderMergableSources based on the project type
 */
class IpsObjectPathUtil {

    private final String OUTPUT_FOLDER_MERGABLE_SOURCES = '$OutputFolderMergableSources$'
    private final String IPS_PROJECT = ".ipsproject"
    private final String srcMainJavaPath = "src/main/java"
    private final String isModelProject
    private final String isProductDefinitionProject
    private final Path projectPath
    private final File srcMainJavaDirectory
    private final File packageDirectory

    IpsObjectPathUtil(Path projectPath, String isModelProject, String isProductDefinitionProject, String packageName) {
        this.projectPath = projectPath
        this.isModelProject = isModelProject
        this.isProductDefinitionProject = isProductDefinitionProject
        this.srcMainJavaDirectory = new File(projectPath.resolve(srcMainJavaPath).toString())
        this.packageDirectory = new File(projectPath.resolve(srcMainJavaPath).resolve(packageName).toString())
    }

    /**
     * Sets the outputFolderMergableSources attribute value in the .ipsproject.
     */
    void setOutputFolderMergableSources() {
        File ipsProject = new File(projectPath.resolve(IPS_PROJECT).toString())
        if (Boolean.valueOf(isModelProject)) {
            String newValue = ipsProject.text.replace(OUTPUT_FOLDER_MERGABLE_SOURCES, srcMainJavaPath)
            ipsProject.text = newValue
        } else if(Boolean.valueOf(isProductDefinitionProject)) {
            packageDirectory.delete()
            srcMainJavaDirectory.delete()
            String newValue = ipsProject.text.replace(OUTPUT_FOLDER_MERGABLE_SOURCES, "")
            ipsProject.text = newValue
        }
    }
}

