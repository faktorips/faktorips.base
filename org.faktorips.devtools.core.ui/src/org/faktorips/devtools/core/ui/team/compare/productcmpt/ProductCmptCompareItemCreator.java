/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.type.IAssociation;

/**
 * Creates a structure of {@link ProductCmptCompareItem ProductCmptCompareItems} that is used for
 * comparing {@link ProductCmpt ProductCmpts}.
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
     * {@inheritDoc}
     * <p>
     * Creates a structure/tree of {@link ProductCmptCompareItem ProductCmptCompareItems} from the
     * given {@link IIpsSrcFile} to represent an {@link IProductCmpt}. The {@link IIpsSrcFile}, the
     * {@link IProductCmpt}, its {@link IProductCmptGeneration IProductCmptGenerations} and all
     * contained {@link IConfigElement IConfigElements} and {@link IAssociation IAssociations} are
     * represented by a {@link ProductCmptCompareItem}.
     * <p>
     * The returned {@link ProductCmptCompareItem} is the root of the created structure and contains
     * the given {@link IIpsSrcFile}. It has exactly one child representing (and referencing) the
     * {@link IProductCmpt} contained in the source file. This {@link ProductCmptCompareItem} has a
     * child for each generation the product component possesses. Each generation-compare-item
     * contains multiple {@link ProductCmptCompareItem ProductCmptCompareItems} representing the
     * attributes ({@link IConfigElement}) and relations ( {@link IAssociation}) of the product
     * component (in the current generation).
     * 
     */
    @Override
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof IProductCmpt) {
                ProductCmptCompareItem root = new ProductCmptCompareItem(null, file);
                IProductCmpt productCmpt = (IProductCmpt)file.getIpsObject();
                ProductCmptCompareItem productCmptItem = new ProductCmptCompareItem(root, productCmpt);

                IIpsElement[] children = productCmpt.getChildren();
                for (IIpsElement element : children) {
                    if (element instanceof IIpsObjectGeneration) {
                        continue;
                    } else if (element instanceof IDescription) {
                        continue;
                    }
                    new ProductCmptCompareItem(productCmptItem, element);
                }

                // Generations of product
                IIpsObjectGeneration[] gens = productCmpt.getGenerationsOrderedByValidDate();
                for (IIpsObjectGeneration gen : gens) {
                    ProductCmptCompareItem generationItem = new ProductCmptCompareItem(productCmptItem, gen);
                    IIpsElement[] genChildren = gen.getChildren();
                    for (IIpsElement element : genChildren) {
                        new ProductCmptCompareItem(generationItem, element);
                    }
                }
                // create the name, root document and ranges for all nodes
                root.init();
                return root;
            }
        } catch (CoreRuntimeException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

}
