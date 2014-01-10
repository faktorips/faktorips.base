/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;

/**
 * Defines an interface for generators which create annotations for IpsElements.
 * 
 * @author Roman Grutza
 */
public interface IAnnotationGenerator {

    /**
     * Returns the type of the Java Element that is being annotated. See
     * {@link AnnotatedJavaElementType} for an enumeration of possible constructs that can be
     * annotated.
     */
    public AnnotatedJavaElementType getAnnotatedJavaElementType();

    /**
     * Creates the annotations for the given ipsElement.
     */
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode);

    /**
     * Returns <code>true</code> if annotations should be generated for the given IpsElement by this
     * generator. Returns <code>false</code> if no annotation should be added by this generator.
     */
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement);
}
