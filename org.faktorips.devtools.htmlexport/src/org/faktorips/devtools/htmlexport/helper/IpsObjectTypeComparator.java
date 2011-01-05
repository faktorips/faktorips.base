/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.Comparator;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * Comparator for IpsObjectTypes.
 * 
 * Node: The ordering by this Comparator is inconsistent with equals
 * 
 * 
 * @author dicker
 */
public class IpsObjectTypeComparator implements Comparator<IpsObjectType> {
    @Override
    public int compare(IpsObjectType o1, IpsObjectType o2) {
        if (o1.isProductDefinitionType() && !o2.isProductDefinitionType()) {
            return 1;
        }
        if (!o1.isProductDefinitionType() && o2.isProductDefinitionType()) {
            return -1;
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