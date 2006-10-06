/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;

/**
 * {@inheritDoc}
 * FIXME Doku
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItemCreator extends AbstractCompareItemCreator{

    public ProductCmptCompareItemCreator() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return Messages.ProductCmptCompareItemCreator_StructureViewer_title;
    }


    /**
     * Returns a tree of <code>ProductCmptCompareItem</code>s. Each
     * <code>ProductCmptCompareItem</code> represents an IpsSrcFile, a ProductCmpt, a Generation,
     * a ConfigElement or a Relation.
     * {@inheritDoc}
     */
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof IProductCmpt) {
                ProductCmptCompareItem root = new ProductCmptCompareItem(null, file);
                IProductCmpt product = (IProductCmpt)file.getIpsObject();
                ProductCmptCompareItem ipsObject = new ProductCmptCompareItem(root, product);
                // Generations of product
                IIpsObjectGeneration[] gens = product.getGenerations();
                for (int i = 0; i < gens.length; i++) {
                    ProductCmptCompareItem generation = new ProductCmptCompareItem(ipsObject, gens[i]);
                    // configElements for each generation
                    IConfigElement[] ces = ((IProductCmptGeneration)gens[i]).getConfigElements();
                    for (int j = 0; j < ces.length; j++) {
                        new ProductCmptCompareItem(generation, ces[j]);
                    }
                    // relations for each generation
                    IProductCmptRelation[] rels = ((IProductCmptGeneration)gens[i]).getRelations();
                    for (int j = 0; j < rels.length; j++) {
                        new ProductCmptCompareItem(generation, rels[j]);
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
