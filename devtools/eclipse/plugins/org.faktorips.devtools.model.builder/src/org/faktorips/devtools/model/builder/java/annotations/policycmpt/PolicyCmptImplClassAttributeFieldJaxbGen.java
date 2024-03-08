/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.policycmpt;

import java.util.Map;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.annotations.JaxbAnnGenFactory.IpsXmlAdapters;
import org.faktorips.devtools.model.builder.java.annotations.JaxbAnnGenFactory.JaxbAnnotation;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * Generates JAXB annotations for policy component type fields.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD
 */
public class PolicyCmptImplClassAttributeFieldJaxbGen extends AbstractJaxbAnnotationGenerator {

    private static final Map<Class<? extends Datatype>, IpsXmlAdapters> XML_ADAPTERS = Map
            .of(LocalDateDatatype.class, IpsXmlAdapters.LocalDateAdapter,
                    LocalDateTimeDatatype.class, IpsXmlAdapters.LocalDateTimeAdapter,
                    LocalTimeDatatype.class, IpsXmlAdapters.LocalTimeAdapter,
                    MonthDayDatatype.class, IpsXmlAdapters.MonthDayAdapter,
                    DecimalDatatype.class, IpsXmlAdapters.DecimalAdapter,
                    MoneyDatatype.class, IpsXmlAdapters.MoneyAdapter,
                    GregorianCalendarDatatype.class, IpsXmlAdapters.GregorianCalendarAdapter,
                    GregorianCalendarAsDateDatatype.class, IpsXmlAdapters.GregorianCalendarAdapter);

    public PolicyCmptImplClassAttributeFieldJaxbGen() {
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyAttribute xPolicyAttribute) {
            IPolicyCmptTypeAttribute attribute = xPolicyAttribute.getAttribute();
            ValueDatatype datatype = attribute.findDatatype(attribute.getIpsProject());

            String annotationParam = "name=\"" + attribute.getName() + "\"";
            if (!datatype.isPrimitive()) {
                annotationParam += ",nillable=true";
            }
            builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlElement, generatorModelNode), annotationParam);

            generateXmlAdaptersForJavaTimeDataTypes(generatorModelNode, builder, datatype);

            generateXmlAdaptersForIpsDataTypes(generatorModelNode, builder, datatype);
        }
        return builder.getFragment();
    }

    /**
     * Generate {@code XmlJavaTypeAdapter} annotation for {@link Decimal}, {@link Money} and
     * {@code GregorianCalendar}
     */
    private void generateXmlAdaptersForIpsDataTypes(AbstractGeneratorModelNode generatorModelNode,
            JavaCodeFragmentBuilder builder,
            ValueDatatype datatype) {
        if (datatype instanceof DecimalDatatype || datatype instanceof MoneyDatatype
                || datatype instanceof GregorianCalendarDatatype
                || datatype instanceof GregorianCalendarAsDateDatatype) {
            generateXmlAdapterAnnotation(generatorModelNode, builder, datatype);
        }
    }

    /**
     * Generate {@code XmlJavaTypeAdapter} annotation for {@code java.time} classes but not
     * {@code joda.time} classes.
     */
    private void generateXmlAdaptersForJavaTimeDataTypes(AbstractGeneratorModelNode generatorModelNode,
            JavaCodeFragmentBuilder builder,
            ValueDatatype datatype) {
        if (generatorModelNode.getDatatypeHelper(datatype).getJavaClassName().contains("java.time")) {
            generateXmlAdapterAnnotation(generatorModelNode, builder, datatype);
        }
    }

    /**
     * Generates {@code XmlJavaTypeAdapter} for a given datatype, if registered.
     */
    private void generateXmlAdapterAnnotation(AbstractGeneratorModelNode generatorModelNode,
            JavaCodeFragmentBuilder builder,
            ValueDatatype datatype) {
        IpsXmlAdapters adapter = XML_ADAPTERS.get(datatype.getClass());
        if (adapter != null) {
            JavaCodeFragment adapterFragment = new JavaCodeFragment();
            adapterFragment.appendClassName(getQualifiedName(adapter, generatorModelNode));
            adapterFragment.append(".class");
            builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlJavaTypeAdapter, generatorModelNode),
                    adapterFragment);
        }
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }

}
