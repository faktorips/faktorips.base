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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.faktorips.devtools.core.IpsStatus;
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
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from Faktor-IPS Version 2.2.1.test to 2.3.0.rc1.
 * <p>
 * Replaces all table structures that have been declared to be enumeration structures with new,
 * abstract <code>IEnumType</code>s.
 * <p>
 * All table contents that are built upon such a table structure will also become
 * <code>IEnumType</code>s and will contain the enum values. The referenced table structure will be
 * the super enum type.
 * 
 * @author Alexander Weickmann
 */
public class Migration_2_2_1_test extends AbstractIpsProjectMigrationOperation {

    /** Creates <code>Migration_2_2_1_test</code>. */
    public Migration_2_2_1_test(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "New ips object types" + " have been added for modeling enumerations. All table structures"
                + " that represent enum types will be changed to abstract EnumType objects. The referencing"
                + " table contents will also be replaced with EnumType objects containing the enum values.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTargetVersion() {
        return "2.3.0.rc1"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        Job job = new Job("Migration230") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {

                    // Find all enum type table structures
                    IIpsSrcFile[] tableStructureSrcFiles = getIpsProject().findIpsSrcFiles(
                            IpsObjectType.TABLE_STRUCTURE);
                    List<ITableStructure> enumTableStructures = new ArrayList<ITableStructure>();
                    for (IIpsSrcFile currentIpsSrcFile : tableStructureSrcFiles) {
                        ITableStructure currentTableStructure = (ITableStructure)currentIpsSrcFile.getIpsObject();
                        if (currentTableStructure.getTableStructureType().equals(TableStructureType.ENUMTYPE_MODEL)) {
                            enumTableStructures.add(currentTableStructure);
                        }
                    }

                    // Find all table contents that refer to enum type table structures
                    IIpsSrcFile[] tableContentsSrcFiles = getIpsProject().findIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
                    List<ITableContents> enumTableContents = new ArrayList<ITableContents>();
                    for (IIpsSrcFile currentIpsSrcFile : tableContentsSrcFiles) {
                        ITableContents currentTableContents = (ITableContents)currentIpsSrcFile.getIpsObject();
                        if (currentTableContents.findTableStructure(currentTableContents.getIpsProject())
                                .getTableStructureType().equals(TableStructureType.ENUMTYPE_MODEL)) {
                            enumTableContents.add(currentTableContents);
                        }
                    }

                    // Replace the table structures and table contents
                    replaceTableStructures(enumTableStructures, monitor);
                    replaceTableContents(enumTableContents, monitor);

                } catch (CoreException e) {
                    return new IpsStatus(e);
                }

                return new IpsStatus(IStatus.OK, "");
            }
        };

        getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
        job.setPriority(Job.BUILD);
        job.schedule(5000);

        job = new Job("Migration230PostBuild") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                } catch (CoreException e) {
                    return new IpsStatus(e);
                }

                return new IpsStatus(IStatus.OK, "");
            }
        };

        job.setPriority(Job.BUILD);
        job.schedule(6000);

        return new MessageList();
    }

    /** Replaces the given enum table structures with new enum types. */
    private void replaceTableStructures(List<ITableStructure> enumTableStructures, IProgressMonitor monitor)
            throws CoreException {

        /*
         * Create a new enum type object for each of the found enum type table structures and delete
         * the old table structures.
         */
        for (ITableStructure currentTableStructure : enumTableStructures) {
            // Create the new enum type
            IIpsSrcFile newFile = currentTableStructure.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableStructure.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setAbstract(true);
            newEnumType.setValuesArePartOfModel(false);

            // Create enum attributes
            // 2. key is java identifier
            String identifier = currentTableStructure.getUniqueKeys()[1].getKeyItemAt(0).getName();
            for (IColumn currentColumn : currentTableStructure.getColumns()) {
                IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
                String currentColumnName = currentColumn.getName();
                newEnumAttribute.setName(currentColumnName);
                newEnumAttribute.setDatatype(currentColumn.getDatatype());
                newEnumAttribute.setIdentifier((identifier.equals(currentColumnName)));
                newEnumAttribute.setInherited(false);
            }
            
            // Save the new file
            newEnumType.getIpsSrcFile().save(true, null);

            // Delete the old table structure
            currentTableStructure.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        }
    }

    /**
     * Replaces the given table contents refering to enum table structures with new enum types
     * containing the enum values.
     */
    private void replaceTableContents(List<ITableContents> enumTableContents, IProgressMonitor monitor)
            throws CoreException {

        /*
         * Create a new enum type object for each of the found table contents and delete the old
         * table contents.
         */
        for (ITableContents currentTableContents : enumTableContents) {
            // Create the new enum content
            IIpsSrcFile newFile = currentTableContents.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableContents.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setSuperEnumType(currentTableContents.getTableStructure());
            newEnumType.setAbstract(false);
            newEnumType.setValuesArePartOfModel(true);

            // Inherit the enum attributes
            IEnumType superEnumType = currentTableContents.getIpsProject().findEnumType(
                    currentTableContents.getTableStructure());
            for (IEnumAttribute currentEnumAttribute : superEnumType.getEnumAttributes()) {
                IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
                newEnumAttribute.setInherited(true);
                newEnumAttribute.setIdentifier(currentEnumAttribute.isIdentifier());
                newEnumAttribute.setDatatype(currentEnumAttribute.getDatatype());
                newEnumAttribute.setName(currentEnumAttribute.getName());
            }

            // Create enum values
            for (IRow currentRow : ((ITableContentsGeneration)currentTableContents.getFirstGeneration()).getRows()) {
                IEnumValue newEnumValue = newEnumType.newEnumValue();
                List<IEnumAttributeValue> enumAttributeValues = newEnumValue.getEnumAttributeValues();
                for (int i = 0; i < enumAttributeValues.size(); i++) {
                    IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
                    currentEnumAttributeValue.setValue(currentRow.getValue(i));
                }
            }
            
            // Save the new file
            newEnumType.getIpsSrcFile().save(true, null);

            // Delte the old table contents
            currentTableContents.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        }
    }

}
