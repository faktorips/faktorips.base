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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
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
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.method.GenProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.tableusage.GenTableStructureUsage;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Range;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.w3c.dom.Element;

/**
 * Builder that generates Java source files (compilation units) containing the source code for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenImplClassBuilder extends BaseProductCmptTypeBuilder {

    public static final String XML_ATTRIBUTE_TARGET_RUNTIME_ID = "targetRuntimeId"; //$NON-NLS-1$

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private EnumTypeBuilder enumTypeBuilder;

    public ProductCmptGenImplClassBuilder(StandardBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
        setMergeEnabled(true);
    }

    public void setEnumTypeBuilder(EnumTypeBuilder enumTypeBuilder) {
        this.enumTypeBuilder = enumTypeBuilder;
    }

    public ProductCmptInterfaceBuilder getProductCmptInterfaceBuilder() {
        return productCmptInterfaceBuilder;
    }

    public void setProductCmptInterfaceBuilder(ProductCmptInterfaceBuilder productCmptInterfaceBuilder) {
        this.productCmptInterfaceBuilder = productCmptInterfaceBuilder;
    }

    public ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return productCmptGenInterfaceBuilder;
    }

    public void setProductCmptGenInterfaceBuilder(ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder) {
        this.productCmptGenInterfaceBuilder = productCmptGenInterfaceBuilder;
    }

    /**
     * If a policy component type contains an derived or computed attribute, the product component
     * generation class must be abstract, as the computation formulas are defined per generation.
     * 
     * {@inheritDoc}
     */
    @Override
    protected int getClassModifier() throws CoreException {
        int modifier = super.getClassModifier();
        if ((modifier & Modifier.ABSTRACT) > 0) {
            return modifier;
        }
        if (getStandardBuilderSet().getFormulaCompiling().compileToXml()) {
            return modifier;
        }
        // check if there is any formula in type hierarchy so we have to set class to abstract
        GetClassModifierFunction fct = new GetClassModifierFunction(getIpsProject(), modifier);
        fct.start(getProductCmptType());
        return fct.getModifier();
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return getJavaNamingConvention().getImplementationClassName(ipsSrcFile.getIpsObjectName() + generationAbb);
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    @Override
    protected String getSuperclass() throws CoreException {
        IProductCmptType supertype = (IProductCmptType)getProductCmptType().findSupertype(getIpsProject());
        if (supertype != null) {
            String pack = getPackage(supertype.getIpsSrcFile());
            return StringUtil.qualifiedName(pack, getUnqualifiedClassName(supertype.getIpsSrcFile()));
        }
        return ProductComponentGeneration.class.getName();
    }

    @Override
    protected String[] getExtendedInterfaces() throws CoreException {
        // The implementation implements the published interface.
        return new String[] { getStandardBuilderSet().getGenerator(getProductCmptType())
                .getQualifiedClassNameForProductCmptTypeGen(true) };
    }

    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CLASS", getStandardBuilderSet().getGenerator(getProductCmptType()) //$NON-NLS-1$
                .getUnqualifiedClassNameForProductCmptTypeGen(true), getIpsObject(), builder);
    }

    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder); //$NON-NLS-1$
        builder.append("public "); //$NON-NLS-1$
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(getStandardBuilderSet().getGenerator(getProductCmptType()).getQualifiedName(false));
        builder.append(" productCmpt)"); //$NON-NLS-1$
        builder.openBracket();
        builder.appendln("super(productCmpt);"); //$NON-NLS-1$

        builder.closeBracket();
    }

    @Override
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        generateMethodTypeSafeGetProductCmpt(methodsBuilder);
        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        generateMethodDoInitReferencesFromXml(methodsBuilder);
        generateMethodDoInitTableUsagesFromXml(methodsBuilder);
        if (isUseTypesafeCollections()) {
            generateMethodGetLink(methodsBuilder);
            generateMethodGetLinks(methodsBuilder);
        }
    }

    /**
     * Code sample.
     * 
     * <pre>
     * public IHtMotorPolicyType getHtMotorPolicyType() {
     *     return (IHtMotorPolicyType)getProductComponent();
     * }
     * </pre>
     */
    protected void generateMethodTypeSafeGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, true);
        productCmptGenInterfaceBuilder.generateSignatureTypeSafeGetProductCmpt(getProductCmptType(), methodsBuilder);
        methodsBuilder.openBracket();
        String productCmptType = productCmptInterfaceBuilder.getQualifiedClassName(getIpsSrcFile());
        methodsBuilder.append("return ("); //$NON-NLS-1$
        methodsBuilder.appendClassName(productCmptType);
        methodsBuilder.appendln(")" + MethodNames.GET_PRODUCT_COMPONENT + "();"); //$NON-NLS-1$//$NON-NLS-2$
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitPropertiesFromXml(Map configMap) {
     *      super.doInitPropertiesFromXml(configMap);
     *      Element configElement = null;
     *      String value = null;
     *      configElement = (Element)configMap.get(&quot;testTypeDecimal&quot;);
     *      if (configElement != null) {
     *          value = ValueToXmlHelper.getValueFromElement(configElement, &quot;Value&quot;);
     *          testTypeDecimal = Decimal.valueOf(value);
     *      }
     *  }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitPropertiesFromXml(Map&lt;String, Element&gt; configMap) {
     *      super.doInitPropertiesFromXml(configMap);
     *      Element configElement = null;
     *      String value = null;
     *      configElement = configMap.get(&quot;testTypeDecimal&quot;);
     *      if (configElement != null) {
     *          value = ValueToXmlHelper.getValueFromElement(configElement, &quot;Value&quot;);
     *          testTypeDecimal = Decimal.valueOf(value);
     *      }
     *  }
     * </pre>
     */
    private void generateMethodDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitPropertiesFromXml", new String[] { "configMap" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new String[] { isUseTypesafeCollections() ? Map.class.getName() + "<" + String.class.getName() + ", " //$NON-NLS-1$//$NON-NLS-2$
                        + Element.class.getName() + ">" : Map.class.getName() }); //$NON-NLS-1$

        builder.appendln("super.doInitPropertiesFromXml(configMap);"); //$NON-NLS-1$

        boolean attributeFound = false;
        GenProductCmptType typeGenerator = getStandardBuilderSet().getGenerator(getProductCmptType());
        for (Iterator<GenProductCmptTypeAttribute> it = typeGenerator.getGenProdAttributes(); it.hasNext();) {
            GenProductCmptTypeAttribute generator = it.next();
            if (attributeFound == false) {
                generateDefineLocalVariablesForXmlExtraction(builder);
                attributeFound = true;
            }
            generator.generateDoInitPropertiesFromXml(builder);
        }
        IPolicyCmptType policyCmptType = getPcType();
        IPolicyCmptTypeAttribute[] attributes = policyCmptType == null ? new IPolicyCmptTypeAttribute[0]
                : policyCmptType.getPolicyCmptTypeAttributes();
        for (IPolicyCmptTypeAttribute attribute : attributes) {
            IPolicyCmptTypeAttribute a = attribute;
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
            GenPolicyCmptType genPolicyCmptType = getStandardBuilderSet().getGenerator(a.getPolicyCmptType());
            GenPolicyCmptTypeAttribute generator = genPolicyCmptType.getGenerator(a);
            ValueDatatype datatype = a.findDatatype(getIpsProject());
            DatatypeHelper helper = getProductCmptType().getIpsProject().getDatatypeHelper(datatype);
            generateGetElementFromConfigMapAndIfStatement(a.getName(), builder);
            generateExtractValueFromXml(generator.getFieldNameDefaultValue(), helper, builder);
            generateExtractValueSetFromXml(generator, helper, builder);
            builder.closeBracket(); // close if statement generated three lines above
        }
        builder.methodEnd();
    }

    private void generateDefineLocalVariablesForXmlExtraction(JavaCodeFragmentBuilder builder) {
        builder.appendClassName(Element.class);
        builder.appendln(" configElement = null;"); //$NON-NLS-1$
        builder.appendClassName(String.class);
        builder.appendln(" value = null;"); //$NON-NLS-1$
    }

    private void generateGetElementFromConfigMapAndIfStatement(String attributeName, JavaCodeFragmentBuilder builder) {
        if (isUseTypesafeCollections()) {
            builder.append("configElement = configMap.get(\""); //$NON-NLS-1$
        } else {
            builder.append("configElement = ("); //$NON-NLS-1$
            builder.appendClassName(Element.class);
            builder.append(")configMap.get(\""); //$NON-NLS-1$
        }
        builder.append(attributeName);
        builder.appendln("\");"); //$NON-NLS-1$
        builder.append("if (configElement != null) "); //$NON-NLS-1$
        builder.openBracket();
    }

    private void generateExtractValueFromXml(String memberVar, DatatypeHelper helper, JavaCodeFragmentBuilder builder)
            throws CoreException {

        builder.append("value = "); //$NON-NLS-1$
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".getValueFromElement(configElement, \"Value\");"); //$NON-NLS-1$
        builder.append(memberVar);
        builder.append(" = "); //$NON-NLS-1$
        builder.append(getCodeToGetValueFromExpression(helper, "value")); //$NON-NLS-1$
        builder.appendln(";"); //$NON-NLS-1$
    }

    private JavaCodeFragment getCodeToGetValueFromExpression(DatatypeHelper helper, String expression)
            throws CoreException {

        if (helper instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)helper;
            IEnumType enumType = enumHelper.getEnumType();
            if (!enumType.isContainingValues()) {
                return enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(enumType, expression,
                        new JavaCodeFragment("getRepository()")); //$NON-NLS-1$
            }
        }
        return helper.newInstanceFromExpression(expression);
    }

    private void generateExtractValueSetFromXml(GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder builder) throws CoreException {

        ValueSetType valueSetType = genPolicyCmptTypeAttribute.getValueSet().getValueSetType();
        JavaCodeFragment frag = new JavaCodeFragment();
        helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), helper);
        if (valueSetType.isRange()) {
            generateExtractRangeFromXml(genPolicyCmptTypeAttribute, helper, frag);
        } else if (valueSetType.isEnum()) {
            generateExtractEnumSetFromXml(genPolicyCmptTypeAttribute, helper, frag);
        } else if (valueSetType.isUnrestricted()) {
            generateExtractAnyValueSetFromXml(genPolicyCmptTypeAttribute, helper, frag);
        }
        builder.append(frag);
    }

    private void generateExtractAnyValueSetFromXml(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        generateInitValueSetVariable(attribute, helper, frag);
        generateExtractEnumSetFromXml(attribute, helper, frag);
        if (getIpsProject().isValueSetTypeApplicable(attribute.getDatatype(), ValueSetType.RANGE)) {
            generateExtractRangeFromXml(attribute, helper, frag);
        }
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateInitValueSetVariable(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        frag.append(attribute.getFieldNameSetOfAllowedValues());
        frag.append(" = "); //$NON-NLS-1$
        if (helper.getDatatype().isEnum()) {
            if (helper.getDatatype() instanceof EnumTypeDatatypeAdapter) {
                EnumTypeDatatypeAdapter enumAdapter = (EnumTypeDatatypeAdapter)helper.getDatatype();
                generateCreateValueSetContainingAllEnumValueForFipsEnumDatatype(attribute, helper, enumAdapter, frag);
            } else {
                generateCreateValueSetContainingAllEnumValueForRegisteredEnumClass(attribute, helper, frag);
            }
        } else {
            generateCreateUnrestrictedValueSet(helper, frag);
        }
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateCreateUnrestrictedValueSet(DatatypeHelper helper, JavaCodeFragment frag) {
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(UnrestrictedValueSet.class);
        if (isUseTypesafeCollections()) {
            frag.append("<"); //$NON-NLS-1$
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">"); //$NON-NLS-1$
        }
        frag.append("();"); //$NON-NLS-1$
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateCreateValueSetContainingAllEnumValueForFipsEnumDatatype(@SuppressWarnings("unused") GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            EnumTypeDatatypeAdapter enumAdapter,
            JavaCodeFragment frag) throws CoreException {

        String javaEnumName = enumTypeBuilder.getQualifiedClassName(enumAdapter.getEnumType());
        JavaCodeFragment code = new JavaCodeFragment();
        if (enumAdapter.getEnumType().isContainingValues()) {
            code.appendClassName(Arrays.class);
            code.append(".asList("); //$NON-NLS-1$
            code.appendClassName(javaEnumName);
            code.append(".values())"); //$NON-NLS-1$
        } else {
            code.append("getRepository().getEnumValues("); //$NON-NLS-1$
            code.appendClassName(javaEnumName);
            code.append(".class)"); //$NON-NLS-1$
        }
        frag.append(helper.newEnumValueSetInstance(code, new JavaCodeFragment("true"), isUseTypesafeCollections())); //$NON-NLS-1$
        frag.append(";"); //$NON-NLS-1$
    }

    /**
     * Helper method for {@link #generateExtractAnyValueSetFromXml}.
     */
    private void generateCreateValueSetContainingAllEnumValueForRegisteredEnumClass(@SuppressWarnings("unused") GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) {
        // TODO
        frag.append("new "); //$NON-NLS-1$
        frag.appendClassName(UnrestrictedValueSet.class);
        if (isUseTypesafeCollections()) {
            frag.append("<"); //$NON-NLS-1$
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">"); //$NON-NLS-1$
        }
        frag.append("();"); //$NON-NLS-1$
    }

    private void generateExtractEnumSetFromXml(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        frag.appendClassName(EnumValues.class);
        frag.append(" values = "); //$NON-NLS-1$
        frag.appendClassName(ValueToXmlHelper.class);
        frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");"); //$NON-NLS-1$
        frag.append("if (values != null)"); //$NON-NLS-1$
        frag.appendOpenBracket();
        frag.appendClassName(ArrayList.class);
        if (isUseTypesafeCollections()) {
            frag.append("<"); //$NON-NLS-1$
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">"); //$NON-NLS-1$
        }
        frag.append(" enumValues = new "); //$NON-NLS-1$
        frag.appendClassName(ArrayList.class);
        if (isUseTypesafeCollections()) {
            frag.append("<"); //$NON-NLS-1$
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">"); //$NON-NLS-1$
        }
        frag.append("();"); //$NON-NLS-1$
        frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)"); //$NON-NLS-1$
        frag.appendOpenBracket();
        frag.append("enumValues.add("); //$NON-NLS-1$
        frag.append(getCodeToGetValueFromExpression(helper, "values.getValue(i)")); //$NON-NLS-1$
        frag.appendln(");"); //$NON-NLS-1$
        frag.appendCloseBracket();
        frag.append(attribute.getFieldNameSetOfAllowedValues());
        frag.append(" = "); //$NON-NLS-1$
        frag.append(helper.newEnumValueSetInstance(new JavaCodeFragment("enumValues"), new JavaCodeFragment( //$NON-NLS-1$
                "values.containsNull()"), isUseTypesafeCollections())); //$NON-NLS-1$
        frag.appendln(";"); //$NON-NLS-1$
        frag.appendCloseBracket();
    }

    private void generateExtractRangeFromXml(GenPolicyCmptTypeAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragment frag) throws CoreException {

        frag.appendClassName(Range.class);
        frag.append(" range = "); //$NON-NLS-1$
        frag.appendClassName(ValueToXmlHelper.class);
        frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");"); //$NON-NLS-1$
        frag.append("if (range != null)"); //$NON-NLS-1$
        frag.appendOpenBracket();
        frag.append(attribute.getFieldNameSetOfAllowedValues());
        frag.append(" = "); //$NON-NLS-1$
        JavaCodeFragment newRangeInstanceFrag = helper.newRangeInstance(new JavaCodeFragment("range.getLower()"), //$NON-NLS-1$
                new JavaCodeFragment("range.getUpper()"), new JavaCodeFragment("range.getStep()"), //$NON-NLS-1$ //$NON-NLS-2$
                new JavaCodeFragment("range.containsNull()"), isUseTypesafeCollections()); //$NON-NLS-1$
        if (newRangeInstanceFrag == null) {
            throw new CoreException(new IpsStatus("The " + helper + " for the datatype " //$NON-NLS-1$ //$NON-NLS-2$
                    + helper.getDatatype().getName() + " doesn't support ranges.")); //$NON-NLS-1$
        }
        frag.append(newRangeInstanceFrag);
        frag.appendln(";"); //$NON-NLS-1$
        frag.appendCloseBracket();
    }

    /**
     * Code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitReferencesFromXml(Map elementsMap) {
     *      super.doInitReferencesFromXml(elementsMap);
     *      List associationElements = (List)elementsMap.get(&quot;FtCoverageType&quot;);
     *      if (associationElements != null) {
     *          ftCoverageTypes = new String[associationElements.size()];
     *          cardinalitiesForFtCoverage = new HashMap(associationElements.size());
     *          for (int i = 0; i &lt; associationElements.size(); i++) {
     *              Element element = (Element)associationElements.get(i);
     *              ftCoverageTypes[i] = element.getAttribute(&quot;targetRuntimeId&quot;);
     *              addToCardinalityMap(cardinalitiesForFtCoverage, ftCoverageTypes[i], element);
     *          }
     *      }
     *  }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitReferencesFromXml(Map&lt;String, Element&gt; elementsMap) {
     *      super.doInitReferencesFromXml(elementsMap);
     *      List&lt;Element&gt; associationElements = elementsMap.get(&quot;ProductPart&quot;);
     *      if (associationElements != null) {
     *         productParts = new LinkedHashMap&lt;String, ILink&lt;IProductPart&gt;&gt;(associationElements.size());
     *         for (Element element: associationElements) {
     *            ILink&lt;IProductPart&gt; link = new Link&lt;IProductPart&gt;(this);
     *            link.initFromXml(element);
     *            productParts.put(link.getTargetId(), link);
     *         }
     *      }
     *  }
     * </pre>
     */
    private void generateMethodDoInitReferencesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type == null) {
            return;
        }
        IAssociation[] associations = type.getAssociations();
        if (associations.length == 0) {
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "elementsMap" }; //$NON-NLS-1$
        String[] argTypes = new String[] { isUseTypesafeCollections() ? Map.class.getName() + "<" //$NON-NLS-1$
                + String.class.getName() + ", " + List.class.getName() + "<" + Element.class.getName() + ">>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        : Map.class.getName() };
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitReferencesFromXml", argNames, argTypes); //$NON-NLS-1$ //$NON-NLS-2$
        builder.appendln("super.doInitReferencesFromXml(elementsMap);"); //$NON-NLS-1$

        // before the first association we define a temp variable as follows:
        // Element associationElements = null;

        // for each 1-1 association in the policy component type we generate:
        // associationElements = (ArrayList) associationMap.get("Product");
        // if(associationElement != null) {
        // vertragsteilePk = ((Element)associationElement.get(0)).getAttribute("targetRuntimeId");
        // }
        //
        // for each 1-many association in the policy component type we generate:
        // associationElements = (ArrayList) associationMap.get("Product");
        // if(associationElement != null) {
        // vertragsteilPks[] = new VertragsteilPk[associationElements.length()];
        // for (int i=0; i<vertragsteilsPks.length; i++) {
        // vertragsteilPks[i] =
        // ((Element)associationElement.get(i)).getAttribute("targetRuntimeId");
        // }
        // }
        // }
        boolean associationFound = false;
        for (IAssociation association : associations) {
            IProductCmptTypeAssociation ass = (IProductCmptTypeAssociation)association;
            if (!ass.isValid(getIpsProject())) {
                continue;
            }
            if (!ass.isDerived()) {
                if (associationFound == false) {
                    builder.appendClassName(List.class);
                    if (isUseTypesafeCollections()) {
                        builder.append("<"); //$NON-NLS-1$
                        builder.appendClassName(Element.class);
                        builder.append(">"); //$NON-NLS-1$
                    }
                    builder.append(" "); //$NON-NLS-1$
                    associationFound = true;
                }
                builder.append("associationElements = "); //$NON-NLS-1$
                if (!isUseTypesafeCollections()) {
                    builder.append("("); //$NON-NLS-1$
                    builder.appendClassName(List.class);
                    builder.append(") "); //$NON-NLS-1$
                }
                builder.append("elementsMap.get("); //$NON-NLS-1$
                builder.appendQuoted(ass.getName());
                builder.appendln(");"); //$NON-NLS-1$
                builder.append("if (associationElements != null) {"); //$NON-NLS-1$
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = ass
                        .findMatchingPolicyCmptTypeAssociation(getIpsProject());
                getGenerator(ass).generateCodeForMethodDoInitReferencesFromXml(policyCmptTypeAssociation, builder);
                builder.appendln("}"); //$NON-NLS-1$
            }
        }
        builder.methodEnd();
    }

    /**
     * Code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitTableUsagesFromXml(Map tableUsageMap) {
     *      super.doInitTableUsagesFromXml(tableUsageMap);
     *      Element element = null;
     *      element = (Element)tableUsageMap.get(&quot;ratePlan&quot;);
     *      if (element != null) {
     *          ratePlanName = ValueToXmlHelper.getValueFromElement(element, &quot;TableContentName&quot;);
     *      }
     *  }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitTableUsagesFromXml(Map&lt;String, Element&gt; tableUsageMap) {
     *      super.doInitTableUsagesFromXml(tableUsageMap);
     *      Element element = null;
     *      element = tableUsageMap.get(&quot;ratePlan&quot;);
     *      if (element != null) {
     *          ratePlanName = ValueToXmlHelper.getValueFromElement(element, &quot;TableContentName&quot;);
     *      }
     *  }
     * </pre>
     */
    private void generateMethodDoInitTableUsagesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type == null || !type.isValid(getIpsProject())) {
            return;
        }
        ITableStructureUsage[] tsus = type.getTableStructureUsages();
        if (tsus.length == 0) {
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "tableUsageMap" }; //$NON-NLS-1$
        String string = "<"; //$NON-NLS-1$
        String[] argTypes = new String[] { isUseTypesafeCollections() ? Map.class.getName() + string
                + String.class.getName() + ", " + Element.class.getName() + ">" : Map.class.getName() }; //$NON-NLS-1$ //$NON-NLS-2$
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitTableUsagesFromXml", argNames, argTypes); //$NON-NLS-1$//$NON-NLS-2$
        builder.appendln("super.doInitTableUsagesFromXml(tableUsageMap);"); //$NON-NLS-1$
        builder.appendClassName(Element.class);
        builder.appendln(" element = null;"); //$NON-NLS-1$
        for (ITableStructureUsage tsu : tsus) {
            if (isUseTypesafeCollections()) {
                builder.append("element = tableUsageMap.get(\""); //$NON-NLS-1$
            } else {
                builder.append("element = ("); //$NON-NLS-1$
                builder.appendClassName(Element.class);
                builder.append(")tableUsageMap.get(\""); //$NON-NLS-1$
            }
            builder.append(tsu.getRoleName());
            builder.appendln("\");"); //$NON-NLS-1$
            builder.appendln("if (element != null){"); //$NON-NLS-1$
            builder.append(getTableStructureUsageRoleName(tsu));
            builder.appendln(" = "); //$NON-NLS-1$
            builder.appendClassName(ValueToXmlHelper.class);
            builder.append(".getValueFromElement(element, \"TableContentName\");"); //$NON-NLS-1$
            builder.appendln("}"); //$NON-NLS-1$
        }
        builder.appendln("}"); //$NON-NLS-1$
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age) throws FormulaException
     * </pre>
     */
    @Override
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        GenProductCmptTypeMethod generator = getStandardBuilderSet().getGenerator(getProductCmptType()).getGenerator(
                (IProductCmptTypeMethod)method);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenTableStructureUsage generator = getStandardBuilderSet().getGenerator(getProductCmptType()).getGenerator(tsu);
        generator.generate(false, getIpsProject(), getMainTypeSection());

    }

    public String getTableStructureUsageRoleName(ITableStructureUsage tsu) throws CoreException {
        GenTableStructureUsage generator = getStandardBuilderSet().getGenerator(getProductCmptType()).getGenerator(tsu);
        return generator.getMemberVarName();
    }

    @Override
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenProdAssociation generator = getGenerator(association);
        generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
    }

    @Override
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation derivedUnionAssociation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    @Override
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation derivedUnionAssociation,
            List<IAssociation> implAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {

        GenProdAssociation generator = getGenerator(derivedUnionAssociation);
        generator.generateCodeForDerivedUnionAssociationImplementation(implAssociations, methodsBuilder);
    }

    class GetClassModifierFunction extends ProductCmptTypeHierarchyVisitor {

        private int modifier;

        public GetClassModifierFunction(IIpsProject ipsProject, int modifier) {
            super(ipsProject);
            this.modifier = modifier;
        }

        @Override
        protected boolean visit(IProductCmptType type) {
            IProductCmptTypeMethod[] methods = type.getProductCmptTypeMethods();
            for (IProductCmptTypeMethod method : methods) {
                if (method.isFormulaSignatureDefinition()) {
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

    /**
     * Java 5 code sample:
     * 
     * <pre>
     *  [Javadoc]
     *  public ILink&lt;? extends IProductComponent&gt; getLink(String linkName, IProductComponent target) {
     *      if (&quot;ElementarProdukt&quot;.equals(linkName)) {
     *          return getLinkForElementarProdukt((IElementarProdukt)target);
     *      }
     *      if (&quot;VersichertesObjekt&quot;.equals(linkName)) {
     *          return getLinkForVersichertesObjekt((IVersichertesObjekt)target);
     *      }
     *      return null;
     *  }
     * </pre>
     */
    private void generateMethodGetLink(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, false);
        generateSignatureGetLink(methodsBuilder);
        methodsBuilder.openBracket();
        IAssociation[] associations = getProductCmptType().getAssociations();
        for (int i = 0; i < associations.length; i++) {
            IProductCmptTypeAssociation a = (IProductCmptTypeAssociation)associations[i];
            if (!associations[i].isDerivedUnion()) {
                GenProdAssociation genProdAssociation = getGenerator(a);
                if (genProdAssociation != null) {
                    genProdAssociation.generateCodeForGetLink(methodsBuilder);
                }
            }
        }
        methodsBuilder.appendln("return null;"); //$NON-NLS-1$
        methodsBuilder.closeBracket();
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     *  public ILink&lt;? extends IProductComponent&gt; getLink(String linkName, IProductComponent target)
     * </pre>
     */
    private void generateSignatureGetLink(JavaCodeFragmentBuilder methodsBuilder) {
        methodsBuilder.signature(Modifier.PUBLIC, Java5ClassNames.ILink_QualifiedName + "<? extends " //$NON-NLS-1$
                + IProductComponent.class.getName() + ">", "getLink", new String[] { "linkName", "target" }, //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
                new String[] { String.class.getName(), IProductComponent.class.getName() });
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     *  [Javadoc]
     *  public List&lt;ILink&lt;? extends IProductComponent&gt;&gt; getLinks() {
     *      List&lt;ILink&lt;? extends IProductComponent&gt;&gt; list = new ArrayList&lt;ILink&lt;? extends IProductComponent&gt;&gt;();
     *      list.addAll(getLinksForInsuredObjects());
     *      list.addAll(getLinkForProduct());
     *      return list;
     *  }
     * </pre>
     */
    private void generateMethodGetLinks(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, false);
        generateSignatureGetLinks(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(List.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName + "<? extends " //$NON-NLS-1$//$NON-NLS-2$
                + IProductComponent.class.getName() + ">>"); //$NON-NLS-1$
        methodsBuilder.append(" list = ");
        if (getProductCmptType().hasSupertype()) {
            methodsBuilder.append("super.getLinks();");
        } else {
            methodsBuilder.append("new ");
            methodsBuilder.appendClassName(ArrayList.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName
                    + "<? extends " + IProductComponent.class.getName() + ">>");
            methodsBuilder.appendln("();");
        }
        IAssociation[] associations = getProductCmptType().getAssociations();
        for (int i = 0; i < associations.length; i++) {
            IProductCmptTypeAssociation a = (IProductCmptTypeAssociation)associations[i];
            if (!associations[i].isDerivedUnion()) {
                GenProdAssociation genProdAssociation = getGenerator(a);
                if (genProdAssociation != null) {
                    genProdAssociation.generateCodeForGetLinks(methodsBuilder);
                }
            }
        }
        methodsBuilder.appendln("return list;"); //$NON-NLS-1$
        methodsBuilder.closeBracket();
    }

    /**
     * Java 5 code sample:
     * 
     * <pre>
     *  public List&lt;ILink&lt;? extends IProductComponent&gt;&gt; getLinks()
     * </pre>
     */
    private void generateSignatureGetLinks(JavaCodeFragmentBuilder methodsBuilder) {
        String returnType = List.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName + "<? extends " //$NON-NLS-1$ //$NON-NLS-2$
                + IProductComponent.class.getName() + ">>"; //$NON-NLS-1$
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType,
                "getLinks", new String[0], new String[0]); //$NON-NLS-1$
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements, IIpsElement ipsElement) {
        IProductCmptType productCmptType = null;
        if (ipsElement instanceof IProductCmptType) {
            productCmptType = (IProductCmptType)ipsElement;

        } else if (ipsElement instanceof IProductCmptTypeAttribute) {
            productCmptType = ((IProductCmptTypeAttribute)ipsElement).getProductCmptType();

        } else if (ipsElement instanceof IProductCmptTypeMethod) {
            productCmptType = (IProductCmptType)((IProductCmptTypeMethod)ipsElement).getIpsObject();

        } else {
            return;
        }

        IType javaType = getGeneratedJavaTypes(productCmptType).get(0);
        GenProductCmptType genProductCmptType = getGenProductCmptType(productCmptType);
        genProductCmptType.getGeneratedJavaElementsForImplementation(javaElements, javaType, ipsElement);
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
