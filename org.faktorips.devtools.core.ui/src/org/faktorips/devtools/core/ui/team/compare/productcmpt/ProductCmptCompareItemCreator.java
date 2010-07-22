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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;

/**
 * Creates a structure of <code>ProductCmptCompareItems</code> that is used for comparing
 * <code>ProductCmpt</code>s. {@inheritDoc}
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItemCreator extends AbstractCompareItemCreator {

    public ProductCmptCompareItemCreator() {
        super();
    }

    /**
     * Returns the title for the structure-differences viewer. {@inheritDoc}
     */
    @Override
    public String getName() {
        return Messages.ProductCmptCompareItemCreator_StructureViewer_title;
    }

    /**
     * Creates a structure/tree of <code>ProductCmptCompareItem</code>s from the given
     * <code>IIpsSrcFile</code> to represent an <code>IProductCmpt</code>. The
     * <code>IIpsSrcFile</code>, the <code>IProductCmpt</code>, its
     * <code>IProductCmptGeneration</code>s and all contained <code>IConfigElement</code>s and
     * <code>IRelation</code>s are represented by a <code>ProductCmptCompareItem</code>.
     * <p>
     * The returned <code>ProductCmptCompareItem</code> is the root of the created structure and
     * contains the given <code>IIpsSrcFile</code>. It has exactly one child representing (and
     * referencing) the <code>IProductCmpt</code> contained in the srcfile. This
     * <code>ProductCmptCompareItem</code> has a child for each generation the productcomponent
     * posesses. Each generation-compareitem contains multiple <code>ProductCmptCompareItem</code>s
     * representing the attributes (<code>IConfigElement</code>) and relations (
     * <code>IRelation</code>) of the productcomponent (in the current generation).
     * 
     * {@inheritDoc}
     */
    @Override
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof IProductCmpt) {
                ProductCmptCompareItem root = new ProductCmptCompareItem(null, file);
                IProductCmpt product = (IProductCmpt)file.getIpsObject();
                ProductCmptCompareItem ipsObject = new ProductCmptCompareItem(root, product);
                // Generations of product
                IIpsObjectGeneration[] gens = product.getGenerationsOrderedByValidDate();
                for (IIpsObjectGeneration gen : gens) {
                    ProductCmptCompareItem generation = new ProductCmptCompareItem(ipsObject, gen);
                    IIpsElement[] children = gen.getChildren();
                    for (IIpsElement element : children) {
                        new ProductCmptCompareItem(generation, element);
                    }
                }
                // create the name, root document and ranges for all nodes
                root.init();
                return root;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

}
