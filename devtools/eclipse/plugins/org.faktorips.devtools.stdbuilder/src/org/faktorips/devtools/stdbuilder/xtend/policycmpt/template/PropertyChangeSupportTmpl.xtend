package org.faktorips.devtools.stdbuilder.xtend.policycmpt.template

import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass


import static extension org.faktorips.devtools.stdbuilder.xtend.template.ClassNames.*
import static extension org.faktorips.devtools.stdbuilder.xtend.template.CommonGeneratorExtensions.*

class PropertyChangeSupportTmpl {

def package static fieldDefinition (XPolicyCmptClass it) '''
    «IF generateChangeSupport && !hasSupertype()»
        /**
         *«localizedJDoc("FIELD_PROPERTY_CHANGE_SUPPORT")»
         *
         * @generated
         */
         «getAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD)»
        protected final «IpsPropertyChangeSupport()» propertyChangeSupport = new «IpsPropertyChangeSupport()»(this);
    «ENDIF»
'''

def package static generalMethods (XPolicyCmptClass it) '''
    «notifyChangeListeners(it)»
    «addRemoveListenerMethods(it)»
'''

def private static notifyChangeListeners (XPolicyCmptClass it) '''
    «IF generateNotifyChangeListeners»
        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public void notifyChangeListeners(«PropertyChangeEvent()» event) {
            «IF !hasSupertype()»
                if (event instanceof «AssociationChangedEvent()») {
                    propertyChangeSupport.fireAssociationChange((«AssociationChangedEvent()»)event);
                } else {
                    propertyChangeSupport.firePropertyChange(event);
                }
            «ELSE»
                super.notifyChangeListeners(event);
            «ENDIF»
            «FOR it : associations»
                «IF implementedDetailToMasterAssociation»
                    if («fieldName» != null) {
                        ((«INotificationSupport()»)«fieldName»).notifyChangeListeners(event);
                    }
                «ENDIF»
            «ENDFOR»
        }
    «ENDIF»
'''

def private static addRemoveListenerMethods (XPolicyCmptClass it) '''
    «IF generateChangeSupport && !hasSupertype()»
        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public void addPropertyChangeListener(«PropertyChangeListener()» listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public void addPropertyChangeListener(«PropertyChangeListener()» listener, boolean propagateEventsFromChildren) {
            propertyChangeSupport.addPropertyChangeListener(listener, propagateEventsFromChildren);
        }

        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public boolean hasListeners(String propertyName) {
            return propertyChangeSupport.hasListeners(propertyName);
        }

        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        /**
         *«inheritDoc»
         *
         * @generated
         */
        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    «ENDIF»
'''

def package static storeOldValue (XPolicyAttribute it) '''
    «IF generateChangeSupport»
        «javaClassName» «oldValueVariable» = «fieldName»;
    «ENDIF»
'''

def package static storeOldValue (XPolicyAssociation it) '''
    «IF generateChangeSupport»
        «targetInterfaceName» «oldValueVariable» = «fieldName»;
    «ENDIF»
'''

def package static notify (XPolicyAttribute it) '''
    «IF generateChangeSupport»
        «IF datatype.name=="int" || datatype.name=="boolean"»
            «notifyForIntOrBoolean(it)»
        «ELSE»
            notifyChangeListeners(new «PropertyChangeEvent()» (this, «constantNamePropertyName», «oldValueVariable», «fieldName»));
        «ENDIF»
    «ENDIF»
'''

def package static notify (XPolicyAssociation it) '''
    «notifyAssociation(oldValueVariable, "newObject", it)»
'''

def package static notifyNewAssociation(String variableName, XPolicyAssociation it) '''
    «notifyAssociation("null", variableName, it)»
'''

def package static notifyRemovedAssociation(String variableName, XPolicyAssociation it) '''
    «notifyAssociation(variableName, "null", it)»
'''

def private static notifyAssociation(String oldValue, String newValue, XPolicyAssociation it) '''
    «IF generateChangeSupport»
        notifyChangeListeners(new «AssociationChangedEvent()» (this, «constantNamePropertyName», «oldValue», «newValue»));
    «ENDIF»
'''

//Workaround entsprechend BeanChangeListenerSupportBuilder#appendFieldAccess()
def private static notifyForIntOrBoolean (XPolicyAttribute it) '''
        «IF datatype.name=="int"»
            notifyChangeListeners(new «PropertyChangeEvent()» (this, «constantNamePropertyName», Integer.valueOf(«oldValueVariable»), Integer.valueOf(«fieldName»)));
        «ELSE»
            notifyChangeListeners(new «PropertyChangeEvent()» (this, «constantNamePropertyName», Boolean.valueOf(«oldValueVariable»), Boolean.valueOf(«fieldName»)));
        «ENDIF»
'''

}