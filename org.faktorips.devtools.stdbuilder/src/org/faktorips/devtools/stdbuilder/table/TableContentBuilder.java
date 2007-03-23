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

package org.faktorips.devtools.stdbuilder.table;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilder;

public class TableContentBuilder extends XmlContentFileCopyBuilder {

    public TableContentBuilder(IIpsArtefactBuilderSet builderSet, String kind) {
        super(IpsObjectType.TABLE_CONTENTS, builderSet, kind);
    }

    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        
        ITableContents contents = (ITableContents)ipsSrcFile.getIpsObject();
        ITableStructure structure = contents.findTableStructure();
        if(structure == null || structure.isEnumType()){
            return;
        }
        super.build(ipsSrcFile);
    }

    
}
