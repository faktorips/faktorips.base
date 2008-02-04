/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A type hierarchy finder that looks for {@link IProductCmptTypeMethod}s which are formula methods with the specified formula name. If
 * the flag <code>findAllSignaturesInHierarchy</code> is set to true the search process is interrupted after the first method is found.
 * 
 * @author Peter Erzberger
 */
public class FormulaSignatureFinder extends ProductCmptTypeHierarchyVisitor {

    private String formulaName;
    private List methods;
    private boolean findAllSignaturesInHierarchy = false;
    
    public FormulaSignatureFinder(IIpsProject ipsProject, String formulaName, boolean findAllSignaturesInHierarchy) {
        super(ipsProject);
        this.formulaName = formulaName;
        this.findAllSignaturesInHierarchy = findAllSignaturesInHierarchy;
        methods = new ArrayList();
    }

    /**
     * Returns the method that was found by this finder or <code>null</code> if non was found. It only makes sense to call this
     * method after visiting of the type hierarchy has been finished.
     */
    public List getMethods() {
        return new ArrayList(methods);
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean visit(IProductCmptType currentType) throws CoreException {
        IProductCmptTypeMethod formulaSignature = currentType.getFormulaSignature(formulaName);
        if(formulaSignature != null){
            methods.add(formulaSignature);
        }
        return findAllSignaturesInHierarchy || methods.isEmpty();
    }
    
}