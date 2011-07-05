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

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.IntegerRange;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class GenAssociation extends GenTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenAssociation.class);

    protected final IPolicyCmptTypeAssociation association;
    protected final IPolicyCmptTypeAssociation inverseAssociation;
    protected final IPolicyCmptType target;

    /** The qualified name of the target's published interface. */
    protected final String targetInterfaceName;

    /**
     * The qualified name of the target's implementation class name. <code>null</code> if used in
     * the interface builder!
     */
    protected final String targetImplClassName;

    protected final String fieldName;

    private final String staticConstantAssociationName;

    public GenAssociation(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAssociation association)
            throws CoreException {
        super(genPolicyCmptType, association, LOCALIZED_STRINGS);
        this.association = association;
        inverseAssociation = association.findInverseAssociation(getGenType().getIpsPart().getIpsProject());
        target = association.findTargetPolicyCmptType(association.getIpsProject());
        targetInterfaceName = GenType.getQualifiedName(target, genPolicyCmptType.getBuilderSet(), true);
        targetImplClassName = GenType.getQualifiedName(target, genPolicyCmptType.getBuilderSet(), false);
        fieldName = computeFieldName();
        staticConstantAssociationName = getLocalizedText("FIELD_ASSOCIATION_NAME", StringUtils.upperCase(fieldName));
    }

    /**
     * Returns the name of the field/member variable for this association.
     */
    protected abstract String computeFieldName();

    public boolean isDerivedUnion() {
        return association.isDerivedUnion();
    }

    public boolean isCompositionMasterToDetail() {
        return association.isCompositionMasterToDetail();
    }

    public boolean isCompositionDetailToMaster() {
        return association.isCompositionDetailToMaster();
    }

    public boolean isAssociation() {
        return association.isAssoziation();
    }

    public String getTargetImplClassName() {
        return targetImplClassName;
    }

    /**
     * Generates a method to create a new child object if the association is a composite and the
     * target is not abstract. If the target is configurable by product a second method with the
     * product component type as argument is also generated.
     */
    protected void generateNewChildMethodsIfApplicable(JavaCodeFragmentBuilder methodsBuilder,
            boolean generatesInterface) throws CoreException {
        if (!association.getAssociationType().isCompositionMasterToDetail()) {
            return;
        }
        if (target.isAbstract()) {
            return;
        }
        generateMethodNewChild(generatesInterface, false, methodsBuilder);
        if (target.isConfigurableByProductCmptType() && target.findProductCmptType(getIpsProject()) != null) {
            generateMethodNewChild(generatesInterface, true, methodsBuilder);
        }
    }

    private void generateMethodNewChild(boolean generatesInterface,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {

        if (generatesInterface) {
            generateInterfaceMethodNewChild(inclProductCmptArg, builder);
        } else {
            generateImplMethodNewChild(inclProductCmptArg, builder);
        }
    }

    /**
     * Code sample without product component parameter:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage();
     * </pre>
     * 
     * Code sample with product component parameter: [Javadoc]
     * 
     * <pre>
     * public ICoverage newCoverage(ICoverageType coverageType);
     * </pre>
     */
    protected void generateInterfaceMethodNewChild(boolean inclProductCmptArg, JavaCodeFragmentBuilder builder)
            throws CoreException {

        String targetTypeName = target.getName();
        String role = association.getTargetRoleSingular();
        if (inclProductCmptArg) {
            String replacements[] = new String[] { targetTypeName, role,
                    getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())) };
            appendLocalizedJavaDoc("METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", replacements, builder);
        } else {
            appendLocalizedJavaDoc("METHOD_NEW_CHILD", new String[] { targetTypeName, role }, builder);
        }
        generateSignatureNewChild(inclProductCmptArg, builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage() {
     *     ICoverage newCoverage = new Coverage();
     *     addCoverageInternal(newCoverage); // for toMany associations, setCoverageInternal(newCoverage) for to1
     *     newCoverage.initialize();
     *     notfiyChangeListener(...); // if change listener support should be supported
     *     return newCoverage;
     * }
     * </pre>
     */
    public void generateImplMethodNewChild(boolean inclProductCmptArg, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureNewChild(inclProductCmptArg, methodsBuilder);
        String addOrSetMethod;
        if (getGenPolicyCmptType().isGenerateChangeListenerSupport()) {
            addOrSetMethod = getMethodNameAddOrSetObjectInternal();
        } else {
            addOrSetMethod = getMethodNameAddOrSetObject();
        }

        String varName = "new" + association.getTargetRoleSingular();
        methodsBuilder.openBracket();
        if (inclProductCmptArg) {
            // if parameter is null we delegate to the newChild method without parameter
            methodsBuilder.append("if (")
                    .append(getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())))
                    .append(" == null)").openBracket();
            methodsBuilder.append("return ").append(getMethodNameNewChild(association)).appendln("();");
            methodsBuilder.closeBracket();

            methodsBuilder.appendClassName(targetInterfaceName);
        } else {
            // The initialize() method is in the interface IConfigurableModelObject. In not
            // configured model objects we do not have the initialize() method in the interface so
            // we use the implementation class here.
            methodsBuilder.appendClassName(targetImplClassName);
        }
        methodsBuilder.append(" ");
        methodsBuilder.append(varName);
        methodsBuilder.append(" = ");
        if (inclProductCmptArg) {
            GenPolicyCmptType targetGenerator = getGeneratorFor(target);
            methodsBuilder
                    .append(getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject()))) //
                    .append('.').append(targetGenerator.getMethodNameCreatePolicyCmpt()).appendln("();");
        } else {
            methodsBuilder.append("new ").appendClassName(targetImplClassName).appendln("();");
        }
        methodsBuilder.append(addOrSetMethod).append("(").append(varName).appendln(");");
        methodsBuilder.append(varName).append(".").append(getGenPolicyCmptType().getMethodNameInitialize())
                .appendln("();");
        // notfiyChangeListener
        if (getGenPolicyCmptType().isGenerateChangeListenerSupport()) {
            generateChangeListenerSupportAfterChange(methodsBuilder, ChangeEventType.ASSOCIATION_OBJECT_ADDED, varName);
        }
        methodsBuilder.append("return ");
        methodsBuilder.append(varName);
        methodsBuilder.appendln(";");
        methodsBuilder.closeBracket();
    }

    /**
     * Returns the name of the method that adds an object to a toMany association or that sets the
     * object in a to1 association respectively.
     */
    public abstract String getMethodNameAddOrSetObject();

    /**
     * Returns the name of the internal method that adds an object to a toMany association or that
     * sets the object in a to1 association respectively. The internal method could be used to add
     * or set the object without trigger a change listener event.
     */
    public abstract String getMethodNameAddOrSetObjectInternal();

    /**
     * Code sample without product component argument:
     * 
     * <pre>
     * public Coverage newCoverage()
     * </pre>
     * 
     * Code sample with product component argument:
     * 
     * <pre>
     * public Coverage newCoverage(ICoverageType coverageType)
     * </pre>
     */
    protected void generateSignatureNewChild(boolean inclProductCmptArg, JavaCodeFragmentBuilder builder)
            throws CoreException {

        String methodName = getMethodNameNewChild(association);
        String[] argNames, argTypes;
        if (inclProductCmptArg) {
            IProductCmptType productCmptType = target.findProductCmptType(getIpsProject());
            argNames = new String[] { getParamNameForProductCmptInNewChildMethod(productCmptType) };
            argTypes = new String[] { getGenType().getBuilderSet().getGenerator(productCmptType).getQualifiedName(true) };
        } else {
            argNames = EMPTY_STRING_ARRAY;
            argTypes = EMPTY_STRING_ARRAY;
        }
        builder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, methodName, argNames, argTypes);
    }

    /**
     * Returns the name of the method to create a new child object and add it to the parent.
     */
    public String getMethodNameNewChild(IPolicyCmptTypeAssociation association) {
        return getLocalizedText("METHOD_NEW_CHILD_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the internal name of the method to set the parent object.
     */
    public String getMethodNameSetParentObjectInternal(boolean useInverse) {
        return "set" + (useInverse ? inverseAssociation : association).getTargetRoleSingular() + "Internal";
    }

    /**
     * Returns the name of the method to get the parent object.
     */
    public String getMethodNameGetParentObject(boolean useInverse) {
        return "get" + (useInverse ? inverseAssociation : association).getTargetRoleSingular();
    }

    /**
     * Returns the name of the parameter in the new child method, e.g. coverageType.
     */
    protected String getParamNameForProductCmptInNewChildMethod(IProductCmptType targetProductCmptType)
            throws CoreException {
        String targetProductCmptClass = getGenType().getBuilderSet().getGenerator(targetProductCmptType)
                .getQualifiedName(true);
        return StringUtils.uncapitalize(StringUtil.unqualifiedName(targetProductCmptClass));
    }

    /**
     * If <code>castFromPublishedInterfaceToImplClassNeeded</code> = <code>true</code>, the
     * following is generated.
     * 
     * <pre>
     * ((Policy)parentModelObject).setPolicyInternal(this);
     * </pre>
     * 
     * Otherwise
     * 
     * <pre>
     * parentModelObject.setPolicyInternal(this);
     * </pre>
     */
    protected JavaCodeFragment generateCodeToSynchronizeReverseComposition(String varName,
            String newValue,
            boolean castFromPublishedInterfaceToImplClassNeeded) throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();

        if (castFromPublishedInterfaceToImplClassNeeded) {
            code.append("((");
            code.appendClassName(getQualifiedClassName(getTargetPolicyCmptType(), false));
            code.append(')');
        }
        code.append(varName);
        if (castFromPublishedInterfaceToImplClassNeeded) {
            code.append(')');
        }
        code.append('.');
        code.append(getMethodNameSetParentObjectInternal(true));
        code.append('(');
        code.append(newValue);
        code.appendln(");");
        return code;
    }

    protected void generateChangeListenerSupportBeforeChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String paramName) throws CoreException {

        getGenPolicyCmptType().generateChangeListenerSupportBeforeChange(methodsBuilder, eventType,
                getQualifiedClassName(target, true), fieldName, paramName, getStaticConstantAssociationName());
    }

    protected void generateChangeListenerSupportAfterChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String paramName) throws CoreException {

        getGenPolicyCmptType().generateChangeListenerSupportAfterChange(methodsBuilder, eventType,
                getQualifiedClassName(target, true), fieldName, paramName, getStaticConstantAssociationName());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public final static String PROPERTY_PREMIUM = &quot;premium&quot;;
     * </pre>
     */
    protected void generateAssociationNameConstant(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("FIELD_ASSOCIATION_NAME", fieldName, builder);
        builder.append("public final static ");
        builder.appendClassName(String.class);
        builder.append(' ');
        builder.append(getStaticConstantAssociationName());
        builder.append(" = ");
        builder.appendQuoted(fieldName);
        builder.appendln(";");
    }

    public String getStaticConstantAssociationName() {
        return staticConstantAssociationName;
    }

    /**
     * Returns the name of the method setting the referenced object. e.g. setCoverage(ICoverage
     * newObject)
     */
    public String getMethodNameSetObject() {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the method returning the single referenced object. e.g. getCoverage()
     */
    public String getMethodNameGetRefObject() {
        return getLocalizedText("METHOD_GET_REF_OBJECT_NAME",
                StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ISubTypeB getSubTypeB(ISubTypeBConfig qualifier) {
     * //1ToMany public ISubTypeB[] getSubTypeB(ISubTypeBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   //1ToMany List result = new ArrayList();
     *   for (Iterator it = subTypeBs.iterator(); it.hasNext();) {
     *     ISubTypeB subTypeB = (ISubTypeB) it.next();
     *     if(subTypeB.getSubTypeBConfig().equals(qualifier)){
     *       return subTypeB;
     *       //1ToMany result.add(subTypeB);
     *     }
     *   }
     *   return null;
     *   //1ToMany return (ISubTypeB[]) result.toArray(new ISubTypeB[result.size()]);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ISubTypeB getSubTypeB(ISubTypeBConfig qualifier) {
     * //1ToMany public List&lt;ISubTypeB&gt; getSubTypeB(ISubTypeBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   //1ToMany List&lt;ISubTypeB&gt; result = new ArrayList&lt;ISubTypeB&gt;();
     *   for (Iterator it = subTypeBs.iterator(); it.hasNext();) {
     *     ISubTypeB subTypeB = (ISubTypeB) it.next();
     *     if(subTypeB.getSubTypeBConfig().equals(qualifier)){
     *       return subTypeB;
     *       //1ToMany result.add(subTypeB);
     *     }
     *   }
     *   return null;
     *   //1ToMany return result;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectsByQualifierForNonDerivedUnion(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        GenProductCmptType generator = getGenType().getBuilderSet().getGenerator(
                association.findQualifier(getIpsProject()));
        if (generator == null) {
            return;
        }
        generateSignatureGetRefObjectByQualifier(methodsBuilder);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        String className = getQualifiedClassName(target, true);
        String pcTypeLocalVariable = StringUtils.uncapitalize(getUnqualifiedClassName(target, false));
        methodsBuilder.openBracket();
        methodsBuilder.append("if(qualifier == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.appendClassName(List.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append(" result = new ");
            methodsBuilder.appendClassName(ArrayList.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append("();");
        }
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("for (");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(" ");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(" : ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(")");
            methodsBuilder.openBracket();
        } else {
            methodsBuilder.append("for (");
            methodsBuilder.appendClassName(Iterator.class);
            methodsBuilder.append(" it = ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(".iterator(); it.hasNext();)");
            methodsBuilder.openBracket();
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(' ');
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(" = (");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(")");
            methodsBuilder.append(" it.next();");
            methodsBuilder.appendln();
        }
        methodsBuilder.append("if(");
        methodsBuilder.append(pcTypeLocalVariable);
        methodsBuilder.append('.');
        methodsBuilder.append(generator.getMethodNameGetProductCmpt());
        methodsBuilder.append("().equals(qualifier))");
        methodsBuilder.openBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.append("result.add(");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(");");
        } else {
            methodsBuilder.append("return ");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(';');
        }
        methodsBuilder.closeBracket();
        methodsBuilder.closeBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("return result;");
            } else {
                methodsBuilder.append("return (");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[])");
                methodsBuilder.append("result.toArray(new ");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[result.size()]);");
            }
        } else {
            methodsBuilder.append("return null;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IB getB(IBConfig qualifier) {
     * //1ToMany public IB getB(IBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   IB[] bs = getBs();
     *   //1ToMany List result = new ArrayList();
     *   for (int i = 0; i &lt; bs.length; i++) {
     *     if (bs[i].getBConfig().equals(qualifier)) {
     *       return bs[i];
     *       //1ToMany result.add(bs[i]);
     *     }
     *   }
     *   return null;
     *   //1ToMany return (IB[]) result.toArray(new IB[result.size()]);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IB getB(IBConfig qualifier) {
     * //1ToMany public IB getB(IBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   List&lt;B&gt; bs = getBs();
     *   //1ToMany List result = new ArrayList();
     *   for (B b : bs) {
     *     if (b.getBConfig().equals(qualifier)) {
     *       return b;
     *       //1ToMany result.add(b);
     *     }
     *   }
     *   return null;
     *   //1ToMany return result;
     * }
     * </pre>
     * 
     */
    protected void generateMethodGetRefObjectsByQualifierForDerivedUnion(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        GenProductCmptType generator = getGenType().getBuilderSet().getGenerator(
                association.findQualifier(getIpsProject()));
        if (generator == null) {
            return;
        }
        generateSignatureGetRefObjectByQualifier(methodsBuilder);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        String className = getQualifiedClassName(target, true);
        String allObjectsMethodName = getMethodNameGetAllRefObjects();
        String localVarName = "elements";
        methodsBuilder.openBracket();
        methodsBuilder.append("if(qualifier == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendClassName(List.class);
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(">");
        } else {
            methodsBuilder.appendClassName(className);
            methodsBuilder.append("[] ");
        }
        methodsBuilder.append(localVarName);
        methodsBuilder.append(" = ");
        methodsBuilder.append(allObjectsMethodName);
        methodsBuilder.append("();");
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.appendClassName(List.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append(" result = new ");
            methodsBuilder.appendClassName(ArrayList.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append("();");
        }
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("for (");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(" element : ");
            methodsBuilder.append(localVarName);
            methodsBuilder.append(")");
            methodsBuilder.openBracket();
            methodsBuilder.append("if(");
            methodsBuilder.append("element.");
        } else {
            methodsBuilder.append("for (int i = 0; i < ");
            methodsBuilder.append(localVarName);
            methodsBuilder.append(".length; i++)");
            methodsBuilder.openBracket();
            methodsBuilder.append("if(");
            methodsBuilder.append(localVarName);
            methodsBuilder.append("[i].");
        }
        methodsBuilder.append(generator.getMethodNameGetProductCmpt());
        methodsBuilder.append("().equals(qualifier))");
        methodsBuilder.openBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.append("result.add(");
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("element");
            } else {
                methodsBuilder.append(localVarName);
                methodsBuilder.append("[i]");
            }
            methodsBuilder.append(");");
        } else {
            methodsBuilder.append("return ");
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("element;");
            } else {
                methodsBuilder.append(localVarName);
                methodsBuilder.append("[i];");
            }
        }
        methodsBuilder.closeBracket();
        methodsBuilder.closeBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("return result;");
            } else {
                methodsBuilder.append("return (");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[])");
                methodsBuilder.append("result.toArray(new ");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[result.size()]);");
            }
        } else {
            methodsBuilder.append("return null;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Returns the name of the method returning the referenced objects, e.g. getCoverages()
     */
    public String getMethodNameGetAllRefObjects() {
        return getLocalizedText("METHOD_GET_ALL_REF_OBJECTS_NAME",
                StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     * 
     * Java 5 Code sample:
     * 
     * <pre>
     * public List&lt;ICoverage&gt; getCoverages()
     * </pre>
     */
    public void generateSignatureGetAllRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameGetAllRefObjects();
        String returnType;
        if (isUseTypesafeCollections()) {
            returnType = List.class.getName() + "<"
                    + getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true) + ">";
        } else {
            returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true) + "[]";
        }

        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage(ICoverageType qualifier);
     * </pre>
     */
    protected void generateMethodGetRefObjectByQualifier(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_QUALIFIER",
                StringUtils.capitalize(association.getTargetRoleSingular()), methodsBuilder);
        generateSignatureGetRefObjectByQualifier(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage getCoverage(ICoverageType qualifier)
     * </pre>
     */
    public void generateSignatureGetRefObjectByQualifier(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetRefObject();
        String returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        if (association.is1ToManyIgnoringQualifier()) {
            if (isUseTypesafeCollections()) {
                returnType = List.class.getName() + "<" + returnType + ">";
            } else {
                returnType = returnType + "[]";
            }
        }
        IProductCmptType qualifier = association.findQualifier(getIpsProject());
        GenProductCmptType generator = getGenType().getBuilderSet().getGenerator(qualifier);
        if (generator != null) {
            String qualifierClassName = generator.getQualifiedName(true);
            methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName,
                    new String[] { "qualifier" }, new String[] { qualifierClassName });
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     ICoverage[] targets = getCoverages();
     *     for (int i = 0; i &lt; targets.length; i++) {
     *         if (targets[i] == objectToTest)
     *             return true;
     *     }
     *     return false;
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     return getCoverages().contains(objectToTest);
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForContainerAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        String paramName = getParamNameForContainsObject();

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureContainsObject(methodsBuilder);

        methodsBuilder.openBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("return ");
            methodsBuilder.append(getMethodNameGetAllRefObjects());
            methodsBuilder.append("().contains(" + paramName + ");");
        } else {
            methodsBuilder.appendClassName(getQualifiedClassName(
                    (IPolicyCmptType)association.findTarget(getIpsProject()), true));
            methodsBuilder.append("[] targets = ");
            methodsBuilder.append(getMethodNameGetAllRefObjects());
            methodsBuilder.append("();");
            methodsBuilder.append("for(int i=0;i < targets.length;i++) {");
            methodsBuilder.append("if(targets[i] == " + paramName + ") return true; }");
            methodsBuilder.append("return false;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    public void generateSignatureGetRefObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameGetRefObject();
        String returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public int getNumOfCoverages()
     * </pre>
     */
    public void generateSignatureGetNumOfRefObjects(JavaCodeFragmentBuilder methodsBuilder) {

        String methodName = getMethodNameGetNumOfRefObjects();
        methodsBuilder
                .signature(java.lang.reflect.Modifier.PUBLIC, "int", methodName, new String[] {}, new String[] {});
    }

    /**
     * Returns the name of the method returning the number of referenced objects, e.g.
     * getNumOfCoverages().
     */
    public String getMethodNameGetNumOfRefObjects() {
        return getLocalizedText("METHOD_GET_NUM_OF_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Returns the name of the parameter for the method removing an object from a multi-value
     * association, e.g. objectToRemove.
     */
    public String getParamNameForRemoveObject() {
        return getLocalizedText("PARAM_OBJECT_TO_REMOVE_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    protected void generateMethodContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureContainsObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public boolean containsCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameContainsObject();
        String paramClass = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        String paramName = getParamNameForContainsObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "boolean", methodName, new String[] { paramName },
                new String[] { paramClass });
    }

    /**
     * Returns the name of the method returning the number of referenced objects, e.g.
     * getNumOfCoverages().
     */
    public String getMethodNameContainsObject() {
        return getLocalizedText("METHOD_CONTAINS_OBJECT_NAME",
                StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the parameter for the method that tests if an object is references in a
     * multi-value association, e.g. objectToTest.
     */
    public String getParamNameForContainsObject() {
        return getLocalizedText("PARAM_OBJECT_TO_TEST_NAME", association.getTargetRoleSingular());
    }

    protected void generateFieldGetMaxCardinalityFor(JavaCodeFragmentBuilder attrBuilder) {
        appendLocalizedJavaDoc("FIELD_MAX_CARDINALITY", association.getTargetRoleSingular(), attrBuilder);
        String fieldName = getFieldNameGetMaxCardinalityFor();
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new ");
        frag.appendClassName(IntegerRange.class);
        frag.append("(");
        frag.append(association.getMinCardinality());
        frag.append(", ");
        frag.append(association.getMaxCardinality());
        frag.append(")");
        attrBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC, IntegerRange.class.getName(), fieldName, frag);
        attrBuilder.appendln();
    }

    /**
     * Returns the name for the field GetMaxCardinalityFor + single target role of the provided
     * association
     */
    public String getFieldNameGetMaxCardinalityFor() {
        return getLocalizedText("FIELD_MAX_CARDINALITY_NAME",
                StringUtils.upperCase(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the method that returns a reference object at a specified index.
     */
    public String getMethodNameGetRefObjectAtIndex() {
        // TODO extend JavaNamingConventions for association accessor an mutator methods
        return "get" + StringUtils.capitalize(association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index)
     * </pre>
     */
    public void generateSignatureGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_INDEX", association.getTargetRoleSingular(), methodBuilder);
        methodBuilder.signature(java.lang.reflect.Modifier.PUBLIC,
                getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true),
                getMethodNameGetRefObjectAtIndex(), new String[] { "index" }, new String[] { Integer.TYPE.getName() });
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index);
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.append(';');
    }

    /**
     * Returns the name of the method to create a new child object and add it to the parent.
     */
    public String getMethodNameNewChild() {
        return getLocalizedText("METHOD_NEW_CHILD_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of field/member var for the association.
     */
    public abstract String getFieldNameForAssociation() throws CoreException;

    /**
     * Code sample for 1-1 composition
     * 
     * <pre>
     * if (child1 != null) {
     *     copy.child1 = (CpChild1)child1.newCopy();
     *     copy.child1.setCpChild1Internal(copy);
     * }
     * </pre>
     * 
     * Code sample for 1-Many composition
     * 
     * <pre>
     * for (Iterator it = child2s.iterator(); it.hasNext();) {
     *     ICpChild2 cpChild2 = (ICpChild2)it.next();
     *     ICpChild2 copycpChild2 = (ICpChild2)cpChild2.newCopy();
     *     ((CpChild2)copycpChild2).setCpChild1Internal(copy); // generated only if inverse association is
     *                                                         // given
     *     copy.child2s.add(copycpChild2);
     * }
     * </pre>
     */
    public void generateCodeForCopyPropertiesForComposition(String paramName,
            String copyMapName,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String field = getFieldNameForAssociation();
        IPolicyCmptType targetType = association.findTargetPolicyCmptType(getIpsProject());
        String targetTypeQName = getQualifiedClassName(targetType, false);

        generateCodeForCopyPropertiesForComposition(paramName, copyMapName, methodsBuilder, field, targetType,
                targetTypeQName);
    }

    protected abstract void generateCodeForCopyPropertiesForComposition(String paramName,
            String copyMapName,
            JavaCodeFragmentBuilder methodsBuilder,
            String field,
            IPolicyCmptType targetType,
            String targetTypeQName) throws CoreException;

    public abstract void generateMethodCopyPropertiesForAssociation(String paramName,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    public abstract void generateCodeForRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder,
            String paramName) throws CoreException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            if (association.isQualified()) {
                generateMethodGetRefObjectByQualifier(builder);
            }
        } else {
            if (association.isQualified()) {
                if (association.isDerivedUnion()) {
                    generateMethodGetRefObjectsByQualifierForDerivedUnion(builder);
                } else {
                    generateMethodGetRefObjectsByQualifierForNonDerivedUnion(builder);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            if (!association.isDerivedUnion() && !association.getAssociationType().isCompositionDetailToMaster()) {
                generateFieldGetMaxCardinalityFor(builder);
            }
            generateAssociationNameConstant(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        // nothing to do
    }

    public abstract void generateCodeForContainerAssociationImplementation(List<IAssociation> associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    public abstract void generateCodeForValidateDependants(JavaCodeFragment body) throws CoreException;

    public IPolicyCmptType getTargetPolicyCmptType() throws CoreException {
        return association.getIpsProject().findPolicyCmptType(association.getTarget());
    }

    public abstract JavaCodeFragment generateCodeToSynchronizeReverseAssoziation(String fieldName,
            String targetImplClassName) throws CoreException;

    public void generateSnippetForAcceptVisitorIfAccplicable(String paramName, JavaCodeFragmentBuilder builder)
            throws CoreException {
        if (isDerivedUnion() || !isCompositionMasterToDetail()) {
            return;
        }
        generateSnippetForAcceptVisitor(paramName, builder);
    }

    public abstract void generateSnippetForAcceptVisitor(String paramName, JavaCodeFragmentBuilder builder)
            throws CoreException;

    public abstract void generateCodeForCopyAssociation(String varCopy,
            String varCopyMap,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    public abstract void generateCodeForCopyComposition(String varCopy,
            String varCopyMap,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    public GenAssociation getGeneratorForInverseAssociation() throws CoreException {
        if (inverseAssociation == null) {
            return null;
        }
        GenPolicyCmptType generatorForTargetSide = getGeneratorFor(target);
        if (generatorForTargetSide == null) {
            return null;
        }
        return generatorForTargetSide.getGenerator(inverseAssociation);
    }

    GenPolicyCmptType getGeneratorFor(IPolicyCmptType policyCmptType) throws CoreException {
        StandardBuilderSet builderSet = getGenType().getBuilderSet();
        return builderSet.getGenerator(policyCmptType);
    }

    /**
     * Returns the association this generator belongs to.
     */
    public IPolicyCmptTypeAssociation getAssociation() {
        return association;
    }

    /**
     * Returns the inverse association of the association this generator belongs to, or null is no
     * inverse association is specified.
     */
    public IPolicyCmptTypeAssociation getInverseAssociation() {
        return inverseAssociation;
    }

    protected GenPolicyCmptType getGenPolicyCmptType() {
        return (GenPolicyCmptType)getGenType();
    }

    protected String getQualifiedInterfaceClassNameForTarget() throws CoreException {
        return getGenType().getBuilderSet()
                .getGenerator((IPolicyCmptType)association.findTarget(association.getIpsProject()))
                .getQualifiedName(true);
    }

    protected String getUnqualifiedInterfaceClassNameForTarget() {
        try {
            return getGenType().getBuilderSet()
                    .getGenerator((IPolicyCmptType)association.findTarget(association.getIpsProject()))
                    .getUnqualifiedClassName(true);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void addFieldGetMaxCardinalityForToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldToGeneratedJavaElements(javaElements, generatedJavaType, getFieldNameGetMaxCardinalityFor());
    }

    protected final void addMethodNewChildToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameNewChild());
    }

    protected final void addMethodNewChildConfiguredToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IProductCmptType targetConfiguringProductCmptType) {

        try {
            addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameNewChild(), "Q"
                    + getGenType().getBuilderSet().getGenerator(targetConfiguringProductCmptType)
                            .getUnqualifiedClassName(true) + ";");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void addFieldAssociationToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        try {
            addFieldToGeneratedJavaElements(javaElements, generatedJavaType, getFieldNameForAssociation());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void addFieldAssociationNameToGeneratedJavaElements(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldToGeneratedJavaElements(javaElements, generatedJavaType, getStaticConstantAssociationName());
    }

}
