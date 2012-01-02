/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * Comparator for {@link IpsObjectType IpsObjectTypes} within the HTMLExport.
 * 
 * The order is based on the following List of IpsObjectTypes:
 * {@link IpsObjectType#POLICY_CMPT_TYPE}, {@link IpsObjectType#ENUM_TYPE},
 * {@link IpsObjectType#TABLE_STRUCTURE}, {@link IpsObjectType#PRODUCT_CMPT},
 * {@link IpsObjectType#ENUM_CONTENT}, {@link IpsObjectType#TABLE_CONTENTS}
 * <ol>
 * <li>non productdefinitiontypes before productdefinitiontypes (depending on the result of
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

    private static List<IpsObjectType> IPS_OBJECT_TYPES = Arrays.asList(new IpsObjectType[] {
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.ENUM_TYPE,
            IpsObjectType.TABLE_STRUCTURE, IpsObjectType.PRODUCT_CMPT, IpsObjectType.ENUM_CONTENT,
            IpsObjectType.TABLE_CONTENTS });

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