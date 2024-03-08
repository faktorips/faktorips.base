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

import java.util.ArrayList;
import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.XType;
import org.faktorips.runtime.model.annotation.IpsAssociations;
import org.faktorips.runtime.model.annotation.IpsAttributes;

public abstract class AbstractTypeDeclClassAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        if (modelNode instanceof XType type) {
            return new JavaCodeFragmentBuilder().append(createAnnType(type)).append(createAnnAttributes(type))
                    .append(createAnnAssociations(type)).getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

    protected abstract JavaCodeFragment createAnnType(XType type);

    /**
     * @return an annotation containing the names of all {@code XAttribute}s if the type has any.
     * @see IpsAttributes
     */
    protected JavaCodeFragment createAnnAttributes(XType type) {
        Set<? extends AbstractGeneratorModelNode> attributes = type.getAllDeclaredAttributes();
        Class<?> annotationClass = IpsAttributes.class;
        return createAnnotationWithNodes(annotationClass, attributes);
    }

    /**
     * @return an annotation containing the names of all {@code XAssociation}s if the type has any.
     * @see IpsAssociations
     */
    protected JavaCodeFragment createAnnAssociations(XType type) {
        Set<? extends AbstractGeneratorModelNode> associations = type.getAllDeclaredAssociations();
        Class<?> annotationClass = IpsAssociations.class;
        return createAnnotationWithNodes(annotationClass, associations);
    }

    protected JavaCodeFragment createAnnotationWithNodes(Class<?> annotationClass,
            Set<? extends AbstractGeneratorModelNode> nodes) {
        if (nodes.size() > 0) {
            ArrayList<String> nodeNames = new ArrayList<>();
            for (AbstractGeneratorModelNode node : nodes) {
                nodeNames.add(node.getName());
            }
            return new JavaCodeFragmentBuilder().annotationLn(annotationClass,
                    "{\"" + String.join("\", \"", nodeNames) + "\"}").getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

    @Override
    public abstract boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement);
}
