/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    ModelExplorerCategory(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
