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

package org.faktorips.devtools.core.ui.editors.question;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.model.question.IQuestion;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * 
 * @author INSERT YOUR NAME
 */
public class QuestionEditor extends IpsObjectEditor {

    public QuestionEditor() {
    }

    public IQuestion getQuestion() {
        return (IQuestion)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new QuestionPage(this, getQuestion()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUniformPageTitle() {
        return "Question: " + getQuestion().getName();
    }
}
