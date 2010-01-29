package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;

public class ProductCmptTypeContentPageElement extends AbstractTypeContentPageElement<ProductCmptType> {

	protected ProductCmptTypeContentPageElement(IProductCmptType productCmptType, DocumentorConfiguration config) {
		super(productCmptType, config);
	}

	
	
	@Override
	public void build() {
		super.build();
		
		// Produktbausteine Tabelle
		addPageElements(createProductCmptTable());
	}

	private PageElement createProductCmptTable() {
		IIpsSrcFile[] allProductCmptSrcFiles;
		List<IProductCmpt> productCmpts;
		try {
			allProductCmptSrcFiles = object.getIpsProject().findAllProductCmptSrcFiles(getProductCmptType(), true);
			productCmpts = Util.getIpsObjects(allProductCmptSrcFiles);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		
		WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Produktbausteine", TextType.HEADING_2));

		if (productCmpts.size() == 0) {
			wrapper.addPageElements(new TextPageElement("keine Produktbausteine"));
			return wrapper;
		}

		List<LinkPageElement> createLinkPageElements = PageElementUtils.createLinkPageElements(object, productCmpts, "content", new LinkedHashSet<Style>());
		ListPageElement liste = new ListPageElement(createLinkPageElements);

		wrapper.addPageElements(liste);

		return wrapper;
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
			addPageElements(new WrapperPageElement(PageElementWrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Vertragsklasse: "),
					new LinkPageElement(to, "content", to.getName(), true) }));
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
