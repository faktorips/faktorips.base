package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Represents a table with the values of the {@link IAttribute}s of an {@link IProductCmpt} as rows
 * and the generations of the {@link IProductCmpt} as columns
 * 
 * @author dicker
 * 
 */
public class ProductGenerationAttributeTable extends AbstractSpecificTablePageElement {

    private final IProductCmpt productCmpt;
    private final IAttribute[] attributes;
    private final DocumentorConfiguration config;

    /**
     * Creates a {@link ProductGenerationAttributeTable} for the specified
     * 
     * @param productCmpt
     * @param productCmptType
     * @param config
     */
    public ProductGenerationAttributeTable(IProductCmpt productCmpt, IProductCmptType productCmptType,
            DocumentorConfiguration config) {
        this.productCmpt = productCmpt;
        this.attributes = findAttributes(productCmptType);
        this.config = config;
    }

    /**
     * returns the attributes of the given {@link ProductCmptType}
     * 
     * @param productCmptType
     * @return
     */
    private IAttribute[] findAttributes(IProductCmptType productCmptType) {
        try {
            return productCmptType.findAllAttributes(productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * addDataRows()
     */
    @Override
    protected void addDataRows() {
        for (IAttribute attribute : attributes) {
            addAttributeRow(attribute);
        }
    }

    /**
     * adds the row of an attribute with the value of all generations
     * 
     * @param attribute
     */
    private void addAttributeRow(IAttribute attribute) {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        String name = attribute.getName();
        cells[0] = new TextPageElement(name);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IAttributeValue attributeValue = productCmpt.getProductCmptGeneration(i).getAttributeValue(name);
            String value = attributeValue == null ? Messages.ProductGenerationAttributeTable_undefined : attributeValue
                    .getValue();
            cells[i + 1] = new TextPageElement(value);
        }

        addSubElement(new TableRowPageElement(cells));

    }

    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.ProductGenerationAttributeTable_generationFrom);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            headline.add(config.getSimpleDateFormat().format(
                    productCmpt.getProductCmptGeneration(i).getValidFrom().getTime()));
        }
        return headline;
    }

    @Override
    public boolean isEmpty() {
        return ArrayUtils.isEmpty(attributes) || productCmpt.getNumOfGenerations() == 0;
    }

}
