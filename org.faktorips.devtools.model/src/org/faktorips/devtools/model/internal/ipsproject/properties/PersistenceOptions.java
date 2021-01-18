/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import org.faktorips.devtools.model.internal.pctype.CamelCaseToUpperUnderscoreColumnNamingStrategy;
import org.faktorips.devtools.model.internal.pctype.CamelCaseToUpperUnderscoreTableNamingStrategy;
import org.faktorips.devtools.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.ipsproject.ITableNamingStrategy;
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
    private boolean allowLazyFetchForSingleValuedAssociations = true;

    private int maxTableColumnScale = 255;
    private int maxTableColumnPrecision = 255;
    private int maxTableColumnSize = 255;
    // note that only the maximum could be changed in the ips-project-file, the minimum is always 1
    private int minTableColumnScale = 1;
    private int minTableColumnPrecision = 1;
    private int minTableColumnSize = 1;

    public PersistenceOptions() {
        this(null);
    }

    /**
     * Initializes this strategy using the XML-Element named PersistenceOptions.
     * <p>
     * The concrete structure is:
     * 
     * <pre>
     *       &lt;PersistenceOptions maxColumnNameLength=&quot;255&quot; maxTableNameLength=&quot;255&quot; maxTableColumnPrecision=&quot;255&quot; maxTableColumnScale=&quot;255&quot;
     *       maxTableColumnSize=&quot;255&quot; allowLazyFetchForSingleValuedAssociations=&quot;false&quot;&gt;
     *         &lt;TableNamingStrategy id=&quot;org.faktorips.devtools.model.CamelCaseToUpperUnderscoreTableNamingStrategy&quot; /&gt;
     *         &lt;TableColumnNamingStrategy id=&quot;org.faktorips.devtools.model.CamelCaseToUpperUnderscoreColumnNamingStrategy&quot; /&gt;
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
        maxTableColumnSize = getValueOrDefault(element, MAX_TABLE_COLUMN_SIZE, maxTableColumnSize);
        maxTableColumnScale = getValueOrDefault(element, MAX_TABLE_COLUMN_SCALE, maxTableColumnScale);
        maxTableColumnPrecision = getValueOrDefault(element, MAX_TABLE_COLUMN_PRECISION, maxTableColumnPrecision);

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

    private int getValueOrDefault(Element element, String attrName, int defaultValue) {
        String attributeValue = element.getAttribute(attrName);
        if (attributeValue == null || attributeValue.length() == 0) {
            return defaultValue;
        }
        return Integer.valueOf(attributeValue);
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

    @Override
    public int getMaxTableColumnScale() {
        return maxTableColumnScale;
    }

    @Override
    public int getMaxTableColumnPrecision() {
        return maxTableColumnPrecision;
    }

    @Override
    public int getMaxTableColumnSize() {
        return maxTableColumnSize;
    }

    @Override
    public int getMinTableColumnScale() {
        return minTableColumnScale;
    }

    @Override
    public int getMinTableColumnPrecision() {
        return minTableColumnPrecision;
    }

    @Override
    public int getMinTableColumnSize() {
        return minTableColumnSize;
    }

    @Override
    public void setMaxTableColumnScale(int scale) {
        this.maxTableColumnScale = scale;
    }

    @Override
    public void setMaxTableColumnPrecision(int precision) {
        this.maxTableColumnPrecision = precision;
    }

    @Override
    public void setMaxTableColumnSize(int size) {
        this.maxTableColumnSize = size;
    }
}
