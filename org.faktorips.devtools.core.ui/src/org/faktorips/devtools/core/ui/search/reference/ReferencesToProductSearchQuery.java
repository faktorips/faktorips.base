/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.model.testcase.ITestCase;

/**
 * Find references to a given product cmpt. This query searches for product cmpt's and test cases
 * which contains an reference to the given product cmpt.
 * 
 * @author Stefan Widmaier
 */
public class ReferencesToProductSearchQuery extends AbstractReferenceFromProductSearchQuery {

    public ReferencesToProductSearchQuery(IProductCmpt referenced) {
        super(referenced);
    }

    @Override
    protected IIpsElement[] findReferences() throws CoreException {
        List<IProductPartsContainer> referencingProductCmptGenerations = getReferencingProductCmptGenerations();
        List<ITestCase> referencingTestCases = referenced.getIpsModel().searchReferencingTestCases(
                (IProductCmpt)referenced);

        List<IIpsElement> result = new ArrayList<IIpsElement>(referencingProductCmptGenerations.size()
                + referencingTestCases.size());
        result.addAll(referencingProductCmptGenerations);
        result.addAll(referencingTestCases);
        return result.toArray(new IIpsElement[result.size()]);
    }

    @Override
    protected Object[] getDataForResult(IIpsElement object) {
        if (object instanceof ITestCase) {
            return new Object[] { object };
        }

        return super.getDataForResult(object);
    }
}
