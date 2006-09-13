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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.businessfct.BusinessFunctionImpl;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.util.ArgumentCheck;


/**
 * Class that represents the type of IPS objects. 
 * 
 * This class is not intended to be subclassed.
 * 
 * @author Jan Ortmann
 */
public final class IpsObjectType {
    
    /**
     * Type for Policy component type.
     */
    public final static IpsObjectType BUSINESS_FUNCTION = 
        new IpsObjectType("BusinessFunction", Messages.IpsObjectType_nameBusinessFunction, "ipsbf", false, false, "BusinessFunction.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for Policy component type.
     */
    public final static IpsObjectType POLICY_CMPT_TYPE = 
        new IpsObjectType("PolicyCmptType", Messages.IpsObjectType_namePolicyClass, "ipspct", true, false, "PolicyCmptType.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

    /**
     * Type for product component type.
     */
    public final static IpsObjectType PRODUCT_CMPT_TYPE = 
        new IpsObjectType("ProductCmptType", Messages.IpsObjectType_nameProductClass, "ipsproductcmpttype", false, false, "PolicyCmptType.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

    /**
     * Type for table structures.
     */
    public final static IpsObjectType TABLE_STRUCTURE = 
        new IpsObjectType("TableStructure", Messages.IpsObjectType_nameTableStructure, "ipstablestructure", false, false, "TableStructure.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for product components.
     */
    public final static IpsObjectType PRODUCT_CMPT = 
        new IpsObjectType("ProductCmpt", Messages.IpsObjectType_nameProductComponent, "ipsproduct", false, true, "ProductCmpt.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for tables contents objects.
     */
    public final static IpsObjectType TABLE_CONTENTS = 
        new IpsObjectType("TableContents", Messages.IpsObjectType_nameTableContents, "ipstablecontents", false, true, "TableContents.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for test case types.
     */
    public final static IpsObjectType TEST_CASE_TYPE =  
        new IpsObjectType("TestCaseType", "Test Case Type", "ipstestcasetype", false, false, "TestCaseType.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Type for test cases.
     */
    public final static IpsObjectType TEST_CASE =  
        new IpsObjectType("TestCase", "Test Case", "ipstestcase", false, true, "TestCase.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    public final static IpsObjectType[] ALL_TYPES = new IpsObjectType[]
    	{BUSINESS_FUNCTION, POLICY_CMPT_TYPE, TABLE_STRUCTURE, PRODUCT_CMPT, TABLE_CONTENTS, TEST_CASE_TYPE, TEST_CASE};
    
    /**
     * Returns the IpsObjectType that has the given file extension. 
	 * Returns null, if no type with the given file extension exists or the given
	 * fileExtension is null.
     * 
     */
    public final static IpsObjectType getTypeForExtension(String fileExtension) {
        for (int i=0; i<ALL_TYPES.length; i++) {
            if (ALL_TYPES[i].fileExtension.equals(fileExtension)) {
                return ALL_TYPES[i];
            }
        }
        return null;
    }
    
	/**
	 * Returns the IpsObjectType that has the given name.
	 * Returns <code>null</code>, if no type with the given name exists or the given
	 * name is <code>null</code>.
	 */
	public final static IpsObjectType getTypeForName(String name) {
		for (int i=0; i<ALL_TYPES.length; i++) {
			if (ALL_TYPES[i].name.equals(name)) {
				return ALL_TYPES[i];
			}
		}
		return null;
	}
	
    // the human readable type's name
    private String name;
    
    // Name of xml elements that represent objects of this type. 
    private String xmlElementName;
    
    // extension of files that store objects of this type
    private String fileExtension;
    
    // name of the image file
    private String image;
    
    private boolean datatype = false;
    
    private boolean productDefinitionType = false;
	
	/**
	 * Creates the ips object for the given file.
	 */
	public final IIpsObject newObject(IIpsSrcFile file) {
	    if (this==BUSINESS_FUNCTION) {
	        return new BusinessFunctionImpl(file);
	    }
	    if (this==POLICY_CMPT_TYPE) {
	        return new PolicyCmptType(file);
	    }
	    if (this==TABLE_STRUCTURE) {
	        return new TableStructure(file);
	    }
	    if (this==PRODUCT_CMPT) {
	        return new ProductCmpt(file);
	    }
	    if (this==TABLE_CONTENTS) {
	        return new TableContents(file);
	    }
	    if (this==TEST_CASE_TYPE) {
	        return new TestCaseType(file);
	    }
	    if (this==TEST_CASE) {
	        return new TestCase(file);
	    }	    
	    throw new RuntimeException("Can't create object for type " + this); //$NON-NLS-1$
	}

	/**
	 * Returns the type's name. The name is used for example in the
	 * QualifiedName.
	 * This method never returns null. 
	 */
	public final String getName() {
		return name;
	}
    
	/**
	 * Returns the name of Xml elements that represent the state of PdObjects
	 * of that type. 
	 * This method never returns null. 
	 */
	public final String getXmlElementName() {
		return xmlElementName;
	}
    
	/**
	 * Returns the extenions of files PdObjects of that type are stored in. 
	 * This method never returns null. 
	 */
	public final String getFileExtension() {
		return fileExtension;
	}
	
	/**
	 * Returns <code>true</code> if the ips objects of this type are also datatypes,
	 * otherwise <code>false</code>. 
	 */
	public boolean isDatatype() {
		return datatype;
	}
    
    public boolean isProductDefinitionType(){
        return productDefinitionType;
    }
	
	/**
	 * Returns the type's image.
	 */
	public final Image getImage() {
	    return IpsPlugin.getDefault().getImage(image);
	}
    
	/**
	 * Returns the name of a file (including the extension) that stores a IpsObject
	 * with the given name.
	 * 
	 * @throws IllegalArgumentException if ipsObjectName is null.
	 */
	public final String getFileName(String ipsObjectName) {
		ArgumentCheck.notNull(ipsObjectName);
		return ipsObjectName + "." + fileExtension; //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final String toString() {
		return xmlElementName;
	}
    
    private IpsObjectType(
            String xmlElementName, 
            String name, 
            String fileExtension,
            boolean datatype,
            boolean productDefinitionType,
            String image) {
        
    	ArgumentCheck.notNull(xmlElementName);
		ArgumentCheck.notNull(name);
		ArgumentCheck.notNull(fileExtension);
		ArgumentCheck.notNull(image);
		
        this.xmlElementName = xmlElementName;
        this.name = name;
        this.fileExtension = fileExtension;
        this.datatype = datatype;
        this.productDefinitionType = productDefinitionType;
        this.image = image;
    }
    
}
