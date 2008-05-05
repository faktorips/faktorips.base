/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.internal.java5;

import java.util.Map;

import org.faktorips.valueset.java5.IntegerRange;
import org.w3c.dom.Element;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ProductComponentGeneration {

    
    /**
     * This method for implementations of the <code>doInitReferencesFromXml</code> method to 
     * read the cardinality bounds from an xml dom element. An IntegerRange object is created and
     * added to the provided cardinalityMap. 
     */
    public static void addToCardinalityMap(Map<String, IntegerRange> cardinalityMap, String targetId,  Element relationElement){
        String maxStr = relationElement.getAttribute("maxCardinality");
        Integer maxCardinality = null;
        if("*".equals(maxStr) || "n".equals(maxStr.toLowerCase())){
            maxCardinality = new Integer(Integer.MAX_VALUE);
        }
        else{
            maxCardinality = Integer.valueOf(maxStr); 
        }
        
        Integer minCardinality = Integer.valueOf(relationElement.getAttribute("minCardinality"));
        cardinalityMap.put(targetId, new IntegerRange(minCardinality, maxCardinality));
    }
}
