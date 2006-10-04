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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.util.message.MessageList;


/**
 * The test case message cue label provider takes a given label provider and
 * decorates the image with a message cue image. The text is returned
 * unchanged.
 */
public class TestCaseMessageCueLabelProvider extends MessageCueLabelProvider {
    
    private TestCaseSection testCaseSection;
    
    public TestCaseMessageCueLabelProvider(ILabelProvider baseProvider, TestCaseSection testCaseSection) {
        super(baseProvider);
        this.testCaseSection = testCaseSection;
    }

    /**
     * Returns the message list applying to the given element. Forwards the validation to the 
     * test case section. Because if the relation level is hidden the parent of child test policy cmpts will
     * be used to validate the group of the underlying relations.
     * 
     * @see TestCaseSection#validateElement(Object)
     *
     * @throws CoreException if an error occurs during the creation of the message list.
     */
    protected MessageList getMessages(Object element) throws CoreException {
        if (element instanceof Validatable) {
            return testCaseSection.validateElement(element);
        } else {
            return new MessageList();
        }
    }
}
