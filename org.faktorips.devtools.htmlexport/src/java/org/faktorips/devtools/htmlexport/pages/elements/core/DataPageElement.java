package org.faktorips.devtools.htmlexport.pages.elements.core;

/**
 * A {@link DataPageElement} is a {@link PageElement} representing simply structured data like lists or tables
 * @author dicker
 *
 */
public interface DataPageElement {
	/**
	 * @return true, if there is no data to show 
	 */
	public boolean isEmpty();
}
