/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    private Map<String, Integer> sortOrderLookup = new HashMap<String, Integer>(20);

    private List<String> sortOrder = new ArrayList<String>(20);

    @Override
    public String[] getSegmentNames() {
        return sortOrder.toArray(new String[sortOrder.size()]);
    }

    @Override
    public void setSegmentNames(String[] segments) {
        sortOrderLookup.clear();
        sortOrder.clear();

        int pos = 0;

        for (String segment : segments) {

            if (checkLine(segment)) {
                sortOrderLookup.put(segment, new Integer(pos++));
                sortOrder.add(segment);
            }
        }
    }

    @Override
    public int compare(String segment1, String segment2) {

        if (sortOrderLookup.containsKey(segment1) && sortOrderLookup.containsKey(segment2)) {
            Integer pos1 = sortOrderLookup.get(segment1);
            Integer pos2 = sortOrderLookup.get(segment2);

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

    @Override
    public String toPersistenceContent() {
        String content = null;

        List<String> out = new ArrayList<String>(sortOrder);
        out.add(0, Messages.IpsPackageFragmentArbitrarySortDefinition_CommentLine);

        content = StringUtils.join(out.iterator(), StringUtil.getSystemLineSeparator());

        return content;
    }

    @Override
    public void initPersistenceContent(String content) throws CoreException {
        /*
         * do not use system line seperator here because the file could be transfered from another
         * system. This regext splits the content at \r\n (windows), \n (unix) or \r (old mac)
         */
        String[] segments = content.split("[\r\n]++"); //$NON-NLS-1$

        int pos = 0;
        sortOrderLookup.clear();

        for (String line : segments) {
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
     * @return <code>true</code> if it is a valid entry; <code>false</code> if line is empty or a
     *         comment
     */
    private boolean checkLine(String line) {
        if ((line.length() > 0) && (line.trim().length() > 0)) {
            // skip comments
            return !line.startsWith("#"); //$NON-NLS-1$
        }

        return false;
    }

    @Override
    public IIpsPackageFragmentSortDefinition copy() {

        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();

        sortDef.sortOrder = new ArrayList<String>(sortOrder);
        sortDef.sortOrderLookup = new HashMap<String, Integer>(sortOrderLookup);

        return sortDef;
    }

}
