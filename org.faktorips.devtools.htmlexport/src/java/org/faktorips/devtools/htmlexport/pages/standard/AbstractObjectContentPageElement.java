package org.faktorips.devtools.htmlexport.pages.standard;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public abstract class AbstractObjectContentPageElement<T extends IIpsObject> extends AbstractRootPageElement {

	protected T object;
	protected DocumentorConfiguration config;

	public static AbstractRootPageElement getInstance(IIpsObject object, DocumentorConfiguration config) {
		if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
			return new PolicyCmptTypeContentPageElement((IPolicyCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
			return new ProductCmptTypeContentPageElement((IProductCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT)
			return new ProductCmptContentPageElement((IProductCmpt) object, config);
		if (object.getIpsObjectType() == IpsObjectType.ENUM_TYPE)
			return new EnumTypeContentPageElement((IEnumType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.ENUM_CONTENT)
			return new EnumContentContentPageElement((IEnumContent) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TABLE_STRUCTURE)
			return new TableStructureContentPageElement((ITableStructure) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TABLE_CONTENTS)
			return new TableContentsContentPageElement((ITableContents) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TEST_CASE_TYPE)
			return new TestCaseTypeContentPageElement((ITestCaseType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.TEST_CASE)
			return new TestCaseContentPageElement((ITestCase) object, config);
		throw new NotImplementedException("ToDo: " + object.getIpsObjectType().getDisplayName() + " "
				+ object.getIpsObjectType());
	}

	protected AbstractObjectContentPageElement(T object, DocumentorConfiguration config) {
		this.object = object;
		this.config = config;
		setTitle(object.getName());
	}

	@Override
	public void build() {
		super.build();

		addPageElements(new WrapperPageElement(WrapperType.BLOCK, new LinkPageElement("index", "_top",
				"Overview Project " + config.getIpsProject().getName())));

		addPageElements(new LinkPageElement(object.getIpsPackageFragment(), "classes", Util.getIpsPackageName(object
				.getIpsPackageFragment()), true));
		addPageElements(new TextPageElement(object.getIpsObjectType().getDisplayName() + " " + object.getName(),
				TextType.HEADING_1));

		// Typhierarchie
		addTypeHierarchie();

		// Strukturdaten
		addPageElements(new TextPageElement(object.getName(), TextType.HEADING_2));
		addStructureData();

		if (!object.getIpsProject().equals(config.getIpsProject())) {
			addPageElements(TextPageElement.createParagraph("Projekt: " + object.getIpsProject().getName()));
		}
		addPageElements(TextPageElement.createParagraph("Projektverzeichnis: " + object.getIpsSrcFile().getIpsPackageFragment()));

		// Beschreibung
		addPageElements(new TextPageElement("Beschreibung", TextType.HEADING_2));
		addPageElements(new TextPageElement(
				StringUtils.isBlank(object.getDescription()) ? "keine Beschreibung vorhanden" : object.getDescription(),
				TextType.BLOCK));

		// Validations
		addValidationErrors();
	}

	private void addValidationErrors() {
		try {

			MessageList messageList = object.validate(object.getIpsProject());
			if (messageList.isEmpty())
				return;

			WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
			wrapper.addPageElements(new TextPageElement("Validation Errors", TextType.HEADING_2));

			TablePageElement tablePageElement = new MessageListTablePageElement(messageList);

			wrapper.addPageElements(tablePageElement);

			addPageElements(wrapper);

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/*
	 * zum Ueberschreiben fuer Subklassen
	 */
	protected void addStructureData() {
	}

	/*
	 * Einhaengen von objektspezifischen Super- und Subklassen
	 */
	protected void addTypeHierarchie() {
	}

	@Override
	public String getPathToRoot() {
		return PathUtilFactory.createPathUtil(object).getPathToRoot();
	}

	protected PageElement getTableOrAlternativeText(AbstractSpecificTablePageElement tablePageElement,
			String alternativeText) {
		if (tablePageElement.isEmpty()) {
			return new TextPageElement(alternativeText);
		}
		return tablePageElement;
	}
}
