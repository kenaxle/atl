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
package org.eclipse.m2m.atl.emftvm.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.m2m.atl.common.ATLLogger;
import org.eclipse.m2m.atl.emftvm.EmftvmFactory;
import org.eclipse.m2m.atl.emftvm.EmftvmPackage;
import org.eclipse.m2m.atl.emftvm.ExecEnv;
import org.eclipse.m2m.atl.emftvm.Metamodel;
import org.eclipse.m2m.atl.emftvm.Model;
import org.eclipse.m2m.atl.emftvm.trace.TracePackage;

/**
 * EMFTVM static utility methods.
 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
 */
public final class EMFTVMUtil {
	
	/**
	 * Native type namespace.
	 */
	public static final String NATIVE = "#native";

	/**
	 * Type namespace delimiter.
	 */
	public static final String NS_DELIM = "::";

	/**
	 * Type namespace matching pattern.
	 * @see #NS_DELIM
	 */
	public static final Pattern DELIM_PATTERN = Pattern.compile(NS_DELIM);

	/**
	 * Name of the "main" static parameterless operation.
	 */
	public static final String MAIN_OP_NAME = "main";

	/**
	 * Name if the XMI ID feature for {@link EObject}s contained in {@link XMIResource}s.
	 */
	public static final String XMI_ID_FEATURE = "__xmiID__";

	/**
	 * Cache used to store native Java methods.
	 * 
	 * @author <a href="mailto:frederic.jouault@univ-nantes.fr">Frederic Jouault</a>
	 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
	 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
	 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
	 */
	private static final WeakHashMap<Class<?>, Map<String, Method>> METHOD_CACHE = 
		new WeakHashMap<Class<?>, Map<String, Method>>();

	private static Metamodel ecoreMetamodel;
	private static Metamodel emfTvmMetamodel;
	private static Metamodel traceMetamodel;

	/**
	 * Not used.
	 */
	private EMFTVMUtil() {
	}

	/**
	 * Finds the type with typename in the metamodel with modelname, or the native type.
	 * @param env the execution environment to search for the metamodel
	 * @param modelname the metamodel name
	 * @param typename the type name
	 * @return the type with typename in the metamodel with modelname, or the native type
	 * @throws ClassNotFoundException
	 */
	public static Object findType(final ExecEnv env, final String modelname, 
			final String typename) throws ClassNotFoundException {
		if (NATIVE.equals(modelname)) {
			final Matcher m = DELIM_PATTERN.matcher(typename);
			return Class.forName(m.replaceAll("."));
		} else {
			final Metamodel mm = env.getMetaModels().get(modelname);
			if (mm == null) {
				throw new IllegalArgumentException(String.format("Metamodel %s not found", modelname));
			}
			return mm.findType(typename);
		}
	
	}

	/**
	 * Returns the name of <code>type</code>, for printing.
	 * @param env the current {@link ExecEnv}
	 * @param type the type
	 * @return the name of <code>type</code>, for printing
	 */
	public static String getTypeName(final ExecEnv env, final Object type) {
		if (type instanceof EClass) {
			EClass eCls = (EClass)type;
			for (Map.Entry<String, Metamodel> mm : env.getMetaModels().entrySet()) {
				if (mm.getValue().getResource() == eCls.eResource()) {
					return mm.getKey() + '!' + eCls.getName();
				}
			}
			return ((EClass)type).getName();
		} else if (type instanceof Class<?>) {
			return NATIVE + '!' + ((Class<?>)type).getName();
		} else {
			return type.toString();
		}
	}

	/**
	 * Returns the names of <code>types</code>, for printing.
	 * @param env the current {@link ExecEnv}.
	 * @param types the types
	 * @return the names of <code>types</code>, for printing
	 */
	public static String getTypeNames(final ExecEnv env, final Collection<?> types) {
		final StringBuffer names = new StringBuffer();
		boolean notFirst = false;
		for (Object type : types) {
			if (notFirst) {
				names.append(", ");
			}
			names.append(getTypeName(env, type));
		}
		return names.toString();
	}

	/**
	 * Returns the type object to use for the registry.
	 * @param type the type object
	 * @return the type object to use for the registry
	 * @throws IllegalArgumentException if type is a primitive EMF type without instance class
	 */
	public static Object getRegistryType(final Object type) throws IllegalArgumentException {
		if (type instanceof EClassifier && !(type instanceof EClass)) {
			final Class<?> ic = ((EClassifier)type).getInstanceClass();
			if (ic == null) {
				throw new IllegalArgumentException(String.format("Primitive EMF type without instance class %s", type));
			}
			return ic;
		}
		return type;
	}

	/**
	 * Returns the singleton instance of the Ecore metamodel.
	 * @return the singleton instance of the Ecore metamodel
	 */
	public static Metamodel getEcoreMetamodel() {
		if (ecoreMetamodel == null) {
			ecoreMetamodel = EmftvmFactory.eINSTANCE.createMetamodel();
			ecoreMetamodel.setResource(EcorePackage.eINSTANCE.eResource());
		}
		return ecoreMetamodel;
	}

	/**
	 * Returns the singleton instance of the EMFTVM metamodel.
	 * @return the singleton instance of the EMFTVM metamodel
	 */
	public static Metamodel getEmfTvmMetamodel() {
		if (emfTvmMetamodel == null) {
			emfTvmMetamodel = EmftvmFactory.eINSTANCE.createMetamodel();
			emfTvmMetamodel.setResource(EmftvmPackage.eINSTANCE.eResource());
		}
		return emfTvmMetamodel;
	}

	/**
	 * Returns the singleton instance of the Trace metamodel.
	 * @return the singleton instance of the Trace metamodel
	 */
	public static Metamodel getTraceMetamodel() {
		if (traceMetamodel == null) {
			traceMetamodel = EmftvmFactory.eINSTANCE.createMetamodel();
			traceMetamodel.setResource(TracePackage.eINSTANCE.eResource());
		}
		return traceMetamodel;
	}

	/**
	 * Finds all instances of type in the registered input/inout models.
	 * @param env the current {@link ExecEnv}
	 * @param type the type
	 * @return all instances of type in the registered input/inout models
	 */
	public static LazyList<EObject> findAllInstances(final ExecEnv env, final EClass type) {
		LazyList<EObject> allInst = new LazyList<EObject>();
		for (Model model : env.getInputModels().values()) {
			allInst = allInst.union(model.allInstancesOf(type));
		}
		for (Model model : env.getInoutModels().values()) {
			allInst = allInst.union(model.allInstancesOf(type));
		}
		return allInst;
	}

	/**
	 * Finds all instances of type in the given model.
	 * @param env the current {@link ExecEnv}
	 * @param type the type
	 * @param modelname the model name
	 * @return all instances of type in the given model
	 */
	public static LazyList<EObject> findAllInstIn(final ExecEnv env, final EClass type, final Object modelname) {
		Model model = env.getInputModels().get(modelname);
		if (model == null) {
			model = env.getInoutModels().get(modelname);
		}
		if (model == null) {
			throw new IllegalArgumentException(String.format("No input/inout model found with name %s", modelname));
		}
		return model.allInstancesOf(type);
	}

	/**
	 * Offers an alternative to the default <code>toString()</code> method.
	 * Uses <code>env</code> to determine the containing model of types.
	 * Compensates for {@link EObject}'s notoriously bad <code>toString()</code>.
	 * @param object
	 * @param env
	 * @return the string representation of <code>object</code>.
	 */
	public static String toPrettyString(final Object object, final ExecEnv env) {
		if (object instanceof EClass) {
			final StringBuffer sb = new StringBuffer();
			if (env != null) {
				final Model model = env.getModelOf((EClass) object);
				if (model != null) {
					sb.append(env.getModelID(model));
					sb.append('!');
				}
			}
			sb.append(((EClass) object).getName());
			return sb.toString();
		} else if (object instanceof EObject) { // EObjects have a notoriously bad toString()
			final StringBuffer buf = new StringBuffer();
			final EObject eo = (EObject) object;
			EStructuralFeature sf = eo.eClass().getEIDAttribute();
			if (sf == null) {
				sf = eo.eClass().getEStructuralFeature("name");
			}
			if (sf != null && eo.eGet(sf) != null) {
				buf.append(eo.eGet(sf));
			} else {
				buf.append(Integer.toHexString(eo.hashCode()));
			}
			buf.append(':');
			buf.append(toPrettyString(eo.eClass(), env));
			return buf.toString();
		} else if (object instanceof Class<?>) {
			return ((Class<?>) object).getName();
		} else if (object != null) {
			return object.toString();
		} else {
			return "null";
		}
	}

	/**
	 * Offers an alternative to the default <code>toString()</code> method.
	 * Uses <code>env</code> to determine the containing model of types.
	 * Compensates for {@link EObject}'s notoriously bad <code>toString()</code>.
	 * @param coll
	 * @param env
	 * @return the string representation of <code>coll</code>.
	 */
	public static String toPrettyString(final Collection<?> coll, final ExecEnv env) {
		final StringBuffer sb = new StringBuffer();
		sb.append('[');
		boolean first = true;
		for (Object object : coll) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(EMFTVMUtil.toPrettyString(object, env));
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Offers an alternative to the default <code>toString()</code> method.
	 * Uses <code>env</code> to determine the containing model of types.
	 * Compensates for {@link EObject}'s notoriously bad <code>toString()</code>.
	 * @param array
	 * @param env
	 * @return the string representation of <code>coll</code>.
	 */
	public static <T> String toPrettyString(final T[] array, final ExecEnv env) {
		final StringBuffer sb = new StringBuffer();
		sb.append('[');
		boolean first = true;
		for (Object object : array) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(EMFTVMUtil.toPrettyString(object, env));
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Retrieves the value of <code>eo.sf</code>.
	 * Checks that <code>eo</code> is not in an output model.
	 * @param env the current {@link ExecEnv}
	 * @param eo the model element to retrieve the value from
	 * @param sf the structural feature to retrieve the value from
	 * @return the value of <code>eo.sf</code>.
	 */
	public static Object get(final ExecEnv env, final EObject eo, final EStructuralFeature sf) {
		if (env.getOutputModelOf(eo) != null) {
			throw new IllegalArgumentException(String.format(
					"Cannot read properties of %s, as it is contained in an output model",
					toPrettyString(eo, env)));
		}
		return uncheckedGet(env, eo, sf);
	}

	/**
	 * Retrieves the value of <code>eo.sf</code>.
	 * @param env the current {@link ExecEnv}
	 * @param eo the model element to retrieve the value from
	 * @param sf the structural feature to retrieve the value from
	 * @return the value of <code>eo.sf</code>.
	 */
	public static Object uncheckedGet(final ExecEnv env, final EObject eo, final EStructuralFeature sf) {
		return emf2vm(env, eo, eo.eGet(sf));
	}

	/**
	 * Converts <code>value</code> to an EMFTVM value.
	 * @param env the current {@link ExecEnv}
	 * @param eo the {@link EObject} from which the value was obtained
	 * @param value the EMF value to convert
	 * @return the EMFTVM value
	 */
	@SuppressWarnings("unchecked")
	private static Object emf2vm(final ExecEnv env, final EObject eo, final Object value) {
		if (value instanceof Enumerator) {
			return new EnumLiteral(value.toString());
		} else if (value instanceof EList<?>) {
			final EnumConversionList converted = new EnumConversionList((EList<Object>)value);
			if (eo != null && env.getInoutModelOf(eo) != null) {
				//Copy list for inout models
				converted.cache();
			}
			return converted;
		}
		assert eo == null || !(value instanceof Collection<?>); // All EMF collections should be ELists
		return value;
	}

	/**
	 * Sets the <code>value</code> of <code>eo.sf</code>.
	 * @param env the current {@link ExecEnv}
	 * @param eo the model element to set the value for
	 * @param sf the structural feature to set the value for
	 * @param value the value to set
	 */
	public static void set(final ExecEnv env, final EObject eo, final EStructuralFeature sf, 
			final Object value) {
		if (!sf.isChangeable()) {
			throw new IllegalArgumentException(String.format(
					"Field %s::%s is not changeable", 
					toPrettyString(sf.getEContainingClass(), env), sf.getName()));
		}
		if (env.getInputModelOf(eo) != null) {
			throw new IllegalArgumentException(String.format(
					"Cannot set properties of %s, as it is contained in an input model",
					toPrettyString(eo, env)));
		}
		if (sf.isMany()) {
			if (!(value instanceof Collection<?>)) {
				throw new IllegalArgumentException(String.format(
						"Cannot assign %s to multi-valued field %s::%s",
						value, sf.getEContainingClass().getName(), sf.getName()));
			}
			EMFTVMUtil.setMany(env, eo, sf, (Collection<?>)value);
		} else {
			EMFTVMUtil.setSingle(env, eo, sf, value, -1);
		}
		assert eo.eResource() != null;
	}

	/**
	 * Adds the <code>value</code> of <code>eo.sf</code>.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 * @param index the insertion index (-1 for end)
	 */
	public static void add(final ExecEnv env, final EObject eo, final EStructuralFeature sf, 
			final Object value, final int index) {
		if (!sf.isChangeable()) {
			throw new IllegalArgumentException(String.format(
					"Field %s::%s is not changeable", 
					toPrettyString(sf.getEContainingClass(), env), sf.getName()));
		}
		if (env.getInputModelOf(eo) != null) {
			throw new IllegalArgumentException(String.format(
					"Cannot add properties to %s, as it is contained in an input model",
					toPrettyString(eo, env)));
		}
		if (sf.isMany()) {
			if (value instanceof Collection<?>) {
				EMFTVMUtil.addMany(env, eo, sf, (Collection<?>)value, index);
			} else {
				EMFTVMUtil.addMany(env, eo, sf, value, index);
			}
		} else {
			if (eo.eIsSet(sf)) {
				throw new IllegalArgumentException(String.format("Cannot add more than one value to %s::%s", 
						toPrettyString(eo.eClass(), env), sf.getName()));
			}
			EMFTVMUtil.setSingle(env, eo, sf, value, index);
		}
		assert eo.eResource() != null;
	}

	/**
	 * Removes the <code>value</code> from <code>eo.sf</code>.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 */
	public static void remove(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Object value) {
		if (!sf.isChangeable()) {
			throw new IllegalArgumentException(String.format(
					"Field %s::%s is not changeable", 
					toPrettyString(sf.getEContainingClass(), env), sf.getName()));
		}
		if (env.getInputModelOf(eo) != null) {
			throw new IllegalArgumentException(String.format(
					"Cannot remove properties of %s, as it is contained in an input model",
					toPrettyString(eo, env)));
		}
		final EClassifier sfType = sf.getEType();
		if (sf.isMany()) {
			if (value instanceof Collection<?>) {
				EMFTVMUtil.removeMany(env, eo, sf, (Collection<?>)value);
			} else {
				EMFTVMUtil.removeMany(env, eo, sf, value);
			}
		} else {
			final Object oldValue = eo.eGet(sf);
			if (sfType instanceof EEnum && value instanceof EnumLiteral) {
				final EEnum eEnum = (EEnum)sfType;
				if (oldValue != null && oldValue.equals(((EnumLiteral)value).getEnumerator(eEnum))) {
					EMFTVMUtil.setSingle(env, eo, sf, sf.getDefaultValue(), -1);
				}
			} else {
				if (oldValue == null ? value == null : oldValue.equals(value)) {
					EMFTVMUtil.setSingle(env, eo, sf, sf.getDefaultValue(), -1);
				}
			}
		}
		assert eo.eResource() != null;
	}

	/**
	 * Sets the <code>value</code> of <code>eo.sf</code>.
	 * Assumes <code>sf</code> has a multiplicity &lt;= 1.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 * @param index the insertion index (-1 for end)
	 */
	private static void setSingle(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Object value, final int index) {
		assert !sf.isMany();
		if (index > 0) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
		final EClassifier sfType = sf.getEType();
		final boolean allowInterModelReferences = isAllowInterModelReferences(env, eo);
		if (sfType instanceof EEnum) {
			final EEnum eEnum = (EEnum)sfType;
			if (value instanceof EnumLiteral) {
				eo.eSet(sf, ((EnumLiteral)value).getEnumerator(eEnum));
			} else {
				eo.eSet(sf, value);
			}
		} else if (sf instanceof EReference) {
			final EReference ref = (EReference)sf;
			final boolean isContainment = ref.isContainment();
			final boolean isContainer = ref.isContainer();
			if (checkValue(env, eo, ref, value, allowInterModelReferences)) {
				if (isContainment) { // Restore eResource for old value before clearing
					final EObject oldValue = (EObject)eo.eGet(sf);
					if (oldValue != null) {
						eo.eResource().getContents().add(oldValue);
					}
				} else if (isContainer) { // Restore eResource for eo before clearing
					final EObject oldValue = (EObject)eo.eGet(sf);
					if (oldValue != null) {
						oldValue.eResource().getContents().add(eo);
					}
				}
				eo.eSet(sf, value);
				if (isContainment && value instanceof EObject) {
					// Remove value from its resource if it is contained
					((EObject)value).eResource().getContents().remove(value);
					assert ((EObject)value).eContainer() == eo;
					assert ((EObject)value).eResource() == eo.eResource();
				} else if (isContainer && value instanceof EObject) {
					// Remove eo from its resource if it is contained
					eo.eResource().getContents().remove(eo);
					assert eo.eContainer() == value;
					assert ((EObject)value).eResource() == eo.eResource();
				}
			}
		} else {
			eo.eSet(sf, value);
		}
	}

	/**
	 * Sets the <code>value</code> of <code>eo.sf</code>.
	 * Assumes <code>sf</code> has a multiplicity &gt; 1.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 * @param index the insertion index (-1 for end)
	 */
	@SuppressWarnings("unchecked")
	private static void setMany(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Collection<?> value) {
		assert sf.isMany();
		final EList<Object> values = (EList<Object>)eo.eGet(sf);
		if (!values.isEmpty()) {
			if (sf instanceof EReference && ((EReference)sf).isContainment()) {
				// Restore eResource for each value before clearing
				final EList<EObject> resContents = eo.eResource().getContents();
				resContents.addAll((EList<? extends EObject>)values);
				// Adding values to the resource should have cleared values already - apparently only happens for generated metamodels
				//assert values.isEmpty();
			}
			values.clear();
		}
		addMany(env, eo, sf, value, -1);
	}

	/**
	 * Adds <code>value</code> to <code>eo.sf</code>.
	 * Assumes <code>sf</code> has a multiplicity &gt; 1.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 * @param index the insertion index (-1 for end)
	 */
	@SuppressWarnings("unchecked")
	private static void addMany(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Object value, final int index) {
		assert sf.isMany();
		final EClassifier sfType = sf.getEType();
		final EList<Object> values = (EList<Object>)eo.eGet(sf); // All EMF collections are ELists
		if (sfType instanceof EEnum) {
			addEnumValue((EEnum)sfType, values, value, index);
		} else if (sf instanceof EReference) {
			final EReference ref = (EReference)sf;
			addRefValue(env, ref, eo, values, value, index, 
					isAllowInterModelReferences(env, eo));
		} else if (index > -1) {
			values.add(index, value);
		} else {
			values.add(value);
		}
	}

	/**
	 * Adds all <code>value</code> elements to <code>eo.sf</code>.
	 * Assumes <code>sf</code> has a multiplicity &gt; 1.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 * @param index the insertion index (-1 for end)
	 */
	@SuppressWarnings("unchecked")
	private static void addMany(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Collection<?> value, final int index) {
		assert sf.isMany();
		final EClassifier sfType = sf.getEType();
		final EList<Object> values = (EList<Object>)eo.eGet(sf);
		if (sfType instanceof EEnum) {
			final EEnum eEnum = (EEnum)sfType;
			if (index > -1) {
				int currentIndex = index;
				for (Object v : value) {
					addEnumValue(eEnum, values, v, currentIndex++);
				}
			} else {
				for (Object v : value) {
					addEnumValue(eEnum, values, v, -1);
				}
			}
		} else if (sf instanceof EReference) {
			final EReference ref = (EReference)sf;
			final boolean allowInterModelReferences = isAllowInterModelReferences(env, eo);
			if (index > -1) {
				int currentIndex = index;
				for (Object v : value) {
					addRefValue(env, ref, eo, values, v, currentIndex++, allowInterModelReferences);
				}
			} else {
				for (Object v : value) {
					addRefValue(env, ref, eo, values, v, -1, allowInterModelReferences);
				}
			}
		} else if (index > -1) {
			values.addAll(index, value);
		} else {
			values.addAll(value);
		}
	}

	/**
	 * Removes the <code>value</code> from <code>eo.sf</code>.
	 * Assumes <code>sf</code> has a multiplicity &gt; 1.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	private static void removeMany(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Object value) {
		assert sf.isMany();
		final EClassifier sfType = sf.getEType();
		final EList<Object> values = (EList<Object>)eo.eGet(sf);
		if (sfType instanceof EEnum) {
			final EEnum eEnum = (EEnum)sfType;
			removeEnumValue(eEnum, values, value);
		} else if (sf instanceof EReference) {
			final EReference ref = (EReference)sf;
			removeRefValue(ref, eo, values, value);
		} else {
			values.remove(value);
		}
	}

	/**
	 * Removes all elements of <code>value</code> from <code>eo.sf</code>.
	 * Assumes <code>sf</code> has a multiplicity &gt; 1.
	 * @param env
	 * @param eo
	 * @param sf
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	private static void removeMany(final ExecEnv env, final EObject eo, 
			final EStructuralFeature sf, final Collection<?> value) {
		assert sf.isMany();
		final EClassifier sfType = sf.getEType();
		final EList<Object> values = (EList<Object>)eo.eGet(sf);
		if (sfType instanceof EEnum) {
			final EEnum eEnum = (EEnum)sfType;
			for (Object v : value) {
				removeEnumValue(eEnum, values, v);
			}
		} else if (sf instanceof EReference) {
			final EReference ref = (EReference)sf;
			for (Object v : value) {
				removeRefValue(ref, eo, values, v);
			}
		} else {
			values.removeAll(value);
		}
	}

	/**
	 * Adds <code>v</code> to <code>values</code>.
	 * Performs enumerator conversion.
	 * @param eEnum The enumeration type
	 * @param values
	 * @param v
	 * @param index the insertion index (-1 for end)
	 */
	private static void addEnumValue(final EEnum eEnum, 
			final EList<Object> values, final Object v, final int index) {
		final Object v2;
		if (v instanceof EnumLiteral) {
			v2 = ((EnumLiteral)v).getEnumerator(eEnum);
		} else {
			v2 = v;
		}
		if (index > -1) {
			values.add(index, v2);
		} else {
			values.add(v2);
		}
	}

	/**
	 * Removes <code>v</code> from <code>values</code>.
	 * Performs enumerator conversion.
	 * @param eEnum The enumeration type
	 * @param values
	 * @param v
	 */
	private static void removeEnumValue(final EEnum eEnum, 
			final EList<Object> values, final Object v) {
		if (v instanceof EnumLiteral) {
			values.remove(((EnumLiteral)v).getEnumerator(eEnum));
		} else {
			values.remove(v);
		}
	}

	/**
	 * Adds <code>v</code> to <code>values</code>.
	 * Performs constraint checking on <code>v</code>.
	 * @param env
	 * @param ref The reference type
	 * @param eo The object with <code>ref</code> set to <code>values</code>
	 * @param values
	 * @param v
	 * @param index the insertion index (-1 for end)
	 * @param allowInterModelReferences
	 */
	private static void addRefValue(final ExecEnv env, final EReference ref, final EObject eo,
			final EList<Object> values, final Object v, final int index,
			final boolean allowInterModelReferences) {
		if (checkValue(env, eo, ref, v, allowInterModelReferences)) {
			if (index > -1) {
				values.add(index, v);
			} else {
				values.add(v);
			}
			// Adding v to values should have updated (i.e. cleared) its resource if it is contained - apparently only happens for generated metamodels
			//assert !isContainment || (((EObject) v).eContainer() == eo && ((EObject) v).eResource() == eo.eResource());
			if (ref.isContainment() && v instanceof EObject) {
				((EObject)v).eResource().getContents().remove(v);
				assert ((EObject)v).eContainer() == eo;
				assert ((EObject)v).eResource() == eo.eResource();
			}
		}
	}

	/**
	 * Removes <code>v</code> from <code>values</code>.
	 * Performs constraint checking on <code>v</code>.
	 * @param ref The reference type
	 * @param eo The object with <code>ref</code> set to <code>values</code>
	 * @param values
	 * @param v
	 */
	private static void removeRefValue(final EReference ref, final EObject eo,
			final EList<Object> values, final Object v) {
		if (values.remove(v) && ref.isContainment() && v instanceof EObject) {
			eo.eResource().getContents().add((EObject)v);
			assert ((EObject)v).eContainer() == null;
			assert ((EObject)v).eResource() == eo.eResource();
		}
	}

	/**
	 * Checks whether the model containing <code>eo</code> allows inter-model references.
	 * @param env the {@link ExecEnv} in which to find the model.
	 * @param eo the model element to find the model for.
	 * @return <code>true</code> iff the model of <code>eo</code> allows inter-model references
	 */
	private static boolean isAllowInterModelReferences(final ExecEnv env, final EObject eo) {
		final Model eoModel = env.getModelOf(eo);
		if (eoModel != null) {
			return eoModel.isAllowInterModelReferences();
		} else {
			return true;
		}
	}

	/**
	 * Checks whether <code>value</code> may be assigned to <code>eo.ref</code>.
	 * @param env the current {@link ExecEnv}
	 * @param eo the model element to assign to
	 * @param ref the reference of the model element to assign to
	 * @param value the value to assign
	 * @param allowInterModelReferences whether to allow inter-model references
	 * @return <code>true</code> iff the value may be assigned
	 */
	private static boolean checkValue(final ExecEnv env, final EObject eo, final EReference ref, 
			final Object value, final boolean allowInterModelReferences) {
		if (value instanceof EObject) {
			assert eo.eResource() != null;
			final EObject ev = (EObject)value;
			if (eo.eResource() == ev.eResource() || ev.eResource() == null) {
				return true;
			}
			assert ev.eResource() != null;
			if (!allowInterModelReferences) {
				ATLLogger.warning(String.format(
						"Cannot set %s::%s to %s for %s: inter-model references are not allowed for this model",
						toPrettyString(ref.getEContainingClass(), env), 
						ref.getName(), 
						toPrettyString(value, env), 
						toPrettyString(eo, env)));
				return false;
			}
			if (ref.isContainer() || ref.isContainment()) {
				ATLLogger.warning(String.format(
						"Cannot set %s::%s to %s for %s: containment references cannot span across models",
						toPrettyString(ref.getEContainingClass(), env), 
						ref.getName(), 
						toPrettyString(value, env), 
						toPrettyString(eo, env)));
				return false;
			}
			final EReference opposite = ref.getEOpposite();
			if (opposite != null) {
				final Model evModel = env.getInputModelOf(ev);
				if (evModel != null) {
					ATLLogger.warning(String.format(
							"Cannot set %s::%s to %s for %s: inter-model reference with opposite causes changes in input model %s",
							toPrettyString(ref.getEContainingClass(), env), 
							ref.getName(), 
							toPrettyString(value, env), 
							toPrettyString(eo, env),
							env.getModelID(evModel)));
					return false;
				}
				if (!opposite.isMany()) {
					// Single-valued opposites cause changes in their respective opposite,
					// i.e. ref, which can belong to eo or another input model element.
					final Model oppositeModel = env.getInputModelOf((EObject)ev.eGet(opposite));
					if (oppositeModel != null) {
						ATLLogger.warning(String.format(
								"Cannot set %s::%s to %s for %s: inter-model reference with single-valued opposite causes changes in input model %s",
								toPrettyString(ref.getEContainingClass(), env), 
								ref.getName(), 
								toPrettyString(value, env), 
								toPrettyString(eo, env),
								env.getModelID(oppositeModel)));
						return false;
					}
				}
			}
		}
		return true; // any type errors can be delegated to EMF
	}

	/**
	 * Retrieves the types of <code>args</code>.
	 * @param args
	 * @return the types of <code>args</code>
	 */
	public static EList<Object> getArgumentTypes(final Object[] args) {
		final EList<Object> argTypes = new BasicEList<Object>(args.length);
		for (Object arg : args) {
			argTypes.add(getArgumentType(arg));
		}
		return argTypes;
	}

	/**
	 * Retrieves the type of <code>arg</code>.
	 * @param arg
	 * @return the type of <code>arg</code>
	 */
	public static Object getArgumentType(final Object arg) {
		if (arg instanceof EObject) {
			return ((EObject)arg).eClass();
		} else if (arg != null) {
			return arg.getClass();
		}
		// null is an instance of Void for the purpose of our multi-method semantics
		return Void.TYPE;
	}

	/**
	 * Invokes native Java method <code>opname</code> on <code>self</code> with arguments <code>args</code>.
	 * @param frame the current stack frame
	 * @param self the object on which to invoke the method
	 * @param opname the method name
	 * @param args the method arguments
	 * @return the method result
	 */
	public static Object invokeNative(final StackFrame frame, final Object self, 
			final String opname, final Object[] args) {
		final ExecEnv env = frame.getEnv();
		final Class<?> type = self.getClass();
		final Class<?>[] argClasses = EMFTVMUtil.getArgumentClasses(args);
		final Method method = EMFTVMUtil.findNativeMethod(type, opname, argClasses, false);
		if (method != null) {
			final StackFrame subFrame = frame.getSubFrame(method, args);
			try {
				final EObject eo = self instanceof EObject ? (EObject)self : null;
				return emf2vm(env, eo, method.invoke(self, args));
			} catch (InvocationTargetException e) {
				final Throwable target = e.getTargetException();
				if (target instanceof VMException) {
					throw (VMException)target;
				} else {
					throw new VMException(subFrame, target);
				}
			} catch (VMException e) {
				throw e;
			} catch (Exception e) {
				throw new VMException(subFrame, e);
			}
		}
		throw new UnsupportedOperationException(String.format("%s::%s(%s)", 
				EMFTVMUtil.getTypeName(env, type), opname, EMFTVMUtil.getTypeNames(env, getArgumentTypes(args))));
	}

	/**
	 * Invokes static native Java method <code>opname</code> with arguments <code>args</code>.
	 * @param frame the current stack frame
	 * @param type the class in which the static method is defined
	 * @param opname the method name
	 * @param args the method arguments
	 * @return the method result
	 */
	public static Object invokeNativeStatic(final StackFrame frame, final Class<?> type, 
			final String opname, final Object[] args) {
		final ExecEnv env = frame.getEnv();
		final Class<?>[] argClasses = EMFTVMUtil.getArgumentClasses(args);
		final Method method = EMFTVMUtil.findNativeMethod(type, opname, argClasses, false);
		if (method != null) {
			final StackFrame subFrame = frame.getSubFrame(method, args);
			try {
				return emf2vm(env, null, method.invoke(args));
			} catch (InvocationTargetException e) {
				final Throwable target = e.getTargetException();
				if (target instanceof VMException) {
					throw (VMException)target;
				} else {
					throw new VMException(subFrame, target);
				}
			} catch (VMException e) {
				throw e;
			} catch (Exception e) {
				throw new VMException(subFrame, e);
			}
		}
		throw new UnsupportedOperationException(String.format("static %s::%s(%s)", 
				EMFTVMUtil.getTypeName(env, type), opname, EMFTVMUtil.getTypeNames(env, getArgumentTypes(args))));
	}

	/**
	 * Invokes native Java super-method <code>opname</code> on <code>self</code> with arguments <code>args</code>.
	 * @param frame the current stack frame
	 * @param context the execution context class of the invoking operation
	 * @param self the object on which to invoke the method
	 * @param opname the method name
	 * @param args the method arguments
	 * @return the method result
	 */
	public static Object invokeNativeSuper(final StackFrame frame, final Class<?> context, 
			final Object self, final String opname, final Object[] args) {
		final ExecEnv env = frame.getEnv();
		final Class<?> type = self.getClass();
		assert context.isAssignableFrom(type);
		final Class<?>[] argClasses = EMFTVMUtil.getArgumentClasses(args);
		final Method method = EMFTVMUtil.findNativeMethod(context.getSuperclass(), opname, argClasses, false);
		if (method != null) {
			final StackFrame subFrame = frame.getSubFrame(method, args);
			try {
				final EObject eo = self instanceof EObject ? (EObject)self : null;
				return emf2vm(env, eo, method.invoke(self, args));
			} catch (InvocationTargetException e) {
				final Throwable target = e.getTargetException();
				if (target instanceof VMException) {
					throw (VMException)target;
				} else {
					throw new VMException(subFrame, target);
				}
			} catch (VMException e) {
				throw e;
			} catch (Exception e) {
				throw new VMException(subFrame, e);
			}
		}
		throw new UnsupportedOperationException(String.format("super %s::%s(%s)", 
				EMFTVMUtil.getTypeName(env, type), opname, EMFTVMUtil.getTypeNames(env, getArgumentTypes(args))));
	}

	/**
	 * Looks for a native Java method.
	 * 
	 * @param caller
	 *            The class of the method
	 * @param name
	 *            The method name
	 * @param argumentTypes
	 *            The types of all arguments
	 * @param isStatic
	 *            Whether to look for a static method or not
	 * @return the method if found, null otherwise
	 * @author <a href="mailto:frederic.jouault@univ-nantes.fr">Frederic Jouault</a>
	 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
	 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
	 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
	 */
	//TODO implement multi-methods in ExecEnv
	private static Method findNativeMethod(final Class<?> context, final String opname, 
			final Class<?>[] argTypes, final boolean isStatic) {
		if (context == Void.TYPE) {
			return null; // Java methods cannot be invoked on null, or defined on Void
		}
	
		final String sig = getMethodSignature(opname, argTypes, isStatic);
		Method ret = findCachedMethod(context, sig);
		if (ret != null) {
			return ret;
		}
	
		final Method[] methods = context.getDeclaredMethods();
		for (int i = 0; i < (methods.length) && (ret == null); i++) {
			Method method = methods[i];
			if ((Modifier.isStatic(method.getModifiers()) == isStatic) && method.getName().equals(opname)) {
				Class<?>[] pts = method.getParameterTypes();
				if (pts.length == argTypes.length) {
					boolean ok = true;
					for (int j = 0; (j < pts.length) && ok; j++) {
						if (argTypes[j] == EnumLiteral.class && Enumerator.class.isAssignableFrom(pts[j])) {
							continue;
						}
						if (!pts[j].isAssignableFrom(argTypes[j])) {
							if (pts[j] == boolean.class) ok = argTypes[j] == Boolean.class;
							else if (pts[j] == int.class) ok = argTypes[j] == Integer.class;
							else if (pts[j] == char.class) ok = argTypes[j] == Character.class;
							else if (pts[j] == long.class) ok = argTypes[j] == Long.class;
							else if (pts[j] == float.class) ok = argTypes[j] == Float.class;
							else if (pts[j] == double.class) ok = argTypes[j] == Double.class;
							else ok = argTypes[j] == Void.TYPE; // any type
						}
					}
					if (ok) {
						ret = method;
					}
				}
			}
		}
	
		if ((ret == null) && (context.getSuperclass() != null)) {
			ret = findNativeMethod(context.getSuperclass(), opname, argTypes, isStatic);
		}
	
		cacheMethod(context, sig, ret);
	
		return ret;
	}

	/**
	 * Find a method in the cache.
	 * 
	 * @param caller
	 *            The class of the method
	 * @param signature
	 *            The method signature
	 * @return the method
	 * @author <a href="mailto:frederic.jouault@univ-nantes.fr">Frederic Jouault</a>
	 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
	 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
	 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
	 */
	private static Method findCachedMethod(Class<?> caller, String signature) {
		Method ret = null;
		Map<String, Method> sigMap = METHOD_CACHE.get(caller);
		if (sigMap != null) {
			ret = sigMap.get(signature);
		}
		return ret;
	}

	/**
	 * Stores a method in a cache.
	 * 
	 * @param caller
	 *            The class of the method
	 * @param signature
	 *            The method signature
	 * @param method
	 *            The method to store
	 * @author <a href="mailto:frederic.jouault@univ-nantes.fr">Frederic Jouault</a>
	 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
	 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
	 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
	 */
	private static void cacheMethod(Class<?> caller, String signature, Method method) {
		synchronized (METHOD_CACHE) {
			Map<String, Method> sigMap = METHOD_CACHE.get(caller);
			if (sigMap == null) {
				sigMap = new HashMap<String, Method>();
				METHOD_CACHE.put(caller, sigMap);
			}
			sigMap.put(signature, method);
		}
	}

	/**
	 * Generates a String signature to store methods.
	 * 
	 * @param name
	 * @param argumentTypes
	 * @param isStatic
	 * @return The method signature
	 * @author <a href="mailto:frederic.jouault@univ-nantes.fr">Frederic Jouault</a>
	 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
	 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
	 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
	 */
	private static String getMethodSignature(final String name, final Class<?>[] argumentTypes, 
			final boolean isStatic) {
		final StringBuffer sig = new StringBuffer();
		if (isStatic) {
			sig.append("static ");
		}
		sig.append(name);
		sig.append('(');
		for (int i = 0; i < argumentTypes.length; i++) {
			if (i > 0) {
				sig.append(',');
			}
			sig.append(argumentTypes[i].getName());
		}
		sig.append(')');
		return sig.toString();
	}

	/**
	 * Retrieves the classes of <code>args</code>.
	 * @param args
	 * @return the classes of <code>args</code>
	 */
	private static Class<?>[] getArgumentClasses(final Object[] args) {
		final Class<?>[] argTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = args[i] == null ? Void.TYPE : args[i].getClass();
		}
		return argTypes;
	}

}