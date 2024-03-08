/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.datatype.IDynamicEnumDatatype;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.junit.Before;

public class AbstractJavaBuilderPluginTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;
    protected TestJavaBuilderSet builderSet;
    protected ModelService modelService;
    protected GeneratorModelContext modelContext;
    protected TestArtefactBuilderSetInfo testArtefactBuilderSetInfo;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        builderSet = (TestJavaBuilderSet)((IpsModel)IIpsModel.get()).getIpsArtefactBuilderSet(ipsProject, true);
        modelService = builderSet.getModelService();
        modelContext = builderSet.getGeneratorModelContext();
    }

    @Override
    protected void setTestArtefactBuilderSet(IIpsProjectProperties properties, IIpsProject project) {

        // Create the builder set for the project
        builderSet = new TestJavaBuilderSet();

        // Set the builder set id in the project's properties
        properties.setBuilderSetId(TestIpsArtefactBuilderSet.ID);

        // Add the new builder set to the builder set infos of the IPS model
        IpsModel ipsModel = (IpsModel)IIpsModel.get();
        IIpsArtefactBuilderSetInfo[] builderSetInfos = ipsModel.getIpsArtefactBuilderSetInfos();
        List<IIpsArtefactBuilderSetInfo> newBuilderSetInfos = new ArrayList<>(
                builderSetInfos.length + 1);
        for (IIpsArtefactBuilderSetInfo info : builderSetInfos) {
            newBuilderSetInfos.add(info);
        }
        builderSet.setIpsProject(project);
        testArtefactBuilderSetInfo = new TestArtefactBuilderSetInfo(builderSet);
        newBuilderSetInfos.add(testArtefactBuilderSetInfo);
        ipsModel.setIpsArtefactBuilderSetInfos(
                newBuilderSetInfos.toArray(new IIpsArtefactBuilderSetInfo[newBuilderSetInfos.size()]));
    }

    @Override
    protected void addIpsCapabilities(AProject project) {
        super.addIpsCapabilities(project);
        builderSet.init();
    }

    @Override
    protected IDynamicEnumDatatype[] newDefinedEnumDatatype(IIpsProject project, Class<?>[] adaptedClass)
            throws IpsException, IOException {
        IDynamicEnumDatatype[] newDefinedEnumDatatype = super.newDefinedEnumDatatype(project, adaptedClass);
        builderSet.setIpsProject(ipsProject);
        return newDefinedEnumDatatype;
    }

}
