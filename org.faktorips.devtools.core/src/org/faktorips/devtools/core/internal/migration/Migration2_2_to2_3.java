/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a static method that performs the migration to version 2.3 featuring new
 * <tt>IEnumType</tt> IPS objects.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
@SuppressWarnings("restriction")
public class Migration2_2_to2_3 {

    private Migration2_2_to2_3() {
        // Prohibit instantiation.
    }

    /**
     * Creates new, abstract <tt>IEnumType</tt>s for all <tt>ITableStructure</tt>s that have been
     * declared to be enumeration structures. The old <tt>ITableStructure</tt>s will not be deleted
     * however because this would result in <tt>NullPointerException</tt>s when an
     * <tt>ITableContents</tt> in another project is based upon an <tt>ITableStructure</tt> already
     * deleted. Clients need to delete the <tt>ITableStructure</tt>s themselves after the migration
     * was performed.
     * <p>
     * Also, all <tt>ITableContents</tt> that are built upon an <tt>ITableStructure</tt> will become
     * <code>IEnumType</code>s containing the enumeration values. The referenced table structure
     * will be the super enumeration type.
     * 
     * @param ipsProject The IPS project to migrate to version 2.3.
     * @param monitor The progress monitor to use to show progress to the user or <tt>null</tt> if
     *            none is available.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>ITableStructure</tt> or
     *             <tt>ITableContents</tt> IPS objects or while creating the new <tt>IEnumType</tt>
     *             IPS objects.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    @SuppressWarnings("deprecation")
    public static void migrate(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        List<IIpsSrcFile> allIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.collectAllIpsSrcFilesOfSrcFolderEntries(allIpsSrcFiles);

        // Find all enumeration type table structures.
        List<ITableStructure> enumTableStructures = new ArrayList<ITableStructure>();
        for (IIpsSrcFile currentIpsSrcFile : allIpsSrcFiles) {
            if (currentIpsSrcFile.getIpsObjectType().equals(IpsObjectType.TABLE_STRUCTURE)) {
                ITableStructure currentTableStructure = (ITableStructure)currentIpsSrcFile.getIpsObject();
                if (currentTableStructure.getTableStructureType().equals(TableStructureType.ENUMTYPE_MODEL)) {
                    enumTableStructures.add(currentTableStructure);
                }
            }
        }

        // Find all table contents that refer to enumeration type table structures.
        List<ITableContents> enumTableContents = new ArrayList<ITableContents>();
        for (IIpsSrcFile currentIpsSrcFile : allIpsSrcFiles) {
            if (currentIpsSrcFile.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
                ITableContents currentTableContents = (ITableContents)currentIpsSrcFile.getIpsObject();
                ITableStructure tableStructure = currentTableContents.findTableStructure(currentTableContents
                        .getIpsProject());
                if (tableStructure == null) {
                    continue;
                }
                if (tableStructure.getTableStructureType().equals(TableStructureType.ENUMTYPE_MODEL)) {
                    enumTableContents.add(currentTableContents);
                }
            }
        }

        // Start the progress monitor if available (now we know how much work needs to be done).
        if (monitor != null) {
            monitor.beginTask("Migration", enumTableStructures.size() + enumTableContents.size()); //$NON-NLS-1$
        }

        MultiStatus status = new MultiStatus(IpsPlugin.PLUGIN_ID, 0,
                "At least one exception occurred while migrating the ips project: \"" + ipsProject.getName() //$NON-NLS-1$
                        + "\" from faktor ips version 2.2 to 2.3.", null); //$NON-NLS-1$
        // Add enumeration types for the table structures and replace the table contents.
        addForTableStructures(enumTableStructures, monitor, status);
        replaceTableContents(enumTableContents, ipsProject, monitor);

        migrateInitFromXmlMethods(ipsProject, monitor);

        // Finish the monitor if available.
        if (monitor != null) {
            monitor.done();
        }
        if (status.getChildren().length > 0) {
            throw new CoreException(status);
        }
    }

    @SuppressWarnings( { "unchecked" })
    private static void migrateInitFromXmlMethods(IIpsProject ipsProject, IProgressMonitor monitor)
            throws CoreException {

        for (IPackageFragmentRoot root : ipsProject.getJavaProject().getPackageFragmentRoots()) {
            for (IJavaElement javaElement : root.getChildren()) {
                if (javaElement instanceof IPackageFragment) {
                    IPackageFragment fragment = (IPackageFragment)javaElement;
                    for (IJavaElement packageEl : fragment.getChildren()) {
                        if (packageEl instanceof ICompilationUnit) {
                            ICompilationUnit cu = (ICompilationUnit)packageEl;
                            IType type = cu.findPrimaryType();
                            if (type instanceof SourceType) {
                                ASTParser parser = ASTParser.newParser(AST.JLS3);
                                parser.setSource(cu);
                                CompilationUnit rootNode = (CompilationUnit)parser.createAST(monitor);
                                rootNode.recordModifications();
                                AbstractTypeDeclaration abstractDeclaration = (AbstractTypeDeclaration)rootNode.types()
                                        .get(0);
                                boolean modified = false;
                                if (abstractDeclaration instanceof TypeDeclaration) {
                                    TypeDeclaration typeDecl = (TypeDeclaration)abstractDeclaration;
                                    for (MethodDeclaration methodDecl : typeDecl.getMethods()) {
                                        String methodName = methodDecl.getName().getFullyQualifiedName();
                                        if (methodName.startsWith("initPropertiesFromXml")) { //$NON-NLS-1$
                                            List parameterList = methodDecl.parameters();
                                            if (parameterList.size() == 1) {
                                                SingleVariableDeclaration parameter = (SingleVariableDeclaration)parameterList
                                                        .get(0);
                                                Type paramType = parameter.getType();
                                                if (paramType instanceof SimpleType) {
                                                    SimpleType paraTypeSimple = (SimpleType)paramType;
                                                    if (paraTypeSimple.getName().getFullyQualifiedName().equals(
                                                            "HashMap")) { //$NON-NLS-1$
                                                        methodDecl.delete();
                                                        modified = true;
                                                    }
                                                }
                                                if (paramType instanceof ParameterizedType) {
                                                    ParameterizedType paraType = (ParameterizedType)paramType;
                                                    Type hashTable = paraType.getType();
                                                    if (hashTable instanceof SimpleType) {
                                                        if (((SimpleType)hashTable).getName().getFullyQualifiedName()
                                                                .equals("Map")) { //$NON-NLS-1$
                                                            methodDecl.delete();
                                                            modified = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (modified) {
                                        Document document = new Document(cu.getSource());
                                        TextEdit edits = rootNode.rewrite(document, cu.getJavaProject()
                                                .getOptions(true));
                                        try {
                                            edits.apply(document);
                                            cu.getBuffer().setContents(document.get());
                                            cu.save(monitor, true);
                                        } catch (Exception e) {
                                            throw new CoreException(new IpsStatus(e));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Adds new <tt>IEnumType</tt>s for the given enumeration table structures.
     */
    private static void addForTableStructures(List<ITableStructure> enumTableStructures,
            IProgressMonitor monitor,
            MultiStatus status) throws CoreException {

        /*
         * Create a new EnumType object for each of the found enumeration type table structures. Do
         * not delete the old table structures however (must be done manually by the users after the
         * migration).
         */
        for (ITableStructure currentTableStructure : enumTableStructures) {
            // Create the new enumeration type.
            IIpsSrcFile newFile = currentTableStructure.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableStructure.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setAbstract(true);
            newEnumType.setContainingValues(false);

            @SuppressWarnings("deprecation")
            // OK to suppress warning as there is no locale for a description in version 2.3
            String oldDescription = currentTableStructure.getDescription();
            IDescription description = newEnumType.newDescription();
            description.setText(oldDescription);

            // Create enumeration attributes.
            // 1. key is the id, 2. key is the name.
            IUniqueKey[] uniqueKeys = currentTableStructure.getUniqueKeys();
            String id = uniqueKeys[0].getKeyItemAt(0).getName();
            String name = uniqueKeys[1].getKeyItemAt(0).getName();
            for (IColumn currentColumn : currentTableStructure.getColumns()) {
                IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
                String currentColumnName = currentColumn.getName();
                newEnumAttribute.setName(currentColumnName);
                newEnumAttribute.setDatatype(currentColumn.getDatatype());
                if (name.equals(currentColumnName)) {
                    newEnumAttribute.setUnique(true);
                    newEnumAttribute.setUsedAsNameInFaktorIpsUi(true);
                }
                if (id.equals(currentColumnName)) {
                    newEnumAttribute.setUnique(true);
                    newEnumAttribute.setIdentifier(true);
                }
                newEnumAttribute.setInherited(false);

                @SuppressWarnings("deprecation")
                // OK to suppress warning as there is no locale for a description in version 2.3
                String columnDescription = currentColumn.getDescription();
                IDescription attributeDescription = newEnumAttribute.newDescription();
                attributeDescription.setText(columnDescription);
            }

            renameTableStructure(currentTableStructure, monitor, status);

            // Update monitor if available.
            if (monitor != null) {
                monitor.worked(1);
            }
        }
    }

    /**
     * Renames the TableStructure. It puts an "Old_" in front of the name. If the renaming fails a
     * status is written to the provided MultiStatus Object.
     */
    private static void renameTableStructure(ITableStructure currentTableStructure,
            IProgressMonitor monitor,
            MultiStatus status) {
        try {
            IFile correspondingFile = currentTableStructure.getIpsSrcFile().getCorrespondingFile();
            String correspondingFileName = correspondingFile.getName();
            String markedAsOldFileName = "Old_" + correspondingFileName; //$NON-NLS-1$
            InputStream correspondingFileContents;
            correspondingFileContents = correspondingFile.getContents(true);
            IContainer correspondingFileContainer = correspondingFile.getParent();
            if (correspondingFileContainer instanceof IFolder) {
                IFolder correspondingFileParentFolder = (IFolder)correspondingFileContainer;
                IFile file = correspondingFileParentFolder.getFile(markedAsOldFileName);
                file.create(correspondingFileContents, true, monitor);
                correspondingFile.delete(true, monitor);
            }
        } catch (CoreException e) {
            status.add(new IpsStatus(e));
        }
    }

    /**
     * Replaces the given table contents referring to enumeration table structures with new
     * <tt>IEnumType</tt>s containing the <tt>IEnumValue</tt>s.
     */
    private static void replaceTableContents(List<ITableContents> enumTableContents,
            IIpsProject ipsProject,
            IProgressMonitor monitor) throws CoreException {

        /*
         * Create a new EnumType object for each of the found table contents and delete the old
         * table contents.
         */
        for (ITableContents currentTableContents : enumTableContents) {
            // Create the new EnumType, extend from old table structure (new abstract EnumType).
            IIpsSrcFile newFile = currentTableContents.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableContents.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setSuperEnumType(currentTableContents.getTableStructure());
            newEnumType.setAbstract(false);
            newEnumType.setContainingValues(true);

            // Inherit the EnumAttributes.
            newEnumType.inheritEnumAttributes(newEnumType.findInheritEnumAttributeCandidates(ipsProject));

            // Create new literal name attribute.
            IEnumLiteralNameAttribute literalNameAttribute = newEnumType.newEnumLiteralNameAttribute();
            IEnumAttribute nameAttribute = newEnumType.findUsedAsNameInFaktorIpsUiAttribute(ipsProject);
            if (nameAttribute != null) {
                literalNameAttribute.setDefaultValueProviderAttribute(nameAttribute.getName());
            }

            // Create the enumeration values.
            for (IRow currentRow : ((ITableContentsGeneration)currentTableContents.getFirstGeneration()).getRows()) {
                IEnumValue newEnumValue = newEnumType.newEnumValue();
                List<IEnumAttributeValue> enumAttributeValues = newEnumValue.getEnumAttributeValues();
                for (int i = 0; i < enumAttributeValues.size(); i++) {
                    IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
                    String value = ""; //$NON-NLS-1$
                    if (i == enumAttributeValues.size() - 1) {
                        /*
                         * The last enumeration attribute value is the literal name attribute value.
                         * For this, we obtain the value from the name attribute. If not known the
                         * literal name values will be empty (should theoretically not happen).
                         */
                        if (nameAttribute != null) {
                            IEnumAttributeValue attributeValue = newEnumValue.getEnumAttributeValue(nameAttribute);
                            // Theoretically not possible to be null.
                            if (attributeValue != null) {
                                value = attributeValue.getValue();
                                currentEnumAttributeValue.setValue(value);
                            }
                        }
                    } else {
                        value = currentRow.getValue(i);
                        currentEnumAttributeValue.setValue(value);
                    }
                }
            }

            // Delete the old table contents.
            currentTableContents.getIpsSrcFile().getCorrespondingResource().delete(true, null);

            // Update monitor if available.
            if (monitor != null) {
                monitor.worked(1);
            }
        }
    }

}
