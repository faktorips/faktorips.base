/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;

/**
 * Structure creator for creating a tree that represents the structure of a
 * <code>ITableContents</code> object.
 * 
 * @author Stefan Widmaier
 */
public class TableContentsCompareItemCreator extends AbstractCompareItemCreator {

    public TableContentsCompareItemCreator() {
        super();
    }

    /**
     * Returns the title for the structure-differences viewer. {@inheritDoc}
     */
    @Override
    public String getName() {
        return Messages.TableContentsCompareItemCreator_TableContentsStructureCompare;
    }

    /**
     * Creates a structure/tree of <code>TableContentsCompareItem</code>s from the given
     * <code>IIpsSrcFile</code> to represent an <code>ITableContents</code> object. The
     * <code>IIpsSrcFile</code>, the <code>ITableContents</code>, its generations and all contained
     * rows are each represented by a <code>TableContentsCompareItem</code>.
     * <p>
     * The returned <code>TableContentsCompareItem</code> is the root of the created structure and
     * contains the given <code>IIpsSrcFile</code>. It has exactly one child representing (and
     * referencing) the <code>ITableContents</code> contained in the srcFile. This
     * <code>TableContentsCompareItem</code> has a child for each generation the table posesses.
     * Each generation-compareitem contains multiple <code>TableContentsCompareItem</code>s
     * representing the rows (<code>IRow</code>) of the table (in the current generation).
     * 
     * {@inheritDoc}
     */
    @Override
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof ITableContents) {
                TableContentsCompareItem root = new TableContentsCompareItem(null, file);
                ITableContents table = (ITableContents)file.getIpsObject();
                TableContentsCompareItem ipsObject = new TableContentsCompareItem(root, table);
                // Generations for table
                IIpsObjectGeneration[] gens = table.getGenerationsOrderedByValidDate();
                for (IIpsObjectGeneration gen : gens) {
                    TableContentsCompareItem generation = new TableContentsCompareItem(ipsObject, gen);
                    // rows for each generation
                    IRow[] rows = ((ITableContentsGeneration)gen).getRows();
                    for (IRow row : rows) {
                        new TableContentsCompareItem(generation, row);
                    }
                }
                // initialize name, root-document and ranges for all nodes
                root.init();
                return root;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

}
