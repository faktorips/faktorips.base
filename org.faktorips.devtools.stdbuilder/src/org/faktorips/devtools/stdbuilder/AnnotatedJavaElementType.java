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
 */
public enum AnnotatedJavaElementType {

    PRODUCT_CMPT_IMPL_CLASS,

    PRODUCT_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_SETTER,

    /**
     * Represents the declaration of association getter methods. The getters are either declared in
     * the published interface or in the implementation class, in case no published interfaces are
     * being generated.
     */
    PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER,
    /**
     * Represents the published interface if it is generated, else the implementation class.
     */
    PRODUCT_CMPT_DECL_CLASS,

    POLICY_CMPT_IMPL_CLASS,

    POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD,

    POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER,

    POLICY_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES,

    POLICY_CMPT_DECL_CLASS_ATTRIBUTE_SETTER,

    POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD,

    POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD,

    POLICY_CMPT_IMPL_CLASS_PRODUCTCONFIGURATION_FIELD,

    /**
     * Represents the declaration of association getter methods. The getters are either declared in
     * the published interface or in the implementation class, in case no published interfaces are
     * being generated.
     */
    POLICY_CMPT_DECL_CLASS_ASSOCIATION_GETTER,

    /**
     * Represents the published interface if it is generated, else the implementation class.
     */
    POLICY_CMPT_DECL_CLASS,

    /**
     * Represents the published interface only, and <em>never</em> the implementation class. Thus it
     * is never used unless published interfaces are being generated.
     */
    PUBLISHED_INTERFACE_CLASS,

    TABLE_CLASS,

    TABLE_ROW_CLASS_COLUMN_GETTER,

    TABLE_USAGE_GETTER,

    /**
     * Using this type the annotation generator provides java doc tags for any {@link IIpsElement}.
     */
    ELEMENT_JAVA_DOC;
}
