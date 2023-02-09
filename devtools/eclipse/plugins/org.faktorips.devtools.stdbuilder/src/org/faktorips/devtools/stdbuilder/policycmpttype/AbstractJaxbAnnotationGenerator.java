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

import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.IpsXmlAdapters;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.JaxbAnnotation;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;

/**
 * Base class for annotation generators of JAXB annotations, abstracting over differences between
 * old JAXB and Jakarta XML Binding.
 */
public abstract class AbstractJaxbAnnotationGenerator extends AbstractAnnotationGenerator {

    /**
     * Returns the qualified name of the given annotation when applied at the given model node,
     * depending on the {@link JaxbSupportVariant} setting of that node's project.
     */
    protected String getQualifiedName(JaxbAnnotation jaxbAnnotation, AbstractGeneratorModelNode generatorModelNode) {
        JaxbSupportVariant jaxbSupport = generatorModelNode.getGeneratorConfig().getJaxbSupport();
        return jaxbAnnotation.qualifiedNameFrom(jaxbSupport);
    }

    /**
     * Returns the qualified name of the given IPS xml adapter when applied at the given model node,
     * depending on the {@link JaxbSupportVariant} setting of that node's project.
     */
    protected String getQualifiedName(IpsXmlAdapters ipsAdapters,
            AbstractGeneratorModelNode generatorModelNode) {
        JaxbSupportVariant jaxbSupport = generatorModelNode.getGeneratorConfig().getJaxbSupport();
        return ipsAdapters.qualifiedNameFrom(jaxbSupport);
    }
}