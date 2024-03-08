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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.runtime.internal.IpsStringUtils;

public class DeprecationJavadocTagGenerator extends AbstractAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragmentBuilder annotationCodeFragmentBuilder = new JavaCodeFragmentBuilder();
        IIpsObjectPartContainer ipsObjectPartContainer = modelNode.getIpsObjectPartContainer();
        if (ipsObjectPartContainer instanceof IVersionControlledElement
                && ((IVersionControlledElement)ipsObjectPartContainer).isDeprecated()) {
            IDeprecation deprecation = ((IVersionControlledElement)ipsObjectPartContainer).getDeprecation();

            annotationCodeFragmentBuilder.append("@deprecated");
            boolean forRemoval = deprecation.isForRemoval();
            if (forRemoval) {
                annotationCodeFragmentBuilder.append(" for removal");
            }
            String sinceVersionString = deprecation.getSinceVersionString();
            boolean hasVersion = IpsStringUtils.isNotBlank(sinceVersionString);
            if (hasVersion) {
                annotationCodeFragmentBuilder.append(" since ");
                annotationCodeFragmentBuilder.append(sinceVersionString);
            }
            IDescription description = deprecation.getDescription(modelNode.getLanguageUsedInGeneratedSourceCode());
            if (description != null && IpsStringUtils.isNotBlank(description.getText())) {
                if (forRemoval || hasVersion) {
                    annotationCodeFragmentBuilder.append(". ");
                } else {
                    annotationCodeFragmentBuilder.append(" ");
                }
                annotationCodeFragmentBuilder.append(description.getText());
            }
            annotationCodeFragmentBuilder.appendln();
        }
        return annotationCodeFragmentBuilder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        IIpsObjectPartContainer ipsObjectPartContainer = modelNode.getIpsObjectPartContainer();
        return ipsObjectPartContainer instanceof IVersionControlledElement
                && ((IVersionControlledElement)ipsObjectPartContainer).isDeprecated();
    }

}
