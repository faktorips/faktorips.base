/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.builder;

import java.util.Arrays;
import java.util.Objects;

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * A test implementation of the {@link IIpsArtefactBuilderSetInfo} interface. Returns the
 * {@link IIpsArtefactBuilderSet} instances provided to the constructor in the create(IIpsProject)
 * according to the {@link IIpsProject} provided to the method. Therefor it is necessary to set the
 * ips-project to the builder set otherwise a NullPointerException is thrown when the
 * create(IIpsProject) method is called.
 *
 * @author Peter Erzberger
 */
public class TestArtefactBuilderSetInfo implements IIpsArtefactBuilderSetInfo {

    private IIpsArtefactBuilderSet[] builderSets;
    private String builderSetId;
    private IIpsBuilderSetPropertyDef[] propertyDefs = {};

    /**
     * Creates an info object for the provided builder set. The IIpsProject for this builderSet must
     * be set to avoid runtime exceptions.
     */
    public TestArtefactBuilderSetInfo(IIpsArtefactBuilderSet builderSet) {
        ArgumentCheck.notNull(builderSet, this);
        ArgumentCheck.notNull(builderSet.getIpsProject(), "The ips project of the provided builder set must be set.");
        builderSets = new IIpsArtefactBuilderSet[] { builderSet };
        builderSetId = builderSet.getId();
    }

    /**
     * Creates an info object witht the provided builder set and their common builderSetId. It is
     * assumed that all builder sets have the same builderSetId.
     */
    public TestArtefactBuilderSetInfo(String builderSetId, IIpsArtefactBuilderSet[] builderSets) {
        ArgumentCheck.notNull(builderSets, this);
        ArgumentCheck.notNull(builderSetId, this);
        for (IIpsArtefactBuilderSet builderSet : builderSets) {
            ArgumentCheck.notNull(builderSet.getIpsProject(),
                    "The ips project of the provided builder set with the id: " + builderSet.getId() + " must be set.");
        }
        this.builderSets = builderSets;
        this.builderSetId = builderSetId;
    }

    @Override
    public IIpsArtefactBuilderSet create(IIpsProject ipsProject) {
        for (IIpsArtefactBuilderSet builderSet : builderSets) {
            if (builderSet.getIpsProject().equals(ipsProject)) {
                return builderSet;
            }
        }
        return null;
    }

    @Override
    public String getBuilderSetId() {
        return builderSetId;
    }

    @Override
    public String getBuilderSetLabel() {
        return null;
    }

    @Override
    public IIpsBuilderSetPropertyDef getPropertyDefinition(String name) {
        return Arrays.stream(propertyDefs).filter(p -> Objects.equals(name, p.getName())).findFirst().orElse(null);
    }

    @Override
    public IIpsBuilderSetPropertyDef[] getPropertyDefinitions() {
        return propertyDefs;
    }

    public void setPropertyDefinitions(IIpsBuilderSetPropertyDef[] propertyDefs) {
        this.propertyDefs = propertyDefs;
    }

    @Override
    public MessageList validateIpsArtefactBuilderSetConfig(IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        return null;
    }

    @Override
    public Message validateIpsBuilderSetPropertyValue(IIpsProject ipsProject,
            String propertyName,
            String propertyValue) {
        return null;
    }

    @Override
    public IIpsArtefactBuilderSetConfigModel createDefaultConfiguration(IIpsProject ipsProject) {
        return null;
    }
}
