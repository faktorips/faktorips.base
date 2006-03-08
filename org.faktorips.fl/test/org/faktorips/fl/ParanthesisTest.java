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

package org.faktorips.fl;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;


/**
 *
 */
public class ParanthesisTest extends CompilerAbstractTest {
    
    public void test() throws Exception {
        execAndTestSuccessfull("3.0 + 2.0 * 5.0", Decimal.valueOf("13.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("(3.0 + 2.0) * 5.0", Decimal.valueOf("25.00"), Datatype.DECIMAL);        
    }

    
}
