/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import java.lang.Runtime.Version;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.runtime.internal.IpsStringUtils;

public class DeprecationAnnotationGenerator extends AbstractAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragmentBuilder annotationCodeFragmentBuilder = new JavaCodeFragmentBuilder();
        IIpsObjectPartContainer ipsObjectPartContainer = modelNode.getIpsObjectPartContainer();
        if (ipsObjectPartContainer instanceof IVersionControlledElement
                && ((IVersionControlledElement)ipsObjectPartContainer).isDeprecated()) {
            IDeprecation deprecation = ((IVersionControlledElement)ipsObjectPartContainer).getDeprecation();
            JavaCodeFragmentBuilder deprecatedAnnArg = new JavaCodeFragmentBuilder();
            Version sourceVersion = ipsObjectPartContainer.getIpsProject().getJavaProject().getSourceVersion();
            if (sourceVersion.compareToIgnoreOptional(Version.parse("9")) >= 0) {
                String sinceVersionString = deprecation.getSinceVersionString();
                boolean hasVersion = IpsStringUtils.isNotBlank(sinceVersionString);
                if (hasVersion) {
                    deprecatedAnnArg.append("since = ");
                    deprecatedAnnArg.appendQuoted(sinceVersionString);
                }
                if (deprecation.isForRemoval()) {
                    if (hasVersion) {
                        deprecatedAnnArg.append(", ");
                    }
                    deprecatedAnnArg.append("forRemoval = ");
                    deprecatedAnnArg.append(Boolean.toString(Boolean.TRUE));
                }
            }
            annotationCodeFragmentBuilder.annotationLn(Deprecated.class, deprecatedAnnArg.getFragment());
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
