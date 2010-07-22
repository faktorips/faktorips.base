/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
package org.faktorips.devtools.core.util;

import java.util.Comparator;
import java.util.TreeSet;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A helper class to create tree sets with special comparators.
 * 
 * @author dirmeier
 */
public class TreeSetHelper {

    /**
     * Creates and returns a tree set containing <code>IIpsSrcFile</code>s with alphabetic ordered
     * names. If two names are equal but different source files, both source files are stored in the
     * tree set.
     */
    public static TreeSet<IIpsSrcFile> newIpsSrcFileTreeSet() {
        return new TreeSet<IIpsSrcFile>(new Comparator<IIpsSrcFile>() {
            @Override
            public int compare(IIpsSrcFile o1, IIpsSrcFile o2) {
                if (o1.equals(o2)) {
                    return 0;
                }
                int result = o1.getName().compareToIgnoreCase(o2.getName());
                if (result == 0) {
                    return -1;
                } else {
                    return result;
                }
            }
        });
    }

    private TreeSetHelper() {
        // Utility class not to be instantiated.
    }

}
