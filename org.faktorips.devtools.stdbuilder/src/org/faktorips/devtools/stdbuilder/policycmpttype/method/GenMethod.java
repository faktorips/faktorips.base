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

package org.faktorips.devtools.stdbuilder.policycmpttype.method;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for method for policy component types.
 * 
 * @author Peter Erzberger
 */
public class GenMethod extends GenTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenMethod.class);

    public GenMethod(GenPolicyCmptType genPolicyCmptType, IMethod method) throws CoreException {
        super(genPolicyCmptType, method, LOCALIZED_STRINGS);
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

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        IMethod method = getMethod();
        try {
            Datatype returnType = ipsProject.findDatatype(method.getDatatype());
            IParameter[] params = method.getParameters();
            Datatype[] paramDatatypes = new Datatype[params.length];
            for (int j = 0; j < paramDatatypes.length; j++) {
                paramDatatypes[j] = ipsProject.findDatatype(params[j].getDatatype());
            }
            if (!generatesInterface) {
                generateClassCodeForMethodDefinedInModel(method, returnType, paramDatatypes, builder);
            }
            if (generatesInterface) {
                generateInterfaceCodeForMethodDefinedInModelInterface(method, returnType, paramDatatypes, builder);
            }

        } catch (Exception e) {
            throw new CoreException(new IpsStatus(IStatus.ERROR, "Error building method " + method.getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                    + getGenType().getQualifiedName(generatesInterface), e));
        }
    }

    public IMethod getMethod() {
        return (IMethod)getIpsPart();
    }

    protected void generateClassCodeForMethodDefinedInModel(IMethod method,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (method.getModifier() == org.faktorips.devtools.core.model.ipsobject.Modifier.PUBLISHED) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        }

        IMethod overiddenMethod = method.findOverriddenMethod(getIpsPart().getIpsProject());
        if (method.getModifier().isPublished()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), overiddenMethod == null);
        } else if (method.getModifier().isPublic() && overiddenMethod != null) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), false);
        }
        generateSignatureForMethodDefinedInModel(method, method.getJavaModifier(), returnType, paramTypes,
                methodsBuilder);
        if (method.isAbstract()) {
            methodsBuilder.appendln(";");
            return;
        }
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement model method.");
        methodsBuilder.append("throw new RuntimeException(\"Not implemented yet!\");");
        methodsBuilder.closeBracket();
    }

    protected void generateInterfaceCodeForMethodDefinedInModelInterface(IMethod method,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        methodsBuilder.javaDoc(method.getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, paramTypes,
                methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code samples:
     * 
     * <pre>
     * public void calculatePremium(IPolicy policy)
     * public ICoverage getCoverageWithHighestSumInsured()
     * </pre>
     */
    public void generateSignatureForMethodDefinedInModel(IMethod method,
            int javaModifier,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        StandardBuilderSet builderset = getGenType().getBuilderSet();
        String[] paramClassNames = new String[paramTypes.length];
        for (int i = 0; i < paramClassNames.length; i++) {
            paramClassNames[i] = builderset.getJavaClassName(paramTypes[i]);
        }
        String returnClassName = builderset.getJavaClassName(returnType);
        methodsBuilder.signature(javaModifier, returnClassName, method.getName(), method.getParameterNames(),
                paramClassNames);
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        // TODO AW: Not implemented yet.
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        // TODO AW: Not implemented yet.
    }

}
