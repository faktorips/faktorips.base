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

package org.faktorips.devtools.core.ui.search.product.conditions;

import java.util.ArrayList;
import java.util.List;

public class SearchOperatorFactory {

    public List<ISearchOperatorType> getSearchOperatorTypes(Class<?> clazz) {
        List<ISearchOperatorType> searchOperatorTypes = new ArrayList<ISearchOperatorType>();

        EqualitySearchOperatorType[] values = EqualitySearchOperatorType.values();

        for (EqualitySearchOperatorType searchOperatorType : values) {
            if (searchOperatorType.getApplicableClass().isAssignableFrom(clazz)) {
                searchOperatorTypes.add(searchOperatorType);
            }
        }

        return searchOperatorTypes;
    }

    public ISearchOperator getSearchOperator(EqualitySearchOperatorType searchOperatorType) {
        switch (searchOperatorType) {
            case EQUALITY:

                break;

            default:
                break;
        }

        ISearchOperator searchOperator = null;
        return searchOperator;

    }
}
