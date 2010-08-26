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

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseContentProvider;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseLabelProvider;

/**
 * Wrapper for test case content objetcs. Wrapps the objects inside the test case tree structure.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseDeltaWrapperObject {

    /** The wrapped object from the test case content */
    private Object baseObject;

    /** The parent of this wrapper, same parent child structure like the test case content */
    private TestCaseDeltaWrapperObject parent;

    /** Flag to indicate that the test case object will be deleted */
    private boolean willBeDeleted;

    /**
     * Flag to indicate that the test case object has test attributes which will be added if the
     * delta will be accepted
     */
    private boolean hasNewTestAttributes;

    /**
     * Flag to indicate that the test case object has test attribute values which will be removed if
     * the delta will be accepted
     */
    private boolean hasToBeDeletedTestAttributes;

    /**
     * The delta type this wrapper object represents.
     */
    private TestCaseDeltaType deltaType;

    /** The content provider of the test case used to get the children of the wrapped base object. */
    private TestCaseContentProvider testCaseContentProvider;

    /**
     * Returns a list of delta wrapper objects for the given list of base objects.
     */
    public static Object[] createWrapperObjects(TestCaseContentProvider testCaseContentProvider,
            TestCaseDeltaType deltaType,
            TestCaseDeltaWrapperObject parent,
            Object[] baseObjects) {

        TestCaseDeltaWrapperObject[] parts = new TestCaseDeltaWrapperObject[baseObjects.length];
        for (int i = 0; i < baseObjects.length; i++) {
            parts[i] = new TestCaseDeltaWrapperObject(testCaseContentProvider, deltaType, parent, baseObjects[i]);
        }
        return parts;
    }

    private TestCaseDeltaWrapperObject(TestCaseContentProvider testCaseContentProvider, TestCaseDeltaType deltaType,
            TestCaseDeltaWrapperObject parent, Object baseObject) {

        this.testCaseContentProvider = testCaseContentProvider;
        this.baseObject = baseObject;
        this.parent = parent;
        this.deltaType = deltaType;
    }

    /**
     * Returns the base object (wrapped object)
     */
    public Object getBaseObject() {
        return baseObject;
    }

    /**
     * Returns the children of the wrapped object, the test case content provider will be used to
     * evaluate the children afterwards for each children a new wrapper object is created and
     * returned in the result list.
     */
    public Object[] getChildren(TestCaseDeltaWrapperObject parent) {
        return createWrapperObjects(testCaseContentProvider, deltaType, parent, testCaseContentProvider
                .getChildren(baseObject));
    }

    /**
     * Returns the text of the base object. The given label provider is used to delegate the getText
     * method.
     */
    public String getText(TestCaseLabelProvider labelProvider) {
        return labelProvider.getText(baseObject);
    }

    /**
     * Returns the image of the base object. The given label provider is used to delegate the
     * getImage method.
     */
    public ImageDescriptor getImage(TestCaseLabelProvider labelProvider) {
        if (isWillBeDeleted()) {
            return getDeletedImage(labelProvider.getImage(baseObject));
        } else {
            return labelProvider.getImageDescriptor(baseObject);
        }
    }

    /**
     * Returns the to be delted image.
     */
    private ImageDescriptor getDeletedImage(Image base) {
        return DeltaCompositeIcon.createDeleteImage(base);
    }

    /**
     * Returns the paren wrapper object or <code>null</code> if there is no parent.
     */
    public TestCaseDeltaWrapperObject getParent() {
        return parent;
    }

    /**
     * Returns the delat type this wrapper object represents.
     */
    public TestCaseDeltaType getDeltaType() {
        return deltaType;
    }

    /**
     * Returns <code>true</code> if the base object (test case model object) of this wrapper object
     * will be deleted or a parent of the base will be deleted. Because if a object will be deleted
     * all the childs will be deleted too.
     */
    public boolean isWillBeDeleted() {
        boolean toBeDeleted = willBeDeleted;
        TestCaseDeltaWrapperObject currObject = getParent();
        while (currObject != null && !toBeDeleted) {
            toBeDeleted = currObject.isWillBeDeleted();
            currObject = currObject.getParent();
        }
        return toBeDeleted;
    }

    /**
     * Sets if the base object will be deleted if the delta will be accepted.
     */
    public void setWillBeDeleted(boolean willBeDeleted) {
        this.willBeDeleted = willBeDeleted;
    }

    /**
     * Returns if the base object will get new test attributes if the delta will be accepted.
     */
    public boolean isHasNewTestAttributes() {
        return hasNewTestAttributes;
    }

    /**
     * Sets if the base object will get new test attributes if the delta will be accepted.
     */
    public void setHasNewTestAttributes(boolean hasNewTestAttributes) {
        this.hasNewTestAttributes = hasNewTestAttributes;
    }

    /**
     * Returns if the base object has test attributes which will be deleted if the delta will be
     * accepted.
     */
    public boolean isHasToBeDeletedTestAttributes() {
        return hasToBeDeletedTestAttributes;
    }

    /**
     * Sets if the base object has test attributes which will be deleted if the delta will be
     * accepted.
     */
    public void setHasToBeDeletedTestAttributes(boolean hasToBeDeletedTestAttributes) {
        this.hasToBeDeletedTestAttributes = hasToBeDeletedTestAttributes;
    }
}
