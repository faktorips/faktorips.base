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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.ui.editors.IActiveGenerationChangedListener;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;

/**
 * A page for presenting the properties of a {@link IProductCmptType}. This page is
 * connected to a {@link ProductCmptEditor} similiar to the outline view.
 *
 * @author Markus Blum
 */

public class ProductCmptModelDescriptionPage extends DefaultModelDescriptionPage implements IActiveGenerationChangedListener, ContentsChangeListener {

    private ProductCmptEditor editor;
    private PropertyValueComparator valueComparator;

    public ProductCmptModelDescriptionPage(ProductCmptEditor editor) throws CoreException {
    	super();
        this.editor = editor;
        valueComparator = new PropertyValueComparator(editor.getProductCmpt().getProductCmptType(), editor.getIpsProject());
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
        IProductCmptGeneration prodCmptGen = (IProductCmptGeneration)generation;
        super.setTitle(prodCmptGen.getProductCmpt().getName());

        List<DescriptionItem> items = new ArrayList<DescriptionItem>();
        for (int i=0; i<ProdDefPropertyType.ALL_TYPES.length; i++) {
            createPropertyDescription(items, prodCmptGen, ProdDefPropertyType.ALL_TYPES[i]);
        }
        
        DescriptionItem[] itemDescs = items.toArray(new DescriptionItem[items.size()]);
        super.setDescriptionItems(itemDescs);
    }

    /**
     * Add description of used tables.
     *
     * @param descriptionsList List with the collected descriptions.
     * @param productCmptGen Get valid tables from {@link IProductCmptGeneration}.
     * 
     * @throws CoreException
     */
    private void createPropertyDescription(List<DescriptionItem> descriptions, IProductCmptGeneration productCmptGen, ProdDefPropertyType propertyType) throws CoreException {
        IPropertyValue[] values  = productCmptGen.getPropertyValues(propertyType);
        Arrays.sort(values, valueComparator);
        for (int i = 0; i < values.length; i++) {
            IProdDefProperty property = values[i].findProperty(productCmptGen.getIpsProject());
            if (property != null ) {
                DescriptionItem item = new DescriptionItem(values[i].getPropertyName() , property.getDescription());
                descriptions.add(item);
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

