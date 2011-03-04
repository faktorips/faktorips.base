/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract base class for <tt>IMethod</tt>s for <tt>IProductCmptType</tt>s and
 * <tt>IPolicyCmptType</tt>s.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenMethod extends GenTypePart {

    protected GenMethod(GenType genType, IIpsObjectPartContainer ipsObjectPartContainer, LocalizedStringsSet stringsSet) {
        super(genType, ipsObjectPartContainer, stringsSet);
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        // Nothing to do.
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        // Nothing to do.
    }

    public final IMethod getMethod() {
        return (IMethod)getIpsPart();
    }

    public final boolean isPublished() {
        return getMethod().getModifier().isPublished();
    }

    protected final Datatype[] getParameterDatatypes() {
        try {
            IParameter[] parameters = getMethod().getParameters();
            Datatype[] parameterDatatypes = new Datatype[parameters.length];
            for (int j = 0; j < parameterDatatypes.length; j++) {
                parameterDatatypes[j] = getIpsProject().findDatatype(parameters[j].getDatatype());
            }
            return parameterDatatypes;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final String[] getParameterClassNames(Datatype[] parameterDatatypes) {
        try {
            String[] parameterClassNames = new String[parameterDatatypes.length];
            for (int i = 0; i < parameterClassNames.length; i++) {
                parameterClassNames[i] = getGenType().getBuilderSet().getJavaClassName(parameterDatatypes[i]);
            }
            return parameterClassNames;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void addMethodToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        Datatype[] parameterDatatypes = getParameterDatatypes();
        String[] parameterTypeSignatures = new String[parameterDatatypes.length];
        for (int i = 0; i < parameterTypeSignatures.length; i++) {
            parameterTypeSignatures[i] = StdBuilderHelper.transformDatatypeToJdtTypeSignature(parameterDatatypes[i],
                    getMethod().getModifier().isPublished());
        }
        org.eclipse.jdt.core.IMethod method = generatedJavaType.getMethod(getMethod().getName(),
                parameterTypeSignatures);
        javaElements.add(method);
    }

}
