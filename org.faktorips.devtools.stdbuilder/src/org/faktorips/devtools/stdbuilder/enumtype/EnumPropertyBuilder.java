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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.IoUtil;
import org.faktorips.util.LocalizedStringsSet;

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
    public void build(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof IEnumType) {
            IEnumType foundEnumType = (IEnumType)ipsObject;
            this.enumType = foundEnumType;
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
        try {
            IFile file = getPropertyFile(enumType.getIpsSrcFile(), enumPropertyGenerator.getLocale());
            if (file != null && file.exists()) {
                InputStream contents = null;
                try {
                    contents = file.getContents();
                    enumPropertyGenerator.readFromStream(contents);
                } finally {
                    IoUtil.close(contents);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void writeToFile(EnumPropertyGenerator enumPropertyGenerator) {
        try {
            IFile file = getPropertyFile(enumType.getIpsSrcFile(), enumPropertyGenerator.getLocale());
            if (file != null) {
                createFolderIfNotThere((IFolder)file.getParent());
                createFileIfNotTher(file);
                InputStream inputStream = enumPropertyGenerator.getStream();
                writeToFile(file, inputStream, true, true);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void createFileIfNotTher(IFile file) throws CoreRuntimeException {
        if (!file.exists()) {
            file.create(new ByteArrayInputStream("".getBytes()), true, null);
            file.setDerived(buildsDerivedArtefacts() && getBuilderSet().isMarkNoneMergableResourcesAsDerived(), null);
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
    IFile getPropertyFile(IIpsSrcFile ipsSrcFile, Locale locale) throws CoreRuntimeException {
        if (ipsSrcFile != null) {
            IFolder artefactDestination = (IFolder)getArtefactDestination(ipsSrcFile).getResource();
            IPath relativeJavaFile = getBuilderSet().getEnumTypeBuilder().getRelativeJavaFile(ipsSrcFile);
            IPath relativePropertyFile = relativeJavaFile.removeFileExtension();
            IPath folder = relativePropertyFile.removeLastSegments(1);
            String filename = relativePropertyFile.lastSegment() + "_" + locale.getLanguage();
            return artefactDestination.getFile(folder.append(filename).addFileExtension("properties"));
        } else {
            return null;
        }
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
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
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        Set<ISupportedLanguage> supportedLanguages = getIpsProject().getReadOnlyProperties().getSupportedLanguages();
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            IFile propertyFile = getPropertyFile(ipsSrcFile, supportedLanguage.getLocale());
            if (propertyFile != null && propertyFile.exists()) {
                propertyFile.delete(true, null);
            }
        }
    }

}
