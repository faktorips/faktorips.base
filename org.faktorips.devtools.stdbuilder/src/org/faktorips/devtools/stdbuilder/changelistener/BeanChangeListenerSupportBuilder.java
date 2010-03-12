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

package org.faktorips.devtools.stdbuilder.changelistener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaGeneratiorHelper;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.LocalizedTextHelper;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.runtime.AssociationChangedEvent;
import org.faktorips.runtime.INotificationSupport;
import org.faktorips.runtime.IpsPropertyChangeSupport;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Implementation of <code>IChangeListenerSupportBuilder</code> creating change listener support
 * with Java Bean bound properties.
 * 
 * @author Daniel Hohenberger
 */
public class BeanChangeListenerSupportBuilder implements IChangeListenerSupportBuilder {
    private final LocalizedTextHelper localizedTextHelper = new LocalizedTextHelper(new LocalizedStringsSet(
            BeanChangeListenerSupportBuilder.class));
    private final GenPolicyCmptType genPolicyCmptType;

    public BeanChangeListenerSupportBuilder(GenPolicyCmptType genPolicyCmptType) {
        this.genPolicyCmptType = genPolicyCmptType;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void generateChangeListenerSupportBeforeChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String fieldType,
            String fieldName,
            String paramName,
            String fieldNameConstant) {
        if (eventType.equals(ChangeEventType.OBJECT_HAS_CHANGED)
                || eventType.equals(ChangeEventType.DERIVED_PROPERTY_CHANGED)
                || eventType.equals(ChangeEventType.MUTABLE_PROPERTY_CHANGED)
                || eventType.equals(ChangeEventType.ASSOCIATION_OBJECT_CHANGED)) {
            methodsBuilder.appendClassName(fieldType);
            methodsBuilder.append(' ');
            methodsBuilder.append(getOldFieldName(fieldName));
            methodsBuilder.append(" = ");
            methodsBuilder.append(fieldName);
            methodsBuilder.appendln(";");
        }
    }

    private String getOldFieldName(String fieldName) {
        return "old" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * {@inheritDoc}
     */
    public void generateChangeListenerSupportAfterChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String fieldType,
            String fieldName,
            String paramName,
            String fieldNameConstant) {
        if (eventType.equals(ChangeEventType.OBJECT_HAS_CHANGED)
                || eventType.equals(ChangeEventType.DERIVED_PROPERTY_CHANGED)
                || eventType.equals(ChangeEventType.MUTABLE_PROPERTY_CHANGED)) {
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(PropertyChangeEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.append(fieldNameConstant);
            methodsBuilder.append(", ");
            appendFieldAccess(methodsBuilder, fieldType, getOldFieldName(fieldName));
            methodsBuilder.append(", ");
            appendFieldAccess(methodsBuilder, fieldType, fieldName);
            methodsBuilder.appendln("));");
        } else if (eventType.equals(ChangeEventType.ASSOCIATION_OBJECT_ADDED)) {
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(AssociationChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.append(fieldNameConstant);
            methodsBuilder.append(", null, ");
            methodsBuilder.append(paramName);
            methodsBuilder.appendln("));");
        } else if (eventType.equals(ChangeEventType.ASSOCIATION_OBJECT_REMOVED)) {
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(AssociationChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.append(fieldNameConstant);
            methodsBuilder.append(", ");
            methodsBuilder.append(paramName);
            methodsBuilder.appendln(", null));");
        } else if (eventType.equals(ChangeEventType.ASSOCIATION_OBJECT_CHANGED)) {
            methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
            methodsBuilder.appendClassName(AssociationChangedEvent.class);
            methodsBuilder.append("(this, ");
            methodsBuilder.append(fieldNameConstant);
            methodsBuilder.append(", ");
            methodsBuilder.append(getOldFieldName(fieldName));
            methodsBuilder.append(", ");
            methodsBuilder.append(paramName);
            methodsBuilder.appendln("));");
        }
    }

    private void appendFieldAccess(JavaCodeFragmentBuilder methodsBuilder, String fieldType, String fieldName) {
        if (fieldType.equals(int.class.getName())) {
            methodsBuilder.append("new ");
            methodsBuilder.appendClassName(Integer.class);
            methodsBuilder.append("(");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(")");
        } else if (fieldType.equals(boolean.class.getName())) {
            methodsBuilder.appendClassName(Boolean.class);
            methodsBuilder.append(".valueOf(");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(")");
        } else {
            methodsBuilder.append(fieldName);
        }
    }

    private static final String propertyChangeSupportFieldName = "propertyChangeSupport";

    /**
     * {@inheritDoc}
     */
    public void generateChangeListenerMethods(JavaCodeFragmentBuilder methodBuilder,
            String[] parentObjectFieldNames,
            boolean createPropertyChangeListenerMethods) {
        generateMethodNotifyChangeListeners(methodBuilder, parentObjectFieldNames, createPropertyChangeListenerMethods);

        if (createPropertyChangeListenerMethods) {
            generateMethodDelegation(methodBuilder, Modifier.PUBLIC, Void.TYPE, "addPropertyChangeListener",
                    new String[] { "listener" }, new Class[] { PropertyChangeListener.class },
                    propertyChangeSupportFieldName);
            generateMethodDelegation(methodBuilder, Modifier.PUBLIC, Void.TYPE, "addPropertyChangeListener",
                    new String[] { "listener", "propagateEventsFromChildren" }, new Class[] {
                            PropertyChangeListener.class, boolean.class }, propertyChangeSupportFieldName);
            generateMethodDelegation(methodBuilder, Modifier.PUBLIC, Void.TYPE, "addPropertyChangeListener",
                    new String[] { "propertyName", "listener" }, new Class[] { String.class,
                            PropertyChangeListener.class }, propertyChangeSupportFieldName);
            generateMethodDelegation(methodBuilder, Modifier.PUBLIC, boolean.class, "hasListeners",
                    new String[] { "propertyName" }, new Class[] { String.class }, propertyChangeSupportFieldName);
            generateMethodDelegation(methodBuilder, Modifier.PUBLIC, Void.TYPE, "removePropertyChangeListener",
                    new String[] { "listener" }, new Class[] { PropertyChangeListener.class },
                    propertyChangeSupportFieldName);
            generateMethodDelegation(methodBuilder, Modifier.PUBLIC, Void.TYPE, "removePropertyChangeListener",
                    new String[] { "propertyName", "listener" }, new Class[] { String.class,
                            PropertyChangeListener.class }, propertyChangeSupportFieldName);
        }
    }

    /**
     * <pre>
     * public void notifyChangeListeners(PropertyChangeEvent event) {
     * -if createPropertyChangeListenerMethods=true:
     *     if (event instanceof AssociationChangedEvent) {
     *         propertyChangeSupport.fireAssociationChange((AssociationChangedEvent)event);
     *     } else {
     *         propertyChangeSupport.firePropertyChange(event);
     *     }
     * -else
     *     super.notifyChangeListeners(event);
     * -for each parent object:
     *     if (parentObject != null) {
     *         parentObject.notifyChangeListeners(event);
     *     }
     *     ...
     * }
     * note that this method will only be created if 
     * a) createPropertyChangeListenerMethods=true or
     * b) parentModelObjectNames.length > 0
     * 
     * </pre>
     */
    public void generateMethodNotifyChangeListeners(JavaCodeFragmentBuilder methodBuilder,
            String parentModelObjectNames[],
            boolean createPropertyChangeListenerMethods) {
        if (!createPropertyChangeListenerMethods && parentModelObjectNames.length == 0) {
            return;
        }

        methodBuilder.javaDoc(genPolicyCmptType.getJavaDocCommentForOverriddenMethod(),
                JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (!createPropertyChangeListenerMethods) {
            JavaGeneratiorHelper.appendOverrideAnnotation(methodBuilder, genPolicyCmptType.getPolicyCmptType()
                    .getIpsProject(), false);
        }
        methodBuilder.methodBegin(Modifier.PUBLIC, Void.TYPE, MethodNames.NOTIFIY_CHANGE_LISTENERS,
                new String[] { "event" }, new Class[] { PropertyChangeEvent.class });
        if (createPropertyChangeListenerMethods) {
            methodBuilder.append("if (event instanceof ");
            methodBuilder.appendClassName(AssociationChangedEvent.class);
            methodBuilder.append(") {");
            methodBuilder.append(propertyChangeSupportFieldName + ".fireAssociationChange((");
            methodBuilder.appendClassName(AssociationChangedEvent.class);
            methodBuilder.appendln(")event);");
            methodBuilder.append("} else {");
            methodBuilder.appendln(propertyChangeSupportFieldName + ".firePropertyChange(event);");
            methodBuilder.append("}");
        } else {
            methodBuilder.append("super.");
            methodBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS);
            methodBuilder.append("(event);");
        }
        for (int i = 0; i < parentModelObjectNames.length; i++) {
            methodBuilder.append("if (");
            methodBuilder.append(parentModelObjectNames[i]);
            methodBuilder.appendln("!=null) {");
            methodBuilder.append("((");
            methodBuilder.appendClassName(INotificationSupport.class);
            methodBuilder.append(")");
            methodBuilder.append(parentModelObjectNames[i]);
            methodBuilder.append(").");
            methodBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS);
            methodBuilder.appendln("(event);");
            methodBuilder.appendln("}");
        }
        methodBuilder.methodEnd();
    }

    private void generateMethodDelegation(JavaCodeFragmentBuilder methodBuilder,
            int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            String delegateName) {
        methodBuilder.javaDoc(genPolicyCmptType.getJavaDocCommentForOverriddenMethod(),
                JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodBuilder.methodBegin(modifier, returnType, methodName, argName, argClass);
        if (!returnType.equals(Void.TYPE)) {
            methodBuilder.append("return ");
        }
        methodBuilder.append(delegateName + "." + methodName + "(");
        for (int i = 0; i < argName.length; i++) {
            methodBuilder.append(argName[i]);
            if (i < argName.length - 1) {
                methodBuilder.append(", ");
            }
        }
        methodBuilder.appendln(");");
        methodBuilder.methodEnd();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * protected final IpsPropertyChangeSupport propertyChangeSupport = new IpsPropertyChangeSupport(this);
     * </pre>
     */
    public void generateChangeListenerConstants(JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc("FIELD_PROPERTY_CHANGE_SUPPORT", builder, genPolicyCmptType
                .getLanguageUsedInGeneratedSourceCode());

        genPolicyCmptType.getBuilderSet().addAnnotations(
                AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD, genPolicyCmptType.getPolicyCmptType(),
                builder);

        builder.appendJavaModifier(Modifier.PROTECTED);
        builder.append(' ');
        builder.appendJavaModifier(Modifier.FINAL);
        builder.append(' ');
        builder.appendClassName(IpsPropertyChangeSupport.class);
        builder.append(' ');
        builder.append(propertyChangeSupportFieldName);
        builder.append(" = new ");
        builder.appendClassName(IpsPropertyChangeSupport.class);
        builder.appendln("(this);");
    }

    /**
     * 
     * {@inheritDoc}
     */
    protected String generate() throws CoreException {
        return null; // nothing to do as we don't build our own classes.
    }

    /**
     * 
     * {@inheritDoc}
     */
    public String getNotificationSupportInterfaceName() {
        return INotificationSupport.class.getName();
    }
}
