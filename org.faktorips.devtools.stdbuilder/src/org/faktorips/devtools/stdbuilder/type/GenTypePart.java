/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.type;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class for <tt>IIpsObjectPart</tt>s.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenTypePart extends DefaultJavaGeneratorForIpsPart {

    private final GenType genType;

    public GenTypePart(GenType genType, IIpsObjectPartContainer ipsObjectPartContainer, LocalizedStringsSet stringsSet) {
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

    public String getQualifiedClassName(IPolicyCmptType target, boolean forInterface) throws CoreException {
        return genType.getBuilderSet().getGenerator(target).getQualifiedName(forInterface);
    }

    public String getUnqualifiedClassName(IPolicyCmptType target, boolean forInterface) throws CoreException {
        return genType.getBuilderSet().getGenerator(target).getUnqualifiedClassName(forInterface);
    }

    protected StandardBuilderSet getBuilderSet() {
        return genType.getBuilderSet();
    }

    protected final void addFieldToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType,
            String fieldName) {

        IField field = generatedJavaType.getField(fieldName);
        javaElements.add(field);
    }

    protected final void addMethodToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType,
            String methodName,
            String... parameterTypeSignatures) {

        IMethod method = generatedJavaType.getMethod(methodName, parameterTypeSignatures);
        javaElements.add(method);
    }

}
