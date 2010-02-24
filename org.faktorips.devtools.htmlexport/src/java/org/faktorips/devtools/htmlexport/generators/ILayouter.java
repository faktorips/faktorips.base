package org.faktorips.devtools.htmlexport.generators;

import java.util.Set;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

public interface ILayouter extends IGenerator {

	/**
	 * Layout fuer den Root des Dokumentbaumes
	 * 
	 * @param pageElement
	 */
	public void layoutRootPageElement(AbstractRootPageElement pageElement);

	/**
	 * Layout für Text ohne weitere Struktur und mit nur einer Formatierung
	 * 
	 * @param pageElement
	 */
	public void layoutTextPageElement(TextPageElement pageElement);

	/**
	 * Layout fuer Link
	 * 
	 * @param pageElement
	 */
	public void layoutLinkPageElement(LinkPageElement pageElement);

	/**
	 * Layout fuer Liste
	 * 
	 * @param pageElement
	 */
	public void layoutListPageElement(ListPageElement pageElement);

	/**
	 * Layout fuer WrapperElement
	 * 
	 * @param wrapperPageElement
	 */
	public void layoutTablePageElement(TablePageElement pageElement);

	/**
	 * Layout fuer WrapperElement
	 * 
	 * @param wrapperPageElement
	 */
	public void layoutWrapperPageElement(WrapperPageElement wrapperPageElement);

	/**
	 * loescht den bisherigen Text
	 */
	public void clean();
	
	/**
	 * Gibt die Resourcen zurueck, die in externen Dateien gespeichert werden
	 */
	public Set<LayoutResource> getLayoutResources();

	/**
	 * Layout fuer ein Image 
	 * @param imagePageElement
	 */
	public void layoutImagePageElement(ImagePageElement imagePageElement);
}
