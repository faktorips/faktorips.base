/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.runtime.internal.Range;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.valueset.OrderedValueSet;
import org.w3c.dom.Element;

public class GenPolicyAttributeForProdCmpts {

    private final GenPolicyCmptTypeAttribute generator;
    private final BaseProductCmptTypeBuilder baseProdCmptTypeBuilder;
    private final EnumTypeBuilder enumTypeBuilder;
    private ValueDatatype datatype;
    private DatatypeHelper datatypeHelper;

    public GenPolicyAttributeForProdCmpts(GenPolicyCmptTypeAttribute generator,
            BaseProductCmptTypeBuilder baseProdCmptTypeBuilder, EnumTypeBuilder enumTypeBuilder) throws CoreException {
        this.generator = generator;
        this.baseProdCmptTypeBuilder = baseProdCmptTypeBuilder;
        this.enumTypeBuilder = enumTypeBuilder;
        datatype = getAttribute().findDatatype(getIpsProject());
        datatypeHelper = getProductCmptType().getIpsProject().getDatatypeHelper(datatype);
    }

    public void generateExtractFromXML(JavaCodeFragmentBuilder builder) throws CoreException {
        generateGetElementFromConfigMapAndIfStatement(getAttribute().getName(), builder);
        generateExtractValueFromXml(generator.getFieldNameDefaultValue(), datatypeHelper, builder);
        generateExtractValueSetFromXml(generator, datatypeHelper, builder);
        builder.closeBracket(); // close if statement generated three lines above
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

        String javaEnumName = getEnumTypeBuilder().getQualifiedClassName(enumAdapter.getEnumType());
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

    public void generateWriteToXML(JavaCodeFragmentBuilder builder) {
        builder.append(" configElement = element.getOwnerDocument().createElement(\"ConfigElement\");");

        builder.append("configElement.setAttribute(\"attribute\", \"");
        builder.append(getAttribute().getName());
        builder.append("\");");
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".addValueToElement(");
        builder.append(datatypeHelper.getToStringExpression(generator.getFieldNameDefaultValue()));
        builder.append(", configElement, \"Value\");");

        generateWriteValueSetToXml(builder);

        builder.append("element.appendChild(configElement);");
    }

    private void generateWriteValueSetToXml(JavaCodeFragmentBuilder builder) {
        builder.append(" valueSetElement= element.getOwnerDocument().createElement(\"ValueSet\");");
        /*
         * Ignore abstract flag, as value sets can never be abstract at runtime. (A value set is
         * abstract if it does not define concrete values, but the type of value set, e.g. range or
         * enum)
         */
        IValueSet valueSet = generator.getValueSet();
        if (valueSet.isUnrestricted()) {
            // the ProductCmpt could define any type of value set
            generateCodeForUnrestrictedToXML(builder, true);
            generateCodeForRangeToXML(builder, true);
            generateCodeForEnumToXML(builder, true);
        } else if (valueSet.isRange()) {
            generateCodeForRangeToXML(builder, false);
        } else if (valueSet.isEnum()) {
            generateCodeForEnumToXML(builder, false);
        }
        builder.append("configElement.appendChild(valueSetElement);");
    }

    private void generateCodeForUnrestrictedToXML(JavaCodeFragmentBuilder builder, boolean generateInstanceOf) {
        if (generateInstanceOf) {
            builder.append("if (");
            builder.append(generator.getFieldNameSetOfAllowedValues());
            builder.append(" instanceof ");
            builder.appendClassName(org.faktorips.valueset.UnrestrictedValueSet.class);
            builder.append(") {");
        }
        builder.appendClassName(Element.class);
        builder.append(" unrestrictedValueSetElement = element.getOwnerDocument().createElement(\"Unrestricted\");");
        builder.appendClassName(Element.class);
        builder.append(" valueElement = unrestrictedValueSetElement.getOwnerDocument().createElement(\"AllValues\");");
        builder.append("unrestrictedValueSetElement.appendChild(valueElement);");
        builder.append("valueSetElement.appendChild(unrestrictedValueSetElement);");
        if (generateInstanceOf) {
            builder.append("}");
        }
    }

    private void generateCodeForRangeToXML(JavaCodeFragmentBuilder builder, boolean generateInstanceOf) {
        if (generateInstanceOf) {
            builder.append("if (");
            builder.append(generator.getFieldNameSetOfAllowedValues());
            builder.append(" instanceof ");
            builder.appendClassName(org.faktorips.valueset.Range.class);
            builder.append(") {");
            /*
             * In case of an unrestricted valueSet (besides the value instance-of) a local variable
             * must be generated that is casted to range to access its fields.
             */
            builder.appendClassName(org.faktorips.valueset.Range.class);
            appendGenericDatatypeClassname(builder);
            builder.append(" range= (");
            builder.appendClassName(org.faktorips.valueset.Range.class);
            appendGenericDatatypeClassname(builder);
            builder.append(")");
            builder.append(generator.getFieldNameSetOfAllowedValues());
            builder.append(";");
        }
        builder.append("valueSetValuesElement = element.getOwnerDocument().createElement(\"Range\");");
        builder.append("valueSetValuesElement.setAttribute(\"containsNull\", Boolean.toString(");
        builder.append(generator.getFieldNameSetOfAllowedValues());
        builder.append(".containsNull()));");
        generateWriteValueToXML(builder, generateRangeValueToString(".getLowerBound()", generateInstanceOf),
                "valueSetValuesElement", "LowerBound");
        generateWriteValueToXML(builder, generateRangeValueToString(".getUpperBound()", generateInstanceOf),
                "valueSetValuesElement", "UpperBound");
        generateWriteValueToXML(builder, generateRangeValueToString(".getStep()", generateInstanceOf),
                "valueSetValuesElement", "Step");
        builder.append("valueSetElement.appendChild(valueSetValuesElement);");
        if (generateInstanceOf) {
            builder.append("}");
        }
    }

    protected void appendGenericDatatypeClassname(JavaCodeFragmentBuilder builder) {
        builder.append("<");
        builder.append(datatype.getJavaClassName());
        builder.append(">");
    }

    protected String generateRangeValueToString(String getter, boolean generateInstanceOf) {
        String fieldName = generateInstanceOf ? "range" : generator.getFieldNameSetOfAllowedValues();
        return datatypeHelper.getToStringExpression(fieldName + getter).toString();
    }

    protected void generateWriteValueToXML(JavaCodeFragmentBuilder builder,
            String value,
            String elementName,
            String tagName) {
        builder.appendClassName(ValueToXmlHelper.class);
        builder.append(".addValueToElement(");
        builder.append(value);
        builder.append(", ");
        builder.append(elementName);
        builder.append(", \"");
        builder.append(tagName);
        builder.append("\");");
    }

    private void generateCodeForEnumToXML(JavaCodeFragmentBuilder builder, boolean generateInstanceOf) {
        if (generateInstanceOf) {
            builder.append("if (");
            builder.append(generator.getFieldNameSetOfAllowedValues());
            builder.append(" instanceof ");
            builder.appendClassName(OrderedValueSet.class);
            builder.append("){");
            // builder.append("<");
            // builder.append(datatype.getJavaClassName());
            // builder.append(">) {");
        }
        builder.append("valueSetValuesElement = element.getOwnerDocument().createElement(\"Enum\");");
        builder.append("for (");
        builder.append(datatype.getJavaClassName());
        builder.append(" value : ");
        builder.append(generator.getFieldNameSetOfAllowedValues());
        builder.append(".getValues(true)) {");
        builder.appendClassName(Element.class);
        builder.append(" valueElement = element.getOwnerDocument().createElement(\"Value\");");
        generateWriteValueToXML(builder, datatypeHelper.getToStringExpression("value").toString(), "valueElement",
                "Data");
        builder.append("valueSetValuesElement.appendChild(valueElement);");
        builder.append("}");
        builder.append("valueSetElement.appendChild(valueSetValuesElement);");
        if (generateInstanceOf) {
            builder.append("}");
        }
    }

    private boolean isUseTypesafeCollections() {
        return baseProdCmptTypeBuilder.isUseTypesafeCollections();
    }

    private IPolicyCmptTypeAttribute getAttribute() {
        return generator.getAttribute();
    }

    private IIpsElement getIpsSrcFile() {
        return baseProdCmptTypeBuilder.getIpsSrcFile();
    }

    private JavaSourceFileBuilder getEnumTypeBuilder() {
        return enumTypeBuilder;
    }

    private IIpsProject getIpsProject() {
        return baseProdCmptTypeBuilder.getIpsProject();
    }

    private IProductCmptType getProductCmptType() {
        return baseProdCmptTypeBuilder.getProductCmptType();
    }
}
