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

package org.faktorips.devtools.stdbuilder.type;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class for <tt>IIpsObjectPart</tt>s.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenTypePart extends DefaultJavaGeneratorForIpsPart {

    private GenType genType;

    public GenTypePart(GenType genType, IIpsObjectPartContainer ipsObjectPartContainer, LocalizedStringsSet stringsSet)
            throws CoreException {

        super(ipsObjectPartContainer, stringsSet);
        ArgumentCheck.notNull(genType, this);
        this.genType = genType;
    }

    public GenType getGenType() {
        return genType;
    }

    protected IIpsProject getIpsProject() {
        return genType.getType().getIpsProject();
    }

    @Override
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return genType.getLanguageUsedInGeneratedSourceCode();
    }

    protected boolean isUseTypesafeCollections() {
        return genType.getBuilderSet().isUseTypesafeCollections();
    }

    protected boolean isGenerateJaxbSupport() {
        return genType.getBuilderSet().isGenerateJaxbSupport();
    }

    public String getQualifiedClassName(IPolicyCmptType target, boolean forInterface) throws CoreException {
        return genType.getBuilderSet().getGenerator(target).getQualifiedName(forInterface);
    }

    public String getUnqualifiedClassName(IPolicyCmptType target, boolean forInterface) throws CoreException {
        return genType.getBuilderSet().getGenerator(target).getUnqualifiedClassName(forInterface);
    }

    protected final String getJavaParameterTypeSignature(Datatype datatype) {
        char typeIdentifier = 'Q';
        if (datatype.isVoid()) {
            typeIdentifier = 'V';
        } else if (datatype.isPrimitive()) {
            if (datatype.equals(Datatype.PRIMITIVE_INT)) {
                typeIdentifier = 'I';
            } else if (datatype.equals(Datatype.PRIMITIVE_BOOLEAN)) {
                typeIdentifier = 'Z';
            } else if (datatype.equals(Datatype.PRIMITIVE_LONG)) {
                typeIdentifier = 'J';
            }
        }

        return typeIdentifier + datatype.getName() + ";";
    }

}
