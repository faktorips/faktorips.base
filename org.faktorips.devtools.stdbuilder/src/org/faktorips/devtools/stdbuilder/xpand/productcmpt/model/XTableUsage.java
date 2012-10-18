/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.table.model.XTable;
import org.faktorips.runtime.ITable;

public class XTableUsage extends AbstractGeneratorModelNode {

    public XTableUsage(ITableStructureUsage ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    @Override
    public ITableStructureUsage getIpsObjectPartContainer() {
        return (ITableStructureUsage)super.getIpsObjectPartContainer();
    }

    public ITableStructureUsage getTableStructureUsage() {
        return getIpsObjectPartContainer();
    }

    public String getFieldName() {
        return getJavaNamingConvention().getMemberVarName(getName() + "Name");
    }

    public String getMethodNameSetter() {
        return getJavaNamingConvention().getSetterMethodName(getName() + "Name");
    }

    public String getMethodNameGetter() {
        return getJavaNamingConvention().getGetterMethodName(getName());
    }

    public String getTableClassName() {
        if (getTableStructureUsage().getTableStructures().length > 1) {
            return addImport(ITable.class);
        } else {
            String tableStructureName = getTableStructureUsage().getTableStructures()[0];
            ITableStructure tableStructure;
            try {
                tableStructure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE,
                        tableStructureName);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            XTable xTable = getModelNode(tableStructure, XTable.class);
            return xTable.getSimpleName(BuilderAspect.IMPLEMENTATION);
        }
    }
}
