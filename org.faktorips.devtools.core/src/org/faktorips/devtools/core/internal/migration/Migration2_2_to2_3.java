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

package org.faktorips.devtools.core.internal.migration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a static method that performs the migration to version 2.3 featuring new
 * <tt>IEnumType</tt> ips objects.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class Migration2_2_to2_3 {

    /** Prohibit instantiation. */
    private Migration2_2_to2_3() {

    }

    /**
     * Replaces all <tt>ITableStructure</tt>s that have been declared to be enumeration structures
     * with new, abstract <code>IEnumType</code>s.
     * <p>
     * Also, all <tt>ITableContents</tt> that are built upon an <tt>ITableStructure</tt> will become
     * <code>IEnumType</code>s containing the enum values. The referenced table structure will be
     * the super enum type.
     * 
     * @param ipsProject The ips project to migrate to version 2.3.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>ITableStructure</tt> or
     *             <tt>ITableContents</tt> ips objects or while creating the new <tt>IEnumType</tt>
     *             ips objects.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public static void migrate(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        // Find all enum type table structures.
        IIpsSrcFile[] tableStructureSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        List<ITableStructure> enumTableStructures = new ArrayList<ITableStructure>();
        for (IIpsSrcFile currentIpsSrcFile : tableStructureSrcFiles) {
            ITableStructure currentTableStructure = (ITableStructure)currentIpsSrcFile.getIpsObject();
            if (currentTableStructure.getTableStructureType().equals(TableStructureType.ENUMTYPE_MODEL)) {
                enumTableStructures.add(currentTableStructure);
            }
        }

        // Find all table contents that refer to enum type table structures.
        IIpsSrcFile[] tableContentsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
        List<ITableContents> enumTableContents = new ArrayList<ITableContents>();
        for (IIpsSrcFile currentIpsSrcFile : tableContentsSrcFiles) {
            ITableContents currentTableContents = (ITableContents)currentIpsSrcFile.getIpsObject();
            if (currentTableContents.findTableStructure(currentTableContents.getIpsProject()).getTableStructureType()
                    .equals(TableStructureType.ENUMTYPE_MODEL)) {
                enumTableContents.add(currentTableContents);
            }
        }

        // Replace the table structures and table contents.
        replaceTableStructures(enumTableStructures);
        replaceTableContents(enumTableContents);
    }

    /** Replaces the given enum table structures with new enum types. */
    private static void replaceTableStructures(List<ITableStructure> enumTableStructures) throws CoreException {
        /*
         * Create a new enum type object for each of the found enum type table structures and delete
         * the old table structures.
         */
        for (ITableStructure currentTableStructure : enumTableStructures) {
            // Create the new enum type.
            IIpsSrcFile newFile = currentTableStructure.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableStructure.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setAbstract(true);
            newEnumType.setContainingValues(false);

            // Create enum attributes.
            // 2. key is java identifier.
            String identifier = currentTableStructure.getUniqueKeys()[1].getKeyItemAt(0).getName();
            for (IColumn currentColumn : currentTableStructure.getColumns()) {
                IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
                String currentColumnName = currentColumn.getName();
                newEnumAttribute.setName(currentColumnName);
                newEnumAttribute.setDatatype(currentColumn.getDatatype());
                newEnumAttribute.setLiteralName((identifier.equals(currentColumnName)));
                newEnumAttribute.setInherited(false);
            }

            // Delete the old table structure.
            currentTableStructure.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        }
    }

    /**
     * Replaces the given table contents referring to enum table structures with new enum types
     * containing the enum values.
     */
    private static void replaceTableContents(List<ITableContents> enumTableContents) throws CoreException {
        /*
         * Create a new enum type object for each of the found table contents and delete the old
         * table contents.
         */
        for (ITableContents currentTableContents : enumTableContents) {
            // Create the new enum content.
            IIpsSrcFile newFile = currentTableContents.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableContents.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setSuperEnumType(currentTableContents.getTableStructure());
            newEnumType.setAbstract(false);
            newEnumType.setContainingValues(true);

            // Inherit the enum attributes.
            IEnumType superEnumType = currentTableContents.getIpsProject().findEnumType(
                    currentTableContents.getTableStructure());
            for (IEnumAttribute currentEnumAttribute : superEnumType.getEnumAttributes()) {
                IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
                newEnumAttribute.setInherited(true);
                newEnumAttribute.setLiteralName(currentEnumAttribute.isLiteralName());
                newEnumAttribute.setDatatype(currentEnumAttribute.getDatatype());
                newEnumAttribute.setName(currentEnumAttribute.getName());
            }

            // Create enum values.
            for (IRow currentRow : ((ITableContentsGeneration)currentTableContents.getFirstGeneration()).getRows()) {
                IEnumValue newEnumValue = newEnumType.newEnumValue();
                List<IEnumAttributeValue> enumAttributeValues = newEnumValue.getEnumAttributeValues();
                for (int i = 0; i < enumAttributeValues.size(); i++) {
                    IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
                    currentEnumAttributeValue.setValue(currentRow.getValue(i));
                }
            }

            // Delete the old table contents.
            currentTableContents.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        }
    }

}
