/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Element;

/**
 * An ips test case defines the fixture to run a test. To define an ips test case<br>
 * 1) implement a subclass of IpsTestCase2<br>
 * 2) implement the method getRepository to create the runtime repository<br>
 * 3) implement the method executeBusinessLogic to execute the necessary business actions<br>
 * 4) implement the method executeAsserts to ensure the correct result of the test.
 * 
 * @author Jan Ortmann
 */
public abstract class IpsTestCase2 extends IpsTestCaseBase {

    // map containing extension attributes
    private Map<ModelObjectAttribute, Object> extensionAttributes = new HashMap<>();

    public IpsTestCase2(String qName) {
        super(qName);
    }

    /**
     * Initialized the input and expected result objects from the xml.
     */
    public final void initFromXml(Element testCaseEl) {
        initInputFromXml(XmlUtil.getFirstElement(testCaseEl, "Input"));
        initExpectedResultFromXml(XmlUtil.getFirstElement(testCaseEl, "ExpectedResult"));
    }

    /**
     * Adds a extension attribute value identified by the attribute name and the test object the
     * attribute is related to.
     */
    protected void addExtensionAttribute(IModelObject modelObject, String attributeName, Object value) {
        extensionAttributes.put(new ModelObjectAttribute(modelObject, attributeName), value);
    }

    /**
     * Initialized the input from the given element.
     */
    protected abstract void initInputFromXml(Element inputEl);

    /**
     * Initialized the expected result from the given element.
     */
    protected abstract void initExpectedResultFromXml(Element resultEl);

    @Override
    public String toString() {
        return "TestCase " + getQualifiedName();
    }

    @Override
    public int countTestCases() {
        return 1;
    }

    /**
     * Returns the value of the given extension attribute identified by the attribute name and the
     * model object the attribute is related to. Returns <code>null</code> if no such extension
     * attributes exists.
     * 
     * @param modelObject The model object for which the value of the extension attribute should be
     *            returned
     * @param attributeName The name which identifies the extension attribute
     */
    public Object getExtensionAttributeValue(IModelObject modelObject, String attributeName) {
        return extensionAttributes.get(new ModelObjectAttribute(modelObject, attributeName));
    }

    /*
     * Wrapper class stores all extension attributes.
     */
    private static class ModelObjectAttribute {

        private IModelObject modelObject;
        private String attributeName;

        public ModelObjectAttribute(IModelObject modelObject, String attributeName) {
            super();
            this.modelObject = modelObject;
            this.attributeName = attributeName;
        }

        /**
         * Returns the model object
         */
        public IModelObject getModelObject() {
            return modelObject;
        }

        /**
         * Returns the attribute name of the model object this wrapper object belongs to
         */
        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public boolean equals(Object other) {
            if ((this == other)) {
                return true;
            }
            if ((other == null) || !(other instanceof ModelObjectAttribute)) {
                return false;
            }
            ModelObjectAttribute castOther = (ModelObjectAttribute)other;
            return (getAttributeName() != null && getAttributeName().equals(castOther.getAttributeName()))
                    && (getModelObject() != null && getModelObject().equals(castOther.getModelObject()));
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + (getModelObject() == null ? 0 : getModelObject().hashCode());
            return 37 * result + (getAttributeName() == null ? 0 : getAttributeName().hashCode());
        }

    }

}
