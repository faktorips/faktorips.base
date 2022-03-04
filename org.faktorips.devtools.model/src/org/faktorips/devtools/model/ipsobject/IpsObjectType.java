/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.internal.testcasetype.TestCaseType;
import org.faktorips.util.ArgumentCheck;

/**
 * Class that represents the type of IPS objects.
 * 
 * @author Jan Ortmann
 */
public class IpsObjectType {

    /**
     * Type for enumeration content.
     */
    public static final IpsObjectType ENUM_CONTENT = new IpsObjectType(
            "EnumContent", "EnumContent", Messages.IpsObjectType_nameEnumContent, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameEnumContentPlural, "ipsenumcontent", false, true, EnumContent.class); //$NON-NLS-1$

    /**
     * Type for enumeration type.
     */
    public static final IpsObjectType ENUM_TYPE = new IpsObjectType(
            "EnumType", "EnumType", Messages.IpsObjectType_nameEnumType, Messages.IpsObjectType_nameEnumTypePlural, //$NON-NLS-1$ //$NON-NLS-2$
            "ipsenumtype", false, false, EnumType.class); //$NON-NLS-1$

    /**
     * Type for Policy component type.
     */
    public static final IpsObjectType POLICY_CMPT_TYPE = new IpsObjectType(
            "PolicyCmptType", "PolicyCmptType", Messages.IpsObjectType_namePolicyClass, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_namePolicyClassPlural, "ipspolicycmpttype", true, false, PolicyCmptType.class); //$NON-NLS-1$

    /**
     * Type for product component type.
     */
    public static final IpsObjectType PRODUCT_CMPT_TYPE = new IpsObjectType(
            "ProductCmptType2", "ProductCmptType2", Messages.IpsObjectType_nameProductClass, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameProductClassPlural, "ipsproductcmpttype", true, false, ProductCmptType.class); //$NON-NLS-1$

    /**
     * Type for table structures.
     */
    public static final IpsObjectType TABLE_STRUCTURE = new IpsObjectType(
            "TableStructure", "TableStructure", Messages.IpsObjectType_nameTableStructure, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameTableStructurePlural, "ipstablestructure", false, false, TableStructure.class); //$NON-NLS-1$

    /**
     * Type for product components.
     */
    public static final IpsObjectType PRODUCT_CMPT = new IpsObjectType(
            "ProductCmpt", "ProductCmpt", Messages.IpsObjectType_nameProductComponent, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameProductComponentPlural, "ipsproduct", false, true, ProductCmpt.class); //$NON-NLS-1$

    /**
     * Type for product components.
     */
    public static final IpsObjectType PRODUCT_TEMPLATE = new IpsObjectType(
            "ProductTemplate", "ProductCmpt", Messages.IpsObjectType_nameProductTemplate, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameProductTemplatePlural, "ipstemplate", false, true, ProductCmpt.class); //$NON-NLS-1$

    /**
     * Type for tables contents objects.
     */
    public static final IpsObjectType TABLE_CONTENTS = new IpsObjectType(
            "TableContents", "TableContents", Messages.IpsObjectType_nameTableContents, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameTableContentsPlural, "ipstablecontents", false, true, TableContents.class); //$NON-NLS-1$

    /**
     * Type for test case types.
     */
    public static final IpsObjectType TEST_CASE_TYPE = new IpsObjectType(
            "TestCaseType", "TestCaseType", Messages.IpsObjectType_nameTestCaseType, //$NON-NLS-1$ //$NON-NLS-2$
            Messages.IpsObjectType_nameTestCaseTypePlural, "ipstestcasetype", false, false, TestCaseType.class); //$NON-NLS-1$

    /**
     * Type for test cases.
     */
    public static final IpsObjectType TEST_CASE = new IpsObjectType(
            "TestCase", "TestCase", Messages.IpsObjectType_nameTestCase, Messages.IpsObjectType_nameTestCasePlural, //$NON-NLS-1$ //$NON-NLS-2$
            "ipstestcase", false, true, TestCase.class); //$NON-NLS-1$

    /**
     * IPS source file type for IPS objects in none IPS source folder.
     */
    public static final IpsObjectType IPS_SOURCE_FILE = new IpsObjectType(
            "Unknown", "Unknown", "Ips Source file", "Ips Source files", "*", false, true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    /** The human readable type's name */
    private final String displayName;

    /** The human readable type's plural name */
    private final String displayNamePlural;

    /** The identifying name of this type */
    private final String id;

    /** Name of xml elements that represent objects of this type */
    private final String xmlElementName;

    /** Extension of files that store objects of this type */
    private final String fileExtension;

    /** Flag indicating whether this type defines a data type */
    private final boolean datatype;

    /** Flag indicating whether this type is a product definition type */
    private final boolean productDefinitionType;

    /** The class that is implementing the concrete IpsObjectType */
    private final Class<? extends IpsObject> implementingClass;

    /**
     * Creates a new IPS object type.
     * 
     * @param id The name of the new IPS object type.
     * @param xmlElementName The name for the XML element.
     * @param displayName A human readable name for the new IPS object type.
     * @param fileExtension The file extension for the new IPS object type.
     * @param datatype Flag indicating whether this new IPS object type represents a data type.
     * @param productDefinitionType Flag indicating whether this new IPS object type is a product
     *            definition type.
     * 
     * @throws NullPointerException If any of xmlElementName, name, fileExtension or enableImage is
     *             <code>null</code>.
     */
    // CSOFF: ParameterNumber
    protected IpsObjectType(String id, String xmlElementName, String displayName, String displayNamePlural,
            String fileExtension, boolean datatype, boolean productDefinitionType,
            Class<? extends IpsObject> implementingClass) {
        // CSON: ParameterNumber
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

    /**
     * Returns the IPS object type that has the given file extension. Returns <code>null</code>, if
     * no type with the given file extension exists or <code>null</code> has been given as file
     * extension..
     * 
     * @param fileExtension The file extension of the searched IPS object type.
     * 
     * @return The IPS object type that corresponds to the given file extension or <code>null</code>
     *         if no type with the given file extension can be found.
     */
    public static final IpsObjectType getTypeForExtension(String fileExtension) {
        for (IpsObjectType currentType : IIpsModel.get().getIpsObjectTypes()) {
            if (currentType.fileExtension.equals(fileExtension)) {
                return currentType;
            }
        }

        return null;
    }

    /**
     * Returns the IPS object type that has the given name. Returns <code>null</code>, if no type
     * with the given name exists.
     * 
     * @param name The name of the searched IPS object type.
     * 
     * @return The IPS object type that corresponds to the given name or <code>null</code> if no
     *         type with the given name can be found.
     * 
     * @throws NullPointerException If name is <code>null</code>.
     */
    public static final IpsObjectType getTypeForName(String name) {
        ArgumentCheck.notNull(name);
        for (IpsObjectType currentType : IIpsModel.get().getIpsObjectTypes()) {
            if (currentType.id.equals(name)) {
                return currentType;
            }
        }

        return null;
    }

    /**
     * Creates a new IPS object for the given file.
     * 
     * @param file The IPS source file to create the IPS object for.
     * 
     * @return The IPS object that has been created for the given IPS source file.
     */
    // CSOFF: CyclomaticComplexity
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
        if (this == PRODUCT_TEMPLATE) {
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
        if (this == ENUM_TYPE) {
            return new EnumType(file);
        }
        if (this == ENUM_CONTENT) {
            return new EnumContent(file);
        }

        throw new RuntimeException("Can't create object for type " + this); //$NON-NLS-1$
    }
    // CSON: CyclomaticComplexity

    /**
     * Returns the type's name.
     * 
     * @return The name of this IPS object type.
     */
    public final String getId() {
        return id;
    }

    /**
     * Returns the display name of this type.
     * 
     * @return The display name of this IPS object type.
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
     * Returns the name of XML elements that represent the state of PdObjects of that type. This
     * method never returns <code>null</code>.
     * 
     * @return The XML element name of this IPS object type.
     */
    public final String getXmlElementName() {
        return xmlElementName;
    }

    /**
     * Returns the extensions of files PdObjects of that type are stored in. This method never
     * returns <code>null</code>.
     * 
     * @return The file extension of this IPS object type.
     */
    public final String getFileExtension() {
        return fileExtension;
    }

    /**
     * Returns <code>true</code> if the IPS objects of this type are also data types, otherwise
     * <code>false</code>.
     * 
     * @return Flag indicating whether this IPS object type defines a data type.
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
     * <code>false</code>. Currently product components, enumeration values, table contents and test
     * cases are product definition objects.
     * 
     * @return Flag indicating whether this IPS object type is a product definition type.
     */
    public boolean isProductDefinitionType() {
        return productDefinitionType;
    }

    /**
     * Returns the name of a file (including the extension) that stores a IPS object with the given
     * name.
     * 
     * @return The given IPS object name with the file extension of this IPS object type appended.
     * 
     * @throws NullPointerException If ipsObjectName is <code>null</code>.
     */
    public final String getFileName(String ipsObjectName) {
        ArgumentCheck.notNull(ipsObjectName);
        return ipsObjectName + "." + fileExtension; //$NON-NLS-1$
    }

    @Override
    public final String toString() {
        return "IpsObjectTyp: " + displayName; //$NON-NLS-1$
    }

    public Class<? extends IpsObject> getImplementingClass() {
        return implementingClass;
    }

}
