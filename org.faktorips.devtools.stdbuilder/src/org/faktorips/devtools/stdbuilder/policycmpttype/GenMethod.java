/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for method for policy component types.
 * 
 * @author Peter Erzberger
 */
public class GenMethod extends GenPolicyCmptTypePart {

    public GenMethod(GenPolicyCmptType genPolicyCmptType, IMethod method, LocalizedStringsSet stringsSet) throws CoreException {
        super(genPolicyCmptType, method, stringsSet);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface) throws CoreException {
        //nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface) throws CoreException {
        //nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface) throws CoreException {

        IMethod method = getMethod();
        try {
            Datatype returnType = ipsProject.findDatatype(method.getDatatype());
            IParameter[] params = method.getParameters();
            Datatype[] paramDatatypes = new Datatype[params.length];
            for (int j = 0; j < paramDatatypes.length; j++) {
                paramDatatypes[j] = ipsProject.findDatatype(params[j].getDatatype());
            }
            if(!generatesInterface){
                generateClassCodeForMethodDefinedInModel(method, returnType, paramDatatypes, builder);
            }
            if(generatesInterface){
                generateInterfaceCodeForMethodDefinedInModelInterface(method, returnType, paramDatatypes, builder);  
            }
            
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(IStatus.ERROR,
                    "Error building method " + method.getName() + " of " //$NON-NLS-1$ //$NON-NLS-2$
                            + getGenPolicyCmptType().getQualifiedName(generatesInterface), e));
        }
    }

    public IMethod getMethod(){
        return (IMethod)getIpsPart();
    }
    
    protected void generateClassCodeForMethodDefinedInModel(IMethod method, Datatype returnType, Datatype[] paramTypes, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (method.getModifier()==org.faktorips.devtools.core.model.ipsobject.Modifier.PUBLISHED) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        }
        generateSignatureForMethodDefinedInModel(method, method.getJavaModifier(),
                returnType, paramTypes, methodsBuilder);
        if (method.isAbstract()) {
            methodsBuilder.appendln(";");
            return;
        }
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement model method.");
        methodsBuilder.append("throw new RuntimeException(\"Not implemented yet!\");");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateInterfaceCodeForMethodDefinedInModelInterface(
            IMethod method,
            Datatype returnType,
            Datatype[] paramTypes,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (method.getModifier() != Modifier.PUBLISHED) {
            return;
        }
        methodsBuilder.javaDoc(method.getDescription(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureForMethodDefinedInModel(method, java.lang.reflect.Modifier.PUBLIC, returnType, paramTypes, methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code samples:
     * <pre>
     * public void calculatePremium(IPolicy policy)
     * public ICoverage getCoverageWithHighestSumInsured()
     * </pre>
     */
    public void generateSignatureForMethodDefinedInModel(
        IMethod method,
        int javaModifier,
        Datatype returnType,
        Datatype[] paramTypes,
        JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String[] paramClassNames = new String[paramTypes.length];
        for (int i = 0; i < paramClassNames.length; i++) {
            if (paramTypes[i] instanceof IPolicyCmptType) {
                paramClassNames[i] = getGenPolicyCmptType().getBuilderSet().getGenerator((IPolicyCmptType)paramTypes[i]).getQualifiedName(true);
            } else {
                paramClassNames[i] = paramTypes[i].getJavaClassName();
            }
        }
        String returnClassName;
        if  (returnType instanceof IPolicyCmptType) {
            returnClassName = getGenPolicyCmptType().getBuilderSet().getGenerator((IPolicyCmptType)returnType).getQualifiedName(true);
        } else {
            returnClassName = returnType.getJavaClassName();
        }
        methodsBuilder.signature(javaModifier, returnClassName, method.getName(), 
                method.getParameterNames(), paramClassNames);
    }
    
}
