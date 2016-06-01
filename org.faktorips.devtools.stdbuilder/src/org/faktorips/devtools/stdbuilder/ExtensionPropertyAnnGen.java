/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsExtensionProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates the {@link IpsExtensionProperty} annotation.
 */
public class ExtensionPropertyAnnGen implements IAnnotationGenerator {

    private static final List<AnnotatedJavaElementType> RELEVANT_TYPES = Arrays.asList(
            AnnotatedJavaElementType.TABLE_CLASS, AnnotatedJavaElementType.TABLE_ROW_CLASS_COLUMN_GETTER,
            AnnotatedJavaElementType.PRODUCT_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER,
            AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER,
            AnnotatedJavaElementType.POLICY_CMPT_DECLARATION_CLASS,
            AnnotatedJavaElementType.PRODUCT_CMPT_DECLARATION_CLASS,
            AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ASSOCIATION_GETTER,
            AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER);

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        IExtensionPropertyAccess extensionPropertyAccess = modelNode.getIpsObjectPartContainer();
        for (IExtensionPropertyDefinition extensionPropertyDefinition : extensionPropertyAccess
                .getExtensionPropertyDefinitions()) {
            /*
             * If an extension property is not available in the current installation, obtaining it's
             * value may fail, see
             * org.faktorips.devtools.core.internal.model.ipsobject.ExtensionPropertyHandler
             * .getExtPropertyValue(String)
             */
            if (extensionPropertyAccess.isExtPropertyDefinitionAvailable(extensionPropertyDefinition.getPropertyId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        IIpsObjectPartContainer ipsObjectPartContainer = modelNode.getIpsObjectPartContainer();
        List<JavaCodeFragment> annotations = new ArrayList<JavaCodeFragment>();
        for (IExtensionPropertyDefinition extensionPropertyDefinition : ipsObjectPartContainer
                .getExtensionPropertyDefinitions()) {
            String propertyId = extensionPropertyDefinition.getPropertyId();
            if (ipsObjectPartContainer.isExtPropertyDefinitionAvailable(propertyId)) {
                annotations.add(createAnnotation(ipsObjectPartContainer, extensionPropertyDefinition));
            }
        }

        JavaCodeFragmentBuilder annotationArg = new JavaCodeFragmentBuilder();
        if (annotations.size() == 1) {
            annotationArg.append(annotations.get(0));
        } else {
            annotationArg.append("{");
            for (JavaCodeFragment annotation : annotations) {
                annotationArg.appendln();
                annotationArg.append("\t");
                annotationArg.append(annotation);
            }
            annotationArg.append(" }");
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
            Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
            Element valueElement = doc.createElement("value");
            extensionPropertyDefinition.valueToXml(valueElement, value);
            try {
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, ipsObjectPartContainer.getIpsProject()
                        .getXmlFileCharset());
                DOMSource source = new DOMSource(valueElement);
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);
                String valueString = writer.toString().replace("\r\r", "\r");
                valueString = valueString.substring(valueString.indexOf("<value>") + 7,
                        valueString.lastIndexOf("</value>"));
                valueString = StringEscapeUtils.escapeJava(valueString);
                annotationArg.append(valueString);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
            annotationArg.append("\"");
        }
        return new JavaCodeFragmentBuilder().annotationLn(IpsExtensionProperty.class, annotationArg.getFragment())
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
