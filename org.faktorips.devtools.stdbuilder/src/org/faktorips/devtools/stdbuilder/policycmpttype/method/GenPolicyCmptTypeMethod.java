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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.type.GenMethod;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for <tt>IMethod</tt>s used for <tt>IPolicyCmptType</tt>s.
 * 
 * @author Peter Erzberger
 */
public class GenPolicyCmptTypeMethod extends GenMethod {

    public GenPolicyCmptTypeMethod(GenPolicyCmptType genPolicyCmptType, IMethod method) throws CoreException {
        super(genPolicyCmptType, method, new LocalizedStringsSet(GenPolicyCmptTypeMethod.class));
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        try {
            Datatype returnType = ipsProject.findDatatype(getMethod().getDatatype());
            if (!generatesInterface) {
                generateClassCodeForMethodDefinedInModel(getMethod(), returnType, builder);
            }
            if (generatesInterface) {
                generateInterfaceCodeForMethodDefinedInModelInterface(getMethod(), returnType, builder);
            }

        } catch (CoreException e) {
            throw new CoreException(new IpsStatus(IStatus.ERROR,
                    "Error building method " + getMethod().getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                            + getGenType().getQualifiedName(generatesInterface), e));
        }
    }

    private void generateClassCodeForMethodDefinedInModel(IMethod method,
            Datatype returnType,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (isPublished()) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        } else {
            methodsBuilder.javaDoc(getDescriptionInGeneratorLanguage(method),
                    JavaSourceFileBuilder.ANNOTATION_GENERATED);
        }

        IMethod overiddenMethod = method.findOverriddenMethod(getIpsPart().getIpsProject());
        if (isPublished()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), overiddenMethod == null);
        } else if (method.getModifier().isPublic() && overiddenMethod != null) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), false);
        }
        generateSignatureForMethodDefinedInModel(method, method.getJavaModifier(), returnType, methodsBuilder);
        if (method.isAbstract()) {
            methodsBuilder.appendln(";");
            return;
        }
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement model method.");
        methodsBuilder.append("throw new RuntimeException(\"Not implemented yet!\");");
        methodsBuilder.closeBracket();
    }

    private void generateInterfaceCodeForMethodDefinedInModelInterface(IMethod method,
            Datatype returnType,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (!(isPublished())) {
            return;
        }
        methodsBuilder.javaDoc(getDescriptionInGeneratorLanguage(method), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, methodsBuilder);
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
    private void generateSignatureForMethodDefinedInModel(IMethod method,
            int javaModifier,
            Datatype returnType,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        StandardBuilderSet builderset = getGenType().getBuilderSet();
        String returnClassName = builderset.getJavaClassName(returnType);
        methodsBuilder.signature(javaModifier, returnClassName, getMethod().getName(), method.getParameterNames(),
                getParameterClassNames(getParameterDatatypes()));
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        if (isPublished()) {
            addMethodToGeneratedJavaElements(javaElements, generatedJavaType);
        }
    }

}
