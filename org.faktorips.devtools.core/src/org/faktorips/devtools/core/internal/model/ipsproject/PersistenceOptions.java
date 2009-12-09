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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Roman Grutza
 */
public class PersistenceOptions implements IPersistenceOptions {

    private static final String MAX_TABLE_NAME_LENGTH_ATTRIBUTENAME = "maxTableNameLength";
    private static final String MAX_COLUMN_NAME_LENGTH_ATTRIBUTENAME = "maxColumnNameLength";

    private final static int DEFAULT_COLUMN_NAME_LENGTH = 255;
    private final static int DEFAULT_TABLE_NAME_LENGTH = 255;

    private ITableColumnNamingStrategy tableColumnNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
    private ITableNamingStrategy tableNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
    private int maxColumnNameLength;
    private int maxTableNameLength;

    public PersistenceOptions() {
        this(null);
    }

    /**
     * Initializes this strategy using the XML-Element named PersistenceOptions.
     * <p>
     * The concrete structure is:
     * 
     * <pre>
     *       &lt;PersistenceOptions maxColumnNameLength=&quot;255&quot; maxTableNameLength=&quot;255&quot;&gt;
     *         &lt;TableNamingStrategy id=&quot;org.faktorips.devtools.core.CamelCaseToUpperUnderscoreNamingStrategy&quot; /&gt;
     *         &lt;TableColumnNamingStrategy id=&quot;org.faktorips.devtools.core.CamelCaseToUpperUnderscoreNamingStrategy&quot; /&gt;
     *       &lt;/PersistenceOptions&gt;
     * </pre>
     */
    public PersistenceOptions(Element element) {
        if (element == null || !element.getTagName().equals("PersistenceOptions")) {
            setMaxColumnNameLength(DEFAULT_COLUMN_NAME_LENGTH);
            setMaxTableNameLength(DEFAULT_TABLE_NAME_LENGTH);
            tableColumnNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
            tableNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
            return;
        }
        maxColumnNameLength = Integer.valueOf(element.getAttribute(MAX_COLUMN_NAME_LENGTH_ATTRIBUTENAME));
        maxTableNameLength = Integer.valueOf(element.getAttribute(MAX_TABLE_NAME_LENGTH_ATTRIBUTENAME));

        tableNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
        NodeList elementsByTagName = element.getElementsByTagName(ITableNamingStrategy.XML_TAG_NAME);
        if (elementsByTagName.getLength() > 0) {
            String id = ((Element)elementsByTagName.item(elementsByTagName.getLength())).getAttribute("id");
            if (id.equals(CamelCaseToUpperUnderscoreNamingStrategy.EXTENSION_ID)) {
                tableNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
            }
        }

        tableColumnNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
        elementsByTagName = element.getElementsByTagName(ITableColumnNamingStrategy.XML_TAG_NAME);
        if (element != null) {
            String id = element.getAttribute("id");
            if (id.equals(CamelCaseToUpperUnderscoreNamingStrategy.EXTENSION_ID)) {
                tableColumnNamingStrategy = new CamelCaseToUpperUnderscoreNamingStrategy();
            }
        }
    }

    public int getMaxColumnNameLenght() {
        return maxColumnNameLength;
    }

    public int getMaxTableNameLength() {
        return maxTableNameLength;
    }

    public ITableColumnNamingStrategy getTableColumnNamingStrategy() {
        return tableColumnNamingStrategy;
    }

    public ITableNamingStrategy getTableNamingStrategy() {
        return tableNamingStrategy;
    }

    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy) {
        ArgumentCheck.notNull(newStrategy);
        tableColumnNamingStrategy = newStrategy;
    }

    public void setTableNamingStrategy(ITableNamingStrategy newStrategy) {
        ArgumentCheck.notNull(newStrategy);
        tableNamingStrategy = newStrategy;
    }

    public void setMaxColumnNameLength(int length) {
        maxColumnNameLength = length;
    }

    public void setMaxTableNameLength(int length) {
        maxTableNameLength = length;
    }

}
