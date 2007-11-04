/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.util.StringUtil;

/**
 * Implementation of {@link IIpsPackageFragmentArbitrarySortDefinition}.
 *
 * @author Markus Blum
 */
public class IpsPackageFragmentArbitrarySortDefinition implements IIpsPackageFragmentArbitrarySortDefinition {

    private Map sortOrderLookup = new HashMap(20);

    private List sortOrder = new ArrayList(20);

    /**
     * {@inheritDoc}
     */
    public String[] getSegmentNames() {
        return (String[])sortOrder.toArray(new String[sortOrder.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void setSegmentNames(String[] segments) {

        sortOrderLookup.clear();
        sortOrder.clear();

        int pos = 0;

        for (int i = 0; i < segments.length; i++) {

            if (checkLine(segments[i])) {
                sortOrderLookup.put(segments[i], new Integer(pos++));
                sortOrder.add(segments[i]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String segment1, String segment2) {

        if (sortOrderLookup.containsKey(segment1) && sortOrderLookup.containsKey(segment2)) {
            Integer pos1 = (Integer) sortOrderLookup.get(segment1);
            Integer pos2 = (Integer) sortOrderLookup.get(segment2);

            return pos1.compareTo(pos2);
        } else {
            // packages not included in the sortdefinition will be put at the end of the list.
            if (sortOrderLookup.containsKey(segment1)) {
                return -1;
            }

            if (sortOrderLookup.containsKey(segment2)) {
                return 1;
            }

        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public String toPersistenceContent() {
        String content = null;

        List out = new ArrayList(sortOrder);
        out.add(0, Messages.IpsPackageFragmentArbitrarySortDefinition_CommentLine);

        content = StringUtils.join(out.iterator(), StringUtil.getSystemLineSeparator());

        return content;
    }

    /**
     * {@inheritDoc}
     */
    public void initPersistenceContent(String content) throws CoreException {

        String[] segments = StringUtils.split(content, StringUtil.getSystemLineSeparator());

        int pos = 0;
        sortOrderLookup.clear();

        for (int i = 0; i < segments.length; i++) {
            String line = segments[i];

            // skip empty lines (incl. whitespaces) and comments
            if (checkLine(line)) {
                sortOrderLookup.put(line, new Integer(pos++));
                sortOrder.add(line);
            }
        }
    }

    /**
     * Skip empty lines and lines starting with a comment ('#').
     *
     * @param line Onje single line (String) of the sort order.
     * @return <code>true</code> if it is a valid entry; <code>false</code> if line is empty or a comment
     */
    private boolean checkLine(String line) {
        if ((line.length() > 0 ) && (line.trim().length() > 0)) {
            // skip comments
            return !line.startsWith("#");
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentSortDefinition copy() {

        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();

        sortDef.sortOrder = (List)((ArrayList)this.sortOrder).clone();
        sortDef.sortOrderLookup = (Map)((HashMap) sortOrderLookup).clone();

        return sortDef;
    }
}
