package org.faktorips.devtools.htmlexport.generators.html.objects;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;

public class ProductCmptContentPageElement extends AbstractTypeContentPageElement<ProductCmptType> {

    protected ProductCmptContentPageElement(ProductCmptType object, DocumentorConfiguration config) {
        super(object, config);
    }

    @Override
	protected void addStructureData() {
		super.addStructureData();
		
		try {
			IPolicyCmptType to = object.getIpsProject().findPolicyCmptType(getProductCmptType().getPolicyCmptType());
			if (to == null) {
				addPageElements(TextPageElement.newBlock("Vertragsklasse: keine"));
				return;
			}
			addPageElements(new WrapperPageElement(LayouterWrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Vertragsklasse: "),
					new LinkPageElement(object, to, "content", new TextPageElement(to.getName())) }));
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private IProductCmptType getProductCmptType() {
		return (IProductCmptType)object;
	}
}
