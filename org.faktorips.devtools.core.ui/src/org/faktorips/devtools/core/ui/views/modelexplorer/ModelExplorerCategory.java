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

package org.faktorips.devtools.core.ui.views.modelexplorer;

enum ModelExplorerCategory {

    CAT_IPS_CONTAINERS(0),

    CAT_PROJECT(2),

    CAT_FOLDER(4),

    CAT_POLICY_CMPT_TYPE(11),

    CAT_PRODUCT_CMPT_TYPE(12),

    CAT_ENUM_TYPE(13),

    CAT_BUSINESS_FUNCTION(14),

    CAT_TABLE_STRUCTURE(15),

    CAT_TEST_CASE_TYPE(16),

    CAT_PRODUCT_CMPT(21),

    CAT_ENUM_CONTENT(22),

    CAT_TABLE_CONTENTS(23),

    CAT_TEST_CASE(24),

    CAT_OTHER_IPS_OBJECTS(100);

    private final int order;

    private ModelExplorerCategory(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
