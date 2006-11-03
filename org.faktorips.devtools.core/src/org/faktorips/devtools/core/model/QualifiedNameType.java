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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.ArgumentCheck;

/**
 * Combines the qualified name and ips object type.
 * 
 * @author Jan Ortmann
 */
public class QualifiedNameType {

    private String qualifiedName;
    private IpsObjectType type;
    private int hashCode;
    
    /**
     * Returns the qualified name type for he given path. 
     * 
     * @param pathToFile a relative to an ips src file, e.g. base/motor/MotorPolicy.ipspct
     * @return The qualified name type
     * 
     * @throws CoreException if the path can't be parsed to a qualified name type
     */
    public final static QualifiedNameType newQualifedNameType(String pathToFile) throws CoreException {
        int index = pathToFile.lastIndexOf('.');
        if (index==-1 || index==pathToFile.length()-1) {
            throw new CoreException(new IpsStatus("Path " + pathToFile + " can't be parsed to a qualified name type."));
        }
        IpsObjectType type = IpsObjectType.getTypeForExtension(pathToFile.substring(index+1));
        if (type==null) {
            throw new CoreException(new IpsStatus("Path " + pathToFile + " does not specifiy an ips object type."));
        }
        String qName = pathToFile.substring(0, index).replace(IPath.SEPARATOR, IpsPackageFragment.SEPARATOR);
        if (qName.equals("")) {
            throw new CoreException(new IpsStatus("Path " + pathToFile + " does not specifiy a qualified name."));
        }
        return new QualifiedNameType(qName, type);
    }
    
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

    /**
     * Returns the qualified name.
     */
    public String getName() {
    	return qualifiedName;
    }

    /**
     * Returns the ips object type.
     */
    public IpsObjectType getIpsObjectType(){
        return type;
    }

    /**
     * Returns the package name part of the qualified name.
     */
    public String getPackageName() {
        int index = qualifiedName.lastIndexOf('.');
        if (index==-1) {
            return "";
        }
        return qualifiedName.substring(0, index);
    }

    /**
     * Returns the unqualified name.
     */
    public String getUnqualifiedName() {
        int index = qualifiedName.lastIndexOf('.');
        if (index==-1) {
            return qualifiedName;
        }
        if (index==qualifiedName.length()-1) {
            return "";
        }
        return qualifiedName.substring(index+1);
    }
    
    /**
     * Transforms this qualified name part into an IPath.
     * E.g.: mycompany.motor.MotorPolicy of type PolicyCmptType becomes mycompany/motor/MotorPolicy.ipspct
     */
    public IPath toPath() {
        return new Path(qualifiedName.replace(IIpsPackageFragment.SEPARATOR, IPath.SEPARATOR)
            + '.' + type.getFileExtension());
    }
    
    /**
     * Returns the name for files in that an ips object with this qualified name type is stored.
     * E.g.: for "mycompany.motor.MotorPolicy" of type PolicyCmptType the method returns "MotorPolicy.ipspct"
     */
    public String getFilename() {
        return getUnqualifiedName() + "." + type.getFileExtension();
    }
    
    public IIpsObject findIpsObject(IIpsPackageFragmentRoot root) throws CoreException{
        if(root == null || !root.exists()){
            return null;
        }

        String packName = ""; //$NON-NLS-1$
        String unqualifiedName = qualifiedName;
        int index = qualifiedName.lastIndexOf(IIpsPackageFragment.SEPARATOR);
        if (index>0) {
            packName = qualifiedName.substring(0, index);
            unqualifiedName = qualifiedName.substring(index+1);
        }
        IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
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
    
    public boolean equals(Object obj) {
        if(obj instanceof QualifiedNameType){
            QualifiedNameType other = (QualifiedNameType)obj;
            return type.equals(other.type) && qualifiedName.equals(other.qualifiedName);
        }
        return false;
    }
    
    public String toString() {
        return type + ": " + qualifiedName; //$NON-NLS-1$
    }
    

}
