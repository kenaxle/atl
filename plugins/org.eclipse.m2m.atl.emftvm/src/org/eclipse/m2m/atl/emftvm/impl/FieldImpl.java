/*******************************************************************************
 * Copyright (c) 2011 Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel - initial API and
 *         implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.m2m.atl.emftvm.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.atl.emftvm.CodeBlock;
import org.eclipse.m2m.atl.emftvm.EmftvmPackage;
import org.eclipse.m2m.atl.emftvm.Field;
import org.eclipse.m2m.atl.emftvm.Rule;
import org.eclipse.m2m.atl.emftvm.util.StackFrame;
import org.eclipse.m2m.atl.emftvm.util.VMException;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Field</b></em>'.
 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.m2m.atl.emftvm.impl.FieldImpl#getStaticValue <em>Static Value</em>}</li>
 *   <li>{@link org.eclipse.m2m.atl.emftvm.impl.FieldImpl#getInitialiser <em>Initialiser</em>}</li>
 *   <li>{@link org.eclipse.m2m.atl.emftvm.impl.FieldImpl#getRule <em>Rule</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FieldImpl extends FeatureImpl implements Field {

	/**
	 * The default value of the '{@link #getStaticValue() <em>Static Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStaticValue()
	 * @generated
	 * @ordered
	 */
	protected static final Object STATIC_VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStaticValue() <em>Static Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStaticValue()
	 * @generated
	 * @ordered
	 */
	protected Object staticValue = STATIC_VALUE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getInitialiser() <em>Initialiser</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInitialiser()
	 * @generated
	 * @ordered
	 */
	protected CodeBlock initialiser;

	/**
	 * Map of instance values.
	 */
	protected final Map<Object, Object> values = new HashMap<Object, Object>();

	/**
	 * Flag that signifies whether this field's static value is initialised.
	 */
	protected boolean staticValueInitialised;

	/**
	 * <!-- begin-user-doc -->
	 * Creates a new {@link FieldImpl}.
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FieldImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * Returns the {@link EClass} that correspond to this metaclass.
	 * @return the {@link EClass} that correspond to this metaclass.
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EmftvmPackage.Literals.FIELD;
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getStaticValue() {
		return staticValue;
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStaticValue(Object newStaticValue) {
		Object oldStaticValue = staticValue;
		staticValue = newStaticValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftvmPackage.FIELD__STATIC_VALUE, oldStaticValue, staticValue));
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CodeBlock getInitialiser() {
		return initialiser;
	}

	/**
	 * <!-- begin-user-doc. -->
	 * @see #setInitialiser(CodeBlock)
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInitialiser(CodeBlock newInitialiser, NotificationChain msgs) {
		CodeBlock oldInitialiser = initialiser;
		initialiser = newInitialiser;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EmftvmPackage.FIELD__INITIALISER, oldInitialiser, newInitialiser);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInitialiser(CodeBlock newInitialiser) {
		if (newInitialiser != initialiser) {
			NotificationChain msgs = null;
			if (initialiser != null)
				msgs = ((InternalEObject)initialiser).eInverseRemove(this, EmftvmPackage.CODE_BLOCK__INITIALISER_FOR, CodeBlock.class, msgs);
			if (newInitialiser != null)
				msgs = ((InternalEObject)newInitialiser).eInverseAdd(this, EmftvmPackage.CODE_BLOCK__INITIALISER_FOR, CodeBlock.class, msgs);
			msgs = basicSetInitialiser(newInitialiser, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftvmPackage.FIELD__INITIALISER, newInitialiser, newInitialiser));
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Rule getRule() {
		if (eContainerFeatureID() != EmftvmPackage.FIELD__RULE) return null;
		return (Rule)eContainer();
	}

	/**
	 * <!-- begin-user-doc. -->
	 * @see #setRule(Rule)
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRule(Rule newRule, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newRule, EmftvmPackage.FIELD__RULE, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRule(Rule newRule) {
		if (newRule != eInternalContainer() || (eContainerFeatureID() != EmftvmPackage.FIELD__RULE && newRule != null)) {
			if (EcoreUtil.isAncestor(this, newRule))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newRule != null)
				msgs = ((InternalEObject)newRule).eInverseAdd(this, EmftvmPackage.RULE__FIELDS, Rule.class, msgs);
			msgs = basicSetRule(newRule, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftvmPackage.FIELD__RULE, newRule, newRule));
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 */
	public Object getValue(final Object context) {
		return values.get(context==null ? Void.TYPE : context);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 */
	public void setValue(final Object context, final Object value) {
		values.put(context==null ? Void.TYPE : context, value);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 */
	public Object getValue(final Object context, final StackFrame frame) {
		if (!values.containsKey(context==null ? Void.TYPE : context)) {
			checkFrame(context, frame);
			final CodeBlock cb = getInitialiser();
			setValue(context, cb.execute(frame.getSubFrame(cb, context, null)));
		}
		return getValue(context);
	}

	/**
	 * Checks <pre>frame</pre> for execution loop in initialiser code block.
	 * @param context the 'self' object of the stack frame
	 * @param frame the stack frame to check
	 */
	private void checkFrame(final Object context, final StackFrame frame) {
		final CodeBlock initCb = getInitialiser();
		StackFrame newFrame = frame;
		while (newFrame != null) {
			if (newFrame.getCodeBlock() == initCb) {
				if (newFrame.getLocal(0, 0) == context) {
					throw new VMException(newFrame, String.format(
							"Infinite loop detected in field initialiser for %s.%s", 
							context, this));
				}
			}
			newFrame = newFrame.getParent();
		}
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 */
	public Object getStaticValue(final StackFrame frame) {
		if (!staticValueInitialised) {
			checkStaticFrame(frame);
			final CodeBlock cb = getInitialiser();
			setStaticValue(cb.execute(frame.getSubFrame(cb, null)));
			staticValueInitialised = true;
		}
		return getStaticValue();
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 */
	public void clear() {
		staticValueInitialised = false;
		staticValue = null;
		values.clear();
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EmftvmPackage.FIELD__INITIALISER:
				if (initialiser != null)
					msgs = ((InternalEObject)initialiser).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EmftvmPackage.FIELD__INITIALISER, null, msgs);
				return basicSetInitialiser((CodeBlock)otherEnd, msgs);
			case EmftvmPackage.FIELD__RULE:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetRule((Rule)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EmftvmPackage.FIELD__INITIALISER:
				return basicSetInitialiser(null, msgs);
			case EmftvmPackage.FIELD__RULE:
				return basicSetRule(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case EmftvmPackage.FIELD__RULE:
				return eInternalContainer().eInverseRemove(this, EmftvmPackage.RULE__FIELDS, Rule.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EmftvmPackage.FIELD__STATIC_VALUE:
				return getStaticValue();
			case EmftvmPackage.FIELD__INITIALISER:
				return getInitialiser();
			case EmftvmPackage.FIELD__RULE:
				return getRule();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case EmftvmPackage.FIELD__STATIC_VALUE:
				setStaticValue(newValue);
				return;
			case EmftvmPackage.FIELD__INITIALISER:
				setInitialiser((CodeBlock)newValue);
				return;
			case EmftvmPackage.FIELD__RULE:
				setRule((Rule)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case EmftvmPackage.FIELD__STATIC_VALUE:
				setStaticValue(STATIC_VALUE_EDEFAULT);
				return;
			case EmftvmPackage.FIELD__INITIALISER:
				setInitialiser((CodeBlock)null);
				return;
			case EmftvmPackage.FIELD__RULE:
				setRule((Rule)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case EmftvmPackage.FIELD__STATIC_VALUE:
				return STATIC_VALUE_EDEFAULT == null ? staticValue != null : !STATIC_VALUE_EDEFAULT.equals(staticValue);
			case EmftvmPackage.FIELD__INITIALISER:
				return initialiser != null;
			case EmftvmPackage.FIELD__RULE:
				return getRule() != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc. -->
	 * {@inheritDoc}
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		if (isStatic()) {
			result.append(" (staticValue: ");
			result.append(staticValue);
			result.append(')');
		}
		return result.toString();
	}

	/**
	 * Checks frame for execution loop in a static initialiser code block.
	 * @param frame the stack frame to check
	 */
	private void checkStaticFrame(final StackFrame frame) {
		final CodeBlock initCb = getInitialiser();
		StackFrame newFrame = frame;
		while (newFrame != null) {
			if (newFrame.getCodeBlock() == initCb) {
				throw new VMException(newFrame, String.format(
						"Infinite loop detected in field initialiser for %s", 
						this));
			}
			newFrame = newFrame.getParent();
		}
	}

} //FieldImpl