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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.method.GenProdMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.tableusage.GenTableStructureUsage;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.EnumValues;
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

    public static final String XML_ATTRIBUTE_TARGET_RUNTIME_ID = "targetRuntimeId";

    public ProductCmptGenImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
        setMergeEnabled(true);
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
        return new String[] { ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getQualifiedClassNameForProductCmptTypeGen(true) };
    }

    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CLASS", ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getUnqualifiedClassNameForProductCmptTypeGen(true), getIpsObject(), builder);
    }

    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder);
        builder.append("public ");
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getQualifiedName(false));
        builder.append(" productCmpt)");
        builder.openBracket();
        builder.appendln("super(productCmpt);");
        builder.closeBracket();
    }

    @Override
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        generateMethodDoInitReferencesFromXml(methodsBuilder);
        generateMethodDoInitTableUsagesFromXml(methodsBuilder);
        if (isUseTypesafeCollections()) {
            generateMethodGetLink(methodsBuilder);
            generateMethodGetLinks(methodsBuilder);
        }
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
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitPropertiesFromXml", new String[] { "configMap" },
                new String[] { isUseTypesafeCollections() ? Map.class.getName() + "<" + String.class.getName() + ", "
                        + Element.class.getName() + ">" : Map.class.getName() });

        builder.appendln("super.doInitPropertiesFromXml(configMap);");

        boolean attributeFound = false;
        GenProductCmptType typeGenerator = getStandardBuilderSet().getGenerator(getProductCmptType());
        for (Iterator<GenProdAttribute> it = typeGenerator.getGenProdAttributes(); it.hasNext();) {
            GenProdAttribute generator = it.next();
            if (attributeFound == false) {
                generateDefineLocalVariablesForXmlExtraction(builder);
                attributeFound = true;
            }
            generator.generateDoInitPropertiesFromXml(builder);
        }
        IPolicyCmptType policyCmptType = getPcType();
        IPolicyCmptTypeAttribute[] attributes = policyCmptType == null ? new IPolicyCmptTypeAttribute[0]
                : policyCmptType.getPolicyCmptTypeAttributes();
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
            GenPolicyCmptType genPolicyCmptType = ((StandardBuilderSet)getBuilderSet()).getGenerator(a
                    .getPolicyCmptType());
            GenAttribute generator = genPolicyCmptType.getGenerator(a);
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
        builder.appendln(" configElement = null;");
        builder.appendClassName(String.class);
        builder.appendln(" value = null;");
    }

    private void generateGetElementFromConfigMapAndIfStatement(String attributeName, JavaCodeFragmentBuilder builder) {
        if (isUseTypesafeCollections()) {
            builder.append("configElement = configMap.get(\"");
        } else {
            builder.append("configElement = (");
            builder.appendClassName(Element.class);
            builder.append(")configMap.get(\"");
        }
        builder.append(attributeName);
        builder.appendln("\");");
        builder.append("if (configElement != null) ");
        builder.openBracket();
    }

    private void generateExtractValueFromXml(String memberVar, DatatypeHelper helper, JavaCodeFragmentBuilder builder)
            throws CoreException {
        builder.append("value = ");
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".getValueFromElement(configElement, \"Value\");");
        builder.append(memberVar);
        builder.append(" = ");
        builder.append(getCodeToGetValueFromExpression(helper, "value"));
        builder.appendln(";");
    }

    private JavaCodeFragment getCodeToGetValueFromExpression(DatatypeHelper helper, String expression)
            throws CoreException {
        if (helper instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)helper;
            IEnumType enumType = enumHelper.getEnumType();
            if (!enumType.isContainingValues()) {
                return enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(enumType, expression,
                        new JavaCodeFragment("getRepository()"));
            }
        }
        return helper.newInstanceFromExpression(expression);
    }

    private void generateExtractValueSetFromXml(GenAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder builder) throws CoreException {

        ValueSetType valueSetType = attribute.getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
        JavaCodeFragment frag = new JavaCodeFragment();
        helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), helper);
        if (valueSetType.isRange()) {
            generateExtractRangeFromXml(attribute, helper, frag);
        } else if (valueSetType.isEnum()) {
            generateExtractEnumSetFromXml(attribute, helper, frag);
        } else if (valueSetType.isUnrestricted()) {
            generateExtractAnyValueSetFromXml(attribute, helper, frag);
        }
        builder.append(frag);
    }

    private void generateExtractAnyValueSetFromXml(GenAttribute attribute, DatatypeHelper helper, JavaCodeFragment frag)
            throws CoreException {
        frag.append(attribute.getFieldNameForSetOfAllowedValues());
        frag.append(" = new ");
        frag.appendClassName(UnrestrictedValueSet.class);
        if (isUseTypesafeCollections()) {
            frag.append("<");
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">");
        }
        frag.append("();");
        generateExtractEnumSetFromXml(attribute, helper, frag);
        if (getIpsProject().isValueSetTypeApplicable(attribute.getDatatype(), ValueSetType.RANGE)) {
            generateExtractRangeFromXml(attribute, helper, frag);
        }
    }

    private void generateExtractEnumSetFromXml(GenAttribute attribute, DatatypeHelper helper, JavaCodeFragment frag)
            throws CoreException {

        frag.appendClassName(EnumValues.class);
        frag.append(" values = ");
        frag.appendClassName(ValueToXmlHelper.class);
        frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");");
        frag.append("if (values != null)");
        frag.appendOpenBracket();
        frag.appendClassName(ArrayList.class);
        if (isUseTypesafeCollections()) {
            frag.append("<");
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">");
        }
        frag.append(" enumValues = new ");
        frag.appendClassName(ArrayList.class);
        if (isUseTypesafeCollections()) {
            frag.append("<");
            frag.appendClassName(helper.getJavaClassName());
            frag.append(">");
        }
        frag.append("();");
        frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)");
        frag.appendOpenBracket();
        frag.append("enumValues.add(");
        frag.append(getCodeToGetValueFromExpression(helper, "values.getValue(i)"));
        frag.appendln(");");
        frag.appendCloseBracket();
        frag.append(attribute.getFieldNameForSetOfAllowedValues());
        frag.append(" = ");
        frag.append(helper.newEnumValueSetInstance(new JavaCodeFragment("enumValues"), new JavaCodeFragment(
                "values.containsNull()"), isUseTypesafeCollections()));
        frag.appendln(";");
        frag.appendCloseBracket();
    }

    private void generateExtractRangeFromXml(GenAttribute attribute, DatatypeHelper helper, JavaCodeFragment frag)
            throws CoreException {

        frag.appendClassName(Range.class);
        frag.append(" range = ");
        frag.appendClassName(ValueToXmlHelper.class);
        frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");");
        frag.append("if (range != null)");
        frag.appendOpenBracket();
        frag.append(attribute.getFieldNameForSetOfAllowedValues());
        frag.append(" = ");
        JavaCodeFragment newRangeInstanceFrag = helper.newRangeInstance(new JavaCodeFragment("range.getLower()"),
                new JavaCodeFragment("range.getUpper()"), new JavaCodeFragment("range.getStep()"),
                new JavaCodeFragment("range.containsNull()"), isUseTypesafeCollections());
        if (newRangeInstanceFrag == null) {
            throw new CoreException(new IpsStatus("The " + helper + " for the datatype "
                    + helper.getDatatype().getName() + " doesn't support ranges."));
        }
        frag.append(newRangeInstanceFrag);
        frag.appendln(";");
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
        String[] argNames = new String[] { "elementsMap" };
        String[] argTypes = new String[] { isUseTypesafeCollections() ? Map.class.getName() + "<"
                + String.class.getName() + ", " + List.class.getName() + "<" + Element.class.getName() + ">>"
                : Map.class.getName() };
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitReferencesFromXml", argNames, argTypes);
        builder.appendln("super.doInitReferencesFromXml(elementsMap);");

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
        for (int i = 0; i < associations.length; i++) {
            IProductCmptTypeAssociation ass = (IProductCmptTypeAssociation)associations[i];
            if (!ass.isValid()) {
                continue;
            }
            if (!ass.isDerived()) {
                if (associationFound == false) {
                    builder.appendln();
                    builder.appendClassName(List.class);
                    if (isUseTypesafeCollections()) {
                        builder.append("<");
                        builder.appendClassName(Element.class);
                        builder.append(">");
                    }
                    builder.append(" ");
                    associationFound = true;
                }
                builder.append("associationElements = ");
                if (!isUseTypesafeCollections()) {
                    builder.append("(");
                    builder.appendClassName(List.class);
                    builder.append(") ");
                }
                builder.append("elementsMap.get(");
                builder.appendQuoted(ass.getName());
                builder.appendln(");");
                builder.append("if (associationElements != null) {");
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = ass
                        .findMatchingPolicyCmptTypeAssociation(getIpsProject());
                getGenerator(ass).generateCodeForMethodDoInitReferencesFromXml(policyCmptTypeAssociation, builder);
                builder.appendln("}");
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
        if (type == null) {
            return;
        }
        ITableStructureUsage[] tsus = type.getTableStructureUsages();
        if (tsus.length == 0) {
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "tableUsageMap" };
        String[] argTypes = new String[] { isUseTypesafeCollections() ? Map.class.getName() + "<"
                + String.class.getName() + ", " + Element.class.getName() + ">" : Map.class.getName() };
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitTableUsagesFromXml", argNames, argTypes);
        builder.appendln("super.doInitTableUsagesFromXml(tableUsageMap);");
        builder.appendClassName(Element.class);
        builder.appendln(" element = null;");
        for (int i = 0; i < tsus.length; i++) {
            if (isUseTypesafeCollections()) {
                builder.append("element = tableUsageMap.get(\"");
            } else {
                builder.append("element = (");
                builder.appendClassName(Element.class);
                builder.append(")tableUsageMap.get(\"");
            }
            builder.append(tsus[i].getRoleName());
            builder.appendln("\");");
            builder.appendln("if (element != null){");
            builder.append(getTableStructureUsageRoleName(tsus[i]));
            builder.appendln(" = ");
            builder.appendClassName(ValueToXmlHelper.class);
            builder.append(".getValueFromElement(element, \"TableContentName\");");
            builder.appendln("}");
        }
        builder.appendln("}");
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

        GenProdMethod generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getGenerator((IProductCmptTypeMethod)method);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenTableStructureUsage generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getGenerator(tsu);
        generator.generate(false, getIpsProject(), getMainTypeSection());

    }

    public String getTableStructureUsageRoleName(ITableStructureUsage tsu) throws CoreException {
        GenTableStructureUsage generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getGenerator(tsu);
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
     * 
     * @throws CoreException
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
        methodsBuilder.appendln("return null;");
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
        methodsBuilder.signature(Modifier.PUBLIC, Java5ClassNames.ILink_QualifiedName + "<? extends "
                + IProductComponent.class.getName() + ">", "getLink", new String[] { "linkName", "target" },
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
     * 
     * @throws CoreException
     */
    private void generateMethodGetLinks(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodsBuilder, false);
        generateSignatureGetLinks(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(List.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName + "<? extends "
                + IProductComponent.class.getName() + ">>");
        methodsBuilder.append(" list = new ");
        methodsBuilder.appendClassName(ArrayList.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName
                + "<? extends " + IProductComponent.class.getName() + ">>");
        methodsBuilder.appendln("();");
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
        methodsBuilder.appendln("return list;");
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
        String returnType = List.class.getName() + "<" + Java5ClassNames.ILink_QualifiedName + "<? extends "
                + IProductComponent.class.getName() + ">>";
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), returnType,
                "getLinks", new String[0], new String[0]);
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
