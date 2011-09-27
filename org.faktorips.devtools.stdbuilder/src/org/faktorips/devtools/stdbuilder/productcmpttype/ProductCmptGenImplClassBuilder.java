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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.method.GenProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.tableusage.GenTableStructureUsage;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
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

    public ProductCmptGenImplClassBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
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
        if (getBuilderSet().getFormulaCompiling().isCompileToXml()) {
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
        return new String[] { getBuilderSet().getGenerator(getProductCmptType())
                .getQualifiedClassNameForProductCmptTypeGen(true) };
    }

    @Override
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CLASS", getBuilderSet().getGenerator(getProductCmptType()) //$NON-NLS-1$
                .getUnqualifiedClassNameForProductCmptTypeGen(true), getIpsObject(), builder);
    }

    @Override
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder); //$NON-NLS-1$
        builder.append("public "); //$NON-NLS-1$
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(getBuilderSet().getGenerator(getProductCmptType()).getQualifiedName(false));
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

        if (getBuilderSet().isGenerateToXmlSupport()) {
            generateMethodWritePropertiesToXml(methodsBuilder);
            generateMethodWriteReferencesToXml(methodsBuilder);
            generateMethodWriteTableUsagesToXml(methodsBuilder);
        }

        generateMethodGetLink(methodsBuilder);
        generateMethodGetLinks(methodsBuilder);
    }

    @Override
    protected boolean isChangingOverTimeContainer() {
        return true;
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

    @Override
    protected void generateAdditionalWritePropertiesToXml(JavaCodeFragmentBuilder builder) throws CoreException {
        IPolicyCmptType policyCmptType = getPcType();
        List<IPolicyCmptTypeAttribute> attributes = policyCmptType == null ? new ArrayList<IPolicyCmptTypeAttribute>()
                : policyCmptType.getPolicyCmptTypeAttributes();
        boolean reusableLocalVariablesGenerated = false;
        for (IPolicyCmptTypeAttribute attribute : attributes) {
            if (ignoreAttributeForXMLMethods(attribute)) {
                continue;
            }
            if (!reusableLocalVariablesGenerated) {
                reusableLocalVariablesGenerated = true;
                builder.appendClassName(Element.class);
                builder.append(" configElement= null;");
                builder.appendClassName(Element.class);
                builder.append(" valueSetElement= null;");
                builder.appendClassName(Element.class);
                builder.append(" valueSetValuesElement= null;");
            }
            GenPolicyCmptType genPolicyCmptType = getBuilderSet().getGenerator(attribute.getPolicyCmptType());
            GenPolicyCmptTypeAttribute generator = genPolicyCmptType.getGenerator(attribute);
            generator.setEnumTypeBuilder(enumTypeBuilder);
            generator.generateWriteToXML(builder);
        }
    }

    @Override
    protected void generateAdditionalDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder,
            boolean reusableLocalVariablesGenerated) throws CoreException {
        IPolicyCmptType policyCmptType = getPcType();
        List<IPolicyCmptTypeAttribute> attributes = policyCmptType == null ? new ArrayList<IPolicyCmptTypeAttribute>()
                : policyCmptType.getPolicyCmptTypeAttributes();
        for (IPolicyCmptTypeAttribute attribute : attributes) {
            if (ignoreAttributeForXMLMethods(attribute)) {
                continue;
            }
            if (reusableLocalVariablesGenerated == false) {
                generateDefineLocalVariablesForXmlExtraction(builder);
                reusableLocalVariablesGenerated = true;
            }
            GenPolicyCmptType genPolicyCmptType = getBuilderSet().getGenerator(attribute.getPolicyCmptType());
            GenPolicyCmptTypeAttribute generator = genPolicyCmptType.getGenerator(attribute);
            generator.setEnumTypeBuilder(enumTypeBuilder);
            generator.generateExtractFromXML(builder);
        }
    }

    private boolean ignoreAttributeForXMLMethods(IPolicyCmptTypeAttribute attribute) throws CoreException {
        if (attribute.validate(getIpsProject()).containsErrorMsg()) {
            return true;
        }
        if (!attribute.isProductRelevant() || !attribute.isChangeable()) {
            return true;
        }
        return false;
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
        List<IAssociation> associations = type.getAssociations();
        if (associations.isEmpty()) {
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "elementsMap" }; //$NON-NLS-1$
        String[] argTypes = new String[] { Map.class.getName() + "<" //$NON-NLS-1$
                + String.class.getName() + ", " + List.class.getName() + "<" + Element.class.getName() + ">>" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
        List<ITableStructureUsage> tsus = type.getTableStructureUsages();
        if (tsus.isEmpty()) {
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

    private void generateMethodWriteReferencesToXml(JavaCodeFragmentBuilder builder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type == null) {
            return;
        }
        List<IAssociation> associations = type.getAssociations();
        if (associations.isEmpty()) {
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "element" }; //$NON-NLS-1$
        String[] argTypes = new String[] { Element.class.getName() };
        builder.methodBegin(Modifier.PROTECTED, "void", "writeReferencesToXml", argNames, argTypes); //$NON-NLS-1$ //$NON-NLS-2$
        builder.appendln("super.writeReferencesToXml(element);"); //$NON-NLS-1$

        for (IAssociation association : associations) {
            IProductCmptTypeAssociation ass = (IProductCmptTypeAssociation)association;
            if (!ass.isValid(getIpsProject())) {
                continue;
            }
            if (!ass.isDerived()) {
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = ass
                        .findMatchingPolicyCmptTypeAssociation(getIpsProject());
                getGenerator(ass).generateCodeForMethodWriteReferencesToXml(policyCmptTypeAssociation, builder);
            }
        }
        builder.methodEnd();
    }

    private void generateMethodWriteTableUsagesToXml(JavaCodeFragmentBuilder builder) throws CoreException {
        IProductCmptType type = getProductCmptType();
        if (type == null || !type.isValid(getIpsProject())) {
            return;
        }
        List<ITableStructureUsage> tsus = type.getTableStructureUsages();
        if (tsus.isEmpty()) {
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        String[] argNames = new String[] { "element" }; //$NON-NLS-1$
        String[] argTypes = new String[] { Element.class.getName() };
        builder.methodBegin(Modifier.PROTECTED, "void", "writeTableUsagesToXml", argNames, argTypes); //$NON-NLS-1$//$NON-NLS-2$
        builder.appendln("super.writeTableUsagesToXml(element);"); //$NON-NLS-1$
        for (ITableStructureUsage tsu : tsus) {
            builder.append("writeTableUsageToXml(element, \"");
            builder.append(tsu.getRoleName());
            builder.append("\", ");
            builder.append(getTableStructureUsageRoleName(tsu));
            builder.append(");");
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

        GenProductCmptTypeMethod generator = getBuilderSet().getGenerator(getProductCmptType()).getGenerator(
                (IProductCmptTypeMethod)method);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    @Override
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenTableStructureUsage generator = getBuilderSet().getGenerator(getProductCmptType()).getGenerator(tsu);
        generator.generate(false, getIpsProject(), getMainTypeSection());

    }

    public String getTableStructureUsageRoleName(ITableStructureUsage tsu) throws CoreException {
        GenTableStructureUsage generator = getBuilderSet().getGenerator(getProductCmptType()).getGenerator(tsu);
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

    class GetClassModifierFunction extends TypeHierarchyVisitor<IProductCmptType> {

        private int modifier;

        public GetClassModifierFunction(IIpsProject ipsProject, int modifier) {
            super(ipsProject);
            this.modifier = modifier;
        }

        @Override
        protected boolean visit(IProductCmptType type) {
            List<IProductCmptTypeMethod> methods = type.getProductCmptTypeMethods();
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
        List<IProductCmptTypeAssociation> associations = getProductCmptType().getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation association : associations) {
            if (!association.isDerivedUnion()) {
                GenProdAssociation genProdAssociation = getGenerator(association);
                if (genProdAssociation != null) {
                    genProdAssociation.generateCodeForGetLink(methodsBuilder);
                }
            }
        }
        if (getProductCmptType().hasSupertype()) {
            methodsBuilder.append("return super.getLink(linkName, target);");
        } else {
            methodsBuilder.appendln("return null;"); //$NON-NLS-1$
        }
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
        List<IAssociation> associations = getProductCmptType().getAssociations();
        for (IAssociation association : associations) {
            IProductCmptTypeAssociation a = (IProductCmptTypeAssociation)association;
            if (!association.isDerivedUnion()) {
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
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
