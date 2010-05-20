/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The table column naming strategy defines how a table column name is constructed from a given
 * String. Possible uses include a conversion from from a CamelCase naming to an upper cased naming
 * (e.g. ColumnName -> COLUMN_NAME).
 * 
 * @author Roman Grutza
 */
public interface ITableColumnNamingStrategy {

    /**
     * Name of XML tags representing a table column naming strategy.
     */
    public final static String XML_TAG_NAME = "TableColumnNamingStrategy"; //$NON-NLS-1$

    /**
     * Applies this naming strategy to the given name and returns the possibly altered table column
     * name.
     */
    public String getTableColumnName(String baseName);

    /**
     * Applies this naming strategy to the given name and returns the possibly altered table column
     * name.
     * 
     * @param baseName The name on which to apply the naming strategy.
     * @param maxLength A positive number which marks the maximum length of the returned String.
     */
    public String getTableColumnName(String baseName, int maxLength);

    /**
     * Sets the IPS project this strategy belongs to. Is called when the strategy is instantiated.
     * Should never be called by clients.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     */
    public void setIpsProject(IIpsProject project);

    /**
     * Returns the IPS project the strategy belongs to.
     */
    public IIpsProject getIpsProject();

    /**
     * Initializes the strategy with the data from the XML element. This method must be able to read
     * those elements created by the toXml() method. The element's node name is expected to be the
     * name defined in <code>XML_TAG_NAME</code>.
     * <p>
     * Concrete classes implementing this interface use their own tag name for an element that is
     * nested inside the given element. E.g.
     * 
     * <pre>
     *     &lt;TableColumnNamingStrategy&gt;
     *         &lt;CamelCaseToUppercaseWithUnderscoreColumnNamingStrategy/&gt;
     *     &lt;/TableColumnNamingStrategy&gt;
     * </pre>
     */
    public void initFromXml(Element el);

    /**
     * Creates an XML element representation of this strategy. The element's node name is defined in
     * <code>XML_TAG_NAME</code>.
     * 
     * @param doc The XML document to create new elements.
     */
    public Element toXml(Document doc);

    /**
     * Validates if the given table column name conforms to this strategy.
     */
    public void validate(String name);

}
