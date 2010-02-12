package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;

public class ProductCmptTypeContentPageElement extends AbstractTypeContentPageElement<ProductCmptType> {

	public class TableStructureTablePageElement extends AbstractSpecificTablePageElement {
		private IProductCmptType productCmptType;

		public TableStructureTablePageElement(IProductCmptType productCmptType) {
			super();
			this.productCmptType = productCmptType;
		}

		@Override
		protected void addDataRows() {
			ITableStructureUsage[] tableStructureUsages = productCmptType.getTableStructureUsages();
			for (ITableStructureUsage tableStructureUsage : tableStructureUsages) {
				addTableStructureUsageRow(tableStructureUsage);
			}

		}

		private void addTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
			addSubElement(new TableRowPageElement(new PageElement[] {
					new TextPageElement(tableStructureUsage.getRoleName()),
					getTableStructureLinks(tableStructureUsage),
					new TextPageElement(tableStructureUsage.isMandatoryTableContent() ? "X" : "-"),
					new TextPageElement(tableStructureUsage.getDescription()) }));
		}

		private PageElement getTableStructureLinks(ITableStructureUsage tableStructureUsage) {
			String[] tableStructures = tableStructureUsage.getTableStructures();
			if (tableStructures.length == 0)
				return new TextPageElement("keine Tabellenstrukturen");

			List<LinkPageElement> links = new ArrayList<LinkPageElement>();
			for (String tableStructure : tableStructures) {
				try {
					IIpsObject ipsObject = tableStructureUsage.getIpsProject().findIpsObject(
							IpsObjectType.TABLE_STRUCTURE, tableStructure);
					links.add(new LinkPageElement(ipsObject, "content", tableStructure, true));
				} catch (CoreException e) {
					new RuntimeException(e);
				}
			}

			if (links.size() == 1)
				return links.get(0);

			return new ListPageElement(links);
		}

		@Override
		protected List<String> getHeadline() {
			List<String> headline = new ArrayList<String>();

			headline.add(ITableStructureUsage.PROPERTY_ROLENAME);
			headline.add(ITableStructureUsage.PROPERTY_TABLESTRUCTURE);

			addHeadlineAndColumnLayout(headline, ITableStructureUsage.PROPERTY_MANDATORY_TABLE_CONTENT, Style.CENTER);

			headline.add(ITableStructureUsage.PROPERTY_DESCRIPTION);

			return headline;
		}

		public boolean isEmpty() {
			return ArrayUtils.isEmpty(productCmptType.getTableStructureUsages());
		}

	}

	protected ProductCmptTypeContentPageElement(IProductCmptType productCmptType, DocumentorConfiguration config) {
		super(productCmptType, config);
	}

	@Override
	public void build() {
		super.build();

		// Tabellenstrukturen
		addPageElements(createTableStructureTable());

		// Produktbausteine Tabelle
		addPageElements(createProductCmptList());
	}

	private PageElement createTableStructureTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Tabellenstrukturen", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new TableStructureTablePageElement(getProductCmptType()),
				"keine Tabellenstrukturen vorhanden"));

		return wrapper;
	}

	private PageElement createProductCmptList() {
		IIpsSrcFile[] allProductCmptSrcFiles;
		List<IProductCmpt> productCmpts;
		try {
			allProductCmptSrcFiles = getProductCmptType().findAllMetaObjectSrcFiles(object.getIpsProject(), true);
			productCmpts = Util.getIpsObjects(allProductCmptSrcFiles);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Produktbausteine", TextType.HEADING_2));

		if (productCmpts.size() == 0) {
			wrapper.addPageElements(new TextPageElement("keine Produktbausteine"));
			return wrapper;
		}

		List<LinkPageElement> createLinkPageElements = PageElementUtils.createLinkPageElements(object, productCmpts,
				"content", new LinkedHashSet<Style>());
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
			addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
					new TextPageElement("Vertragsklasse: "), new LinkPageElement(to, "content", to.getName(), true) }));
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
