/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Comparator for {@link IpsObjectType IpsObjectTypes} within the HTMLExport.
 * 
 * The order is based on the following List of IpsObjectTypes:
 * {@link IpsObjectType#POLICY_CMPT_TYPE}, {@link IpsObjectType#ENUM_TYPE},
 * {@link IpsObjectType#TABLE_STRUCTURE}, {@link IpsObjectType#PRODUCT_CMPT},
 * {@link IpsObjectType#ENUM_CONTENT}, {@link IpsObjectType#TABLE_CONTENTS}
 * <ol>
 * <li>non-product-definition-types before product-definition-types (depending on the result of
 * {@link IpsObjectType#isProductDefinitionType()})</li>
 * <li>members of the list before non-members</li>
 * <li>the order of the list</li>
 * <li>non datatypes before datatypes (depending on the result of {@link IpsObjectType#isDatatype()}
 * )</li>
 * </ol>
 * 
 * 
 * Note: The ordering by this Comparator is inconsistent with equals, because the comparator just
 * uses the fields productDefinitionType and datatype. If the values of two fields are equal in the
 * compared IpsObjectTypes, then there is no clear ordering.
 * 
 * @author dicker
 */
public class IpsObjectTypeComparator implements Comparator<IpsObjectType> {

    private static final List<IpsObjectType> IPS_OBJECT_TYPES = Arrays.asList(IpsObjectType.PRODUCT_CMPT_TYPE,
            IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.ENUM_TYPE, IpsObjectType.TABLE_STRUCTURE,
            IpsObjectType.TEST_CASE_TYPE, IpsObjectType.PRODUCT_CMPT, IpsObjectType.PRODUCT_TEMPLATE,
            IpsObjectType.ENUM_CONTENT, IpsObjectType.TABLE_CONTENTS, IpsObjectType.TEST_CASE);

    @Override
    public int compare(IpsObjectType o1, IpsObjectType o2) {

        if (o1.equals(o2)) {
            return 0;
        }

        if (o1.isProductDefinitionType() && !o2.isProductDefinitionType()) {
            return 1;
        }
        if (!o1.isProductDefinitionType() && o2.isProductDefinitionType()) {
            return -1;
        }

        int indexOf1 = IPS_OBJECT_TYPES.indexOf(o1);
        int indexOf2 = IPS_OBJECT_TYPES.indexOf(o2);

        if (indexOf1 != -1) {
            if (indexOf2 != -1) {
                return indexOf1 - indexOf2;
            } else {
                return -1;
            }
        }
        if (indexOf2 != -1) {
            return 1;
        }

        if (o1.isDatatype() && !o2.isDatatype()) {
            return -1;
        }
        if (!o1.isDatatype() && o2.isDatatype()) {
            return 1;
        }

        return 0;
    }
}
