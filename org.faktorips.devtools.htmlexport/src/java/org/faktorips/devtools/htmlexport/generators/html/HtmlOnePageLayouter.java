package org.faktorips.devtools.htmlexport.generators.html;

import java.io.File;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;

public class HtmlOnePageLayouter extends HtmlLayouter {

	public HtmlOnePageLayouter(String resourcePath) {
		super(resourcePath);
	}

	@Override
	protected void initRootPage(AbstractRootPageElement pageElement) {
		setPathToRoot("");
	}
	
	@Override
	String createLinkBase(LinkPageElement pageElement) {
		return '.' + createInternalPath(pageElement.getPathFromRoot());
	}

	/**
	 * @param pageElement
	 * @return
	 */
	private String createInternalPath(String pathFromRoot) {
		return pathFromRoot.replace(File.separatorChar, '.');
	}

	
	
	@Override
	public void layoutRootPageElement(AbstractRootPageElement pageElement) {
		initRootPage(pageElement);

		append("<hr />");
		
		if (pageElement.hasId()) {
			append("<a name=\"" + createInternalPath(pageElement.getId()) + "\">");
		}
		
		visitSubElements(pageElement);
	}
}
