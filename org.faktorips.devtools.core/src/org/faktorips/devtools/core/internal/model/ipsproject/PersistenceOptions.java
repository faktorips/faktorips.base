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

import org.faktorips.devtools.core.internal.model.pctype.CamelCaseToUpperUnderscoreColumnNamingStrategy;
import org.faktorips.devtools.core.internal.model.pctype.CamelCaseToUpperUnderscoreTableNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Default implementation of {@link IPersistenceOptions}.
 * 
 * @author Roman Grutza
 */
public class PersistenceOptions implements IPersistenceOptions {

    private ITableColumnNamingStrategy tableColumnNamingStrategy = new CamelCaseToUpperUnderscoreColumnNamingStrategy();
    private ITableNamingStrategy tableNamingStrategy = new CamelCaseToUpperUnderscoreTableNamingStrategy();

    private int maxColumnNameLength = 255;
    private int maxTableNameLength = 255;
    private boolean allowLazyFetchForSingleValuedAssociations = false;

    public PersistenceOptions() {
        this(null);
    }

    /**
     * Initializes this strategy using the XML-Element named PersistenceOptions.
     * <p>
     * The concrete structure is:
     * 
     * <pre>
     *       &lt;PersistenceOptions maxColumnNameLength=&quot;255&quot; maxTableNameLength=&quot;255&quot; allowLazyFetchForSingleValuedAssociations=&quot;false&quot;&gt;
     *         &lt;TableNamingStrategy id=&quot;org.faktorips.devtools.core.CamelCaseToUpperUnderscoreTableNamingStrategy&quot; /&gt;
     *         &lt;TableColumnNamingStrategy id=&quot;org.faktorips.devtools.core.CamelCaseToUpperUnderscoreColumnNamingStrategy&quot; /&gt;
     *       &lt;/PersistenceOptions&gt;
     * </pre>
     */
    public PersistenceOptions(Element element) {
        if (element == null || !element.getTagName().equals("PersistenceOptions")) { //$NON-NLS-1$
            return;
        }
        maxColumnNameLength = Integer.valueOf(element.getAttribute(MAX_COLUMN_NAME_LENGTH_ATTRIBUTENAME));
        maxTableNameLength = Integer.valueOf(element.getAttribute(MAX_TABLE_NAME_LENGTH_ATTRIBUTENAME));
        allowLazyFetchForSingleValuedAssociations = Boolean.valueOf(element
                .getAttribute(ALLOW_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS));

        NodeList elementsByTagName = element.getElementsByTagName(ITableNamingStrategy.XML_TAG_NAME);
        if (elementsByTagName.getLength() > 0) {
            String id = ((Element)elementsByTagName.item(0)).getAttribute("id"); //$NON-NLS-1$
            if (id.equals(CamelCaseToUpperUnderscoreTableNamingStrategy.EXTENSION_ID)) {
                tableNamingStrategy = new CamelCaseToUpperUnderscoreTableNamingStrategy();
            }
        }

        elementsByTagName = element.getElementsByTagName(ITableColumnNamingStrategy.XML_TAG_NAME);
        if (elementsByTagName.getLength() > 0) {
            String id = ((Element)elementsByTagName.item(0)).getAttribute("id"); //$NON-NLS-1$
            if (id.equals(CamelCaseToUpperUnderscoreColumnNamingStrategy.EXTENSION_ID)) {
                tableColumnNamingStrategy = new CamelCaseToUpperUnderscoreColumnNamingStrategy();
            }
        }
    }

    @Override
    public int getMaxColumnNameLenght() {
        return maxColumnNameLength;
    }

    @Override
    public int getMaxTableNameLength() {
        return maxTableNameLength;
    }

    @Override
    public ITableColumnNamingStrategy getTableColumnNamingStrategy() {
        return tableColumnNamingStrategy;
    }

    @Override
    public ITableNamingStrategy getTableNamingStrategy() {
        return tableNamingStrategy;
    }

    @Override
    public void setTableColumnNamingStrategy(ITableColumnNamingStrategy newStrategy) {
        ArgumentCheck.notNull(newStrategy);
        tableColumnNamingStrategy = newStrategy;
    }

    @Override
    public void setTableNamingStrategy(ITableNamingStrategy newStrategy) {
        ArgumentCheck.notNull(newStrategy);
        tableNamingStrategy = newStrategy;
    }

    @Override
    public void setMaxColumnNameLength(int length) {
        ArgumentCheck.isTrue(length > 0);
        maxColumnNameLength = length;
    }

    @Override
    public void setMaxTableNameLength(int length) {
        ArgumentCheck.isTrue(length > 0);
        maxTableNameLength = length;
    }

    @Override
    public boolean isAllowLazyFetchForSingleValuedAssociations() {
        return allowLazyFetchForSingleValuedAssociations;
    }

    @Override
    public void setAllowLazyFetchForSingleValuedAssociations(boolean allowLazyFetchForSingleValuedAssociations) {
        this.allowLazyFetchForSingleValuedAssociations = allowLazyFetchForSingleValuedAssociations;
    }

}
