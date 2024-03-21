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

import org.faktorips.devtools.model.IIpsElement;

/**
 * Java element types that can be annotated.
 */
public enum AnnotatedJavaElementType {

    PRODUCT_CMPT_IMPL_CLASS,

    PRODUCT_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_GETTER,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_ALLOWED_VALUES_SETTER,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_DEFAULT_SETTER,

    PRODUCT_CMPT_DECL_CLASS_ATTRIBUTE_SETTER,

    /**
     * Represents the declaration of association getter methods. The getters are either declared in
     * the published interface or in the implementation class, in case no published interfaces are
     * being generated.
     */
    PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER,

    /**
     * Represents the declaration of association remover methods. The remover methods are declared
     * in the implementation classes.
     */
    PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_REMOVER,

    /**
     * Represents the declaration of association setter(..1)/adder(..*) methods. The setters/adders
     * are either declared in the published interface or in the implementation class, in case no
     * published interfaces are being generated.
     */
    PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER,

    /**
     * Represents the declaration of association setter(..1)/adder(..*) methods, that specify a
     * cardinality. The setters/adders are either declared in the published interface or in the
     * implementation class, in case no published interfaces are being generated.
     */
    PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_WITH_CARDINALITY_SETTER_ADDER,

    /**
     * Represents the declaration of methods that return all links for an association. The link
     * getters are either declared in the published interface or in the implementation class, in
     * case no published interfaces are being generated.
     */
    PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_LINKS,

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
     * Represents the declaration of association setter(..1)/adder(..*) methods. The setters/adders
     * are either declared in the published interface or in the implementation class, in case no
     * published interfaces are being generated.
     */
    POLICY_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER,

    /**
     * Represents the declaration of association remover methods. The removers are either declared
     * in the published interface or in the implementation class, in case no published interfaces
     * are being generated.
     */
    POLICY_CMPT_DECL_CLASS_ASSOCIATION_REMOVER,

    /**
     * Represents the published interface if it is generated, else the implementation class.
     */
    POLICY_CMPT_DECL_CLASS,

    /**
     * Represents the declaration of a policy component's separate validator class.
     */
    POLICY_CMPT_SEPARATE_VALIDATOR_CLASS,

    /**
     * Represents the declaration of validation rule execution methods.
     */
    POLICY_CMPT_VALIDATION_RULE,

    /**
     * Represents the published interface only, and <em>never</em> the implementation class. Thus it
     * is never used unless published interfaces are being generated.
     */
    PUBLISHED_INTERFACE_CLASS,

    TABLE_CLASS,

    TABLE_ROW_CLASS_COLUMN_GETTER,

    TABLE_USAGE_GETTER,

    FORMULA_COMPUTATION_METHOD,

    ENUM_CLASS,

    ENUM_ATTRIBUTE_GETTER,

    /** Usable when {@link Deprecated} is the only annotation relevant for an element **/
    DEPRECATION,

    /**
     * Using this type the annotation generator provides java doc tags for any {@link IIpsElement}.
     */
    ELEMENT_JAVA_DOC;
}
