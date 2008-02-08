/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Range;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.EnumValueSet;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenImplClassBuilder extends AbstractProductCmptTypeBuilder{
    
    // property key for the constructor's Javadoc.
    private final static String GET_TABLE_USAGE_METHOD_JAVADOC = "GET_TABLE_USAGE_METHOD_JAVADOC";
    
    public static final String XML_ATTRIBUTE_TARGET_RUNTIME_ID = "targetRuntimeId";
    
    private ProductCmptGenInterfaceBuilder interfaceBuilder;
    private ProductCmptImplClassBuilder productCmptTypeImplCuBuilder;
    private ProductCmptInterfaceBuilder productCmptTypeInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    
    private TableImplBuilder tableImplBuilder;
    
    public ProductCmptGenImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
        setMergeEnabled(true);
    }
    
    public void setInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        ArgumentCheck.notNull(builder);
        this.interfaceBuilder = builder;
    }
    
    public ProductCmptGenInterfaceBuilder getInterfaceBuilder() {
        return interfaceBuilder;
    }
    
    public void setProductCmptTypeImplBuilder(ProductCmptImplClassBuilder builder) {
        ArgumentCheck.notNull(builder);
        productCmptTypeImplCuBuilder = builder;
    }
    
    public void setProductCmptTypeInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        this.productCmptTypeInterfaceBuilder = builder;
    }

    public void setTableImplBuilder(TableImplBuilder tableImplBuilder) {
        this.tableImplBuilder = tableImplBuilder;
    }

    /**
     * If a policy component type contains an derived or computed attribute, the product component
     * generation class must be abstract, as the computation formulas are defined per generation. 
     * 
     * {@inheritDoc}
     */
    protected int getClassModifier() throws CoreException {
        int modifier = super.getClassModifier();
        if ((modifier & Modifier.ABSTRACT) > 0) {
            return modifier;
        }
        GetClassModifierFunction fct = new GetClassModifierFunction(getIpsProject(), modifier);
        fct.start(getProductCmptType());
        return fct.getModifier();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return getJavaNamingConvention().getImplementationClassName(ipsSrcFile.getIpsObjecName() + generationAbb);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype != null) {
            String pack = getPackage(supertype.getIpsSrcFile());
            return StringUtil.qualifiedName(pack, getUnqualifiedClassName(supertype.getIpsSrcFile()));
        }
        return ProductComponentGeneration.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        // The implementation implements the published interface.
        return new String[] { interfaceBuilder.getQualifiedClassName(getIpsSrcFile()) };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CLASS", interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile()), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder);
        builder.append("public ");
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(productCmptTypeImplCuBuilder.getQualifiedClassName(getIpsSrcFile()));
        builder.append(" productCmpt)");
        builder.openBracket();
        builder.appendln("super(productCmpt);");
        builder.closeBracket();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        generateMethodDoInitReferencesFromXml(methodsBuilder);
        generateMethodDoInitTableUsagesFromXml(methodsBuilder);
    }
    
    private void generateMethodDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        builder.methodBegin(Modifier.PROTECTED, Void.TYPE, "doInitPropertiesFromXml", 
                new String[]{"configMap"}, new Class[]{Map.class});
        
        builder.appendln("super.doInitPropertiesFromXml(configMap);");
        
        boolean attributeFound = false;
        IProductCmptTypeAttribute[] productAttributes = getProductCmptType().getProductCmptTypeAttributes();
        for (int i = 0; i < productAttributes.length; i++) {
            IProductCmptTypeAttribute a = productAttributes[i];
            if (a.validate(getIpsProject()).containsErrorMsg()) {
                continue;
            }
            if (attributeFound == false) {
                generateDefineLocalVariablesForXmlExtraction(builder);
                attributeFound = true;
            }
            ValueDatatype datatype = a.findDatatype(getIpsProject());
            DatatypeHelper helper = getIpsProject().getDatatypeHelper(datatype);
            generateGetElementFromConfigMapAndIfStatement(a.getName(), builder);
            generateExtractValueFromXml(getFieldNameValue(a), helper, builder);
            builder.closeBracket(); // close if statement generated three lines above
        }
        IPolicyCmptType policyCmptType = getPolicyCmptType();
        IPolicyCmptTypeAttribute[] attributes = policyCmptType == null ? new IPolicyCmptTypeAttribute[0] : policyCmptType.getPolicyCmptTypeAttributes();
        for (int i = 0; i < attributes.length; i++) {
            IPolicyCmptTypeAttribute a = attributes[i];
            if (a.validate(getIpsProject()).containsErrorMsg()) {
                continue;
            }
            if (!a.isProductRelevant() || !a.isChangeable()) {
                continue;
            }
            if (attributeFound == false) {
                generateDefineLocalVariablesForXmlExtraction(builder);
                attributeFound = true;
            }
            ValueDatatype datatype = a.findDatatype(getIpsProject());
            DatatypeHelper helper = getProductCmptType().getIpsProject().getDatatypeHelper(datatype);
            generateGetElementFromConfigMapAndIfStatement(a.getName(), builder);
            generateExtractValueFromXml(getFieldNameDefaulValue(a), helper, builder);
            generateExtractValueSetFromXml(a, helper, builder);
            builder.closeBracket(); // close if statement generated three lines above
        }
        builder.methodEnd();
    }
    
    private void generateDefineLocalVariablesForXmlExtraction(JavaCodeFragmentBuilder builder) {
        builder.appendClassName(Element.class);
        builder.appendln(" configElement = null;");
        builder.appendClassName(String.class);
        builder.appendln(" value = null;");
    }

    private void generateGetElementFromConfigMapAndIfStatement(String attributeName, JavaCodeFragmentBuilder builder) {
        builder.append("configElement = (");
        builder.appendClassName(Element.class);
        builder.append(")configMap.get(\"");
        builder.append(attributeName);
        builder.appendln("\");");
        builder.append("if (configElement != null) ");
        builder.openBracket();
    }
    
    private void generateExtractValueFromXml(String memberVar, DatatypeHelper helper, JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append("value = ");
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".getValueFromElement(configElement, \"Value\");");
        builder.append(memberVar);
        builder.append(" = ");
        builder.append(helper.newInstanceFromExpression("value"));
        builder.appendln(";");
    }
    
    private void generateExtractValueSetFromXml(IPolicyCmptTypeAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder builder) throws CoreException {
        ValueSetType valueSetType = a.getValueSet().getValueSetType();
        JavaCodeFragment frag = new JavaCodeFragment();
        helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), helper);
        if(ValueSetType.RANGE.equals(valueSetType)){
            frag.appendClassName(Range.class);
            frag.append(" range = ");
            frag.appendClassName(ValueToXmlHelper.class);
            frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");");
            frag.append(getFieldNameRangeFor(a));
            frag.append(" = ");
            JavaCodeFragment newRangeInstanceFrag = helper.newRangeInstance(
                    new JavaCodeFragment("range.getLower()"), new JavaCodeFragment("range.getUpper()"),
                    new JavaCodeFragment("range.getStep()"), new JavaCodeFragment("range.containsNull()"));
            if(newRangeInstanceFrag == null){
                throw new CoreException(new IpsStatus("The " + helper + " for the datatype " +  helper.getDatatype().getName() + " doesn't support ranges."));
            }
            frag.append(newRangeInstanceFrag);
            frag.appendln(";");
        }
        else if(ValueSetType.ENUM.equals(valueSetType)){
            frag.appendClassName(EnumValues.class);
            frag.append(" values = ");
            frag.appendClassName(ValueToXmlHelper.class);
            frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");");
            frag.appendClassName(ArrayList.class);
            frag.append(" enumValues = new ");
            frag.appendClassName(ArrayList.class);
            frag.append("();");
            frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)");
            frag.appendOpenBracket();
            frag.append("enumValues.add(");
            frag.append(helper.newInstanceFromExpression("values.getValue(i)"));
            frag.appendln(");");
            frag.appendCloseBracket();
            frag.append(getFieldNameAllowedValuesFor(a));
            frag.append(" = ");
            frag.append(helper.newEnumValueSetInstance(new JavaCodeFragment("enumValues"), 
                    new JavaCodeFragment("values.containsNull()")));
            frag.appendln(";");
        }
        builder.append(frag);
    }
    
    private void generateMethodDoInitReferencesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String[] argNames = new String[]{"elementsMap"};
        String[] argTypes = new String[]{Map.class.getName()};
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitReferencesFromXml", argNames, argTypes);
        builder.appendln("super.doInitReferencesFromXml(elementsMap);");
        
        // before the first association we define a temp variable as follows:
        // Element associationElements = null;

        // for each 1-1 association in the policy component type we generate:
        // associationElements = (ArrayList) associationMap.get("Product");
        // if(associationElement != null) {
        //     vertragsteilePk = ((Element)associationElement.get(0)).getAttribute("targetRuntimeId");
        // }
        // 
        // for each 1-many association in the policy component type we generate:
        // associationElements = (ArrayList) associationMap.get("Product");
        // if(associationElement != null) {
        //     vertragsteilPks[] = new VertragsteilPk[associationElements.length()];
        //     for (int i=0; i<vertragsteilsPks.length; i++) {
        //         vertragsteilPks[i] = ((Element)associationElement.get(i)).getAttribute("targetRuntimeId");
        //         }
        //     }
        // }
        IAssociation[] associations = getProductCmptType().getAssociations();
        boolean associationFound = false;
        for (int i = 0; i < associations.length; i++) {
            IProductCmptTypeAssociation ass = (IProductCmptTypeAssociation)associations[i];
            if (!ass.isDerived()) {
                if (associationFound == false) {
                    builder.appendln();
                    builder.appendClassName(List.class);
                    builder.append(" ");
                    associationFound = true;
                }
                builder.append("associationElements = (");
                builder.appendClassName(List.class);
                builder.append(") elementsMap.get(");
                builder.appendQuoted(ass.getName());
                builder.appendln(");");
                builder.append("if (associationElements != null) {");
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = ass.findMatchingPolicyCmptTypeAssociation(getIpsProject());
                String cardinalityFieldName = policyCmptTypeAssociation == null ? "" : getFieldNameCardinalityForAssociation(ass);
                if (ass.is1ToMany()) {
                    String fieldName = getFieldNameToManyAssociation(ass);
                    builder.append(fieldName);
                    builder.appendln(" = new ");
                    builder.appendClassName(String.class);
                    builder.appendln("[associationElements.size()];");
                    if (policyCmptTypeAssociation!=null) {
                        builder.append(cardinalityFieldName);
                        builder.append(" = new ");
                        builder.appendClassName(HashMap.class);
                        builder.appendln("(associationElements.size());");
                    }
                    builder.appendln("for (int i=0; i<associationElements.size(); i++) {");
                    builder.appendClassName(Element.class);
                    builder.append(" element = (");
                    builder.appendClassName(Element.class);
                    builder.appendln(")associationElements.get(i);");
                    builder.append(fieldName);
                    builder.append("[i] = ");
                    builder.appendln("element.getAttribute(\"" + XML_ATTRIBUTE_TARGET_RUNTIME_ID + "\");");
                    if (policyCmptTypeAssociation!=null) {
                        builder.append("addToCardinalityMap(");
                        builder.append(cardinalityFieldName);
                        builder.append(", ");
                        builder.append(fieldName);
                        builder.append("[i], ");
                        builder.appendln("element);");
                    }
                    builder.appendln("}");
                } else {
                    String fieldName = getFieldNameTo1Association(ass);
                    builder.appendClassName(Element.class);
                    builder.append(" element = (");
                    builder.appendClassName(Element.class);
                    builder.appendln(")associationElements.get(0);");
                    builder.append(fieldName);
                    builder.append(" = ");
                    builder.appendln("element.getAttribute(\"" + XML_ATTRIBUTE_TARGET_RUNTIME_ID + "\");");
                    if (policyCmptTypeAssociation!=null) {
                        builder.append(cardinalityFieldName);
                        builder.append(" = new ");
                        builder.appendClassName(HashMap.class);
                        builder.appendln("(1);");
                        builder.append("addToCardinalityMap(");
                        builder.append(cardinalityFieldName);
                        builder.append(", ");
                        builder.append(fieldName);
                        builder.appendln(", element);");
                    }
                }
                builder.appendln("}");
            }
        }
        builder.methodEnd();
    }

    private void generateMethodDoInitTableUsagesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type==null) {
            return;
        }
        ITableStructureUsage[] tsus = type.getTableStructureUsages();
        if (tsus.length == 0){
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String[] argNames = new String[]{"tableUsageMap"};
        String[] argTypes = new String[]{Map.class.getName()};
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitTableUsagesFromXml", argNames, argTypes);
        builder.appendln("super.doInitTableUsagesFromXml(tableUsageMap);");
        builder.appendClassName(Element.class);
        builder.appendln(" element = null;");
        for (int i = 0; i < tsus.length; i++) {
            builder.append(" element = (");
            builder.appendClassName(Element.class);
            builder.append(") tableUsageMap.get(\"");
            builder.append(tsus[i].getRoleName());
            builder.appendln("\");");
            builder.appendln("if (element != null){");
            builder.append(getTableStructureUsageRoleName(tsus[i]));
            builder.appendln(" = ");
            builder.appendClassName(ValueToXmlHelper.class);
            builder.append(".getValueFromElement(element, \"TableContentName\");");
            builder.appendln("};");
        }
        builder.appendln("};");
    }
    
    /*
     * Generates the method to return the table content which is related to the specific role.<br>
     * Example:
     * <code>
     *   public FtTable getRatePlan() {
     *      if (ratePlanName == null) {
     *          return null;
     *      }
     *      return (FtTable)getRepository().getTable(ratePlanName);
     *  }
     * <code>
     */
    private void generateMethodGetTableStructure(ITableStructureUsage tsu, JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        // generate the method to return the corresponding table content
        String methodName = getMethodNameGetTableUsage(tsu);
        String[] tss = tsu.getTableStructures();
        // get the class name of the instance which will be returned,
        // if the usage contains only one table structure then the returned class will be the
        // generated class of this table structure, otherwise the return class will be the ITable interface class
        String tableStructureClassName = "";
        if (tss.length == 1) {
            tableStructureClassName = tss[0];
            ITableStructure ts = (ITableStructure) getProductCmptType().getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, tableStructureClassName);
            if (ts == null){
                // abort because table structure not found
                return;
            }
            tableStructureClassName = tableImplBuilder.getQualifiedClassName(ts.getIpsSrcFile());
        } else if (tss.length > 1) {
            tableStructureClassName = ITable.class.getName();
        } else {
            // if no table structure is related, do nothing, this is a validation error
            return;
        }

        String javaDoc = getLocalizedText(getIpsSrcFile(), GET_TABLE_USAGE_METHOD_JAVADOC, tsu.getRoleName());
        String roleName = getTableStructureUsageRoleName(tsu);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if (");
        body.append(roleName);
        body.appendln(" == null){");
        body.appendln("return null;");
        body.appendln("}");
        body.append("return ");
        body.append("(");
        body.appendClassName(tableStructureClassName);
        body.append(")");
        body.append(MethodNames.GET_REPOSITORY);
        body.append("().getTable(");
        body.append(roleName);
        body.appendln(");");
        codeBuilder.method(Modifier.PUBLIC, tableStructureClassName, methodName, new String[0], new String[0], body,
                javaDoc, ANNOTATION_GENERATED);
    }

    private String getMethodNameGetTableUsage(ITableStructureUsage tsu) {
        return "get" + StringUtils.capitalize(tsu.getRoleName());
    }
    
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateFieldDefaultValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetDefaultValue(a, datatypeHelper, methodsBuilder);

        //if the datatype is a primitive datatype the datatypehelper will be switched to the helper of the 
        //wrapper type
        datatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), datatypeHelper);
        if(ValueSetType.RANGE.equals(a.getValueSet().getValueSetType())){
            generateFieldRangeFor(a , datatypeHelper, memberVarsBuilder);
            generateMethodGetRangeFor(a, datatypeHelper, methodsBuilder);
        }
        else if(ValueSetType.ENUM.equals(a.getValueSet().getValueSetType())){
            generateFieldAllowedValuesFor(a, memberVarsBuilder);
            generateMethodGetAllowedValuesFor(a, datatypeHelper.getDatatype(), methodsBuilder);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer minAge;
     * </pre>
     */
    private void generateFieldDefaultValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", a.getName(), a, memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaulValue(a), defaultValueExpression);
    }    
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge() {
     *     return minAge;
     * </pre>
     */
    private void generateMethodGetDefaultValue(IPolicyCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetDefaultValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameDefaulValue(a));
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameDefaulValue(IPolicyCmptTypeAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameDefaultValue(a));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForProductCmptTypeAttribute(
            IProductCmptTypeAttribute a, 
            DatatypeHelper datatypeHelper, 
            JavaCodeFragmentBuilder memberVarsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder, 
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {
        
        generateFieldValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetValue(a, datatypeHelper, methodsBuilder);
        generateMethodSetValue(a, datatypeHelper, methodsBuilder);
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer taxRate;
     * </pre>
     */
    private void generateFieldValue(IProductCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_VALUE", StringUtils.capitalize(a.getName()), a, builder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        builder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameValue(a), defaultValueExpression);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getInterestRate() {
     *     return interestRate;
     * </pre>
     */
    private void generateMethodGetValue(IProductCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameValue(a));
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setInterestRate(Decimal newValue) {
     *     if (getRepository()!=null && !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.interestRate = newValue;
     * }
     * </pre>
     */
    private void generateMethodSetValue(IProductCmptTypeAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_SET_VALUE", a.getName(), a, methodsBuilder);
        String methodName = getJavaNamingConvention().getSetterMethodName(interfaceBuilder.getPropertyNameValue(a), datatypeHelper.getDatatype());
        String[] paramNames = new String[]{"newValue"};
        String[] paramTypes = new String[]{datatypeHelper.getJavaClassName()};
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + getFieldNameValue(a));
        methodsBuilder.appendln(" = newValue;");
        methodsBuilder.closeBracket();
    }
    
    private JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()." + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
    }
    
    private String getFieldNameValue(IProductCmptTypeAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameValue(a));
    }

    
    /**
     * Code sample:
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age) throws FormulaException
     * </pre>
     */
    public void generateCodeForModelMethod(
            IProductCmptTypeMethod method,
            JavaCodeFragmentBuilder fieldsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        if (method.isFormulaSignatureDefinition()) {
            if (method.getModifier().isPublished()) {
                // nothing to do, signature is generated by the interface builder, implementation by the product component builder.
            } else {
                methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
                productCmptGenInterfaceBuilder.generateSignatureForModelMethod(method, true, false, methodsBuilder);
                methodsBuilder.append(';');
            }
            if(method.isOverloadsFormula()){
                IProductCmptTypeMethod overloadedFormulaMethod = method.findOverloadedFormulaMethod(getIpsProject());
                methodsBuilder.appendln();
                methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
                productCmptGenInterfaceBuilder.generateSignatureForModelMethod(overloadedFormulaMethod, false, false, methodsBuilder);
                methodsBuilder.openBracket();
                methodsBuilder.appendln("// TODO a delegation to the method " + method.getSignatureString() + " needs to be implemented here");
                methodsBuilder.appendln("// And make sure to disable the regeneration of this method.");
                methodsBuilder.append("throw new ");
                methodsBuilder.appendClassName(RuntimeException.class);
                methodsBuilder.appendln("(\"Not implemented yet.\");");
                methodsBuilder.closeBracket();
            }
            return;
        }
        if (method.getModifier().isPublished()) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        } else {
            methodsBuilder.javaDoc(method.getDescription(), ANNOTATION_GENERATED);
        }
        productCmptGenInterfaceBuilder.generateSignatureForModelMethod(method, method.isAbstract(), false, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.appendln("// TODO implement method!");
        Datatype datatype = method.getIpsProject().findDatatype(method.getDatatype());
        if (!datatype.isVoid()) {
            if (datatype.isValueDatatype()) {
                methodsBuilder.appendln("return " + ((ValueDatatype)datatype).getDefaultValue() + ';');
            } else {
                methodsBuilder.appendln("return null;");
            }
        }
        methodsBuilder.closeBracket();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // generate the code for the fields role name which will be initialized with the related
        // table content qualified name
        appendLocalizedJavaDoc("FIELD_TABLE_USAGE", tsu.getRoleName(), tsu, fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment("null");
        fieldsBuilder.varDeclaration(Modifier.PROTECTED, String.class, getTableStructureUsageRoleName(tsu), expression);
        // generate the code for the table getter methods
        generateMethodGetTableStructure(tsu, methodsBuilder);
    }

    public String getTableStructureUsageRoleName(ITableStructureUsage tsu){
        return tsu.getRoleName() + "Name";
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (association.is1ToMany()) {
            generateFieldToManyAssociation(association, memberVarsBuilder);
            generateMethodGetManyRelatedCmpts(association, methodsBuilder);
            generateMethodGetRelatedCmptAtIndex(association, methodsBuilder);
            generateMethodAddRelatedCmpt(association, methodsBuilder);
        } else {
            generateFieldTo1Association(association, memberVarsBuilder);
            generateMethodGet1RelatedCmpt(association, methodsBuilder);
            generateMethodSet1RelatedCmpt(association, methodsBuilder);
        }
        if (association.findMatchingPolicyCmptTypeAssociation(getIpsProject())!=null) {
            generateMethodGetCardinalityFor1ToManyAssociation(association, methodsBuilder);
            generateFieldCardinalityForAssociation(association, memberVarsBuilder);
        }
        if (association.is1ToMany()) {
            generateMethodGetNumOfRelatedProductCmpts(association, methodsBuilder);  
        }
    }
    
    private String getFieldNameToManyAssociation(IProductCmptTypeAssociation association) throws CoreException {
        return getJavaNamingConvention().getMultiValueMemberVarName(interfaceBuilder.getPropertyNameToManyAssociation(association));
    }
    
    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * private CoverageType[] optionalCoverageTypes;
     * </pre>
     */
    private void generateFieldToManyAssociation(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRolePlural());
        appendLocalizedJavaDoc("FIELD_TOMANY_RELATION", role, association, memberVarsBuilder);
        String type = String.class.getName() + "[]";
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyAssociation(association), new JavaCodeFragment("new String[0]"));
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[coverageTypes.length];
     *     for (int i = 0; i < result.length; i++) {
     *         result[i] = (ICoverageType) getRepository().getProductComponent(coverageTypes[i]);
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(
            IProductCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetManyRelatedCmpts(association, methodsBuilder);

        String fieldName = getFieldNameToManyAssociation(association);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".length];");

        methodsBuilder.appendln("for (int i=0; i<result.length; i++) {");
        methodsBuilder.appendln("result[i] = (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln("[i]);");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return result;");
    
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType[] target) {
     *     if (getRepository()!=null && !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     String[] tmp = new String[coverageTypes.length+1];
     *     System.arraycopy(coverageTypes, 0, tmp, 0, coverageTypes.length);
     *     tmp[tmp.length-1] = target.getId();
     *     coverageTypes = tmp;
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmpt(
            IProductCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IProductCmptType target = association.findTargetProductCmptType(getIpsProject());
        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT", association.getTargetRoleSingular(), association, methodsBuilder);
        String methodName = "add" + StringUtils.capitalize(association.getTargetRoleSingular());
        String[] argNames = new String[]{"target"};
        String[] argTypes = new String[]{productCmptTypeInterfaceBuilder.getQualifiedClassName(target)};
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), 
                "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameToManyAssociation(association);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.appendln("String[] tmp = new String[this." + fieldName + ".length+1];");
        methodsBuilder.appendln("System.arraycopy(this." + fieldName + ", 0, tmp, 0, this." + fieldName + ".length);");
        methodsBuilder.appendln("tmp[tmp.length-1] = " + argNames[0] + "."  + MethodNames.GET_PRODUCT_COMPONENT_ID + "();");
        methodsBuilder.appendln("this." + fieldName + " = tmp;");
        methodsBuilder.closeBracket();
    }
        
    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType(int index) {
     *     return (ICoverageType) getRepository().getProductComponent(coverageTypes[index]);
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptAtIndex(
            IProductCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRelatedCmptsAtIndex(association, methodsBuilder);
        String fieldName = getFieldNameToManyAssociation(association);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append("[index]);");
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameTo1Association(IProductCmptTypeAssociation association) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameTo1Association(association));
    }

    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * private CoverageType mainCoverage;
     * </pre>
     */
    private void generateFieldTo1Association(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalize(association.getTargetRoleSingular());
        appendLocalizedJavaDoc("FIELD_TO1_RELATION", role, association, memberVarsBuilder);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, String.class, getFieldNameTo1Association(association), new JavaCodeFragment("null"));
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType() {
     *     return (CoveragePk) getRepository().getProductComponent(mainCoverageType);
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmpt(
            IProductCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGet1RelatedCmpt(association, methodsBuilder);
        String fieldName = getFieldNameTo1Association(association);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public void setMainCoverageType(ICoverageType target) {
     *     mainCoverageType = target==null ? null : target.getId();
     * }
     * </pre>
     */
    private void generateMethodSet1RelatedCmpt(
            IProductCmptTypeAssociation association, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_1_RELATED_CMPT", association.getTargetRoleSingular(), association, methodsBuilder);

        String propName = interfaceBuilder.getPropertyNameTo1Association(association);
        String methodName = getJavaNamingConvention().getSetterMethodName(propName, Datatype.INTEGER);
        String[] argNames = new String[]{"target"};
        String[] argTypes = new String[]{productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()))};
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameTo1Association(association);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append(fieldName + " = (" + argNames[0] + "==null ? null : " + argNames[0] + "." + MethodNames.GET_PRODUCT_COMPONENT_ID + "() );");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation containerAssociation, List implAssociations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMethodGetRelatedCmptsInContainer(containerAssociation, implAssociations, methodsBuilder);
        if (containerAssociation.is1ToMany()) {
            generateMethodGetNumOfRelatedProductCmpts(containerAssociation, implAssociations, methodsBuilder);
            generateMethodGetNumOfRelatedProductCmptsInternal(containerAssociation, implAssociations, methodsBuilder);
        }
    }
    
    /**
     * Code sample where a 1-1 and a 1-many association implement a container association.
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[getNumOfCoverageTypes()];
     *     int index = 0;
     *     if (collisionCoverageType!=null) {
     *         result[index++] = getCollisionCoverageType();
     *     }
     *     ITplCoverageType[] tplCoverageTypesObjects = getTplcCoverageTypes();
     *     for (int i=0; i<tplCoverageTypesObjects.length; i++) {
     *         result[index++] = tplCoverageTypes[i];
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptsInContainer(
            IProductCmptTypeAssociation association,
            List implAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureDerivedUnionAssociation(association, methodsBuilder);

        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(association.findTarget(getIpsProject()));
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(getMethodNameGetNumOfRelatedCmptsInternal(association));
        methodsBuilder.appendln("()];");

        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype!=null && !supertype.isAbstract()) {
            // ICoverage[] superResult = super.getCoverages();
            // System.arraycopy(superResult, 0, result, 0, superResult.length);
            // int counter = superResult.length;
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[] superResult = super.");       
            methodsBuilder.appendln(interfaceBuilder.getMethodNameGetManyRelatedCmpts(association) + "();");
            methodsBuilder.appendln("System.arraycopy(superResult, 0, result, 0, superResult.length);");
            methodsBuilder.appendln("int index = superResult.length;");
        } else {
            methodsBuilder.append("int index = 0;");
        }
        for (Iterator it = implAssociations.iterator(); it.hasNext();) {
            IProductCmptTypeAssociation implAssociation = (IProductCmptTypeAssociation)it.next();
            if (implAssociation.is1ToMany()) {
                String objectArrayVar = getFieldNameToManyAssociation(implAssociation) + "Objects";
                String getterMethod = interfaceBuilder.getMethodNameGetManyRelatedCmpts(implAssociation) + "()";
                methodsBuilder.appendClassName(productCmptTypeInterfaceBuilder.getQualifiedClassName(implAssociation.findTarget(getIpsProject())));
                methodsBuilder.append("[] " + objectArrayVar + " = " + getterMethod + ";");
                methodsBuilder.appendln("for (int i=0; i<" + objectArrayVar + ".length; i++) {");
                methodsBuilder.appendln("result[index++] = " + objectArrayVar + "[i];");
                methodsBuilder.appendln("}");
            } else {
                String accessCode;
                if (implAssociation.isDerivedUnion()) {
                    // if the implementation association is itself a container association, use the access method
                    accessCode = interfaceBuilder.getMethodNameGet1RelatedCmpt(implAssociation) + "()";
                } else {
                    // otherwise use the field.
                    accessCode = getFieldNameTo1Association(implAssociation);
                }
                methodsBuilder.appendln("if (" + accessCode + "!=null) {");
                methodsBuilder.appendln("result[index++] = " + interfaceBuilder.getMethodNameGet1RelatedCmpt(implAssociation) + "();");
                methodsBuilder.appendln("}");
            }
        }
        methodsBuilder.appendln("return result;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Generates the getNumOfXXX() method for none container associations. 
     * <p>
     * Code sample for 1-1 associations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageType==null ? 0 : 1;
     * }
     * </pre>
     * <p>
     * Code sample for 1-many associations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageTypes.length;
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmpts(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder builder) throws CoreException {
        if (association.isDerivedUnion()) {
            throw new IllegalArgumentException("Association needn't be a container association.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRelatedCmpts(association, builder);
        builder.openBracket();
        builder.append("return ");
        if (association.is1ToMany()) {
            builder.append(getFieldNameToManyAssociation(association));
            builder.appendln(".length;");
        } else {
            builder.append(getFieldNameTo1Association(association));
            builder.appendln(" ==null ? 0 : 1;");
        }
        builder.closeBracket();
    }

    /**
     * Generates the getNumOfXXX() method for a container association. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return getNumOfCoverageTypesInternal();
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmpts(IProductCmptTypeAssociation containerAssociation, List implAssociations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!containerAssociation.isDerivedUnion()) {
            throw new IllegalArgumentException("Association must be a container association.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRelatedCmpts(containerAssociation, builder);
        builder.openBracket();
        String internalMethodName = getMethodNameGetNumOfRelatedCmptsInternal(containerAssociation);
        builder.appendln("return " + internalMethodName + "();");
        builder.closeBracket();
    }
    
    /**
     * Generates the getNumOfXXXInternal() method for a container association. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypesInternal() {
     *     int numOf = 0;
     *     numOf += getNumOfCollisionCoverages();
     *     numOf += getNumOfTplCoverages();
     *     return numOf;
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmptsInternal(IProductCmptTypeAssociation containerAssociation, List implAssociations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!containerAssociation.isDerivedUnion()) {
            throw new IllegalArgumentException("Association must be a container association.");
        }
        builder.javaDoc("", ANNOTATION_GENERATED);
        String methodName = getMethodNameGetNumOfRelatedCmptsInternal(containerAssociation);
        builder.signature(java.lang.reflect.Modifier.PRIVATE, "int", methodName, new String[]{}, new String[]{});
        builder.openBracket();
        builder.appendln("int num = 0;");
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype!=null && !supertype.isAbstract()) {
            String methodName2 = interfaceBuilder.getMethodNameGetNumOfRelatedCmpts(containerAssociation);
            builder.appendln("num += super." + methodName2 + "();");
        }
        for (Iterator it = implAssociations.iterator(); it.hasNext();) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)it.next();
            builder.append("num += ");
            if (association.is1To1()) {
                builder.append(getFieldNameTo1Association(association) + "==null ? 0 : 1;");
            } else {
                builder.append(interfaceBuilder.getMethodNameGetNumOfRelatedCmpts(association));
                builder.append("();");
            }
        }
        builder.appendln("return num;");
        builder.closeBracket();
    }
    
    /*
     * Returns the name of the internal method returning the number of referenced objects,
     * e.g. getNumOfCoveragesInternal()
     */
    private String getMethodNameGetNumOfRelatedCmptsInternal(IProductCmptTypeAssociation association) {
        return getLocalizedText(association, "METHOD_GET_NUM_OF_INTERNAL_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }
    
    private void generateMethodGetCardinalityFor1ToManyAssociation(IProductCmptTypeAssociation association, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("@inheritDoc", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetCardinalityForAssociation(association, methodsBuilder);
        String[][] params = interfaceBuilder.getParamGetCardinalityForAssociation(association);
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendOpenBracket();
        frag.append("if(");
        frag.append(params[0][0]);
        frag.append(" != null)");
        frag.appendOpenBracket();
        frag.append("return ");
        frag.append('(');
        frag.appendClassName(IntegerRange.class);
        frag.append(')');
        frag.append(getFieldNameCardinalityForAssociation(association));
        frag.append(".get(");
        frag.append(params[0][0]);
        frag.append(".getId());");
        frag.appendCloseBracket();
        frag.append("return null;");
        frag.appendCloseBracket();
        methodsBuilder.append(frag);
    }

    public String getFieldNameCardinalityForAssociation(IProductCmptTypeAssociation association) throws CoreException{
        return getLocalizedText(association, "FIELD_CARDINALITIES_FOR_NAME", association.findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular());
    }
    
    private void generateFieldCardinalityForAssociation(
            IProductCmptTypeAssociation association, JavaCodeFragmentBuilder fieldsBuilder) throws CoreException{
        appendLocalizedJavaDoc("FIELD_CARDINALITIES_FOR", association.findMatchingPolicyCmptTypeAssociation(getIpsProject()).getTargetRoleSingular(), association, fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment();
        expression.append(" new ");
        expression.appendClassName(HashMap.class);
        expression.append("(0);");
        fieldsBuilder.varDeclaration(Modifier.PRIVATE, Map.class, getFieldNameCardinalityForAssociation(association), expression);
    }
    
    private void generateMethodGetRangeFor(IPolicyCmptTypeAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRangeFor(a, helper, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameRangeFor(a));
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameRangeFor(IPolicyCmptTypeAttribute a){
        return getLocalizedText(a, "FIELD_RANGE_FOR_NAME", StringUtils.capitalize(a.getName()));
    }
    
    private void generateFieldRangeFor(IPolicyCmptTypeAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_RANGE_FOR", a.getName(), a, memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, helper.getRangeJavaClassName(), getFieldNameRangeFor(a)); 
    }

    private void generateMethodGetAllowedValuesFor(IPolicyCmptTypeAttribute a, Datatype datatype, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllowedValuesFor(a, datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameAllowedValuesFor(a));
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameAllowedValuesFor(IPolicyCmptTypeAttribute a){
        return getLocalizedText(a, "FIELD_ALLOWED_VALUES_FOR_NAME", StringUtils.capitalize(a.getName()));
    }
    
    private void generateFieldAllowedValuesFor(IPolicyCmptTypeAttribute a, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_ALLOWED_VALUES_FOR", a.getName(), a, memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, EnumValueSet.class, getFieldNameAllowedValuesFor(a)); 
    }

    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder) {
        this.productCmptGenInterfaceBuilder = productCmptGenInterfaceBuilder;
    }

    class GetClassModifierFunction extends ProductCmptTypeHierarchyVisitor {

        private int modifier;
        
        public GetClassModifierFunction(IIpsProject ipsProject, int modifier) {
            super(ipsProject);
            this.modifier = modifier;
        }
        
        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType type) {
            IProductCmptTypeMethod[] methods = type.getProductCmptTypeMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isFormulaSignatureDefinition()) {
                    modifier = modifier | Modifier.ABSTRACT;
                    return false;
                }
            }
            return true;
        }
        
        public int getModifier() {
            return modifier;
        }
        
    }

}
