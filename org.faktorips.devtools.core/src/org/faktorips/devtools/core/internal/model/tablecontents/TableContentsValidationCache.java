/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;

/**
 * This class caches for all {@link ITableStructure} the corresponding {@link ITableContents}. It
 * could be used validate how much table contents are instantiated for a table structure. This
 * information is necessary for example to validate that there exists only one content for
 * single-table structures.
 * 
 */
public class TableContentsValidationCache {

    private final IIpsModel ipsModel;

    /**
     * The cached tabled structures to available table contents. This field is marked as volatile
     * because it is initialized by lazy loading and is used to check the initialized state of the
     * map using the double null check, @see {@link #checkInit()}.
     */
    private volatile TableStructureMap tableStructureMap;

    /**
     * The constructor getting the {@link IIpsModel} which is used to get the table contents and
     * structures.
     * 
     * @param ipsModel The {@link IIpsModel} which holds all the {@link IIpsSrcFile source files
     *            that should be cached}
     */
    public TableContentsValidationCache(IIpsModel ipsModel) {
        ArgumentCheck.notNull(ipsModel);
        this.ipsModel = ipsModel;
    }

    /**
     * Returns the list of table contents that are instances of the given table structure. If there
     * is no table content for the given table structure, an empty list is returned.
     * <p>
     * When first calling this method all table contents will be checked and initialized. This may
     * take some time. When the cache is initialized it is updated using
     * {@link IResourceChangeEvent}.
     * 
     * @param tableStructures The {@link IIpsSrcFile} of the table structure you want get the
     *            contents for.
     * @return The list of found table contents for the given table structure.
     */
    public List<IIpsSrcFile> getTableContents(IIpsSrcFile tableStructures) {
        checkInit();
        return tableStructureMap.get(tableStructures);
    }

    private void checkInit() {
        if (tableStructureMap == null) {
            synchronized (this) {
                if (tableStructureMap == null) {
                    init();
                }
            }
        }
    }

    private void init() {
        tableStructureMap = new TableStructureMap();
        IIpsProject[] ipsProjects;
        ipsProjects = getIpsProjects();
        for (IIpsProject ipsProject : ipsProjects) {
            List<IIpsSrcFile> ipsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
            populateMaps(ipsSrcFiles);
        }
    }

    private IIpsProject[] getIpsProjects() {
        try {
            return ipsModel.getIpsProjects();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void populateMaps(List<IIpsSrcFile> ipsSrcFiles) {
        populateTableContents(ipsSrcFiles);
    }

    private void populateTableContents(List<IIpsSrcFile> ipsSrcFiles) {
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            if (IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType())) {
                IIpsSrcFile tableStructure = getReferencedTableStructure(ipsSrcFile);
                if (tableStructure != null) {
                    tableStructureMap.put(tableStructure, ipsSrcFile);
                }
            }
        }
    }

    private IIpsSrcFile getReferencedTableStructure(IIpsSrcFile ipsSrcFile) {
        try {
            String structureName = ipsSrcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
            return ipsSrcFile.getIpsProject().findIpsSrcFile(
                    new QualifiedNameType(structureName, IpsObjectType.TABLE_STRUCTURE));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private static class TableStructureMap {

        private ConcurrentHashMap<IIpsSrcFile, List<IIpsSrcFile>> tableStructureToContents = new ConcurrentHashMap<IIpsSrcFile, List<IIpsSrcFile>>();

        public List<IIpsSrcFile> get(IIpsSrcFile tableStructure) {
            List<IIpsSrcFile> list = tableStructureToContents.get(tableStructure);
            if (list == null) {
                return Collections.emptyList();
            } else {
                return list;
            }
        }

        public synchronized void put(IIpsSrcFile tableStructure, IIpsSrcFile tableContent) {
            List<IIpsSrcFile> list = tableStructureToContents.get(tableStructure);
            if (list == null) {
                list = new ArrayList<IIpsSrcFile>();
                put(tableStructure, list);
            }
            list.add(tableContent);
        }

        public void put(IIpsSrcFile tableStructure, List<IIpsSrcFile> tableContents) {
            tableStructureToContents.put(tableStructure, tableContents);
        }

    }

}
