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
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;

/**
 * A page for presenting the attributes of a {@link IProductCmptType}. This page is
 * connected to a {@link ProductCmptEditor} similiar to the outline view.
 * 
 * The attributes and their description are presented within a ExpandableComposite.
 * 
 * @author Markus Blum
 *
 */

public class ProductCmptModelDescriptionPage extends DefaultModelDescriptionPage {
    
    public ProductCmptModelDescriptionPage(IProductCmptGeneration productCmptGen) throws CoreException {
    	super();
        
        String productName = productCmptGen.getProductCmpt().getName();
        List attributeList = new ArrayList();
        
        // TODO Comment
    	createAttributeDescription(attributeList, productCmptGen, ConfigElementType.PRODUCT_ATTRIBUTE);
        createTableDescription(attributeList, productCmptGen);
    	createAttributeDescription(attributeList, productCmptGen, ConfigElementType.FORMULA);
    	createAttributeDescription(attributeList, productCmptGen, ConfigElementType.POLICY_ATTRIBUTE);
        
        DescriptionItem[] itemList = new DescriptionItem[attributeList.size()];
        itemList = (DescriptionItem[]) attributeList.toArray(itemList);
        
        setInput(productName, itemList);
    }
    
    /**
     * @param attributeList
     * @param productCmptGen
     * @param type
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
     * @param attributeList
     * @param productCmptGen
     * @throws CoreException
     */
    private void createTableDescription(List attributeList, IProductCmptGeneration productCmptGen) throws CoreException {
		
        // create table descriptions
        ITableContentUsage[] contentUsage  = productCmptGen.getTableContentUsages();
        
        for (int i = 0; i < contentUsage.length; i++) {
			ITableStructureUsage structure = contentUsage[i].findTableStructureUsage();
			
			if (structure != null )
			{
				DescriptionItem item = new DescriptionItem(contentUsage[i].getStructureUsage() , structure.getDescription());
				attributeList.add(item);
			}
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
    	super.dispose();
    }

}

