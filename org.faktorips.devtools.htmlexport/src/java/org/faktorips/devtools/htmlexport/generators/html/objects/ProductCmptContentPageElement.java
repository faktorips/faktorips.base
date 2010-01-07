package org.faktorips.devtools.htmlexport.generators.html.objects;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;

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
		return (IProductCmptType) object;
	}

	@Override
	protected MethodsTablePageElement getMethodsTablePageElement() {
		return new MethodsTablePageElement(object) {

			@Override
			protected List<String> getHeadline() {
				
				List<String> headline = super.getHeadline();
				headline.add(IProductCmptTypeMethod.PROPERTY_FORMULA_NAME);

				return headline;
			}

			@Override
			protected List<String> getMethodData(IMethod method) {
				List<String> methodData = super.getMethodData(method);

				IProductCmptTypeMethod productMethod = (IProductCmptTypeMethod) method;
				methodData.add(productMethod.getFormulaName());

				return methodData;
			}

		};
	}

}
