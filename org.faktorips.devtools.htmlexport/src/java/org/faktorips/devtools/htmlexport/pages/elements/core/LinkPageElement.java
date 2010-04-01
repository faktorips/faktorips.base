package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;

/**
 * {@link PageElement} representing a link
 * @author dicker
 *
 */
public class LinkPageElement extends AbstractCompositePageElement {
	
	/**
	 * the link target for e.g. the frame, where the linked file should be loaded
	 */
	private String target;
	/**
	 * the path of the link
	 */
	private String path;

	/**
	 * creates a Link to the representation of the given {@link IIpsElement} using the given {@link PageElement}s to display the Link
	 * @param to
	 * @param target
	 * @param pageElements
	 */
	public LinkPageElement(IIpsElement to, String target, PageElement... pageElements) {
		this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), target);
		addPageElements(pageElements);
	}

	/**
	 * creates a Link to the representation of the given {@link IIpsElement} using the given text to display the link. The image of the type of the given IIpsElement will be used too, if useImage is true
	 * @param to
	 * @param target
	 * @param text
	 * @param useImage
	 */
	public LinkPageElement(IIpsElement to, String target, String text, boolean useImage) {
    	this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), target);
        
    	if (!useImage) {
            addPageElements(new TextPageElement(text));
            return;
        }        

    	
    	addPageElements(new ImagePageElement(to));	
        addPageElements(new TextPageElement(" " + text));
    }

	/**
	 * creates a Link to the representation of the given {@link IIpsElement} using the element's name to display the Link
	 * @param to
	 */
	public LinkPageElement(IIpsElement to) {
		this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), null, new TextPageElement(to.getName()));
	}

	/**
	 * @param to
	 * @param pageElements
	 */
	public LinkPageElement(IIpsElement to, PageElement... pageElements) {
		this(PathUtilFactory.createPathUtil(to).getPathFromRoot(LinkedFileType.getLinkedFileTypeByIpsElement(to)), null, pageElements);
	}

	/**
	 * @param path
	 * @param target
	 * @param pageElements
	 */
	public LinkPageElement(String path, String target, PageElement... pageElements) {
		this(path, target);
		addPageElements(pageElements);
	}

	/**
	 * @param path
	 * @param target
	 * @param text
	 */
	public LinkPageElement(String path, String target, String text) {
		this(path, target, new TextPageElement(text));
	}

	/**
	 * @param path
	 * @param target
	 */
	private LinkPageElement(String path, String target) {
		this.path = path;
		this.target = target;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @return the pathFromRoot
	 */
	public String getPathFromRoot() {
		return path;

	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#acceptLayouter(org.faktorips.devtools.htmlexport.generators.ILayouter)
	 */
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutLinkPageElement(this);
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#build()
	 */
	@Override
	public void build() {
	}

	/**
	 * sets the target
	 * @param target
	 */
	public void setTarget(String target) {
		this.target = target;
	}
}
