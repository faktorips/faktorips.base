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
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;

/**
 * Defines an interface for generators which create annotations for IpsElements.
 * 
 * @author Roman Grutza
 */
public interface IAnnotationGenerator {

    /**
     * Creates the annotations for the given ipsElement.
     */
    JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode);

    /**
     * Returns <code>true</code> if annotations should be generated for the given IpsElement by this
     * generator. Returns <code>false</code> if no annotation should be added by this generator.
     */
    boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement);
}
