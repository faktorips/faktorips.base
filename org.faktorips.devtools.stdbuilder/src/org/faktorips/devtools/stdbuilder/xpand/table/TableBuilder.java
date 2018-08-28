/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.table;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.XtendBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.table.model.XTable;
import org.faktorips.devtools.stdbuilder.xpand.table.template.TableTmpl;
import org.faktorips.util.LocalizedStringsSet;

public class TableBuilder extends XtendBuilder<XTable> {

    private TableRowBuilder tableRowBuilder;

    public TableBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext, ModelService modelService) {
        super(builderSet, modelContext, modelService, new LocalizedStringsSet(TableBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        return TableTmpl.body(getGeneratorModelRoot(ipsObject));
    }

    @Override
    protected Class<XTable> getGeneratorModelRootType() {
        return XTable.class;
    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        try {
            return isBuilderFor(ipsObjectPartContainer.getIpsSrcFile());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof ITableStructure) {
            return ipsObject;
        } else {
            return null;
        }
    }

    @Override
    protected boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    public void setTableRowBuilder(TableRowBuilder tableRowBuilder) {
        this.tableRowBuilder = tableRowBuilder;
    }

    public String getTableRowName() {
        return tableRowBuilder.getName();
    }

}
