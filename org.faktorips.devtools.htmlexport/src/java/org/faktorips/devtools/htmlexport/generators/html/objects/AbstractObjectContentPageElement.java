package org.faktorips.devtools.htmlexport.generators.html.objects;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public abstract class AbstractObjectContentPageElement<T extends IIpsObject> extends RootPageElement {

	protected T object;

	public static RootPageElement getInstance(IIpsObject object) {
		if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
			return new PolicyCmptContentPageElement((PolicyCmptType) object);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
			return new ProductCmptContentPageElement((ProductCmptType) object);
		throw new NotImplementedException();
	}

	protected AbstractObjectContentPageElement(T object) {
		this.object = object;
		setTitle(object.getName());
	}

	@Override
	public void build() {
		super.build();
		addPageElements(new TextPageElement(Util.getIpsPackageName(object.getIpsPackageFragment())));
		addPageElements(new TextPageElement(object.getName(), TextType.HEADING_1));

		// Typhierarchie
		addTypeHierarchie();

		// Strukturdaten
		addPageElements(new TextPageElement(object.getName(), TextType.HEADING_2));
		addStructureData();
		addPageElements(new TextPageElement("Projektverzeichnis: " + object.getIpsSrcFile().getIpsPackageFragment()));
		
		// Beschreibung
		addPageElements(new TextPageElement("Beschreibung", TextType.HEADING_2));
		addPageElements(new TextPageElement(StringUtils.isBlank(object.getDescription()) ? "keine Beschreibung vorhanden" : object.getDescription(), TextType.BLOCK));

	}

	/*
	 * zum Ueberschreiben fuer Subklassen
	 */
	protected void addStructureData() {
	}

	/*
	 * zum Ueberschreiben fuer Subklassen
	 */
	protected void addTypeHierarchie() {
	}
}
