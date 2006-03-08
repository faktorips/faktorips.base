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

package org.faktorips.devtools.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.ArgumentCheck;

public class QualifiedNameType {

    private String qualifiedName;
    private IpsObjectType type;
    private int hashCode;
    
    /**
     * @param name
     * @param type
     */
    public QualifiedNameType(String name, IpsObjectType type) {
        super();
        ArgumentCheck.notNull(name, this);
        ArgumentCheck.notNull(type, this);
        qualifiedName = name;
        this.type = type;
        calculateHashCode();
    }

    public String getName() {
    	return qualifiedName;
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof QualifiedNameType){
            QualifiedNameType other = (QualifiedNameType)obj;
            return type.equals(other.type) && qualifiedName.equals(other.qualifiedName);
        }
        return false;
    }
    
    public IpsObjectType getIpsObjectType(){
        return type;
    }
    
    public IIpsObject findIpsObject(IIpsPackageFragmentRoot root) throws CoreException{
        if(root == null || !root.exists()){
            return null;
        }

        String folderName = ""; //$NON-NLS-1$
        String unqualifiedName = qualifiedName;
        int index = qualifiedName.lastIndexOf(IIpsPackageFragment.SEPARATOR);
        if (index>0) {
            folderName = qualifiedName.substring(0, index);
            unqualifiedName = qualifiedName.substring(index+1);
        }
        IIpsPackageFragment pack = root.getIpsPackageFragment(folderName);
        if (!pack.exists()) {
        	return null;
        }
        if (type==IpsObjectType.PRODUCT_CMPT_TYPE) {
        	return findProductCmptType((IpsPackageFragment)pack, unqualifiedName);
        }
        IIpsSrcFile file = pack.getIpsSrcFile(type.getFileName(unqualifiedName));
        if (!file.exists()) {
            return null;
        }
        return file.getIpsObject();
    }
    
    private IProductCmptType findProductCmptType(IpsPackageFragment pack, String productCmptTypeName) throws CoreException {
		List result = new ArrayList();
		pack.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, result);
		for (Iterator it = result.iterator(); it.hasNext();) {
			PolicyCmptType policyCmptType = (PolicyCmptType) it.next();
			if (policyCmptType.getUnqualifiedProductCmptType().equals(productCmptTypeName)) {
				return new ProductCmptType(policyCmptType);
			}
		}
		return null;
	}
    
    private void calculateHashCode(){
        int result = 17;
        result = result*37 + qualifiedName.hashCode();
        result = result*37 + type.hashCode();
        hashCode = result;
    }
    
    public int hashCode() {
        return hashCode;
    }
    
    public String toString() {
        return type + ": " + qualifiedName; //$NON-NLS-1$
    }
    

}
