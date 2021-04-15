/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpttype;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * A type hierarchy finder that looks for {@link IProductCmptTypeMethod IProductCmptTypeMethods}
 * which are formula methods with the specified formula name. If the flag
 * <code>findAllSignaturesInHierarchy</code> is set to true the search process is interrupted after
 * the first method is found.
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
        methods = new ArrayList<>();
    }

    /**
     * Returns the method that was found by this finder or <code>null</code> if non was found. It
     * only makes sense to call this method after visiting of the type hierarchy has been finished.
     */
    public List<IMethod> getMethods() {
        return new ArrayList<>(methods);
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
