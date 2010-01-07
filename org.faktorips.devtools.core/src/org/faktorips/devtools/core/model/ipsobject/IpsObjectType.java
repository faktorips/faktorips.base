/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.businessfct.BusinessFunctionImpl;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.util.ArgumentCheck;

/**
 * Class that represents the type of IPS objects.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectType {

    /**
     * Type for enum content.
     */
    public final static IpsObjectType ENUM_CONTENT = new IpsObjectType(
            "EnumContent", "EnumContent", Messages.IpsObjectType_nameEnumContent, Messages.IpsObjectType_nameEnumContentPlural, "ipsenumcontent", false, true, EnumContent.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for enum type.
     */
    public final static IpsObjectType ENUM_TYPE = new IpsObjectType(
            "EnumType", "EnumType", Messages.IpsObjectType_nameEnumType, Messages.IpsObjectType_nameEnumTypePlural, "ipsenumtype", false, false, EnumType.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for business function.
     */
    public final static IpsObjectType BUSINESS_FUNCTION = new IpsObjectType(
            "BusinessFunction", "BusinessFunction", Messages.IpsObjectType_nameBusinessFunction, Messages.IpsObjectType_nameBusinessFunctionPlural, "ipsbf", false, false, BusinessFunctionImpl.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for Policy component type.
     */
    public final static IpsObjectType POLICY_CMPT_TYPE = new IpsObjectType(
            "PolicyCmptType", "PolicyCmptType", Messages.IpsObjectType_namePolicyClass, Messages.IpsObjectType_namePolicyClassPlural, "ipspolicycmpttype", true, false, PolicyCmptType.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for product component type.
     */
    public final static IpsObjectType PRODUCT_CMPT_TYPE = new IpsObjectType(
            "ProductCmptType2", "ProductCmptType2", Messages.IpsObjectType_nameProductClass, Messages.IpsObjectType_nameProductClassPlural, "ipsproductcmpttype", true, false, ProductCmptType.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for table structures.
     */
    public final static IpsObjectType TABLE_STRUCTURE = new IpsObjectType(
            "TableStructure", "TableStructure", Messages.IpsObjectType_nameTableStructure, Messages.IpsObjectType_nameTableStructurePlural, "ipstablestructure", false, false, TableStructure.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for product components.
     */
    public final static IpsObjectType PRODUCT_CMPT = new IpsObjectType(
            "ProductCmpt", "ProductCmpt", Messages.IpsObjectType_nameProductComponent, Messages.IpsObjectType_nameProductComponentPlural, "ipsproduct", false, true, ProductCmpt.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for tables contents objects.
     */
    public final static IpsObjectType TABLE_CONTENTS = new IpsObjectType(
            "TableContents", "TableContents", Messages.IpsObjectType_nameTableContents, Messages.IpsObjectType_nameTableContentsPlural, "ipstablecontents", false, true, TableContents.class); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for test case types.
     */
    public final static IpsObjectType TEST_CASE_TYPE = new IpsObjectType(
            "TestCaseType", "TestCaseType", Messages.IpsObjectType_nameTestCaseType, Messages.IpsObjectType_nameTestCaseTypePlural, "ipstestcasetype", false, false, TestCaseType.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Type for test cases.
     */
    public final static IpsObjectType TEST_CASE = new IpsObjectType(
            "TestCase", "TestCase", Messages.IpsObjectType_nameTestCase, Messages.IpsObjectType_nameTestCasePlural, "ipstestcase", false, true, TestCase.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 

    /**
     * Ips source file type for ips objects in none ips source folder.
     */
    public final static IpsObjectType IPS_SOURCE_FILE = new IpsObjectType(
            "Unknown", "Unknown", "Ips Source file", "Ips Source files", "*", false, true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 

    /**
     * Returns the ips object type that has the given file extension. Returns <code>null</code>, if
     * no type with the given file extension exists or <code>null</code> has been given as file
     * extension..
     * 
     * @param fileExtension The file extension of the searched ips object type.
     * 
     * @return The ips object type that corresponds to the given file extension or <code>null</code>
     *         if no type with the given file extension can be found.
     */
    public final static IpsObjectType getTypeForExtension(String fileExtension) {
        for (IpsObjectType currentType : IpsPlugin.getDefault().getIpsModel().getIpsObjectTypes()) {
            if (currentType.fileExtension.equals(fileExtension)) {
                return currentType;
            }
        }

        return null;
    }

    /**
     * Returns the ips object type that has the given name. Returns <code>null</code>, if no type
     * with the given name exists.
     * 
     * @param name The name of the searched ips object type.
     * 
     * @return The ips object type that corresponds to the given name or <code>null</code> if no
     *         type with the given name can be found.
     * 
     * @throws NullPointerException If name is <code>null</code>.
     */
    public final static IpsObjectType getTypeForName(String name) {
        ArgumentCheck.notNull(name);
        for (IpsObjectType currentType : IpsPlugin.getDefault().getIpsModel().getIpsObjectTypes()) {
            if (currentType.id.equals(name)) {
                return currentType;
            }
        }

        return null;
    }

    // The human readable type's name
    private final String displayName;

    // The human readable type's plural name
    private final String displayNamePlural;

    // The identifying name of this type
    private final String id;

    // Name of xml elements that represent objects of this type
    private final String xmlElementName;

    // Extension of files that store objects of this type
    private final String fileExtension;

    // Flag indicating whether this type defines a datatype
    private final boolean datatype;

    // Flag indicating whether this type is a product definition type
    private final boolean productDefinitionType;

    // The class that is implementing the concrete IpsObjectType
    private final Class<? extends IpsObject> implementingClass;

    /**
     * Creates a new ips object for the given file.
     * 
     * @param file The ips source file to create the ips object for.
     * 
     * @return The ips object that has been created for the given ips source file.
     */
    public IIpsObject newObject(IIpsSrcFile file) {
        if (this == POLICY_CMPT_TYPE) {
            return new PolicyCmptType(file);
        }
        if (this == PRODUCT_CMPT_TYPE) {
            return new ProductCmptType(file);
        }
        if (this == TABLE_STRUCTURE) {
            return new TableStructure(file);
        }
        if (this == PRODUCT_CMPT) {
            return new ProductCmpt(file);
        }
        if (this == TABLE_CONTENTS) {
            return new TableContents(file);
        }
        if (this == TEST_CASE_TYPE) {
            return new TestCaseType(file);
        }
        if (this == TEST_CASE) {
            return new TestCase(file);
        }
        if (this == BUSINESS_FUNCTION) {
            return new BusinessFunctionImpl(file);
        }
        if (this == ENUM_TYPE) {
            return new EnumType(file);
        }
        if (this == ENUM_CONTENT) {
            return new EnumContent(file);
        }

        throw new RuntimeException("Can't create object for type " + this); //$NON-NLS-1$
    }

    /**
     * Returns the type's name.
     * 
     * @return The name of this ips object type.
     */
    public final String getId() {
        return id;
    }

    /**
     * Returns the display name of this type.
     * 
     * @return The display name of this ips object type.
     */
    public final String getDisplayName() {
        return displayName;
    }

    /**
     * @return The display plural name of this type.
     */
    public String getDisplayNamePlural() {
        return displayNamePlural;
    }

    /**
     * Returns the name of xml elements that represent the state of PdObjects of that type. This
     * method never returns <code>null</code>.
     * 
     * @return The xml element name of this ips object type.
     */
    public final String getXmlElementName() {
        return xmlElementName;
    }

    /**
     * Returns the extenions of files PdObjects of that type are stored in. This method never
     * returns <code>null</code>.
     * 
     * @return The file extension of this ips object type.
     */
    public final String getFileExtension() {
        return fileExtension;
    }

    /**
     * Returns <code>true</code> if the ips objects of this type are also datatypes, otherwise
     * <code>false</code>.
     * 
     * @return Flag indicating whether this ips object type defines a datatype.
     */
    public boolean isDatatype() {
        return datatype;
    }

    /**
     * Returns <code>true</code> if the object type is a entity type (policy component type or
     * product component type), otherwise <code>false</code>.
     * 
     * @return true for entity types
     */
    public boolean isEntityType() {
        return this == POLICY_CMPT_TYPE || this == PRODUCT_CMPT_TYPE;
    }

    /**
     * Returns <code>true</code> if instances of this type are product definition objects, otherwise
     * <code>false</code>. Currently product components, enum values, table contents and test cases
     * are product definition objects.
     * 
     * @return Flag indicating whether this ips object type is a product definition type.
     */
    public boolean isProductDefinitionType() {
        return productDefinitionType;
    }

    /**
     * Returns the name of a file (including the extension) that stores a ips object with the given
     * name.
     * 
     * @param ipsObjectName
     * 
     * @return The given ips object name with the file extension of this ips object type appended.
     * 
     * @throws NullPointerException If ipsObjectName is <code>null</code>.
     */
    public final String getFileName(String ipsObjectName) {
        ArgumentCheck.notNull(ipsObjectName);
        return ipsObjectName + "." + fileExtension; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return xmlElementName;
    }

    /**
     * Creates a new ips object type.
     * 
     * @param id The name of the new ips object type.
     * @param xmlElementName The name for the xml element.
     * @param displayName A human readable name for the new ips object type.
     * @param displayNamePlural TODO
     * @param fileExtension The file extension for the new ips object type.
     * @param datatype Flag indicating whether this new ips object type represents a datatype.
     * @param productDefinitionType Flag indicating whether this new ips object type is a product
     *            definition type.
     * @param enabledImage Image file for enabled look.
     * @param disabledImage Image file for disabled look.
     * @throws NullPointerException If any of xmlElementName, name, fileExtension or enableImage is
     *             <code>null</code>.
     */
    protected IpsObjectType(String id, String xmlElementName, String displayName, String displayNamePlural,
            String fileExtension, boolean datatype, boolean productDefinitionType,
            Class<? extends IpsObject> implementingClass) {

        ArgumentCheck.notNull(xmlElementName);
        ArgumentCheck.notNull(id);
        ArgumentCheck.notNull(fileExtension);

        this.id = id;
        this.xmlElementName = xmlElementName;
        this.displayName = displayName;
        this.displayNamePlural = displayNamePlural;
        this.fileExtension = fileExtension;
        this.datatype = datatype;
        this.productDefinitionType = productDefinitionType;
        this.implementingClass = implementingClass;
    }

    public Class<? extends IpsObject> getImplementingClass() {
        return implementingClass;
    }

}
