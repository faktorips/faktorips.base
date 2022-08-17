/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.runtime.jaxb.LocalDateAdapter;
import org.faktorips.runtime.jaxb.LocalDateTimeAdapter;
import org.faktorips.runtime.jaxb.LocalTimeAdapter;
import org.faktorips.runtime.jaxb.MonthDayAdapter;

/**
 * Generates JAXB annotations for policy component type fields.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD
 */
public class PolicyCmptImplClassAttributeFieldJaxbGen extends AbstractAnnotationGenerator {

    private static final Map<Class<? extends Datatype>, Class<? extends XmlAdapter<?, ?>>> JAVA_TIME_XML_ADAPTERS = Map
            .of(LocalDateDatatype.class, LocalDateAdapter.class,
                    LocalDateTimeDatatype.class, LocalDateTimeAdapter.class,
                    LocalTimeDatatype.class, LocalTimeAdapter.class,
                    MonthDayDatatype.class, MonthDayAdapter.class);

    public PolicyCmptImplClassAttributeFieldJaxbGen() {
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyAttribute) {
            XPolicyAttribute xPolicyAttribute = (XPolicyAttribute)generatorModelNode;

            IPolicyCmptTypeAttribute attribute = xPolicyAttribute.getAttribute();
            ValueDatatype datatype = attribute.findDatatype(attribute.getIpsProject());

            String annotationParam = "name=\"" + attribute.getName() + "\"";
            if (!datatype.isPrimitive()) {
                annotationParam += ",nillable=true";
            }
            builder.annotationLn(XmlElement.class, annotationParam);

            if (generatorModelNode.getDatatypeHelper(datatype).getJavaClassName().contains("java.time")) {
                Class<? extends XmlAdapter<?, ?>> adapterClass = JAVA_TIME_XML_ADAPTERS.get(datatype.getClass());
                if (adapterClass != null) {
                    JavaCodeFragment adapterFragment = new JavaCodeFragment();
                    adapterFragment.appendClassName(adapterClass);
                    adapterFragment.append(".class");
                    builder.annotationLn(XmlJavaTypeAdapter.class, adapterFragment);
                }
            }

        }
        return builder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }

}
