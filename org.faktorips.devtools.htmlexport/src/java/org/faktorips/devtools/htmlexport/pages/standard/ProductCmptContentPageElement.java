package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ProductGenerationAttributeTable;

/**
 * A page representing a {@link IProductCmpt}
 * 
 * @author dicker
 * 
 */
public class ProductCmptContentPageElement extends AbstractObjectContentPageElement<IProductCmpt> {

	/**
	 * creates a page for the given {@link IProductCmpt} with the given config
	 * 
	 * @param object
	 * @param config
	 */
	protected ProductCmptContentPageElement(IProductCmpt object, DocumentorConfiguration config) {
		super(object, config);
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.standard.AbstractObjectContentPageElement#addStructureData()
	 */
	@Override
	protected void addStructureData() {
		IProductCmptType productCmptType = getProductCmptType();

		addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
				new TextPageElement(IpsObjectType.PRODUCT_CMPT_TYPE.getDisplayName() + ": "), //$NON-NLS-1$
				new LinkPageElement(productCmptType, "content", productCmptType.getName(), true) })); //$NON-NLS-1$
	}

	/**
	 * returns the {@link IProductCmptType} for the productCmpt
	 * 
	 * @return
	 */
	protected IProductCmptType getProductCmptType() {
		try {
			return getConfig().getIpsProject().findProductCmptType(getDocumentedIpsObject().getProductCmptType());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.faktorips.devtools.htmlexport.pages.standard.
	 * AbstractObjectContentPageElement#build()
	 */
	@Override
	public void build() {
		super.build();

		addGenerationsList();

		addGenerationAttributeTable();
	}

	/**
	 *adds a table with the attributes of the generations 
	 */
	private void addGenerationAttributeTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement(Messages.ProductCmptContentPageElement_attributes, TextType.HEADING_2));
		
		wrapper.addPageElements(getTableOrAlternativeText(new ProductGenerationAttributeTable(getDocumentedIpsObject(),
				getProductCmptType(), getConfig()), Messages.ProductCmptContentPageElement_noGenerationsOrAttributes));
		addPageElements(wrapper);
	}

	/**
	 * adds a list of generations
	 */
	private void addGenerationsList() {
		IIpsObjectGeneration[] generations = getDocumentedIpsObject().getGenerationsOrderedByValidDate();

		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement(Messages.ProductCmptContentPageElement_generations, TextType.HEADING_2));

		if (generations.length == 0) {
			wrapper.addPageElements(new TextPageElement("No generations")); //$NON-NLS-1$
			addPageElements(wrapper);
			return;
		}

		List<String> validFroms = new ArrayList<String>();

		for (IIpsObjectGeneration ipsObjectGeneration : generations) {
			GregorianCalendar validFrom = ipsObjectGeneration.getValidFrom();
			validFroms.add(getConfig().getSimpleDateFormat().format(validFrom.getTime()));
		}

		wrapper
				.addPageElements(new ListPageElement(Arrays.asList(PageElementUtils.createTextPageElements(validFroms))));
		addPageElements(wrapper);
	}
}
