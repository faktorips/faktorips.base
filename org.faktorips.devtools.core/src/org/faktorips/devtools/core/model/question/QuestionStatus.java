/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.question;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;

/**
 * Status of a question, e.g. open or closed.
 * 
 * @author Faktor Zehn AG, Juergen Niedernhuber
 */
public class QuestionStatus extends DefaultEnumValue {

    public QuestionStatus(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

}
