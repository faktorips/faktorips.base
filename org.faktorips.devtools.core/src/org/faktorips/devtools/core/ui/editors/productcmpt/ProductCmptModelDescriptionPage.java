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
package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.product.ProductCmptGeneration;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.ui.editors.IActiveGenerationChangedListener;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;

/**
 * A page for presenting the attributes of a {@link IProductCmptType}. This page is
 * connected to a {@link ProductCmptEditor} similiar to the outline view.
 *
 * @author Markus Blum
 */

public class ProductCmptModelDescriptionPage extends DefaultModelDescriptionPage implements IActiveGenerationChangedListener, ContentsChangeListener {

    private ProductCmptEditor editor;

    public ProductCmptModelDescriptionPage(ProductCmptEditor editor) throws CoreException {
    	super();

        this.editor = editor;
        // register with GenerationChangedEvent
        editor.addListener(this);
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
        setDescriptionData(editor.getActiveGeneration());
    }

    /**
     * {@inheritDoc}
     */
    public void activeGenerationChanged(IIpsObjectGeneration generation) {
        try {
            setDescriptionData(generation);
        } catch (CoreException e) {
            // TODO Show or handle Excpetion.
            IpsPlugin.log(e);
        }
    }

    /**
     * Set the current data in DefaultModelDescriptionPage.
     *
     * Set the page title to {@link IProductCmpt name}.
     *
     * Create the {@link DescriptionItem}s from attributes of the {@link ProductCmptGeneration} and sort the List in this order:
     * - const. attributes
     * - tables and derived attributes
     * - attributes with range values
     *
     * @param generation the active {@link ProductCmptGeneration}
     * @throws CoreException
     */
    private void setDescriptionData(IIpsObjectGeneration generation) throws CoreException {
        List attributeList = new ArrayList();
        String productName = ((IProductCmptGeneration)generation).getProductCmpt().getName();

        createAttributeDescription(attributeList, (IProductCmptGeneration)generation, ConfigElementType.PRODUCT_ATTRIBUTE);
        createTableDescription(attributeList, (IProductCmptGeneration)generation);
        createAttributeDescription(attributeList, (IProductCmptGeneration)generation, ConfigElementType.FORMULA);
        createAttributeDescription(attributeList, (IProductCmptGeneration)generation, ConfigElementType.POLICY_ATTRIBUTE);

        DescriptionItem[] itemList = new DescriptionItem[attributeList.size()];
        itemList = (DescriptionItem[]) attributeList.toArray(itemList);

        super.setTitle(productName);
        super.setDescriptionItems(itemList);
    }

    /**
     * Add description of attributes to the {@link DescriptionItem} ordered by {@link ConfigElementType}.
     *
     * @param attributeList List with the collected attributes.
     * @param productCmptGen Get valid attributes from {@link IProductCmptGeneration}.
     * @param type Read attributes of {@link ConfigElementType} only.
     * @throws CoreException
     */
    private void createAttributeDescription(List attributeList, IProductCmptGeneration productCmptGen, ConfigElementType type) throws CoreException {

        // Get names and descriptions of attributetypes by ConfigElementType
    	IConfigElement[] elements = productCmptGen.getConfigElements(type);

        if (elements.length > 0)
        {
    		Arrays.sort(elements, new ConfigElementComparator());

    		for (int i = 0; i < elements.length; i++) {
				IAttribute attribute = elements[i].findPcTypeAttribute();

				if (attribute != null) {
					DescriptionItem item = new DescriptionItem(attribute.getName(), attribute.getDescription());
					attributeList.add(item);
				}
			}
        }
	}

    /**
     * Add description of used tables.
     *
     * @param attributeList List with the collected attributes.
     * @param productCmptGen Get valid tables from {@link IProductCmptGeneration}.
     * @throws CoreException
     */
    private void createTableDescription(List attributeList, IProductCmptGeneration productCmptGen) throws CoreException {
        ITableContentUsage[] contentUsage  = productCmptGen.getTableContentUsages();
        for (int i = 0; i < contentUsage.length; i++) {
			ITableStructureUsage structure = contentUsage[i].findTableStructureUsage(productCmptGen.getIpsProject());
			if (structure != null ) {
				DescriptionItem item = new DescriptionItem(contentUsage[i].getStructureUsage() , structure.getDescription());
				attributeList.add(item);
			}
        }
	}

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        editor.removeListener(this);
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        // TODO read new model description event != TYPE_PROPERTY_CHANGED
    }

}

