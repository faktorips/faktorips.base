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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Handles the imports for generated java files. Imports can be added using {@link #add(String)}.
 * All added statements can be retrieved using the {@link #getImports()} method.
 * 
 * @author widmaier
 */
public class ImportHandler {
    private Set<ImportStatement> imports;

    public ImportHandler() {
        this.imports = new LinkedHashSet<ImportStatement>();
    }

    public Set<ImportStatement> getImports() {
        return new LinkedHashSet<ImportStatement>(imports);
    }

    public boolean add(String importStatement) {
        return imports.add(new ImportStatement(importStatement));
    }

    public boolean remove(String importStatement) {
        return imports.remove(new ImportStatement(importStatement));
    }
}