/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations;

import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.ENUM_ATTRIBUTE_GETTER;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.ENUM_CLASS;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ASSOCIATION_GETTER;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.TABLE_CLASS;
import static org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType.TABLE_ROW_CLASS_COLUMN_GETTER;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyAccess;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsExtensionProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates the {@link IpsExtensionProperty} annotation.
 */
public class ExtensionPropertyAnnGen implements IAnnotationGenerator {

    private static final EnumSet<AnnotatedJavaElementType> RELEVANT_TYPES = EnumSet.of(TABLE_CLASS,
            TABLE_ROW_CLASS_COLUMN_GETTER, PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER,
            POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER, POLICY_CMPT_DECL_CLASS, PRODUCT_CMPT_DECL_CLASS,
            POLICY_CMPT_DECL_CLASS_ASSOCIATION_GETTER, PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER,
            ENUM_ATTRIBUTE_GETTER, ENUM_CLASS);

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        IExtensionPropertyAccess partContainer = modelNode.getIpsObjectPartContainer();
        for (IExtensionPropertyDefinition extensionPropertyDefinition : partContainer
                .getExtensionPropertyDefinitions()) {
            /*
             * If an extension property is not available in the current installation, obtaining it's
             * value may fail, see
             * org.faktorips.devtools.core.internal.model.ipsobject.ExtensionPropertyHandler
             * .getExtPropertyValue(String)
             */
            if (isRelevant(extensionPropertyDefinition, partContainer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return whether the extension property definition is both available and retained at runtime.
     */
    private boolean isRelevant(IExtensionPropertyDefinition extensionPropertyDefinition,
            IExtensionPropertyAccess partContainer) {
        return extensionPropertyDefinition.isRetainedAtRuntime()
                && partContainer.isExtPropertyDefinitionAvailable(extensionPropertyDefinition.getPropertyId());
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        IIpsObjectPartContainer ipsObjectPartContainer = modelNode.getIpsObjectPartContainer();
        List<JavaCodeFragment> annotations = new ArrayList<>();
        for (IExtensionPropertyDefinition extensionPropertyDefinition : ipsObjectPartContainer
                .getExtensionPropertyDefinitions()) {
            if (isRelevant(extensionPropertyDefinition, ipsObjectPartContainer)) {
                annotations.add(createAnnotation(ipsObjectPartContainer, extensionPropertyDefinition));
            }
        }

        JavaCodeFragmentBuilder annotationArg = new JavaCodeFragmentBuilder();
        if (annotations.size() == 1) {
            annotationArg.append(annotations.get(0));
        } else {
            annotationArg.append("{").appendln().append("\t");
            annotationArg.appendJoin(annotations, "," + System.lineSeparator() + "\t");
            annotationArg.append("}");
        }

        return new JavaCodeFragmentBuilder().annotationLn(IpsExtensionProperties.class, annotationArg.getFragment())
                .getFragment();
    }

    public JavaCodeFragment createAnnotation(IIpsObjectPartContainer ipsObjectPartContainer,
            IExtensionPropertyDefinition extensionPropertyDefinition) {
        String propertyId = extensionPropertyDefinition.getPropertyId();

        JavaCodeFragmentBuilder annotationArg = new JavaCodeFragmentBuilder();

        annotationArg.append("id = \"");
        annotationArg.append(propertyId);
        annotationArg.append("\"");
        Object value = ipsObjectPartContainer.getExtPropertyValue(propertyId);
        if (value == null) {
            annotationArg.append(", isNull = true");
        } else {
            annotationArg.append(", value = \"");
            Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
            Element valueElement = doc.createElement("value");
            extensionPropertyDefinition.valueToXml(valueElement, value);
            String valueString = valueElement.getTextContent();
            valueString = StringEscapeUtils.escapeJava(valueString);
            annotationArg.append(valueString);
            annotationArg.append("\"");
        }
        return new JavaCodeFragmentBuilder().annotation(IpsExtensionProperty.class, annotationArg.getFragment())
                .getFragment();
    }

    public static class Factory implements IAnnotationGeneratorFactory {

        @Override
        public boolean isRequiredFor(IIpsProject ipsProject) {
            return true;
        }

        @Override
        public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
            if (isRelevant(type)) {
                return new ExtensionPropertyAnnGen();
            } else {
                return null;
            }
        }

        private boolean isRelevant(AnnotatedJavaElementType type) {
            return RELEVANT_TYPES.contains(type);
        }

    }
}
