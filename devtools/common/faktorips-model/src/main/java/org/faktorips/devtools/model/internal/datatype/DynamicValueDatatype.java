/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.datatype.IDynamicValueDatatype;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.NamedDataTypeDisplay;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.MethodAccess;
import org.w3c.dom.Element;

/**
 * A dynamic value data type is a generic value data type where the Java class represented by the
 * data type is defined by it's qualified class name. The class is resolved, when one of the
 * datatype's method like is isParsable() is called, which needs a static method of the underlying
 * Java class. If the Java class' source belongs to the IpsProject/JavaProject, than this class (the
 * byte code) might exist for some time and than be unavailable or incomplete, e.g. if the class has
 * compile errors.
 * 
 * @author Jan Ortmann
 */
public class DynamicValueDatatype extends GenericValueDatatype implements IDynamicValueDatatype {

    public static final String MSGCODE_PREFIX_GET_NAME_METHOD = MSGCODE_PREFIX + "getNameMethod"; //$NON-NLS-1$
    public static final String MSGCODE_GET_NAME_METHOD_IS_BLANK = MSGCODE_PREFIX_GET_NAME_METHOD + " is empty or blank"; //$NON-NLS-1$
    public static final String MSGCODE_PREFIX_GET_VALUE_BY_NAME_METHOD = MSGCODE_PREFIX + "getValueByName"; //$NON-NLS-1$
    public static final String MSGCODE_GET_VALUE_BY_NAME_METHOD_IS_BLANK = MSGCODE_PREFIX_GET_VALUE_BY_NAME_METHOD
            + " is empty or blank"; //$NON-NLS-1$
    private IIpsProject ipsProject;
    private IClassLoaderProvider classLoaderProvider;
    private IClasspathContentsChangeListener listener;

    private boolean isSupportingNames = false;

    private String getNameMethodName = ""; //$NON-NLS-1$
    private String getValueByNameMethodName = ""; //$NON-NLS-1$
    private String className;
    private Class<?> adaptedClass;

    public DynamicValueDatatype(IIpsProject ipsProject) {
        super();
        this.ipsProject = ipsProject;
    }

    @Override
    public void setAdaptedClassName(String className) {
        this.className = className;
        clearCache();
    }

    @Override
    public void setAdaptedClass(Class<?> clazz) {
        adaptedClass = clazz;
        className = clazz.getName();
    }

    @Override
    public String getAdaptedClassName() {
        return className;
    }

    @Override
    protected void clearCache() {
        super.clearCache();
        adaptedClass = null;
    }

    @Override
    public Class<?> getAdaptedClass() {
        if (adaptedClass == null) {
            try {
                classLoaderProvider = ipsProject.getClassLoaderProviderForJavaProject();
                adaptedClass = classLoaderProvider.getClassLoader().loadClass(className);
                listener = this::clearCacheAndRemoveListener;
                classLoaderProvider.addClasspathChangeListener(listener);
                // CSOFF: IllegalCatchCheck
            } catch (Throwable t) {
                IpsLog.log(t);
                /*
                 * Data type remains invalid as long as the class can't be loaded or an exception
                 * occurs while adding the class path change listener.
                 */
                adaptedClass = null;
            }
            // CSON: IllegalCatchCheck
        }
        return adaptedClass;
    }

    private void clearCacheAndRemoveListener(AJavaProject project) {
        clearCache();
        IIpsModel.get().getIpsProject(project.getProject())
                .getClassLoaderProviderForJavaProject()
                .removeClasspathChangeListener(listener);
        listener = null;
    }

    @Override
    public void writeToXml(Element element) {
        if (getAdaptedClassName() != null) {
            element.setAttribute("javaClass", getAdaptedClassName()); //$NON-NLS-1$
        }
        if (getQualifiedName() != null) {
            element.setAttribute("id", getQualifiedName()); //$NON-NLS-1$
        }
        if (getValueOfMethodName() != null) {
            element.setAttribute("valueOfMethod", getValueOfMethodName()); //$NON-NLS-1$
        }
        if (getIsParsableMethodName() != null) {
            element.setAttribute("isParsableMethod", getIsParsableMethodName()); //$NON-NLS-1$
        }
        if (getToStringMethodName() != null) {
            element.setAttribute("valueToStringMethod", getToStringMethodName()); //$NON-NLS-1$
        }
        if (getGetNameMethodName() != null) {
            element.setAttribute("getNameMethod", getGetNameMethodName()); //$NON-NLS-1$
        }
        if (getGetValueByNameMethodName() != null) {
            element.setAttribute("getValueByNameMethod", getGetValueByNameMethodName()); //$NON-NLS-1$
        }
        if (getAllValuesMethodName() != null) {
            element.setAttribute("getAllValuesMethod", getAllValuesMethodName()); //$NON-NLS-1$
        }
        element.setAttribute("isSupportingNames", Boolean.toString(isSupportingNames())); //$NON-NLS-1$
        if (hasNullObject()) {
            ValueToXmlHelper.addValueToElement(getNullObjectId(), element, "NullObjectId"); //$NON-NLS-1$
        }
    }

    public static final DynamicValueDatatype createFromXml(IIpsProject ipsProject, Element element) {
        DynamicValueDatatype datatype = createDynamicValueOrEnumDatatype(ipsProject, element);
        // note: up to version 2.1 it was valueClass, since then it is javaClass
        String javaClass = element.getAttribute("valueClass"); //$NON-NLS-1$
        if (IpsStringUtils.isEmpty(javaClass)) {
            javaClass = element.getAttribute("javaClass"); //$NON-NLS-1$
        }
        datatype.setAdaptedClassName(javaClass);
        datatype.setQualifiedName(element.getAttribute("id")); //$NON-NLS-1$
        if (element.hasAttribute("valueOfMethod")) { //$NON-NLS-1$
            datatype.setValueOfMethodName(element.getAttribute("valueOfMethod")); //$NON-NLS-1$
        } else {
            datatype.setValueOfMethodName(null);
        }
        if (element.hasAttribute("isParsableMethod")) { //$NON-NLS-1$
            datatype.setIsParsableMethodName(element.getAttribute("isParsableMethod")); //$NON-NLS-1$
        } else {
            datatype.setIsParsableMethodName(null);
        }
        if (element.hasAttribute("valueToStringMethod")) { //$NON-NLS-1$
            datatype.setToStringMethodName(element.getAttribute("valueToStringMethod")); //$NON-NLS-1$
        } else {
            datatype.setToStringMethodName(null);
        }
        if (element.hasAttribute("getNameMethod")) { //$NON-NLS-1$
            datatype.setGetNameMethodName(element.getAttribute("getNameMethod")); //$NON-NLS-1$
        } else {
            datatype.setGetNameMethodName(null);
        }
        if (element.hasAttribute("getValueByNameMethod")) { //$NON-NLS-1$
            datatype.setGetValueByNameMethodName(element.getAttribute("getValueByNameMethod")); //$NON-NLS-1$ );
        } else {
            datatype.setGetValueByNameMethodName(null);
        }
        if (element.hasAttribute("getAllValuesMethod")) { //$NON-NLS-1$
            datatype.setAllValuesMethodName(element.getAttribute("getAllValuesMethod")); //$NON-NLS-1$ );
        } else {
            datatype.setAllValuesMethodName(null);
        }
        String isSupporting = element.getAttribute("isSupportingNames"); //$NON-NLS-1$
        datatype.setIsSupportingNames(IpsStringUtils.isEmpty(isSupporting) ? false
                : Boolean.parseBoolean(isSupporting));

        Element nullObjectEl = XmlUtil.getFirstElement(element, "NullObjectId"); //$NON-NLS-1$
        if (nullObjectEl == null) {
            datatype.setNullObjectDefined(false);
            datatype.setNullObjectId(null);
        } else {
            datatype.setNullObjectDefined(true);
            datatype.setNullObjectId(ValueToXmlHelper.getValueFromElement(element, "NullObjectId")); //$NON-NLS-1$
        }
        return datatype;
    }

    private static DynamicValueDatatype createDynamicValueOrEnumDatatype(IIpsProject ipsProject, Element element) {
        DynamicValueDatatype datatype;
        String isEnumTypeString = element.getAttribute("isEnumType"); //$NON-NLS-1$
        if (IpsStringUtils.isEmpty(isEnumTypeString) || !Boolean.parseBoolean(isEnumTypeString)) {
            datatype = new DynamicValueDatatype(ipsProject);
        } else {
            datatype = new DynamicEnumDatatype(ipsProject);
        }
        return datatype;
    }

    @Override
    public void setIsSupportingNames(boolean supporting) {
        isSupportingNames = supporting;
    }

    @Override
    public boolean isSupportingNames() {
        return isSupportingNames;
    }

    @Override
    public void setGetNameMethodName(String getNameMethodName) {
        this.getNameMethodName = getNameMethodName;
    }

    @Override
    public String getGetNameMethodName() {
        return getNameMethodName;
    }

    @Override
    public String getValueName(String id) {
        return getValueName(id, IIpsModelExtensions.get().getModelPreferences().getDatatypeFormattingLocale());
    }

    @Override
    public String getValueName(String id, Locale locale) {
        if (IpsStringUtils.isBlank(getNameMethodName)) {
            throw new UnsupportedOperationException(
                    "This value type does not support a getName() method, value type class: " //$NON-NLS-1$
                            + getAdaptedClass());
        }
        return getValueNameFromClass(id, locale);
    }

    @Override
    public Object getValueByName(String valueName) {
        if (IpsStringUtils.isBlank(getValueByNameMethodName) && IpsStringUtils.isBlank(getAllValuesMethodName())) {
            throw new UnsupportedOperationException(
                    "This value type does not support a getValueByName(String) method, value type class: " //$NON-NLS-1$
                            + getAdaptedClass());
        }
        if (IpsStringUtils.isBlank(getValueByNameMethodName)) {
            return findValueByNameInAllValues(valueName);
        }
        return getValueByNameFromClass(valueName);
    }

    private Object findValueByNameInAllValues(String valueName) {
        Object result = getAllValuesMethod()
                .invokeStatic("to get all values"); //$NON-NLS-1$
        Object[] values;
        if (result instanceof Collection) {
            values = ((Collection<?>)result).toArray(Object[]::new);
        } else {
            values = (Object[])result;
        }
        return Arrays.stream(values)
                .filter(v -> Objects.equals(valueName,
                        getNameFromValue(v,
                                IIpsModelExtensions.get().getModelPreferences().getDatatypeFormattingLocale())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(valueName + " could not be found in all values.")); //$NON-NLS-1$
    }

    @Override
    public String getGetValueByNameMethodName() {
        return getValueByNameMethodName;
    }

    @Override
    public void setGetValueByNameMethodName(String name) {
        getValueByNameMethodName = name;
    }

    @Override
    public MessageList checkReadyToUse() {
        MessageList ml = super.checkReadyToUse();
        if (isSupportingNames()) {
            checkGetValueByName(ml);
            checkGetName(ml);
        }
        return ml;
    }

    private MethodAccess getGetValueByNameMethod() {
        return MethodAccess.of(getAdaptedClass(), getGetValueByNameMethodName(), CharSequence.class);
    }

    private void checkGetValueByName(MessageList ml) {
        if (IpsStringUtils.isBlank(getGetValueByNameMethodName())) {
            if (IpsStringUtils.isBlank(getAllValuesMethodName())) {
                if (NamedDataTypeDisplay.NAME
                        .equals(IIpsModelExtensions.get().getModelPreferences().getNamedDataTypeDisplay())) {
                    ml.add(Message.newError(MSGCODE_GET_VALUE_BY_NAME_METHOD_IS_BLANK,
                            "The datatype display type is \"Name\" but no getValueByNameMethod or getAllValuesMethod are configured for the datatype " //$NON-NLS-1$
                                    + getName()
                                    + ". Either define one of these methods or change the datatype display type to \"Name and Id\" and perform a clean build.")); //$NON-NLS-1$
                }
                if (!(this instanceof EnumDatatype)) {
                    // enums can just iterate over all values and use getNameMethod
                    ml.add(Message.newError(MSGCODE_GET_VALUE_BY_NAME_METHOD_IS_BLANK,
                            "SupportingNames is true but no getValueByNameMethod or getAllValuesMethod is configured for datatype " //$NON-NLS-1$
                                    + getName()));
                }
            }
            return;
        }
        getGetValueByNameMethod()
                .check(ml, MSGCODE_PREFIX_GET_VALUE_BY_NAME_METHOD)
                .exists()
                .isStatic()
                .returnTypeIsCompatible(getAdaptedClass());
    }

    private Object getValueByNameFromClass(String valueName) {
        if (valueName == null) {
            return null;
        }
        return getGetValueByNameMethod().invokeStatic("to get the value for a name", valueName); //$NON-NLS-1$
    }

    private MethodAccess getNameMethod() {
        return MethodAccess.of(getAdaptedClass(), getGetNameMethodName());
    }

    private MethodAccess getNameMethodWithLocale() {
        return MethodAccess.of(getAdaptedClass(), getGetNameMethodName(), Locale.class);
    }

    private void checkGetName(MessageList ml) {
        if (IpsStringUtils.isBlank(getGetNameMethodName())) {
            ml.add(Message.newError(MSGCODE_GET_NAME_METHOD_IS_BLANK,
                    "SupportingNames is true but no getNameMethod is configured.")); //$NON-NLS-1$
            return;
        }

        getNameMethod()
                .check(ml, MSGCODE_PREFIX_GET_NAME_METHOD)
                .exists()
                .isNotStatic()
                .returnTypeIsCompatible(String.class);
    }

    private String getValueNameFromClass(String id, Locale locale) {
        Object value = getValue(id);
        return getNameFromValue(value, locale);
    }

    protected String getNameFromValue(Object value, Locale locale) {
        if (value == null) {
            return null;
        }
        String methodDescription = "to get the value name"; //$NON-NLS-1$
        MethodAccess nameMethodWithLocale = getNameMethodWithLocale();
        return nameMethodWithLocale.exists()
                ? nameMethodWithLocale.invoke(methodDescription, value, locale)
                : getNameMethod().invoke(methodDescription, value);
    }
}
