/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.util.ManifestElement;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.util.StringBuilderJoiner;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.osgi.framework.BundleException;

/**
 * The {@link IpsBundleManifest} is able to read all necessary information to handle the source
 * entries of an {@link IpsObjectPath}. This includes the base package, the output folders and the
 * package fragment roots. To get an {@link IpsObjectPath} use the
 * {@link IpsObjectPathManifestReader} and give him an instance of this class.
 * <p>
 * An {@link IpsBundleManifest} could be used in normal {@link IIpsProject projects} as well as in
 * {@link IpsBundleEntry packed bundles}.
 * <p>
 * Following there is an example of a simple MANIFEST.MF (only Faktor-IPS relevant part):
 * <p>
 * <code>
 * Fips-Base-Package: org.test.basepackage<br>
 * Fips-Sourcecode-Output: src<br>
 * Fips-Resource-Output: resource<br>
 * Fips-Object-Dir: model;toc="faktorips-toc.xml";validation-messages="validation-messages"
 * </code>
 * <p>
 * This is the easiest way to configure a single model folder called 'model' with the output folders
 * 'src' and 'resource'. If you have multiple model folders you could simply add them to the
 * Attribute <em>Fips-Object-Dir</em>. If you need to specify other output folders for different
 * model folders you could overwrite the settings from the main attribute with an additional
 * attribute section with the name of the model folder.
 * <p>
 * <code>
 * Fips-Base-Package: org.test.basepackage<br>
 * Fips-Sourcecode-Output: src<br>
 * Fips-Resource-Output: resource<br>
 * Fips-Object-Dir: model;toc="faktorips-toc.xml";validation-messages="validation-messages",
 *    test;toc="fips-toc-test.xml";validation-messages="validation-messages-test.properties"<br>
 * <br>
 * Name: test<br>
 * Fips-Sourcecode-Output: testSrc<br>
 * Fips-Resource-Output: testResource
 * </code>
 * <p>
 * In this example there are two model folders: 'model' and 'test'. The second one has its output
 * folders configured to 'testSrc' and 'testResource' while the first one uses the default
 * configuration 'src' and 'resource'
 * <p>
 * In contrast to the configuration via {@link IIpsProjectProperties} there is no ability to
 * configure different base packages for different output directories.
 *
 * @see IpsObjectPathManifestReader
 *
 * @author dirmeier
 */
public class IpsBundleManifest {

    public static final String MANIFEST_NAME = JarFile.MANIFEST_NAME;

    public static final String HEADER_BASE_PACKAGE = "Fips-BasePackage"; //$NON-NLS-1$

    public static final String HEADER_SRC_OUT = "Fips-SourcecodeOutput"; //$NON-NLS-1$

    public static final String HEADER_RESOURCE_OUT = "Fips-ResourceOutput"; //$NON-NLS-1$

    public static final String HEADER_OBJECT_DIR = "Fips-ObjectDir"; //$NON-NLS-1$

    public static final String HEADER_UNIQUE_QUALIFIER = "Fips-UniqueQualifier"; //$NON-NLS-1$

    public static final String HEADER_GENERATOR_CONFIG = "Fips-GeneratorConfig"; //$NON-NLS-1$

    public static final String HEADER_RUNTIME_ID_PREFIX = "Fips-RuntimeIdPrefix"; //$NON-NLS-1$

    public static final String ATTRIBUTE_TOC = "toc"; //$NON-NLS-1$

    public static final String ATTRIBUTE_VALIDATION_MESSAGES = "validation-messages"; //$NON-NLS-1$

    private final Manifest manifest;

    /**
     * The constructor of the {@link IpsBundleManifest} getting the Manifest to read the attributes
     * from.
     */
    public IpsBundleManifest(Manifest manifest) {
        ArgumentCheck.notNull(manifest);
        this.manifest = manifest;
    }

    String getBasePackage() {
        Attributes attributes = manifest.getMainAttributes();
        return getValue(attributes, HEADER_BASE_PACKAGE);
    }

    /**
     * Returning the base package for the specified objectDir. If there is no special setting for
     * the specified objectDir the default base package is returned.
     * <p>
     * Note that there is no possibility to configure different base packages for the mergeable and
     * derived artifacts.
     *
     * @param objectDir The name of the model folder for which you want to know the base package
     *
     * @return The base package configured for the given objectDir.
     */
    public String getBasePackage(String objectDir) {
        Attributes attributes = manifest.getAttributes(objectDir);
        if (attributes != null) {
            String result = getValue(attributes, HEADER_BASE_PACKAGE);
            if (result != null) {
                return result;
            }
        }
        return getBasePackage();
    }

    String getUniqueQualifier() {
        Attributes attributes = manifest.getMainAttributes();
        return getValue(attributes, HEADER_UNIQUE_QUALIFIER);
    }

    public String getUniqueQualifier(String objectDir) {
        Attributes attributes = manifest.getAttributes(objectDir);
        if (attributes != null) {
            String result = getValue(attributes, HEADER_UNIQUE_QUALIFIER);
            if (result != null) {
                return result;
            }
        }
        return getUniqueQualifier();
    }

    String getSourcecodeOutput() {
        Attributes attributes = manifest.getMainAttributes();
        return getValue(attributes, HEADER_SRC_OUT);
    }

    protected String getSourcecodeOutput(String objectDir) {
        Attributes attributes = manifest.getAttributes(objectDir);
        if (attributes != null) {
            String result = getValue(attributes, HEADER_SRC_OUT);
            if (result != null) {
                return result;
            }
        }
        return getSourcecodeOutput();
    }

    protected String getResourceOutput() {
        Attributes attributes = manifest.getMainAttributes();
        return getValue(attributes, HEADER_RESOURCE_OUT);
    }

    protected String getResourceOutput(String objectDir) {
        Attributes attributes = manifest.getAttributes(objectDir);
        if (attributes != null) {
            String result = getValue(attributes, HEADER_RESOURCE_OUT);
            if (result != null) {
                return result;
            }
        }
        return getResourceOutput();
    }

    /**
     * Returns a list of all objectDirs (folders containing {@link IIpsElement IPS elements}). The
     * path is relative to the root folder (project or archive).
     *
     * @return A list of all configured objectDirs
     */
    public List<Path> getObjectDirs() {
        ArrayList<Path> result = new ArrayList<>();
        ManifestElement[] objectDirElements = getObjectDirElements();
        for (ManifestElement manifestElement : objectDirElements) {
            result.add(Path.of(manifestElement.getValue()));
        }
        return result;
    }

    public ManifestElement[] getObjectDirElements() {
        Attributes attributes = manifest.getMainAttributes();
        if (attributes == null) {
            return new ManifestElement[0];
        }
        String value = getValue(attributes, HEADER_OBJECT_DIR);
        return getManifestElements(value);
    }

    /**
     * Checks if this {@link IpsBundleManifest} has objectDirs. If it has objectDirs it returns
     * <code>true</code> and if not <code>false</code> is returned.
     *
     * @see #getObjectDirs()
     */
    public boolean hasObjectDirs() {
        return !getObjectDirs().isEmpty();
    }

    private ManifestElement[] getManifestElements(String value) {
        try {
            ManifestElement[] result = ManifestElement.parseHeader(HEADER_OBJECT_DIR, value);
            if (result == null) {
                return new ManifestElement[0];
            } else {
                return result;
            }
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTocPath(ManifestElement objectDir) {
        return objectDir.getAttribute(ATTRIBUTE_TOC);
    }

    String getValidationMessagesBundle(ManifestElement objectDir) {
        return objectDir.getAttribute(ATTRIBUTE_VALIDATION_MESSAGES);
    }

    private String getValue(Attributes attributes, String name) {
        return StringUtils.trim(attributes.getValue(name));
    }

    public Map<String, String> getGeneratorConfig(String builderSetId) {
        Map<String, String> generatorConfig = new HashMap<>();
        Attributes attributes = manifest.getMainAttributes();
        if (attributes != null) {
            String generatorConfigString = getValue(attributes, HEADER_GENERATOR_CONFIG);
            ManifestElement[] manifestElements = getManifestElements(generatorConfigString);
            if (manifestElements != null) {
                for (ManifestElement manifestElement : manifestElements) {
                    if (manifestElement.getValue().toLowerCase().contains(builderSetId.toLowerCase())) {
                        Enumeration<String> keys = manifestElement.getKeys();
                        while (keys.hasMoreElements()) {
                            String key = keys.nextElement();
                            String value = manifestElement.getAttribute(key);
                            generatorConfig.put(key, value);
                        }
                    }
                }
            }

        }
        return generatorConfig;
    }

    /** {@return the runtime ID prefix used in the project} */
    public String getRuntimeIdPrefix() {
        Attributes attributes = manifest.getMainAttributes();
        return getValue(attributes, HEADER_RUNTIME_ID_PREFIX);
    }

    /**
     * Writes the given Faktor-IPS project's generator settings to this manifest, overwriting any
     * old values.
     *
     * @param ipsProject the Faktor-IPS project this manifest belongs to
     * @param manifestFile the location of the manifest
     */
    public void writeBuilderSettings(IIpsProject ipsProject, AFile manifestFile) {
        Attributes attributes = manifest.getMainAttributes();

        writeBasicManifestHeaders(attributes);
        writeIpsProjectSettings(ipsProject, attributes);
        writeGeneratorConfiguration(ipsProject, attributes);
        saveManifestToFile(manifestFile);
    }

    private void writeBasicManifestHeaders(Attributes attributes) {
        if (!attributes.containsKey(Name.MANIFEST_VERSION)) {
            attributes.put(Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
        }
    }

    private void writeIpsProjectSettings(IIpsProject ipsProject, Attributes attributes) {
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        if (ipsObjectPath == null) {
            return;
        }

        writeBasePackageSettings(ipsObjectPath, attributes);
        writeOutputFolderSettings(ipsObjectPath, attributes);
        writeObjectDirectorySettings(ipsObjectPath, attributes);
    }

    private void writeGeneratorConfiguration(IIpsProject ipsProject, Attributes attributes) {
        String builderSetId = ipsProject.getIpsArtefactBuilderSet().getId();
        String delimiter = ";"; //$NON-NLS-1$
        StringBuilder sb = new StringBuilder(builderSetId);
        sb.append(delimiter);

        IIpsArtefactBuilderSetConfig config = ipsProject.getIpsArtefactBuilderSet().getConfig();
        Map<String, Object> properties = new TreeMap<>(
                Arrays.stream(config.getPropertyNames())
                        .map(p -> new Object() {
                            private final String key = p;
                            private final Object value = config.getPropertyValue(p);
                        })
                        .filter(p -> p.value != null)
                        .collect(Collectors.toMap(p -> p.key, p -> p.value)));

        StringBuilderJoiner.join(sb, properties.entrySet(), delimiter, p -> {
            sb.append(p.getKey());
            sb.append('=');
            sb.append('"');
            String value = Objects.toString(p.getValue());
            sb.append(value.replace("\"", "\\\"")); //$NON-NLS-1$//$NON-NLS-2$
            sb.append('"');
        });

        attributes.put(new Name(HEADER_GENERATOR_CONFIG), sb.toString());
        attributes.put(new Name(HEADER_RUNTIME_ID_PREFIX), ipsProject.getRuntimeIdPrefix());
    }

    private void writeBasePackageSettings(IIpsObjectPath ipsObjectPath, Attributes attributes) {
        String basePackage = ipsObjectPath.getBasePackageNameForMergableJavaClasses();
        if (basePackage != null) {
            attributes.putValue(HEADER_BASE_PACKAGE, basePackage);
        }
    }

    private void writeBasePackageSettings(IIpsSrcFolderEntry entry, Attributes attributes) {
        String basePackage = entry.getBasePackageNameForMergableJavaClasses();
        if (basePackage != null) {
            attributes.putValue(HEADER_BASE_PACKAGE, basePackage);
        }
    }

    private void writeOutputFolderSettings(IIpsObjectPath ipsObjectPath, Attributes attributes) {

        if (ipsObjectPath.isOutputDefinedPerSrcFolder()) {
            outputDefinedPerSourceFolder(ipsObjectPath, attributes);
        } else {
            writeOutputFolder(ipsObjectPath, attributes);
        }
    }

    private void outputDefinedPerSourceFolder(IIpsObjectPath ipsObjectPath, Attributes attributes) {

        IIpsSrcFolderEntry[] sourceFolderEntries = ipsObjectPath.getSourceFolderEntries();
        if (sourceFolderEntries != null && sourceFolderEntries.length > 0) {
            IIpsSrcFolderEntry firstEntry = sourceFolderEntries[0];
            if (firstEntry != null) {
                AFolder mergableFolder = firstEntry.getOutputFolderForMergableJavaFiles();
                if (mergableFolder != null && mergableFolder.getProjectRelativePath() != null) {
                    attributes.putValue(HEADER_SRC_OUT,
                            PathUtil.toPortableString(mergableFolder.getProjectRelativePath()));
                }

                AFolder derivedFolder = firstEntry.getOutputFolderForDerivedJavaFiles();
                if (derivedFolder != null && derivedFolder.getProjectRelativePath() != null) {
                    attributes.putValue(HEADER_RESOURCE_OUT,
                            PathUtil.toPortableString(derivedFolder.getProjectRelativePath()));
                }
            }
        }
    }

    private void writeSourceFolderSpecificSettings(IIpsSrcFolderEntry[] srcFolderEntries) {
        List<String> oldKeys = manifest.getEntries().entrySet().stream()
                .filter(e -> e.getValue().containsKey(new Name(HEADER_BASE_PACKAGE))).map(Entry::getKey).toList();
        for (String oldKey : oldKeys) {
            manifest.getEntries().remove(oldKey);
        }
        for (IIpsSrcFolderEntry entry : srcFolderEntries) {
            if (entry != null && entry.getSourceFolder() != null) {
                String folderName = entry.getSourceFolder().getProjectRelativePath().toString();
                Attributes folderAttributes = new Attributes();

                manifest.getEntries().put(folderName, folderAttributes);
                writeBasePackageSettings(entry, folderAttributes);
                writeOutputFolder(entry, folderAttributes);

            }
        }
    }

    private void writeOutputFolder(IIpsSrcFolderEntry entry, Attributes attributes) {
        writeOutputFoldersToAttributes(
                entry.getOutputFolderForMergableJavaFiles(),
                entry.getOutputFolderForDerivedJavaFiles(),
                attributes);
    }

    private void writeOutputFolder(IIpsObjectPath ipsObjectPath, Attributes attributes) {
        writeOutputFoldersToAttributes(
                ipsObjectPath.getOutputFolderForMergableSources(),
                ipsObjectPath.getOutputFolderForDerivedSources(),
                attributes);
    }

    private void writeOutputFoldersToAttributes(AFolder mergableFolder, AFolder derivedFolder, Attributes attributes) {

        if (mergableFolder != null && mergableFolder.getProjectRelativePath() != null) {
            attributes.putValue(HEADER_SRC_OUT,
                    PathUtil.toPortableString(mergableFolder.getProjectRelativePath()));
        }

        if (derivedFolder != null && derivedFolder.getProjectRelativePath() != null) {
            attributes.putValue(HEADER_RESOURCE_OUT,
                    PathUtil.toPortableString(derivedFolder.getProjectRelativePath()));
        }
    }

    private void writeObjectDirectorySettings(IIpsObjectPath ipsObjectPath, Attributes attributes) {
        IIpsSrcFolderEntry[] sourceFolderEntries = ipsObjectPath.getSourceFolderEntries();
        if (sourceFolderEntries == null || sourceFolderEntries.length == 0) {
            return;
        }

        StringBuilder objectDirAttributeBuilder = new StringBuilder();
        for (int i = 0; i < sourceFolderEntries.length; i++) {
            IIpsSrcFolderEntry entry = sourceFolderEntries[i];
            if (entry != null && entry.getSourceFolder() != null) {
                if (i > 0) {
                    objectDirAttributeBuilder.append(",");
                }
                appendObjectDirEntry(objectDirAttributeBuilder, entry);
            }
        }

        if (objectDirAttributeBuilder.length() > 0) {
            attributes.putValue(HEADER_OBJECT_DIR, objectDirAttributeBuilder.toString());
        }

        if (ipsObjectPath.isOutputDefinedPerSrcFolder()) {
            writeSourceFolderSpecificSettings(sourceFolderEntries);
        }
    }

    private void appendObjectDirEntry(StringBuilder builder, IIpsSrcFolderEntry entry) {
        String tocPath = entry.getBasePackageRelativeTocPath();
        tocPath = tocPath != null ? tocPath : "faktorips-repository-toc.xml";

        builder.append(entry.getSourceFolder().getProjectRelativePath().toString());
        builder.append(";toc=\"").append(tocPath).append("\"");
        builder.append(";validation-messages=\"").append(entry.getValidationMessagesBundle()).append("\"");
    }

    private void saveManifestToFile(AFile manifestFile) {
        File actualManifestFile = manifestFile.getLocation().toFile();
        try (FileOutputStream outputStream = new FileOutputStream(actualManifestFile)) {
            manifest.write(outputStream);
            AProject project = manifestFile.getProject();
            if (project != null) {
                String lineSeparator = project.getDefaultLineSeparator();
                String content = getContentAsString(manifestFile.getContents());
                content = content.replaceAll("\\r?\\n", lineSeparator);
                content = content.replace('\\', '/');
                manifestFile.setContents(
                        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), true,
                        null);
            }
        } catch (IOException e) {
            throw new IpsException(new IpsStatus("Can't write " + actualManifestFile, e)); //$NON-NLS-1$
        }
    }

    private String getContentAsString(InputStream is) {
        try {
            return StringUtil.readFromInputStream(is, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

}
