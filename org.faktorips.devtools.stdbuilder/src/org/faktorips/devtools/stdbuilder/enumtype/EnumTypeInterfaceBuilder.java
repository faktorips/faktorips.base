/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Builder that generates the published interface java source for <code>EnumType</code> ips objects.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeInterfaceBuilder extends DefaultJavaSourceFileBuilder {

    /** The package id identifiying the builder */
    public final static String PACKAGE_STRUCTURE_KIND_ID = "EnumTypeInterfaceBuilder.enumtype.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    /**
     * Creates a new <code>EnumTypeInterfaceBuilder</code> that will belong to the given ips
     * artefact builder set.
     * 
     * @param builderSet The ips artefact builder set this builder shall be a part of.
     */
    public EnumTypeInterfaceBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, PACKAGE_STRUCTURE_KIND_ID, new LocalizedStringsSet(EnumTypeInterfaceBuilder.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForJavatype() throws CoreException {
        generateMainTypeSection();
    }

    // Generates the code for the main type section
    private void generateMainTypeSection() {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setUnqualifiedName(getEnumType().getName());
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setClass(true);
    }

    // Returns the enum type for that code is being generated
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

}
