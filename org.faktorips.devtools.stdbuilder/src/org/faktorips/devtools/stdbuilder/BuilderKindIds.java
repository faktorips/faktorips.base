/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.model.ipsproject.IBuilderKindId;

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

    TABLE(),

    TABLE_ROW(),

    TABLE_CONTENT(),

    TEST_CASE_TYPE,

    TEST_CASE,

    BUSINESS_FUNCTION,

    ENUM_TYPE(),

    ENUM_XML_ADAPTER,

    ENUM_CONTENT,

    TOC_FILE(),

    VALIDATION_RULE_MESSAGES;

    @Override
    public String getId() {
        return name();
    }

}
