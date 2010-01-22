package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ProductGenerationAttributeTable;

public class ProductCmptContentPageElement extends AbstractObjectContentPageElement<IProductCmpt> {

	protected ProductCmptContentPageElement(IProductCmpt object, DocumentorConfiguration config) {
		super(object, config);
	}

	@Override
	protected void addStructureData() {
		IProductCmptType productCmptType = getProductCmptType();

		addPageElements(new WrapperPageElement(PageElementWrapperType.BLOCK,
				new PageElement[] {
						new TextPageElement("Vorlage: "),
						new LinkPageElement(object, productCmptType, "content", productCmptType.getName(), true) }));
	}

	protected IProductCmptType getProductCmptType() {
		try {
			return object.getIpsProject().findProductCmptType(object.getProductCmptType());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void build() {
		super.build();

		// Liste mit Generationen
		addPageElements(createGenerationsList());

		// Tabelle mit Generationen / Attributen
		addPageElements(createGenerationAttributeTable());
	}

	private PageElement createGenerationAttributeTable() {
		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Attribute", TextType.HEADING_2));

		if (object.getNumOfGenerations() == 0) {
			wrapper.addPageElements(new TextPageElement("keine Anpassungsstufen"));
			return wrapper;
		}

		IAttribute[] attributes;
		try {
			attributes = getProductCmptType().findAllAttributes(object.getIpsProject());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}


		if (attributes.length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Attribute"));
			return wrapper;
		}

		wrapper.addPageElements(new ProductGenerationAttributeTable(object, attributes, config));

		return wrapper;
	}

	protected PageElement createGenerationsList() {
		IIpsObjectGeneration[] generations = object.getGenerationsOrderedByValidDate();

		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Anpassungsstufen", TextType.HEADING_2));

		if (generations.length == 0) {
			wrapper.addPageElements(new TextPageElement("keine Anpassungsstufen"));
			return wrapper;
		}

		List<String> validFroms = new ArrayList<String>();

		for (IIpsObjectGeneration ipsObjectGeneration : generations) {
			GregorianCalendar validFrom = ipsObjectGeneration.getValidFrom();
			validFroms.add(config.getSimpleDateFormat().format(validFrom.getTime()));
		}

		wrapper
				.addPageElements(new ListPageElement(Arrays.asList(PageElementUtils.createTextPageElements(validFroms))));
		return wrapper;
	}
}
