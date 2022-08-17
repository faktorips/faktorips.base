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

import org.faktorips.devtools.model.ipsproject.IBuilderKindId;

public enum BuilderKindIds implements IBuilderKindId {

    PRODUCT_CMPT_TYPE_INTERFACE,

    PRODUCT_CMPT_TYPE_IMPLEMEMENTATION,

    PRODUCT_CMPT_TYPE_GENERATION_INTERFACE,

    PRODUCT_CMPT_TYPE_GENERATION_IMPLEMEMENTATION,

    PRODUCT_CMPT_MODEL_TYPE,

    PRODUCT_CMPT_IMPLEMENTATION,

    PRODUCT_CMPT_XML,

    POLICY_CMPT_TYPE_INTERFACE,

    POLICY_CMPT_TYPE_IMPLEMEMENTATION(),

    POLICY_CMPT_MODEL_TYPE,

    POLICY_CMPT_VALIDATOR_CLASS,

    TABLE(),

    TABLE_ROW(),

    TABLE_CONTENT(),

    TEST_CASE_TYPE,

    TEST_CASE,

    BUSINESS_FUNCTION,

    ENUM_TYPE(),

    ENUM_XML_ADAPTER,

    ENUM_CONTENT,

    ENUM_PROPERTY,

    VALIDATION_RULE_MESSAGES,

    LABELS_AND_DESCRIPTIONS,

    TOC_FILE();

    @Override
    public String getId() {
        return name();
    }

}
