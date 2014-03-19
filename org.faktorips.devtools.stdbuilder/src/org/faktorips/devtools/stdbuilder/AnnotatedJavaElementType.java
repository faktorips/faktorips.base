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

import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Java element types that can be annotated.
 * 
 * @author Roman Grutza
 */
public enum AnnotatedJavaElementType {

    POLICY_CMPT_IMPL_CLASS,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_SETTER,

    POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD,

    POLICY_CMPT_IMPL_CLASS_ASSOCIATION,

    /**
     * Using this type the annotation generator provides java doc tags for any {@link IIpsElement}.
     */
    ELEMENT_JAVA_DOC;
}
