package org.faktorips.devtools.htmlexport.generators;

import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;

public interface ILayouter extends IGenerator {
    
    /**
     * Layout fuer den Root des Dokumentbaumes
     * @param pageElement
     */
    public void layoutRootPageElement(RootPageElement pageElement);
    
    /**
     * Layout für Text ohne weitere Struktur und mit nur einer Formatierung
     * @param pageElement
     */
    public void layoutTextPageElement(TextPageElement pageElement);
    
    /**
     * Layout fuer Link
     * @param pageElement
     */
    public void layoutLinkPageElement(LinkPageElement pageElement);
    
    /**
     * Layout fuer Liste
     * @param pageElement
     */
    public void layoutListPageElement(ListPageElement pageElement);

    /**
     * Layout fuer WrapperElement
     * @param wrapperPageElement
     */
    public void layoutWrapperPageElement(WrapperPageElement wrapperPageElement);

    /**
     * loescht den bisherigen Text
     */
    public void clean();

}
