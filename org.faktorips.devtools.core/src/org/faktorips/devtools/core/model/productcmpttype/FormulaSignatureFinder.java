/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;

/**
 * A type hierarchy finder that looks for {@link IProductCmptTypeMethod}s which are formula methods
 * with the specified formula name. If the flag <code>findAllSignaturesInHierarchy</code> is set to
 * true the search process is interrupted after the first method is found.
 * 
 * @author Peter Erzberger
 */
public class FormulaSignatureFinder extends TypeHierarchyVisitor<IProductCmptType> {

    private String formulaName;
    private List<IMethod> methods;
    private boolean findAllSignaturesInHierarchy = false;

    public FormulaSignatureFinder(IIpsProject ipsProject, String formulaName, boolean findAllSignaturesInHierarchy) {
        super(ipsProject);
        this.formulaName = formulaName;
        this.findAllSignaturesInHierarchy = findAllSignaturesInHierarchy;
        methods = new ArrayList<IMethod>();
    }

    /**
     * Returns the method that was found by this finder or <code>null</code> if non was found. It
     * only makes sense to call this method after visiting of the type hierarchy has been finished.
     */
    public List<IMethod> getMethods() {
        return new ArrayList<IMethod>(methods);
    }

    @Override
    protected boolean visit(IProductCmptType currentType) {
        IProductCmptTypeMethod formulaSignature = currentType.getFormulaSignature(formulaName);
        if (formulaSignature != null) {
            methods.add(formulaSignature);
        }
        return findAllSignaturesInHierarchy || methods.isEmpty();
    }

}
