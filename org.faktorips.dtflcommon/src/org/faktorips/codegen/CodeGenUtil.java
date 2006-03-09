/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.codegen.dthelpers.PrimitiveBooleanHelper;
import org.faktorips.codegen.dthelpers.PrimitiveIntegerHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;

/**
 * A collection of util methods related to sourcecode generation.
 */
public class CodeGenUtil {
    
    public final static JavaCodeFragment convertPrimitiveToWrapper (Datatype type, JavaCodeFragment expression) {
        if (type instanceof PrimitiveBooleanDatatype) {
            return new PrimitiveBooleanHelper((PrimitiveBooleanDatatype)type).toWrapper(expression); 
        }
        if (type instanceof PrimitiveIntegerDatatype) {
            return new PrimitiveIntegerHelper((PrimitiveIntegerDatatype)type).toWrapper(expression); 
        }
        throw new IllegalArgumentException("Can't convert dataype " + type);
    }

    private CodeGenUtil() {
    }

}
