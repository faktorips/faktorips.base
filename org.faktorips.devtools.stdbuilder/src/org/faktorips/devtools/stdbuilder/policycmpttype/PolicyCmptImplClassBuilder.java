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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.tableusage.GenTableStructureUsage;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.DefaultUnresolvedReference;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IUnresolvedReference;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.AbstractConfigurableModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.DependantObject;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ModelObjectDelta;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Element;

public class PolicyCmptImplClassBuilder extends BasePolicyCmptTypeBuilder {

    public final static String METHOD_COPY_ASSOCIATIONS = "copyAssociationsInternal";

    public final static String METHOD_NEW_COPY = "newCopyInternal";

    private final static String FIELD_PARENT_MODEL_OBJECT = "parentModelObject";

    public PolicyCmptImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) throws CoreException {
        super(builderSet, kindId, new LocalizedStringsSet(PolicyCmptImplClassBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSuperclass() throws CoreException {
        IPolicyCmptType supertype = (IPolicyCmptType)getPcType().findSupertype(getIpsProject());
        if (supertype != null) {
            return getQualifiedClassName(supertype);
        }
        if (getPcType().isConfigurableByProductCmptType()) {
            return AbstractConfigurableModelObject.class.getName();
        } else {
            return AbstractModelObject.class.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String name = StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
        return getJavaNamingConvention().getImplementationClassName(StringUtils.capitalize(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getExtendedInterfaces() throws CoreException {
        String publishedInterface = GenType.getQualifiedName(getPcType(), (StandardBuilderSet)getBuilderSet(), true);
        if (isFirstDependantTypeInHierarchy(getPcType())) {
            return new String[] { publishedInterface, DependantObject.class.getName() };
        }
        return new String[] { publishedInterface };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) {
        builder.javaDoc(null, ANNOTATION_GENERATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        generateConstructorDefault(builder);
        if (getProductCmptType() != null) {
            generateConstructorWithProductCmptArg(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        IPolicyCmptType type = getPcType();
        generateMethodInitialize(methodsBuilder);
        if (getProductCmptType() != null) {
            if (hasValidProductCmptTypeName()) {
                generateMethodGetProductCmpt(methodsBuilder);
                generateMethodGetProductCmptGeneration(methodsBuilder);
                generateMethodSetProductComponent(methodsBuilder);
            }
            generateMethodEffectiveFromHasChanged(methodsBuilder);
            generateTableAccessMethods(methodsBuilder);
        }
        if (type.isAggregateRoot()) {
            if (type.isConfigurableByProductCmptType()) {
                generateMethodGetEffectiveFromAsCalendarForAggregateRoot(methodsBuilder);
            }
        } else if (isFirstDependantTypeInHierarchy(type)) {
            generateCodeForDependantObjectBaseClass(memberVarsBuilder, methodsBuilder);
        }
        if (getPcType().getSupertype().length() == 0) {
            getGenerator().generateChangeListenerMethods(methodsBuilder, FIELD_PARENT_MODEL_OBJECT,
                    isFirstDependantTypeInHierarchy(getPcType()));
        }
        generateMethodRemoveChildModelObjectInternal(methodsBuilder);
        generateMethodInitPropertiesFromXml(methodsBuilder);
        generateMethodCreateChildFromXml(methodsBuilder);
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (associations[i].isAssoziation()) {
                generateMethodCreateUnresolvedReference(methodsBuilder);
                break;
            }
        }
        if (isGenerateDeltaSupport()) {
            generateMethodComputeDelta(methodsBuilder);
        }
        if (isGenerateCopySupport()) {
            if ((getClassModifier() & java.lang.reflect.Modifier.ABSTRACT) == 0) {
                generateMethodNewCopy(methodsBuilder);
            }
            generateMethodNewCopy_CopyMap(methodsBuilder);
            generateMethodCopyProperties(methodsBuilder);
            generateMethodCopyAssociations(methodsBuilder);
        }
        if (isGenerateVisitorSupport()) {
            generateMethodAccept(methodsBuilder);
        }
    }

    /**
     * Code sample
     * 
     * <pre>
     * public IModelObject newCopy() {
     *     CpParent newCopy = new CpParent(getCpParentType());
     *     newCopy.setProductCmptGenerationInternal(getProductCmptGeneration());
     *     copyProperties(newCopy);
     *     return newCopy;
     * }
     * </pre>
     */
    protected void generateMethodNewCopy(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        CheckForOverrideAnnotationForNewCopyMethod checkVisitor = new CheckForOverrideAnnotationForNewCopyMethod();
        checkVisitor.start((IPolicyCmptType)getPcType().findSupertype(getIpsProject()));
        appendOverrideAnnotation(methodsBuilder, checkVisitor.implementsInterfaceMethod);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, IModelObject.class.getName(), MethodNames.NEW_COPY,
                new String[0], new String[0]);
        methodsBuilder.openBracket();
        // declare Map<AbstractModelObject, AbstractModelObject> copyMap = new HashMap<...>()
        String varCopyMap = "copyMap";
        methodsBuilder.append(getHashMapFragment(false)).append(' ').append(varCopyMap).append(" = ") //
                .append("new ").append(getHashMapFragment(true)).appendln("();");

        // declare variable: CpParent newCopy = newCopy(copyMap);
        String varName = "newCopy";
        methodsBuilder.append(getUnqualifiedClassName()).append(' ').append(varName) //
                .append(" = (").append(getUnqualifiedClassName()).append(')') //
                .append(METHOD_NEW_COPY).append('(').append(varCopyMap).appendln(");");
        // copyAssociations(newCopy, copyMap);
        methodsBuilder.methodCall(METHOD_COPY_ASSOCIATIONS, new String[] { varName, varCopyMap }, true);
        // return newCopy
        methodsBuilder.appendln("return " + varName + ";");
        methodsBuilder.closeBracket();
    }

    private JavaCodeFragment getHashMapFragment(boolean instance) {
        JavaCodeFragmentBuilder hashMapFragmentBuilder = new JavaCodeFragmentBuilder();
        if (instance) {
            hashMapFragmentBuilder.appendClassName(HashMap.class);
        } else {
            hashMapFragmentBuilder.appendClassName(Map.class);
        }
        hashMapFragmentBuilder.append(' ');
        appendGenerics(hashMapFragmentBuilder, AbstractModelObject.class, AbstractModelObject.class);
        return hashMapFragmentBuilder.getFragment();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * protected IModelObject newCopy(HashMap&lt;AbstractModelObject, AbstractModelObject&gt; copyMap) {
     *     Policy newCopy = new Policy();
     *     copyProperties(newCopy, copyMap);
     *     return newCopy;
     * }
     * </pre>
     * 
     * @param methodsBuilder
     * @throws CoreException
     */
    private void generateMethodNewCopy_CopyMap(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(getPcType(), "METHOD_NEW_COPY_INTERNAL");
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String varCopyMap = "copyMap";
        if (getPcType().hasSupertype()) {
            appendOverrideAnnotation(methodsBuilder, false);
        }

        boolean isAbstract = getPcType().isAbstract();
        int modifier = Modifier.PUBLIC;
        if (isAbstract) {
            modifier |= Modifier.ABSTRACT;
        }

        methodsBuilder.addImport(Map.class);

        methodsBuilder.signature(modifier, IModelObject.class.getName(), METHOD_NEW_COPY, new String[] { varCopyMap },
                new String[] { getHashMapFragment(false).getSourcecode() });

        if (isAbstract) {
            methodsBuilder.appendln(';');
        } else {
            methodsBuilder.openBracket();

            // declare variable: Policy newCopy = new Policy();
            String varName = "newCopy";
            methodsBuilder.append(getUnqualifiedClassName()).append(' ').append(varName).append(" = ") //
                    .append('(').append(getUnqualifiedClassName()).append(')').append(varCopyMap).appendln(
                            ".get(this);");
            methodsBuilder.append("if (").append(varName).append(" == null)").openBracket() //
                    .append(varName).append(" = new ").append(getUnqualifiedClassName()).appendln("();");
            if (getPcType().isConfigurableByProductCmptType() && getProductCmptType() != null) {
                // call method newCopy.copyProductCmptAndGenerationInternal(this)
                methodsBuilder.append("newCopy.") //
                        .append(MethodNames.COPY_PRODUCT_CMPT_AND_GENERATION_INTERNAL).appendln("(this);");
            }
            // call method copyProperties(newCopy)
            methodsBuilder.methodCall(getMethodNameCopyProperties(), new String[] { varName, varCopyMap }, true);

            methodsBuilder.closeBracket();
            methodsBuilder.append("return ").append(varName).append(';');
            methodsBuilder.methodEnd();
        }
    }

    /**
     * Code sample
     * 
     * <pre>
     * protected void copyProperties(CpParent copy, HashMap&lt;AbstractModelObject, AbstractModelObject&gt; copyMap) {
     *     super.copyProperties(copy); // if class has superclass
     *     copy.changeableAttr = changeableAttr;
     *     copy.derivedExplicitCall = derivedExplicitCall;
     *     if (child1 != null) {
     *         copy.child1 = (CpChild1)child1.newCopy();
     *         copy.child1.setParentModelObjectInternal(copy);
     *     }
     *     for (Iterator it = child2s.iterator(); it.hasNext();) {
     *         CpChild2 CpChild2 = (CpChild2)it.next();
     *         CpChild2 copyCpChild2 = (CpChild2)CpChild2.newCopy();
     *         ((DependantObject)copyCpChild2).setParentModelObjectInternal(copy);
     *         copy.child2s.add(copyCpChild2);
     *     }
     * }
     * </pre>
     * 
     * Java 5 code sample: for (Iterator&lt;ICpChild2&gt; it = child2s.iterator(); it.hasNext();) {
     * 
     */
    protected void generateMethodCopyProperties(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(null, ANNOTATION_GENERATED);
        String paramName = "copy";
        String varCopyMap = "copyMap";
        methodsBuilder.addImport(Map.class);
        methodsBuilder.signature(java.lang.reflect.Modifier.PROTECTED, "void", getMethodNameCopyProperties(),
                new String[] { paramName, varCopyMap }, new String[] { getUnqualifiedClassName(),
                        getHashMapFragment(false).getSourcecode() });
        methodsBuilder.openBracket();

        if (getPcType().hasSupertype()) {
            methodsBuilder.appendln("super." + getMethodNameCopyProperties() + "(" + paramName + ", " + varCopyMap
                    + ");");
        }

        GenPolicyCmptType genPolicyCmptType = getGenerator();
        for (GenPolicyCmptTypeAttribute generator : genPolicyCmptType.getGenAttributes()) {
            if (generator.isMemberVariableRequired()) {
                String field = generator.getMemberVarName();
                methodsBuilder.append(paramName + "." + field + " = ");
                methodsBuilder.append(generator.getDatatypeHelper().referenceOrSafeCopyIfNeccessary(field));
                methodsBuilder.appendln(";");
            }
        }

        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (!associations[i].isValid() || associations[i].isDerived()) {
                continue;
            }
            if (associations[i].isCompositionDetailToMaster()) {
                continue;
            }
            if (associations[i].isAssoziation()) {
                getGenerator(associations[i]).generateMethodCopyPropertiesForAssociation(paramName, methodsBuilder);
            } else {
                getGenerator(associations[i]).generateCodeForCopyPropertiesForComposition(paramName, varCopyMap,
                        methodsBuilder);
            }
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * // Rekursion for compositions
     * protected void copyAssociations(Policy newCopy, HashMap<...> copyMap) {
     *  for (IPerson iPerson : persons) {
     *      Person person = (Person)iPerson;
     *      Person copyPerson = (Person)copyMap.get(person);
     *      person.copyAssociations(copyPerson, copyMap);
     *  }
     *  
     *  for associations: 
     *  // ..1
     *  if (copyMap.containsKey(policyHolder)) {
     *      copy.policyHolder = (Person)copyMap.get(policyHolder);
     *  }
     *  // ..*
     *  for (IPerson insuredPerson : insuredPersons) {
     *      if (copyMap.containsKey(insuredPerson)) {
     *          copy.insuredPersons.remove(insuredPerson);
     *          copy.insuredPersons.add((Person)copyMap.get(insuredPerson));
     *      }
     *  }
     * }
     * </pre>
     * 
     * @param methodsBuilder
     * @throws CoreException
     */
    protected void generateMethodCopyAssociations(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String javaDoc = getLocalizedText(getPcType(), "METHOD_COPY_ASSOCIATIONS_INTERNAL");
        methodsBuilder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String varAbstractCopy = "abstractCopy";
        String varCopyMap = "copyMap";

        if (getPcType().hasSupertype()) {
            appendOverrideAnnotation(methodsBuilder, false);
        }

        methodsBuilder.methodBegin(Modifier.PUBLIC, "void", METHOD_COPY_ASSOCIATIONS, new String[] { varAbstractCopy,
                varCopyMap }, new String[] { AbstractModelObject.class.getName(),
                getHashMapFragment(false).getSourcecode() });

        if (getPcType().hasSupertype()) {
            methodsBuilder.appendln("super.").append(METHOD_COPY_ASSOCIATIONS) //
                    .append("(").append(varAbstractCopy).append(", ").append(varCopyMap).append(");");
        }

        // casted variable newCopy is only necessary if there is at least one association
        String varCopy = "newCopy";
        for (IPolicyCmptTypeAssociation association : getPcType().getPolicyCmptTypeAssociations()) {
            if (association.isAssoziation()) {
                methodsBuilder.varDefinition(getUnqualifiedClassName(), varCopy, "(" + getUnqualifiedClassName() + ")"
                        + varAbstractCopy);
                break;
            }
        }

        for (IPolicyCmptTypeAssociation association : getPcType().getPolicyCmptTypeAssociations()) {
            if (!association.isValid() || association.isDerived()) {
                continue;
            }
            if (association.isCompositionDetailToMaster()) {
                continue;
            }
            if (association.isAssoziation()) {
                getGenerator(association).generateCodeForCopyAssociation(varCopy, varCopyMap, methodsBuilder);
            } else {
                getGenerator(association).generateCodeForCopyComposition(varCopy, varCopyMap, methodsBuilder);
            }
        }
        methodsBuilder.methodEnd();
    }

    private GenAssociation getGenerator(IPolicyCmptTypeAssociation policyCmptTypeAssociation) throws CoreException {
        return getGenerator().getGenerator(policyCmptTypeAssociation);
    }

    private String getMethodNameCopyProperties() {
        return "copyProperties";
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public boolean accept(IModelObjectVisitor visitor) {
     *     // next if statement only for subclasses (of other model classes)
     *     if (!super.accept(visitor)) {
     *         return false;
     *     }
     * 
     *     // next if statement only for classes that are NOT subclasses (of other model classes)
     *     if (!visitor.visit(this)) {
     *         return false;
     *     }
     * 
     *     // code for assocations see the association generators
     * 
     *     return true;
     * }
     * 
     * </pre>
     * 
     * @see GenAssociation#generateSnippetForAcceptVisitorIfAccplicable(String, String,
     *      JavaCodeFragmentBuilder)
     */
    protected void generateMethodAccept(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, !getPcType().hasSupertype());
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "boolean", MethodNames.ACCEPT_VISITOR,
                new String[] { "visitor" }, new String[] { IModelObjectVisitor.class.getName() });
        methodsBuilder.openBracket();

        if (getPcType().hasSupertype()) {
            methodsBuilder.appendln("if (!super." + MethodNames.ACCEPT_VISITOR + "(visitor)) {");
            methodsBuilder.appendln("return false;");
            methodsBuilder.append('}');
        } else {
            methodsBuilder.appendln("if (!visitor.visit(this)) {");
            methodsBuilder.appendln("return false;");
            methodsBuilder.append('}');
        }

        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            GenAssociation generator = getGenerator(associations[i]);
            if (generator != null) {
                generator.generateSnippetForAcceptVisitorIfAccplicable("visitor", methodsBuilder);
            }
        }
        methodsBuilder.appendln("return true;");
        methodsBuilder.closeBracket();
    }

    /**
     * <pre>
     * public IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options) {
     *     ModelObjectDelta delta = (ModelObjectDelta)super.computeDelta(otherObject, options);
     *     if (!Root.class.isAssignableFrom(otherObject.getClass())) {
     *         return delta;
     *     }
     *     Root otherRoot = (Root)otherObject;
     *     delta.checkPropertyChange(IRoot.PROPERTY_STRINGATTRIBUTE, stringAttribute, otherRoot.stringAttribute, options);
     *     delta.checkPropertyChange(IRoot.PROPERTY_INTATTRIBUTE, intAttribute, otherRoot.intAttribute, options);
     *     delta.checkPropertyChange(IRoot.PROPERTY_BOOLEANATTRIBUTE, booleanAttribute, otherRoot.booleanAttribute, options);
     *     ModelObjectDelta.createChildDeltas(delta, children, otherRoot.children, &quot;Child&quot;, options);
     *     ModelObjectDelta.createChildDeltas(delta, somethingElse, otherRoot.somethingElse, &quot;SomethingElse&quot;, options);
     *     return delta;
     * }
     * 
     */
    protected void generateMethodComputeDelta(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, !getPcType().hasSupertype());
        generateSignatureComputeDelta(methodsBuilder);
        methodsBuilder.openBracket();

        if (getPcType().hasSupertype()) {
            // code sample: ModelObjectDelta delta =
            // (ModelObjectDelta)super.computeDelta(otherObject, options);
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append(" delta = (");
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append(")super.");
            methodsBuilder.append(MethodNames.COMPUTE_DELTA);
            methodsBuilder.appendln("(otherObject, options);");
        } else {
            // code sample: ModelObjectDelta delta = ModelObjectDelta.newEmptyDelta(this,
            // otherObject);
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append(" delta = ");
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append('.');
            methodsBuilder.append(MethodNames.MODELOBJECTDELTA_NEW_DELTA);
            methodsBuilder.appendln("(this, otherObject, options);");
        }

        // code sample
        // if (Contract.class.isAssigneableFrom(otherObject.getClass()) {
        // return delta;
        // }
        methodsBuilder.append("if (!");
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.appendln(".class.isAssignableFrom(otherObject.getClass())) {");
        methodsBuilder.appendln("return delta;");
        methodsBuilder.appendln("}");

        // code sample: Contract otherContract = (Contract)otherObject;
        String varOther = " other" + StringUtils.capitalize(getPcType().getName());
        boolean castForOtherGenerated = false;

        // code sample for an attribute:
        // delta.checkPropertyChange(IRoot.PROPERTY_STRINGATTRIBUTE, stringAttribute,
        // otherRoot.stringAttribute, options);
        GenPolicyCmptType genPolicyCmptType = getGenerator();
        for (GenPolicyCmptTypeAttribute generator : genPolicyCmptType.getGenAttributes()) {
            if (generator.needsToBeConsideredInDeltaComputation()) {
                if (!castForOtherGenerated) {
                    castForOtherGenerated = true;
                    generateCodeToCastOtherObject(varOther, methodsBuilder);
                }
                generator.generateDeltaComputation(methodsBuilder, "delta", varOther);
            }
        }

        // code sample (note that the generated code is the same for to1 and toMany associations
        // ModelObjectDelta.createChildDeltas(delta, children, otherRoot.children, "Child",
        // options);

        // code sample for a to1 association:
        // ModelObjectDelta.createChildDeltas(delta, somethingElse, otherRoot.somethingElse,
        // "SomethingElse", options);
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (!associations[i].isValid() || associations[i].isDerived()
                    || !associations[i].isCompositionMasterToDetail()) {
                continue;
            }
            if (!castForOtherGenerated) {
                castForOtherGenerated = true;
                generateCodeToCastOtherObject(varOther, methodsBuilder);
            }
            String fieldName = getGenerator(associations[i]).getFieldNameForAssociation();
            methodsBuilder.appendClassName(ModelObjectDelta.class);
            methodsBuilder.append('.');
            methodsBuilder.append(MethodNames.MODELOBJECTDELTA_CREATE_CHILD_DELTAS);
            methodsBuilder.append("(delta, ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(", ");
            methodsBuilder.append(varOther + '.' + fieldName);
            methodsBuilder.append(", ");
            methodsBuilder.appendQuoted(fieldName); // = rolename
            methodsBuilder.append(", options);");
        }

        methodsBuilder.append("return delta;");
        methodsBuilder.closeBracket();
    }

    /*
     * helper method for generateMethodComputeDelta
     */
    private void generateCodeToCastOtherObject(String varOther, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.append(varOther);
        methodsBuilder.append(" = (");
        methodsBuilder.append(getUnqualifiedClassName());
        methodsBuilder.appendln(")otherObject;");
    }

    protected void generateSignatureComputeDelta(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String[] paramNames = new String[] { "otherObject", "options" };
        String[] paramTypes = new String[] { IModelObject.class.getName(), IDeltaComputationOptions.class.getName() };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, IModelObjectDelta.class.getName(),
                MethodNames.COMPUTE_DELTA, paramNames, paramTypes);
    }

    protected void generateMethodGetEffectiveFromAsCalendarForAggregateRoot(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, !getPcType().hasSupertype());
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Calendar.class,
                MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR, EMPTY_STRING_ARRAY, new Class[0]);
        if (getPcType().hasSupertype()) {
            methodsBuilder.appendln("return super." + MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR + "();");
        } else {
            String todoLine1 = getLocalizedText(getPcType(), "METHOD_GET_EFFECTIVE_FROM_TODO_LINE1");
            methodsBuilder.append("return null; // ");
            methodsBuilder.append(getJavaNamingConvention().getToDoMarker());
            methodsBuilder.append(' ');
            methodsBuilder.appendln(todoLine1);
            methodsBuilder.append("// ");
            methodsBuilder.appendln(getLocalizedText(getPcType(), "METHOD_GET_EFFECTIVE_FROM_TODO_LINE2"));
            methodsBuilder.append("// ");
            methodsBuilder.appendln(getLocalizedText(getPcType(), "METHOD_GET_EFFECTIVE_FROM_TODO_LINE3"));
        }
        methodsBuilder.methodEnd();
    }

    /**
     * Code sample
     * 
     * <pre>
     * public void effectiveFromHasChanged() {
     *     super.effectiveFromHasChanged();
     *     for (Iterator it = ftCoverages.iterator(); it.hasNext();) {
     *         AbstractConfigurableModelObject child = (AbstractConfigurableModelObject)it.next();
     *         child.effectiveFromHasChanged();
     *     }
     * }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * public void effectiveFromHasChanged() {
     *     super.effectiveFromHasChanged();
     *     for (Iterator&lt;IFtCoverage&gt; it = ftCoverages.iterator(); it.hasNext();) {
     *         AbstractConfigurableModelObject child = (AbstractConfigurableModelObject)it.next();
     *         child.effectiveFromHasChanged();
     *     }
     * }
     * </pre>
     */
    protected void generateMethodEffectiveFromHasChanged(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, false);
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Void.TYPE,
                MethodNames.EFFECTIVE_FROM_HAS_CHANGED, EMPTY_STRING_ARRAY, new Class[0]);
        methodsBuilder.appendln("super." + MethodNames.EFFECTIVE_FROM_HAS_CHANGED + "();");

        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            IPolicyCmptTypeAssociation r = associations[i];
            if (r.isValid() && r.isCompositionMasterToDetail() && !r.isDerivedUnion()) {
                IPolicyCmptType target = r.findTargetPolicyCmptType(getIpsProject());
                if (!target.isConfigurableByProductCmptType()) {
                    continue;
                }
                String field = getGenerator(r).getFieldNameForAssociation();
                if (r.is1ToMany()) {
                    methodsBuilder.append("for (");
                    methodsBuilder.appendClassName(Iterator.class);
                    if (isUseTypesafeCollections()) {
                        methodsBuilder.append("<");
                        methodsBuilder.appendClassName(getGenerator(r).getQualifiedClassName(
                                getGenerator(r).getTargetPolicyCmptType(), true));
                        methodsBuilder.append('>');
                    }
                    methodsBuilder.append(" it=" + field + ".iterator(); it.hasNext();) {");
                    methodsBuilder.appendClassName(AbstractConfigurableModelObject.class);
                    methodsBuilder.append(" child = (");
                    methodsBuilder.appendClassName(AbstractConfigurableModelObject.class);
                    methodsBuilder.append(")it.next();");
                    methodsBuilder.append("child.");
                    methodsBuilder.append(MethodNames.EFFECTIVE_FROM_HAS_CHANGED);
                    methodsBuilder.append("();");
                    methodsBuilder.appendln("}");
                } else {
                    methodsBuilder.append("if (");
                    methodsBuilder.append(field);
                    methodsBuilder.append("!=null) {");
                    methodsBuilder.append("((");
                    methodsBuilder.appendClassName(AbstractConfigurableModelObject.class);
                    methodsBuilder.append(")");
                    methodsBuilder.append(field);
                    methodsBuilder.append(").");
                    methodsBuilder.append(MethodNames.EFFECTIVE_FROM_HAS_CHANGED);
                    methodsBuilder.append("();");
                    methodsBuilder.appendln("}");
                }
            }
        }
        methodsBuilder.methodEnd();
    }

    protected void generateMethodRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, false);
        String paramName = "childToRemove";
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Void.TYPE,
                MethodNames.REMOVE_CHILD_MODEL_OBJECT_INTERNAL, new String[] { paramName },
                new Class[] { IModelObject.class });
        methodsBuilder.appendln("super." + MethodNames.REMOVE_CHILD_MODEL_OBJECT_INTERNAL + "(" + paramName + ");");
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (associations[i].isValid() && associations[i].getAssociationType().isCompositionMasterToDetail()
                    && !associations[i].isDerivedUnion()) {
                getGenerator(associations[i]).generateCodeForRemoveChildModelObjectInternal(methodsBuilder, paramName);
            }
        }
        methodsBuilder.methodEnd();
    }

    @Override
    protected void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenProductCmptTypeAttribute generator = getGenerator().getGenerator(attribute);
        if (generator != null) {
            generator.generateCodeForPolicyCmptType(generatesInterface(), methodsBuilder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForContainerAssociationImplementation(IPolicyCmptTypeAssociation derivedUnionAssociation,
            List<IAssociation> associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        GenAssociation gen = getGenerator(derivedUnionAssociation);
        gen.generateCodeForContainerAssociationImplementation(associations, memberVarsBuilder, methodsBuilder);
    }

    @Override
    protected void generateCodeForValidationRules(JavaCodeFragmentBuilder constantBuilder,
            JavaCodeFragmentBuilder memberVarBuilder,
            JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateMethodValidateSelf(methodBuilder, getPcType().getPolicyCmptTypeAttributes());
        generateMethodValidateDependants(methodBuilder);
        super.generateCodeForValidationRules(constantBuilder, memberVarBuilder, methodBuilder);
    }

    /**
     * Code sample
     * 
     * <pre>
     * public void validateDependants(MessageList ml, String businessFunction) {
     *     super.validateDependants(ml, businessFunction);
     *     if (getNumOfFtCoverages() &gt; 0) {
     *         IFtCoverage[] rels = getFtCoverages();
     *         for (int i = 0; i &lt; rels.length; i++) {
     *             ml.add(rels[i].validate(businessFunction));
     *         }
     *     }
     * }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * public void validateDependants(MessageList ml, String businessFunction) {
     *     super.validateDependants(ml, businessFunction);
     *     if (getNumOfFtCoverages() &gt; 0) {
     *         List&lt;IFtCoverage&gt; rels = getFtCoverages();
     *         for (IFtCoverage rel : rels) {
     *             ml.add(rel.validate(businessFunction));
     *         }
     *     }
     * }
     * </pre>
     */
    private void generateMethodValidateDependants(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = "validateDependants";
        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();

        String parameterValidationContext = "context";
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("super.");
        body.append(methodName);
        body.append("(ml, ");
        body.append(parameterValidationContext);
        body.append(");");
        for (int i = 0; i < associations.length; i++) {
            IPolicyCmptTypeAssociation r = associations[i];
            GenAssociation gen = getGenerator(r);
            if (!r.validate(getIpsProject()).containsErrorMsg()) {
                if (r.getAssociationType() == AssociationType.COMPOSITION_MASTER_TO_DETAIL
                        && StringUtils.isEmpty(r.getSubsettedDerivedUnion())) {
                    body.appendln();
                    gen.generateCodeForValidateDependants(body);
                }
            }
        }

        String javaDoc = getLocalizedText(getPcType(), "VALIDATE_DEPENDANTS_JAVADOC", getPcType().getName());
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(), methodName,
                new String[] { "ml", parameterValidationContext }, new String[] { MessageList.class.getName(),
                        IValidationContext.class.getName() });
        builder.append(body);
        builder.methodEnd();
    }

    /**
     * Code sample
     * 
     * <pre>
     * public void validateSelf(MessageList ml, String businessFunction) {
     *     super.validateSelf(ml, businessFunction);
     * }
     * </pre>
     */
    private void generateMethodValidateSelf(JavaCodeFragmentBuilder builder, IPolicyCmptTypeAttribute[] attributes)
            throws CoreException {
        String methodName = "validateSelf";
        String javaDoc = getLocalizedText(getIpsObject(), "VALIDATE_SELF_JAVADOC", getPcType().getName());

        String parameterNameContext = "context";
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if(!");
        body.append("super.");
        body.append(methodName);
        body.append("(ml, ");
        body.append(parameterNameContext);
        body.append("))");
        body.appendOpenBracket();
        body.append(" return false;");
        body.appendCloseBracket();
        IValidationRule[] rules = getPcType().getRules();
        for (int i = 0; i < rules.length; i++) {
            IValidationRule r = rules[i];
            if (r.validate(getIpsProject()).isEmpty()) {
                body.append("if(!");
                body.append(getMethodExpressionExecRule(r, "ml", parameterNameContext));
                body.append(')');
                body.appendOpenBracket();
                body.append(" return false;");
                body.appendCloseBracket();
            }
        }
        body.appendln(" return true;");
        // buildValidationValueSet(body, attributes); wegschmeissen ??
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
                methodName, new String[] { "ml", parameterNameContext }, new String[] { MessageList.class.getName(),
                        IValidationContext.class.getName() });
        builder.append(body);
        builder.methodEnd();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Policy(Product productCmpt) {
     *     super(productCmpt);
     * }
     * </pre>
     */
    protected void generateConstructorWithProductCmptArg(JavaCodeFragmentBuilder builder) throws CoreException {

        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getPcType(), builder);
        String[] paramNames = new String[] { "productCmpt" };
        String[] paramTypes = new String[] { getGenProductCmptType().getQualifiedName(true) };
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(), paramNames, paramTypes);
        builder.append("super(productCmpt);");
        generateInitializationForOverrideAttributes(builder);
        builder.methodEnd();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Policy(Product productCmpt, Date effectiveDate) {
     *     super(productCmpt, effectiveDate);
     *     initialize();
     * }
     * </pre>
     */
    protected void generateConstructorDefault(JavaCodeFragmentBuilder builder) throws CoreException {

        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getPcType(), builder);
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, null, getUnqualifiedClassName(), EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
        builder.append("super();");
        generateInitializationForOverrideAttributes(builder);
        builder.methodEnd();
    }

    private void generateInitializationForOverrideAttributes(JavaCodeFragmentBuilder builder) throws CoreException {
        IPolicyCmptTypeAttribute[] attributes = getPcType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].isChangeable() && attributes[i].isOverwrite()
                    && attributes[i].validate(getIpsProject()).isEmpty()) {
                ((GenChangeableAttribute)getGenerator().getGenerator(attributes[i]))
                        .generateInitializationForOverrideAttributes(builder, getPcType().getIpsProject());
            }
        }
    }

    /**
     * Returns the <code>GenPolicyCmptType</code> for this builder.
     */
    private GenPolicyCmptType getGenerator() throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getPcType());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * protected void initialize() {
     *     super.initialize();
     *     paymentMode = getProductGen().getDefaultPaymentMode();
     *     ... and so for other properties
     * }
     * </pre>
     */
    protected void generateMethodInitialize(JavaCodeFragmentBuilder builder) throws CoreException {
        IPolicyCmptTypeAttribute[] attributes = getPcType().getPolicyCmptTypeAttributes();
        ArrayList<IPolicyCmptTypeAttribute> selectedValues = new ArrayList<IPolicyCmptTypeAttribute>();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (!a.validate(getIpsProject()).containsErrorMsg()) {
                if (a.isProductRelevant() && a.isChangeable() && !a.isOverwrite()) {
                    selectedValues.add(a);
                }
            }
        }
        appendLocalizedJavaDoc("METHOD_INITIALIZE", getPcType(), builder);
        if (getPcType().isConfigurableByProductCmptType() || getPcType().hasSupertype()) {
            appendOverrideAnnotation(builder, false);
        }
        GenPolicyCmptType genPolicyCmptType = getGenerator();
        builder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Datatype.VOID.getJavaClassName(), genPolicyCmptType
                .getMethodNameInitialize(), EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        if (StringUtils.isNotEmpty(getPcType().getSupertype())) {
            builder.append("super." + genPolicyCmptType.getMethodNameInitialize() + "();");
        }
        if (selectedValues.isEmpty()) {
            builder.methodEnd();
            return;
        }
        if (getProductCmptType() == null) {
            builder.methodEnd();
            return;
        }
        String method = getGenProductCmptType().getMethodNameGetProductCmptGeneration();
        builder.appendln("if (" + method + "()==null) {");
        builder.appendln("return;");
        builder.appendln("}");
        for (IPolicyCmptTypeAttribute a : selectedValues) {
            GenChangeableAttribute gen = (GenChangeableAttribute)genPolicyCmptType.getGenerator(a);
            gen.generateInitialization(builder, getIpsProject());
        }
        builder.methodEnd();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IProduct getProduct() {
     *     return (IProduct) getProductComponent();
     * }
     * </pre>
     */
    protected void generateMethodGetProductCmpt(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, true);
        getGenProductCmptType().generateSignatureGetProductCmpt(builder);
        builder.openBracket();
        String productCmptInterfaceQualifiedName = getGenProductCmptType().getQualifiedName(true);
        builder.append("return (");
        builder.appendClassName(productCmptInterfaceQualifiedName);
        builder.append(")getProductComponent();"); // don't use getMethodNameGetProductComponent()
        // as this results in a recursive call
        // we have to call the generic superclass method here
        builder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IProductGen getProductGen() {
     *     return (IProductGen) getProduct().getProductGen(getEffectiveFromAsCalendar());
     * }
     * </pre>
     */
    protected void generateMethodGetProductCmptGeneration(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, true);
        getGenProductCmptType().generateSignatureGetProductCmptGeneration(builder);
        builder.openBracket();
        builder.append("return (");
        String productCmptGenInterface = getGenProductCmptType().getQualifiedClassNameForProductCmptTypeGen(true);
        builder.appendClassName(productCmptGenInterface);
        builder.append(')');
        builder.append(MethodNames.GET_PRODUCT_CMPT_GENERATION);
        builder.appendln("();");
        builder.closeBracket();
    }

    private void generateMethodSetProductComponent(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, true);
        getGenProductCmptType().generateSignatureSetProductComponent(builder);
        String[] paramNames = getGenProductCmptType().getMethodParamNamesSetProductCmpt();
        builder.openBracket();
        builder.appendln(MethodNames.SET_PRODUCT_COMPONENT + "(" + paramNames[0] + ");");
        builder.appendln("if(" + paramNames[1] + ") { initialize(); }");
        builder.closeBracket();
    }

    private String getMethodNameExecRule(IValidationRule r) {
        return StringUtils.uncapitalize(r.getName());
    }

    private String getMethodExpressionExecRule(IValidationRule r, String messageList, String businessFunction) {
        StringBuffer buf = new StringBuffer();
        buf.append(getMethodNameExecRule(r));
        buf.append('(');
        buf.append(messageList);
        buf.append(", ");
        buf.append(businessFunction);
        buf.append(")");
        return buf.toString();
    }

    /**
     * Code sample
     * 
     * <pre>
     * protected void initPropertiesFromXml(HashMap propMap) {
     *     if (propMap.containsKey(&quot;prop0&quot;)) {
     *         prop0 = (String)propMap.get(&quot;prop0&quot;);
     *     }
     *     if (propMap.containsKey(&quot;prop1&quot;)) {
     *         prop1 = (String)propMap.get(&quot;prop1&quot;);
     *     }
     * }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     * protected void initPropertiesFromXml(HashMap propMap) {
     *     final Map&lt;String, String&gt; checkedPropMap = (Map&lt;String, String&gt;)propMap;
     *     if (checkedPropMap.containsKey(&quot;prop0&quot;)) {
     *         prop0 = checkedPropMap.get(&quot;prop0&quot;);
     *     }
     *     if (checkedPropMap.containsKey(&quot;prop1&quot;)) {
     *         prop1 = checkedPropMap.get(&quot;prop1&quot;);
     *     }
     * }
     * </pre>
     */
    private void generateMethodInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        boolean first = true;
        GenPolicyCmptType genPolicyCmptType = getGenerator();
        for (GenPolicyCmptTypeAttribute generator : genPolicyCmptType.getGenAttributes()) {
            if (!generator.isMemberVariableRequired()) {
                continue;
            }
            if (first) {
                first = false;
                builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
                appendOverrideAnnotation(builder, false);
                builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, Void.TYPE.getName(),
                        MethodNames.INIT_PROPERTIES_FROM_XML, new String[] { "propMap", "productRepository" },
                        new String[] {
                                isUseTypesafeCollections() ? Map.class.getName() + "<" + String.class.getName() + ","
                                        + String.class.getName() + ">" : HashMap.class.getName(),
                                IRuntimeRepository.class.getName() });
                builder.appendln("super." + MethodNames.INIT_PROPERTIES_FROM_XML + "(propMap, productRepository);");
            }
            generator.generateInitPropertiesFromXml(builder, new JavaCodeFragment("productRepository"));
        }
        if (!first) {
            builder.methodEnd();
        }
    }

    /**
     * Code sample
     * 
     * <pre>
     *  protected AbstractPolicyComponent createChildFromXml(Element childEl) {
     *     AbstractPolicyComponent newChild ) super.createChildFromXml(childEl);
     *     if (newChild!=null) {
     *         return newChild;
     *     }
     *     String className = childEl.getAttribute(&quot;class&quot;);
     *     if (className.length&gt;0) {
     *         try {
     *             AbstractCoverage abstractCoverage = (AbstractCoverage)Class.forName(className).newInstance();
     *             addAbstractCoverage(abstractCoverage);
     *             initialize();
     *         } catch (Exception e) {
     *             throw new RuntimeException(e);
     *         }
     *     }
     *     if (&quot;Coverage&quot;.equals(childEl.getNodeName())) {
     *         (AbstractPolicyComponent)return newCovergae();
     *     }
     *     return null;
     *  }
     * </pre>
     */
    private void generateMethodCreateChildFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, AbstractModelObject.class,
                MethodNames.CREATE_CHILD_FROM_XML, new String[] { "childEl" }, new Class[] { Element.class });

        builder.appendClassName(AbstractModelObject.class);
        builder.append(" newChild = super." + MethodNames.CREATE_CHILD_FROM_XML + "(childEl);");
        builder.appendln("if (newChild!=null) {");
        builder.appendln("return newChild;");
        builder.appendln("}");

        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            IPolicyCmptTypeAssociation association = associations[i];
            if (!association.isCompositionMasterToDetail() || association.isDerivedUnion() || !association.isValid()) {
                continue;
            }
            builder.append("if (");
            builder.appendQuoted(association.getTargetRoleSingular());
            builder.appendln(".equals(childEl.getNodeName())) {");
            IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
            builder.appendln("String className = childEl.getAttribute(\"class\");");
            builder.appendln("if (className.length()>0) {");
            builder.appendln("try {");
            builder.appendClassName(getQualifiedClassName(target));
            String varName = StringUtils.uncapitalize(association.getTargetRoleSingular());
            builder.append(" " + varName + "=(");
            builder.appendClassName(getQualifiedClassName(target));
            builder.appendln(")Class.forName(className).newInstance();");
            GenAssociation generator = getGenerator(association);
            builder.append(generator.getMethodNameAddOrSetObject() + "(" + varName + ");");
            builder.appendln("return " + varName + ";");
            builder.appendln("}");
            builder.appendln("catch (Exception e) {");
            builder.appendln("throw new RuntimeException(e);");
            builder.appendln("}"); // catch
            builder.appendln("}"); // if
            if (!target.isAbstract()) {
                builder.append("return (");
                builder.appendClassName(AbstractModelObject.class);
                builder.append(")");
                builder.append(getGenerator(association).getMethodNameNewChild());
                builder.appendln("();");
            } else {
                builder
                        .appendln("throw new RuntimeException(childEl.toString() + \": Attribute className is missing.\");");
            }
            builder.appendln("}");
        }
        builder.appendln("return null;");
        builder.methodEnd();
    }

    /**
     * <pre>
     * protected abstract IUnresolvedReference createUnresolvedReference(
     *     Object objectId,
     *     String targetRole,
     *     String targetId) throws Exception {
     * 
     *     if (&quot;InsuredPeson&quot;.equals(targetRole)) {
     *         return new DefaultUnresolvedReference(this, objectId, &quot;setInsuredPerson&quot;, IInsuredPerson.class, targetId);
     *     }
     *     return super.createUnresolvedReference(objectId, targetRole, targetId);
     * }
     * </pre>
     */
    private void generateMethodCreateUnresolvedReference(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "objectId", "targetRole", "targetId" };
        Class<?>[] argClasses = new Class[] { Object.class, String.class, String.class };
        builder.methodBegin(java.lang.reflect.Modifier.PROTECTED, IUnresolvedReference.class,
                MethodNames.CREATE_UNRESOLVED_REFERENCE, argNames, argClasses, new Class[] { Exception.class });

        IPolicyCmptTypeAssociation[] associations = getPcType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < associations.length; i++) {
            if (!associations[i].isValid() || !associations[i].isAssoziation()) {
                continue;
            }
            IPolicyCmptTypeAssociation association = associations[i];
            String targetClass = GenType.getQualifiedName(association.findTargetPolicyCmptType(getIpsProject()),
                    (StandardBuilderSet)getBuilderSet(), true);
            builder.append("if (");
            builder.appendQuoted(association.getTargetRoleSingular());
            builder.append(".equals(targetRole)) {");
            builder.append("return new ");
            builder.appendClassName(DefaultUnresolvedReference.class);
            builder.append("(this, objectId, ");
            GenAssociation generator = getGenerator(association);
            builder.appendQuoted(generator.getMethodNameAddOrSetObject());
            builder.append(", ");
            builder.appendClassName(targetClass);
            builder.append(".class, targetId);");
            builder.append("}");
        }
        builder.appendln("return super." + MethodNames.CREATE_UNRESOLVED_REFERENCE
                + "(objectId, targetRole, targetId);");
        builder.methodEnd();
    }

    private void generateCodeForDependantObjectBaseClass(JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateFieldForParent(memberVarsBuilder);
        if (getPcType().isConfigurableByProductCmptType()) {
            generateMethodGetEffectiveFromAsCalendarForDependantObjectBaseClass(methodBuilder);
        }
        generateMethodGetParentModelObject(methodBuilder);
        generateMethodSetParentModelObjectInternal(methodBuilder);
    }

    protected void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException {
        super.generateConstants(builder);
        if (getPcType().getSupertype().length() == 0) {
            getGenerator().generateChangeListenerConstants(builder);
        }
    }

    /**
     * <pre>
     * private AbstractModelObject parentModelObject;
     * </pre>
     */
    private void generateFieldForParent(JavaCodeFragmentBuilder memberVarsBuilder) {
        String javadoc = getLocalizedText(getPcType(), "FIELD_PARENT_JAVADOC");
        memberVarsBuilder.javaDoc(javadoc, ANNOTATION_GENERATED);

        if (isGenerateJaxbSuppert()) {
            memberVarsBuilder.annotationLn("javax.xml.bind.annotation.XmlIDREF");
            memberVarsBuilder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name", "parent-object.id");
        }

        memberVarsBuilder.append("private ");
        memberVarsBuilder.appendClassName(AbstractModelObject.class);
        memberVarsBuilder.append(' ');
        memberVarsBuilder.append(FIELD_PARENT_MODEL_OBJECT);
        memberVarsBuilder.appendln(";");
    }

    /**
     * <pre>
     * public IModelObject getParentModelObject() {
     *     return parentModelObject;
     * }
     * </pre>
     */
    private void generateMethodGetParentModelObject(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodBuilder.methodBegin(Modifier.PUBLIC, IModelObject.class, MethodNames.GET_PARENT, EMPTY_STRING_ARRAY,
                new Class[0]);
        methodBuilder.appendln("return " + FIELD_PARENT_MODEL_OBJECT + ";");
        methodBuilder.methodEnd();
    }

    /**
     * <pre>
     * public IModelObject getParentModelObject() {
     *     if (parentModelObject != null &amp;&amp; parentModelObject != newParent) {
     *         parentModelObject.removeChildModelObjectInternal(this);
     *     }
     *     return parentModelObject;
     * }
     * </pre>
     */
    private void generateMethodSetParentModelObjectInternal(JavaCodeFragmentBuilder methodBuilder) {
        methodBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        methodBuilder.methodBegin(Modifier.PUBLIC, Void.TYPE, MethodNames.SET_PARENT, new String[] { "newParent" },
                new Class[] { AbstractModelObject.class });
        methodBuilder.appendln("if (" + FIELD_PARENT_MODEL_OBJECT + "!=null) {");
        methodBuilder.appendln(FIELD_PARENT_MODEL_OBJECT + "." + MethodNames.REMOVE_CHILD_MODEL_OBJECT_INTERNAL
                + "(this);");
        methodBuilder.appendln("}");
        methodBuilder.appendln(FIELD_PARENT_MODEL_OBJECT + "=newParent;");
        methodBuilder.methodEnd();
    }

    /**
     * <pre>
     * public Calendar getEffectiveFromAsCalendar() {
     *     IModelObject parent = getParentModelObject();
     *     if (parent instanceof IConfigurableModelObject) {
     *         return ((IConfigurableModelObject)parent).getEffectiveFromAsCalendar();
     *     }
     *     return null;
     * }
     * </pre>
     */
    protected void generateMethodGetEffectiveFromAsCalendarForDependantObjectBaseClass(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, !getPcType().hasSupertype());
        methodsBuilder.methodBegin(java.lang.reflect.Modifier.PUBLIC, Calendar.class,
                MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR, EMPTY_STRING_ARRAY, new Class[0]);
        methodsBuilder.append("if (" + FIELD_PARENT_MODEL_OBJECT + " instanceof ");
        methodsBuilder.appendClassName(IConfigurableModelObject.class);
        methodsBuilder.append(") {");
        methodsBuilder.append("return ((");
        methodsBuilder.appendClassName(IConfigurableModelObject.class);
        methodsBuilder.appendln(")" + FIELD_PARENT_MODEL_OBJECT + ")." + MethodNames.GET_EFFECTIVE_FROM_AS_CALENDAR
                + "();");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return null;");
        methodsBuilder.methodEnd();
    }

    protected void generateTableAccessMethods(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IProductCmptType productCmptType = getProductCmptType();
        if (productCmptType == null) {
            return;
        }
        IPolicyCmptType policyCmptType = getPcType();
        ITableStructureUsage[] tsus = productCmptType.getTableStructureUsages();
        IIpsProject ipsProject = getIpsProject();
        for (int i = 0; i < tsus.length; i++) {
            if (!tsus[i].isValid()) {
                continue;
            }
            String roleCapitalized = StringUtils.capitalize(tsus[i].getRoleName());
            String roleUncapitalized = StringUtils.uncapitalize(tsus[i].getRoleName());
            if (policyCmptType.findAttribute(roleCapitalized, ipsProject) != null) {
                continue; // if the policy component type has an attribute with the table usage's
                // role name, don't generate an access method for the table
            }
            if (policyCmptType.findAttribute(roleUncapitalized, ipsProject) != null) {
                continue; // if the policy component type has an attribute with the table usage's
                // role name, don't generate an access method for the table
            }

            if (policyCmptType.findAssociation(roleCapitalized, ipsProject) != null) {
                continue; // same for association
            }
            if (policyCmptType.findAssociation(roleUncapitalized, ipsProject) != null) {
                continue; // same for association
            }
            if (policyCmptType.findAssociationByRoleNamePlural(roleCapitalized, ipsProject) != null) {
                continue; // same for association
            }
            if (policyCmptType.findAssociationByRoleNamePlural(roleUncapitalized, ipsProject) != null) {
                continue; // same for association
            }
            generateMethodGetTable(methodsBuilder, tsus[i]);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     *     public TableStructure getTableStructure() {
     *         ProductGen gen = (ProductGen)getProductGen();
     *         if (gen==null) {
     *            return null;
     *         }
     *         return gen.getTableRole();
     *     }
     * </pre>
     * 
     */
    protected void generateMethodGetTable(JavaCodeFragmentBuilder builder, ITableStructureUsage usage)
            throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_TABLE", usage.getRoleName(), usage, builder);
        GenTableStructureUsage genTsu = getTsuGeneratorForProductType(usage);
        if (genTsu == null) {
            return;
        }
        builder.methodBegin(Modifier.PUBLIC, genTsu.getReturnTypeOfMethodGetTableUsage(), genTsu
                .getMethodNameGetTableUsage(), EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        String productCmptGenClass = getGenProductCmptType().getQualifiedClassNameForProductCmptTypeGen(false);
        builder.appendClassName(productCmptGenClass);
        builder.append(" productCmpt = (");
        builder.appendClassName(productCmptGenClass);
        builder.append(")");
        builder.append(getGenProductCmptType().getMethodNameGetGeneration());
        builder.append("();");
        builder.appendln("if (productCmpt == null) {");
        builder.appendln("return null;");
        builder.appendln("}");
        builder.appendln("return productCmpt." + genTsu.getMethodNameGetTableUsage() + "();");
        builder.closeBracket();
    }

    private GenTableStructureUsage getTsuGeneratorForProductType(ITableStructureUsage tsu) throws CoreException {
        GenProductCmptType genProductCmptType = ((StandardBuilderSet)getBuilderSet())
                .getGenerator(getProductCmptType());
        if (genProductCmptType == null) {
            return null;
        }
        return genProductCmptType.getGenerator(tsu);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Outputs a @XmlRootElement annotation if JAXB support is enabled.
     * </p>
     */
    @Override
    protected void generateTypeAnnotations(JavaCodeFragmentBuilder builder) throws CoreException {
        super.generateTypeAnnotations(builder);

        if (!getGenPolicyCmptType().getBuilderSet().isGenerateJaxbSupport()) {
            return;
        }
        builder.annotationLn("javax.xml.bind.annotation.XmlRootElement", "name", getUnqualifiedClassName());
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    private class CheckForOverrideAnnotationForNewCopyMethod extends PolicyCmptTypeHierarchyVisitor {

        boolean implementsInterfaceMethod = true;

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            implementsInterfaceMethod = currentType.isAbstract();
            if (!implementsInterfaceMethod) {
                return false;
            }
            return true;
        }
    }

}
