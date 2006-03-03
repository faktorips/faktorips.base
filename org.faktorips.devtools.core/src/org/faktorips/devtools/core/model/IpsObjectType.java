package org.faktorips.devtools.core.model;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.businessfct.BusinessFunctionImpl;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.util.ArgumentCheck;


/**
 * Class that represents the type of IPS objects. 
 * 
 * This class is not intended to be subclassed.
 * 
 * @author Jan Ortmann
 */
public final class IpsObjectType {
    
    // the human readable type's name
    private String name;
    
    // Name of xml elements that represent objects of this type. 
    private String xmlElementName;
    
    // extension of files that store objects of this type
    private String fileExtension;
    
    // name of the image file
    private String image;
    
    /**
     * Type for Policy component type.
     */
    public final static IpsObjectType BUSINESS_FUNCTION = 
        new IpsObjectType("BusinessFunction", Messages.IpsObjectType_nameBusinessFunction, "ipsbf", "BusinessFunction.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for Policy component type.
     */
    public final static IpsObjectType POLICY_CMPT_TYPE = 
        new IpsObjectType("PolicyCmptType", Messages.IpsObjectType_namePolicyClass, "ipspct", "PolicyCmptType.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

    /**
     * Type for product component type.
     */
    public final static IpsObjectType PRODUCT_CMPT_TYPE = 
        new IpsObjectType("ProductCmptType", Messages.IpsObjectType_nameProductClass, "ipsproductcmpttype", "PolicyCmptType.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$

    /**
     * Type for table structures.
     */
    public final static IpsObjectType TABLE_STRUCTURE = 
        new IpsObjectType("TableStructure", Messages.IpsObjectType_nameTableStructure, "ipstablestructure", "TableStructure.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for product components.
     */
    public final static IpsObjectType PRODUCT_CMPT = 
        new IpsObjectType("ProductCmpt", Messages.IpsObjectType_nameProductComponent, "ipsproduct", "ProductCmpt.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * Type for tables contents objects.
     */
    public final static IpsObjectType TABLE_CONTENTS = 
        new IpsObjectType("TableContents", Messages.IpsObjectType_nameTableContents, "ipstablecontents", "TableContents.gif"); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    
    public final static IpsObjectType[] ALL_TYPES = new IpsObjectType[]
    	{BUSINESS_FUNCTION, POLICY_CMPT_TYPE, TABLE_STRUCTURE, PRODUCT_CMPT, TABLE_CONTENTS};
    
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
     * Returns the IpsObjectType for the IpsObject that is contained within the provided IpsSrcFile or null
     * if the IpsObjectType cannot be identified.
     * 
     * @deprecated
     */
    public final static IpsObjectType getType(IIpsSrcFile file){
        return getTypeForExtension(file.getCorrespondingFile().getFileExtension());
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
	 * Returns the type's image.
	 */
	public final Image getImage() {
	    return IpsPlugin.getDefault().getImage(image);
	}
    
	/**
	 * Returns the name of a file (including the extension) that stores a IpsObject
	 * with the given name.
	 * 
	 * @throws IllegalArgumentException if pdObjectName is null.
	 */
	public final String getFileName(String pdObjectName) {
		ArgumentCheck.notNull(pdObjectName);
		return pdObjectName + "." + fileExtension; //$NON-NLS-1$
	}
	
	/**
	 * Overridden method.
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return xmlElementName;
	}
    
    private IpsObjectType(
            String xmlElementName, 
            String name, 
            String fileExtension,
            String image) {
        
    	ArgumentCheck.notNull(xmlElementName);
		ArgumentCheck.notNull(name);
		ArgumentCheck.notNull(fileExtension);
		ArgumentCheck.notNull(image);
		
        this.xmlElementName = xmlElementName;
        this.name = name;
        this.fileExtension = fileExtension;
        this.image = image;
    }
    
}
