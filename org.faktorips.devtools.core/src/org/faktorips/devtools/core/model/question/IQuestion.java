/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.question;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * 
 * @author INSERT YOUR NAME
 */
public interface IQuestion extends IIpsObject {

    public final static String PROPERY_QUESTION_TEXT = "questionText";

    public final static String MSG_CODE_QUESTION_TEXT_IS_EMPTY = "QuestionTextIsEmpty";

    /**
     * Returns the text of the question, e.g. "Why is ...?"
     */
    public String getQuestionText();

    /**
     * Sets the question text.
     * 
     * @throws NullPointerException if newText is <code>null</code>.
     */
    public void setQuestionText(String newText);
}
