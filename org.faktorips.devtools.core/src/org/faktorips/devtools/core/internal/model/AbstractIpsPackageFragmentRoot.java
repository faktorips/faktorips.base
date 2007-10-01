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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragmentRoot extends IpsElement implements IIpsPackageFragmentRoot {

    /**
     * @param parent
     * @param name
     */
    public AbstractIpsPackageFragmentRoot(IIpsProject parent, String name) {
        super(parent, name);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return (IIpsProject)parent;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getDefaultIpsPackageFragment() {
        return getIpsPackageFragment(""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getIpsPackageFragment(String name) {
        if (isValidIpsPackageFragmentName(name)) {
            return newIpsPackageFragment(name);
        }
        return null;
    }
    
    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment or a valid
     * package package fragment name according to <code>JavaConventions.validatePackageName</code>.
     */
    protected boolean isValidIpsPackageFragmentName(String name){
        try {
            return !getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg();
        }
        catch (CoreException e) {
            // nothing to do, will return false
        }
        return false;
    }

    protected abstract IIpsPackageFragment newIpsPackageFragment(String name);
    
    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return findIpsObject(new QualifiedNameType(qualifiedName, type));
    }

    /**
     * {@inheritDoc}
     */
    public final IIpsObject findIpsObject(QualifiedNameType qnt) throws CoreException {
        IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
        if (pack==null) {
            return null;
        }
        IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
        if (!file.exists()) {
            return null;
        }
        return file.getIpsObject();
    }
    
    /**
     * Searches all objects of the given type in the root folder and adds them to the result.
     */
    abstract void findIpsObjects(IpsObjectType type, List result) throws CoreException;

     /**
     * Searches all product components that are based on the given policy component type (either
     * directly or because they are based on a subtype of the given type) and adds them to the
     * result. If pcTypeName is <code>null</code>, returns all product components found in the
     * fragment root.
     * 
     * @param pcTypeName The qualified name of the policy component type, product components are
     *            searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on subtypes of the given policy component are returned, otherwise only product
     *            components that are directly based on the given type are returned.
     * @param result List in which the product components being found are stored in.
     */
    public void findProductCmpts(String pcTypeName, boolean includeSubtypes, List result) throws CoreException {
        List allCmpts = new ArrayList(100);
        IPolicyCmptType pcType = null;
        if (includeSubtypes && pcTypeName != null) {
            pcType = getIpsProject().findPolicyCmptType(pcTypeName);
        }
        findIpsObjects(IpsObjectType.PRODUCT_CMPT, allCmpts);
        for (Iterator it = allCmpts.iterator(); it.hasNext();) {
            IProductCmpt each = (IProductCmpt)it.next();
            if (pcTypeName == null || pcTypeName.equals(each.getPolicyCmptType())) {
                result.add(each);
            } else {
                IPolicyCmptType eachPcType = getIpsProject().findPolicyCmptType(each.getPolicyCmptType());
                if (eachPcType == null){
                    // invalid product, cannot find policy cmpt type of product
                    continue;
                }
                if (eachPcType.isSubtypeOf(pcType)) {
                    result.add(each);
                }
            }
        }
    }

    /**
     * Searches all product components that are based on the given product component type (either
     * directly or because they are based on a subtype of the given type) and adds them to the
     * result. If productCmptType is <code>null</code>, returns all product components found in
     * the fragment root.
     * 
     * @param pcTypeName The product component type product components are searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on subtypes of the given policy component are returned, otherwise only product
     *            components that are directly based on the given type are returned.
     * @param result List in which the product components being found are stored in.
     */
    public void findAllProductCmpts(IProductCmptType productCmptType, boolean includeSubytpes, List result)
            throws CoreException {

        IIpsProject ipsProject = getIpsProject();
        List allCmpts = new ArrayList(100);
        findIpsObjects(IpsObjectType.PRODUCT_CMPT, allCmpts);
        for (Iterator iter = allCmpts.iterator(); iter.hasNext();) {
            IProductCmpt productCmpt = (IProductCmpt)iter.next();
            if (productCmptType == null) {
                result.add(productCmpt);
                continue;
            }
            IProductCmptType productCmptTypeFound = productCmpt.findProductCmptType(ipsProject);
            if (productCmptTypeFound == null) {
                continue;
            }
            if (productCmptType.equals(productCmptTypeFound)
                    || (includeSubytpes && productCmptTypeFound.isSubtypeOf(productCmptType, ipsProject)) ) { 
                result.add(productCmpt);
            }
        }
    }
    
    
}
