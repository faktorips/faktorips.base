package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Class to allow the content provider to evaluate the structure of the data to display once and
 * cache this information using this class.
 * 
 * @author Thorsten Guenther
 */
public class Node {
	private Node[] children;
	private Node parent;
	private IIpsElement wrapped;
	
	public Node(IIpsElement wrapped, Node parent) {
		this.parent = parent;
		this.wrapped = wrapped;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public Node[] getChildren() {
		return children;
	}
	
	public void setChildren(Node[] children) {
		this.children = children;
	}
	
	public IIpsElement getWrappedElement() {
		return wrapped;
	}
	
	public Image getImage() {
		return wrapped.getImage();
	}
	
	public String getText() {
		return wrapped.getName();
	}
}
