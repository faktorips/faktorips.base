/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestCase;

/**
 * Find references to a given product cmpt. This query searches for product cmpt's and test cases
 * which contains an reference to the given product cmpt.
 * 
 * @author Stefan Widmaier
 */
public class ReferencesToProductSearchQuery extends ReferenceSearchQuery {
    
    public ReferencesToProductSearchQuery(IProductCmpt referenced) {
        super(referenced);
    }
    
    /**
     * @inheritDoc
     */
    protected IIpsElement[] findReferences() throws CoreException {
        IIpsElement[] refProductCmptGenerations = referenced.getIpsProject().findReferencingProductCmptGenerations(
                referenced.getQualifiedNameType());
        IIpsElement[] refTestCases = referenced.getIpsProject().findReferencingTestCases(referenced.getQualifiedName());
        
        List generations = Arrays.asList(refProductCmptGenerations);
        List testCases = Arrays.asList(refTestCases);

        List result = new ArrayList(refProductCmptGenerations.length + refTestCases.length);
        result.addAll(generations);
        result.addAll(testCases);
        return (IIpsElement[]) result.toArray(new IIpsElement[result.size()]);
    }
    
    /**
     * @inheritDoc
     */
    protected Object[] getDataForResult(IIpsElement object) {
        if (object instanceof IProductCmptGeneration){
            return new Object[]{((IProductCmptGeneration)object).getProductCmpt(), object};
        } else if (object instanceof ITestCase) {
            return new Object[]{object};
        }
        return null;
	}
}
