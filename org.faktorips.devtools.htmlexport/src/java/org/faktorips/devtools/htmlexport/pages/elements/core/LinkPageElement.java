package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;

public class LinkPageElement extends AbstractCompositePageElement {
	private String target;
	private String path;

	public LinkPageElement(IIpsElement to, String target, PageElement... pageElements) {
		this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), target);
		addPageElements(pageElements);
	}

	public LinkPageElement(IIpsElement to, String target, String text, boolean useImage) {
    	this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), target);
        
    	if (!useImage) {
            addPageElements(new TextPageElement(text));
            return;
        }        

    	
    	addPageElements(new ImagePageElement(to));	
        addPageElements(new TextPageElement(" " + text));
    }

	public LinkPageElement(IIpsElement to) {
		this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), null, new TextPageElement(to.getName()));
	}

	public LinkPageElement(IIpsElement to, PageElement... pageElements) {
		this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), null, pageElements);
	}

	public LinkPageElement(String path, String target, PageElement... pageElements) {
		this(path, target);
		addPageElements(pageElements);
	}

	public LinkPageElement(String path, String target, String text) {
		this(path, target, new TextPageElement(text));
	}

	private LinkPageElement(String path, String target) {
		this.path = path;
		this.target = target;
	}

	public String getTarget() {
		return target;
	}

	public String getPathFromRoot() {
		return path;

	}

	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutLinkPageElement(this);
	}

	@Override
	public void build() {
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
