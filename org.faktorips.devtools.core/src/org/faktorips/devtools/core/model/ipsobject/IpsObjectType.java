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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.businessfct.BusinessFunctionImpl;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.model.Messages;
import org.faktorips.util.ArgumentCheck;


/**
 * Class that represents the type of IPS objects. 
 * 
 * @author Jan Ortmann
 */
public class IpsObjectType {
    
    /**
     * Type for business function.
     */
    public final static IpsObjectType BUSINESS_FUNCTION = 
        new IpsObjectType("BusinessFunction", Messages.IpsObjectType_nameBusinessFunction, "ipsbf", false, false, "BusinessFunction.gif", "BusinessFunctionDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    /**
     * Type for Policy component type.
     */
    public final static IpsObjectType POLICY_CMPT_TYPE = 
        new IpsObjectType("PolicyCmptType", Messages.IpsObjectType_namePolicyClass, "ipspolicycmpttype", true, false, "PolicyCmptType.gif", "PolicyCmptTypeDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Type for product component type.
     */
    public final static IpsObjectType PRODUCT_CMPT_TYPE_V2 = 
        new IpsObjectType("ProductCmptType2", Messages.IpsObjectType_nameProductClass, "ipsproductcmpttype", true, false, "ProductCmptType.gif", "ProductCmptTypeDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Type for product component type.
     */
    public final static IpsObjectType OLD_PRODUCT_CMPT_TYPE = 
        new IpsObjectType("ProductCmptType", Messages.IpsObjectType_nameProductClass, "ipsproductcmpttype", false, false, "PolicyCmptType.gif", "PolicyCmptTypeDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Type for table structures.
     */
    public final static IpsObjectType TABLE_STRUCTURE = 
        new IpsObjectType("TableStructure", Messages.IpsObjectType_nameTableStructure, "ipstablestructure", false, false, "TableStructure.gif", "TableStructureDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    /**
     * Type for product components.
     */
    public final static IpsObjectType PRODUCT_CMPT = 
        new IpsObjectType("ProductCmpt", Messages.IpsObjectType_nameProductComponent, "ipsproduct", false, true, "ProductCmpt.gif", "ProductCmptDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    /**
     * Type for tables contents objects.
     */
    public final static IpsObjectType TABLE_CONTENTS = 
        new IpsObjectType("TableContents", Messages.IpsObjectType_nameTableContents, "ipstablecontents", false, true, "TableContents.gif", "TableContentsDisabled.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    /**
     * Type for test case types.
     */
    public final static IpsObjectType TEST_CASE_TYPE =  
        new IpsObjectType("TestCaseType", Messages.IpsObjectType_nameTestCaseType, "ipstestcasetype", false, false, "TestCaseType.gif", "TestCaseTypeDisabled.gif");   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Type for test cases.
     */
    public final static IpsObjectType TEST_CASE =  
        new IpsObjectType("TestCase", Messages.IpsObjectType_nameTestCase, "ipstestcase", false, true, "TestCase.gif", "TestCaseDisabled.gif");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    //  TODO refactor: use IpsModel.getIpsObjectTypes!!!
    public static IpsObjectType[] ALL_TYPES = null;  
    
    /**
     * Ips source file type for ips objects in none ips source folder.
     */
    public final static IpsObjectType IPS_SOURCE_FILE =  
        new IpsObjectType("Unknown", "Ips Source file", "*", false, true, "IpsSrcFile.gif", "IpsSrcFileDisabled.gif");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    
    /**
     * Returns the IpsObjectType that has the given file extension. 
	 * Returns null, if no type with the given file extension exists or the given
	 * fileExtension is null.
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
    
    // name of the image file with enabled look
    private String enabledImage;
    
    // name of the image file with disabled look
    private String disabledImage;
    
    private boolean datatype = false;
    
    private boolean productDefinitionType = false;
	
	/**
	 * Creates the ips object for the given file.
	 */
	public IIpsObject newObject(IIpsSrcFile file) {
	    if (this==POLICY_CMPT_TYPE) {
	        return new PolicyCmptType(file);
	    }
        if (this==PRODUCT_CMPT_TYPE_V2) {
            return new ProductCmptType(file);
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
        if (this==BUSINESS_FUNCTION) {
            return new BusinessFunctionImpl(file);
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
    
    /**
     * Returns <code>true</code> if instanced of this type are product definition objects,
     * otherwise <code>false</code>. Currently product components, table contents and
     * test cases are product definition objects.
     */
    public boolean isProductDefinitionType(){
        return productDefinitionType;
    }
	
    /**
     * Returns the Image with the indicated enabled or disabled look.
     */
    public final Image getImage(boolean enabled) {
        if (enabled) {
            return getEnabledImage();
        } else {
            return getDisabledImage();
        }
    }
    
	/**
	 * Returns the type's image with enabled look.
	 */
	public final Image getEnabledImage() {
	    return IpsPlugin.getDefault().getImage(enabledImage);
	}
    
    /**
     * Returns the type's image with disabled look.
     */
    public final Image getDisabledImage() {
        if (disabledImage==null) { 
            return getEnabledImage(); 
        }
        return IpsPlugin.getDefault().getImage(disabledImage);
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
    
    public IpsObjectType(
            String xmlElementName, 
            String name, 
            String fileExtension,
            boolean datatype,
            boolean productDefinitionType,
            String enabledImage,
            String disabledImage) {
        
    	ArgumentCheck.notNull(xmlElementName);
		ArgumentCheck.notNull(name);
		ArgumentCheck.notNull(fileExtension);
		ArgumentCheck.notNull(enabledImage);
		
        this.xmlElementName = xmlElementName;
        this.name = name;
        this.fileExtension = fileExtension;
        this.datatype = datatype;
        this.productDefinitionType = productDefinitionType;
        this.enabledImage = enabledImage;
        this.disabledImage = disabledImage;
    }
    
}
