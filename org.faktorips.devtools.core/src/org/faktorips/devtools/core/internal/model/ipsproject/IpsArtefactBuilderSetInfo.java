/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
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

import org.faktorips.devtools.core.internal.model.IpsModel;


/**
 * A class that hold information about IIpsArtefactBuilderSets that are registered with the corresponding extension point.
 * 
 * @see IpsModel#getIpsArtefactBuilderSetInfos()
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetInfo {

    private Class builderSetClass;
    private String builderSetId;
    private String builderSetLabel;
    
    /**
     * @param builderSetClass the class of the IIpsArtefactBuilderSet implementation
     * @param builderSetId the id by which it is registered with the system
     * @param builderSetLabel the label that is used for display purposes
     */
    public IpsArtefactBuilderSetInfo(Class builderSetClass, String builderSetId, String builderSetLabel) {
        super();
        this.builderSetClass = builderSetClass;
        this.builderSetId = builderSetId;
        this.builderSetLabel = builderSetLabel;
    }

    /**
     * Returns the class of the IIpsArtefactBuilderSet implementation class.
     */
    public Class getBuilderSetClass() {
        return builderSetClass;
    }

    /**
     * Returns the id by which the <code>IIpsArtefactBuilderSet</code> is registered with the system.
     */
    public String getBuilderSetId() {
        return builderSetId;
    }

    /**
     * Returns the label for the corresponding <code>IIpsArtefactBuilderSet</code>.
     */
    public String getBuilderSetLabel() {
        return builderSetLabel;
    }
}
