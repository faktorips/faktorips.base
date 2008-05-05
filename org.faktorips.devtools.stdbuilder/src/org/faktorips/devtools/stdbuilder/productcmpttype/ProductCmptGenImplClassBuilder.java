/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
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
import org.w3c.dom.Element;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenImplClassBuilder extends BaseProductCmptTypeBuilder {

    // property key for the constructor's Javadoc.
    private final static String GET_TABLE_USAGE_METHOD_JAVADOC = "GET_TABLE_USAGE_METHOD_JAVADOC";

    public static final String XML_ATTRIBUTE_TARGET_RUNTIME_ID = "targetRuntimeId";

    private ProductCmptGenInterfaceBuilder interfaceBuilder;

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
        appendLocalizedJavaDoc("CLASS", interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile()), getIpsObject(),
                builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder);
        builder.append("public ");
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType()).getQualifiedName(false));
        builder.append(" productCmpt)");
        builder.openBracket();
        builder.appendln("super(productCmpt);");
        builder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder constantsBuilder,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        generateMethodDoInitReferencesFromXml(methodsBuilder);
        generateMethodDoInitTableUsagesFromXml(methodsBuilder);
    }

    private void generateMethodDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        builder.methodBegin(Modifier.PROTECTED, Void.TYPE, "doInitPropertiesFromXml", new String[] { "configMap" },
                new Class[] { Map.class });

        builder.appendln("super.doInitPropertiesFromXml(configMap);");

        boolean attributeFound = false;
        for (Iterator it = ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType()).getGenProdAttributes(); it.hasNext();) {
            GenProdAttribute generator = (GenProdAttribute)it.next();
            if (generator.isValidAttribute()) {
                continue;
            }
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
            GenPolicyCmptType genPolicyCmptType = ((StandardBuilderSet)getBuilderSet()).getGenerator(a.getPolicyCmptType());
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
        builder.append("configElement = (");
        builder.appendClassName(Element.class);
        builder.append(")configMap.get(\"");
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
        builder.append(helper.newInstanceFromExpression("value"));
        builder.appendln(";");
    }

    private void generateExtractValueSetFromXml(GenAttribute attribute,
            DatatypeHelper helper,
            JavaCodeFragmentBuilder builder) throws CoreException {
        ValueSetType valueSetType = attribute.getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
        JavaCodeFragment frag = new JavaCodeFragment();
        helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), helper);
        if (ValueSetType.RANGE.equals(valueSetType)) {
            frag.appendClassName(Range.class);
            frag.append(" range = ");
            frag.appendClassName(ValueToXmlHelper.class);
            frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");");
            frag.append(attribute.getFieldNameRangeFor());
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
        } else if (ValueSetType.ENUM.equals(valueSetType)) {
            frag.appendClassName(EnumValues.class);
            frag.append(" values = ");
            frag.appendClassName(ValueToXmlHelper.class);
            frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");");
            frag.appendClassName(ArrayList.class);
            if(isUseTypesafeCollections()){
                frag.append("<");
                frag.appendClassName(helper.getJavaClassName());
                frag.append(">");
            }
            frag.append(" enumValues = new ");
            frag.appendClassName(ArrayList.class);
            if(isUseTypesafeCollections()){
                frag.append("<");
                frag.appendClassName(helper.getJavaClassName());
                frag.append(">");
            }
            frag.append("();");
            frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)");
            frag.appendOpenBracket();
            frag.append("enumValues.add(");
            frag.append(helper.newInstanceFromExpression("values.getValue(i)"));
            frag.appendln(");");
            frag.appendCloseBracket();
            frag.append(attribute.getFieldNameAllowedValuesFor());
            frag.append(" = ");
            frag.append(helper.newEnumValueSetInstance(new JavaCodeFragment("enumValues"), new JavaCodeFragment(
                    "values.containsNull()"), isUseTypesafeCollections()));
            frag.appendln(";");
        }
        builder.append(frag);
    }

    private void generateMethodDoInitReferencesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String[] argNames = new String[] { "elementsMap" };
        String[] argTypes = new String[] { Map.class.getName() };
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
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = ass
                        .findMatchingPolicyCmptTypeAssociation(getIpsProject());
                getGenerator(ass).generateCodeForMethodDoInitReferencesFromXml(policyCmptTypeAssociation, builder);
                builder.appendln("}");
            }
        }
        builder.methodEnd();
    }

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
        String[] argNames = new String[] { "tableUsageMap" };
        String[] argTypes = new String[] { Map.class.getName() };
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
     * Example: <code> public FtTable getRatePlan() { if (ratePlanName == null) { return null; }
     * return (FtTable)getRepository().getTable(ratePlanName); } <code>
     */
    private void generateMethodGetTableStructure(ITableStructureUsage tsu, JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        // generate the method to return the corresponding table content
        String methodName = getMethodNameGetTableUsage(tsu);
        String[] tss = tsu.getTableStructures();
        // get the class name of the instance which will be returned,
        // if the usage contains only one table structure then the returned class will be the
        // generated class of this table structure, otherwise the return class will be the ITable
        // interface class
        String tableStructureClassName = "";
        if (tss.length == 1) {
            tableStructureClassName = tss[0];
            ITableStructure ts = (ITableStructure)getProductCmptType().getIpsProject().findIpsObject(
                    IpsObjectType.TABLE_STRUCTURE, tableStructureClassName);
            if (ts == null) {
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

    // TODO move to GenProductCmptType
    public JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()."
                + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age) throws FormulaException
     * </pre>
     */
    protected void generateCodeForMethodDefinedInModel(IMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        IProductCmptTypeMethod productCmptTypeMethod = (IProductCmptTypeMethod)method;
        if (productCmptTypeMethod.isFormulaSignatureDefinition()) {
            if (method.getModifier().isPublished()) {
                // nothing to do, signature is generated by the interface builder, implementation by
                // the product component builder.
            } else {
                methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
                interfaceBuilder.generateSignatureForModelMethod(productCmptTypeMethod, true, false,
                        methodsBuilder);
                methodsBuilder.append(';');
            }
            if (productCmptTypeMethod.isOverloadsFormula()) {
                IProductCmptTypeMethod overloadedFormulaMethod = productCmptTypeMethod
                        .findOverloadedFormulaMethod(getIpsProject());
                methodsBuilder.appendln();
                methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
                interfaceBuilder.generateSignatureForModelMethod(overloadedFormulaMethod, false, false,
                        methodsBuilder);
                methodsBuilder.openBracket();
                methodsBuilder.appendln("// TODO a delegation to the method " + method.getSignatureString()
                        + " needs to be implemented here");
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
        interfaceBuilder.generateSignatureForModelMethod(productCmptTypeMethod, method.isAbstract(),
                false, methodsBuilder);
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

    public String getTableStructureUsageRoleName(ITableStructureUsage tsu) {
        return tsu.getRoleName() + "Name";
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneDerivedUnionAssociation(IProductCmptTypeAssociation association,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenProdAssociation generator = getGenerator(association);
        generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationDefinition(IProductCmptTypeAssociation containerAssociation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForDerivedUnionAssociationImplementation(IProductCmptTypeAssociation association,
            List implAssociations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        GenProdAssociation generator = getGenerator(association);
        generator.generateCodeForDerivedUnionAssociationImplementation(implAssociations, methodsBuilder);
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
