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
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilder;

public class TableContentBuilder extends XmlContentFileCopyBuilder {

    public TableContentBuilder(IIpsArtefactBuilderSet builderSet, String kind) {
        super(IpsObjectType.TABLE_CONTENTS, builderSet, kind);
    }

    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {

        ITableContents contents = (ITableContents)ipsSrcFile.getIpsObject();
        ITableStructure structure = contents.findTableStructure(getIpsProject());
        if (structure == null || structure.isModelEnumType()) {
            return;
        }
        super.build(ipsSrcFile);
    }

}
