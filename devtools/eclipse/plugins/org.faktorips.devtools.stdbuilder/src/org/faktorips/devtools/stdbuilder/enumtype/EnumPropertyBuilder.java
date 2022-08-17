/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.IoUtil;

/**
 * This builder generates property files for every language and every enum type that contains
 * multilingual attributes. The property file contains one property for every internationalized enum
 * attribute value. The key of the property will be {@code <name of attribute>_<id of value>}.
 * 
 * @author dirmeier
 */
public class EnumPropertyBuilder extends AbstractArtefactBuilder {

    private IEnumType enumType;

    public EnumPropertyBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(EnumPropertyBuilder.class));
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public String getName() {
        return "EnumPropertyBuilder";
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof IEnumType) {
            IEnumType foundEnumType = (IEnumType)ipsObject;
            enumType = foundEnumType;
            if (foundEnumType.containsValues()) {
                generatePropertyFilesFor();
            }
        }
    }

    void generatePropertyFilesFor() {
        Set<ISupportedLanguage> supportedLanguages = getIpsProject().getReadOnlyProperties().getSupportedLanguages();
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType,
                    supportedLanguage.getLocale());
            generatePropertyFile(enumPropertyGenerator);
        }
    }

    void generatePropertyFile(EnumPropertyGenerator enumPropertyGenerator) {
        loadFromFile(enumPropertyGenerator);
        boolean generatedSomething = enumPropertyGenerator.generatePropertyFile();
        if (generatedSomething) {
            writeToFile(enumPropertyGenerator);
        }
    }

    private void loadFromFile(EnumPropertyGenerator enumPropertyGenerator) {
        AFile file = getPropertyFile(enumType.getIpsSrcFile(), enumPropertyGenerator.getLocale());
        if (file != null && file.exists()) {
            InputStream contents = null;
            try {
                contents = file.getContents();
                enumPropertyGenerator.readFromStream(contents);
            } finally {
                IoUtil.close(contents);
            }
        }
    }

    private void writeToFile(EnumPropertyGenerator enumPropertyGenerator) {
        AFile file = getPropertyFile(enumType.getIpsSrcFile(), enumPropertyGenerator.getLocale());
        if (file != null) {
            createFolderIfNotThere((AFolder)file.getParent());
            createFileIfNotThere(file);
            InputStream inputStream = enumPropertyGenerator.getStream();
            writeToFile(file, inputStream, true, true);
        }
    }

    /**
     * This method returns the property file in which we want to write the messages for the given
     * locale. The property file is located in derived @see {@link #buildsDerivedArtefacts()} but
     * uses the base package of mergeable sources to get the same qualified name as the generated
     * enum java class.
     * 
     * @param ipsSrcFile represents the file for which the property file will be returned
     * @param locale indicates the language of the property file
     */
    AFile getPropertyFile(IIpsSrcFile ipsSrcFile, Locale locale) {
        if (ipsSrcFile != null) {
            AFolder artefactDestination = (AFolder)getArtefactDestination(ipsSrcFile).getResource();
            Path relativeJavaFile = getBuilderSet().getEnumTypeBuilder().getRelativeJavaFile(ipsSrcFile);
            Path javaFile = relativeJavaFile.getFileName();
            String javaFileName = javaFile == null ? IpsStringUtils.EMPTY : javaFile.toString();
            int indexOfFileExtension = javaFileName.lastIndexOf('.');
            String baseFileName = indexOfFileExtension > 0 ? javaFileName.substring(0, indexOfFileExtension)
                    : javaFileName;
            Path folder = relativeJavaFile.getParent();
            String filename = baseFileName + "_" + locale.getLanguage() + ".properties";
            return artefactDestination.getFile(folder == null ? Path.of(filename) : folder.resolve(filename));
        } else {
            return null;
        }
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        IpsObjectType ipsObjectType = ipsSrcFile.getIpsObjectType();
        return IpsObjectType.ENUM_TYPE.equals(ipsObjectType);
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return false;
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) {
        Set<ISupportedLanguage> supportedLanguages = getIpsProject().getReadOnlyProperties().getSupportedLanguages();
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            AFile propertyFile = getPropertyFile(ipsSrcFile, supportedLanguage.getLocale());
            if (propertyFile != null && propertyFile.exists()) {
                propertyFile.delete(null);
            }
        }
    }

}
