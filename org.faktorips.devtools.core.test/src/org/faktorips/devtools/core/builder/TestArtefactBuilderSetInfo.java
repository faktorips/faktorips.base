/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A test implementation of the {@link IIpsArtefactBuilderSetInfo} interface. Returns the {@link IIpsArtefactBuilderSet} instances provided to the
 * constructor in the create(IIpsProject) according to the {@link IIpsProject} provided to the method. Therefor it is necessary to set the 
 * ips-project to the builder set otherwise a NullPointerException is thrown when the create(IIpsProject) method is called.
 * 
 * @author Peter Erzberger
 */
public class TestArtefactBuilderSetInfo implements IIpsArtefactBuilderSetInfo {

    private IIpsArtefactBuilderSet[] builderSets;
    private String builderSetId;
    
    /**
     * Creates an info object for the provided builder set. The IIpsProject for this builderSet must be set to avoid runtime exceptions.
     */
    public TestArtefactBuilderSetInfo(IIpsArtefactBuilderSet builderSet) {
        ArgumentCheck.notNull(builderSet, this);
        ArgumentCheck.notNull(builderSet.getIpsProject(), "The ips project of the provided builder set must be set.");
        this.builderSets = new IIpsArtefactBuilderSet[]{builderSet};
        this.builderSetId = builderSet.getId();
    }

    /**
     * Creates an info object witht the provided builder set and their common builderSetId. It is assumed that all builder sets have the same
     * builderSetId.
     */
    public TestArtefactBuilderSetInfo(String builderSetId, IIpsArtefactBuilderSet[] builderSets) {
        ArgumentCheck.notNull(builderSets, this);
        ArgumentCheck.notNull(builderSetId, this);
        for (int i = 0; i < builderSets.length; i++) {
            ArgumentCheck.notNull(builderSets[i].getIpsProject(),
                    "The ips project of the provided builder set with the id: " + builderSets[i].getId()
                            + " must be set.");
        }
        this.builderSets = builderSets;
        this.builderSetId = builderSetId;
    }

    public IIpsArtefactBuilderSet create(IIpsProject ipsProject) {
        for (int i = 0; i < builderSets.length; i++) {
            if(builderSets[i].getIpsProject().equals(ipsProject)){
                return builderSets[i];
            }
        }
        return null;
    }

    public String getBuilderSetId() {
        return builderSetId;
    }

    public String getBuilderSetLabel() {
        return null;
    }

    public IIpsBuilderSetPropertyDef getPropertyDefinition(String name) {
        return null;
    }

    public IIpsBuilderSetPropertyDef[] getPropertyDefinitions() {
        return new IIpsBuilderSetPropertyDef[0];
    }

    public MessageList validateIpsArtefactBuilderSetConfig(IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        return null;
    }

    public Message validateIpsBuilderSetPropertyValue(IIpsProject ipsProject, String propertyName, String propertyValue) {
        return null;
    }

    public IIpsArtefactBuilderSetConfigModel createDefaultConfiguration(IIpsProject ipsProject) {
        return null;
    }
}
