/*******************************************************************************
 * Copyright (c) 2008 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - ATL Regular VM launcher
 *******************************************************************************/
package org.eclipse.m2m.atl.examples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.m2m.atl.drivers.emf4atl.ASMEMFModel;
import org.eclipse.m2m.atl.engine.AtlEMFModelHandler;
import org.eclipse.m2m.atl.engine.AtlLauncher;
import org.eclipse.m2m.atl.engine.AtlModelHandler;
import org.eclipse.m2m.atl.engine.vm.nativelib.ASMModel;

/**
 * Launches an ATL transformation using the Regular vm.
 * 
 * @author William Piers <a
 *         href="mailto:william.piers@obeo.fr">william.piers@obeo.fr</a>
 */
public class VMLauncher {

	/**
	 * A method launching the VM
	 * 
	 * @param asmURL
	 *            the URL of the .asm file (the compiled transformation)
	 * @param input
	 *            the map of the input models [Model name, Metamodel name]
	 * @param output
	 *            the map of the input models [Model name, Metamodel name]
	 * @param path
	 *            the map of the model paths [(Meta)Model name, path]
	 * @param libsFromConfig
	 *            the map of the libraries [library name, path]
	 * @param options
	 *            the map of the vm options
	 * @param modelHandler
	 *            the map of the model handlers [Metamodel, EMF|UML2|MDR]
	 * @param superimpose
	 *            the list of the superimposed modules
	 * @return the execution time
	 * @throws Exception
	 */
	public static double launch(URL asmURL, Map input, Map output, Map path,
			Map libsFromConfig, Map options, Map modelHandler, List superimpose)
			throws Exception {
		boolean checkSameModel = false;
		// model handler
		Map atlModelHandler = new HashMap();
		for (Iterator i = modelHandler.keySet().iterator(); i.hasNext();) {
			String currentModelHandler = (String) modelHandler.get(i.next());
			if (!atlModelHandler.containsKey(currentModelHandler)
					&& !currentModelHandler.equals("")) //$NON-NLS-1$
				atlModelHandler.put(currentModelHandler, AtlModelHandler
						.getDefault(currentModelHandler));
		}

		Collection toDispose = new HashSet();

		Map inModel = getSourceModels(input, path, modelHandler,
				atlModelHandler, checkSameModel, toDispose);
		Map outModel = getTargetModels(output, path, modelHandler,
				atlModelHandler, inModel, checkSameModel, toDispose);

		Map models = new HashMap();

		for (Iterator i = inModel.keySet().iterator(); i.hasNext();) {
			String mName = (String) i.next();
			models.put(mName, inModel.get(mName));
		}

		for (Iterator i = outModel.keySet().iterator(); i.hasNext();) {
			String mName = (String) i.next();
			models.put(mName, outModel.get(mName));
		}

		long startTime = System.currentTimeMillis();
		AtlLauncher.getDefault().launch(asmURL, libsFromConfig, models,
				Collections.EMPTY_MAP, superimpose, options);
		long endTime = System.currentTimeMillis();

		// save output models
		for (Iterator i = output.keySet().iterator(); i.hasNext();) {
			String mName = (String) i.next();
			ASMModel currentOutModel = (ASMModel) outModel.get(mName);
			String newPath = URI.createFileURI((String) path.get(mName))
					.toString();
			AtlModelHandler.getHandler(currentOutModel).saveModel(
					currentOutModel, newPath);
		}

		for (Iterator i = toDispose.iterator(); i.hasNext();) {
			ASMModel model = (ASMModel) i.next();
			AtlModelHandler.getHandler(model).disposeOfModel(model);
		}

		return (endTime - startTime) / 1000.;
	}

	/**
	 * Get source models of a transformation.
	 * 
	 * @param arg
	 *            the (model_name,metamodel_name) tuples to load
	 * @param path
	 *            the paths of each model
	 * @param modelHandler
	 * @param atlModelHandler
	 * @param checkSameModel
	 * @param toDispose
	 * @return a map containing loaded ASMModels
	 * @throws CoreException
	 */
	private static Map getSourceModels(Map arg, Map path, Map modelHandler,
			Map atlModelHandler, boolean checkSameModel, Collection toDispose)
			throws CoreException {
		Map toReturn = new HashMap();
		try {
			for (Iterator i = arg.keySet().iterator(); i.hasNext();) {
				String mName = (String) i.next();
				String mmName = (String) arg.get(mName);

				AtlModelHandler amh = (AtlModelHandler) atlModelHandler
						.get(modelHandler.get(mmName));
				ASMModel mofmm = amh.getMof();
				toReturn.put("%" + modelHandler.get(mmName), mofmm); //$NON-NLS-1$
				mofmm.setIsTarget(false);
				ASMModel inputModel;
				if (((String) path.get(mmName)).startsWith("#")) { //$NON-NLS-1$
					toReturn.put(mmName, mofmm);
					inputModel = (ASMModel) toReturn.get(mName);
					if (inputModel == null)
						inputModel = loadModel(amh, mName, mofmm, (String) path
								.get(mName), toDispose);
				} else {
					ASMModel inputMetaModel = (ASMModel) toReturn.get(mmName);
					if (inputMetaModel == null) {
						inputMetaModel = loadModel(amh, mmName, mofmm,
								(String) path.get(mmName), toDispose);
						toReturn.put(mmName, inputMetaModel);
					}
					inputMetaModel.setIsTarget(false);
					inputModel = loadModel(amh, mName, inputMetaModel,
							(String) path.get(mName), toDispose);
				}
				inputModel.setIsTarget(false);
				if (inputModel instanceof ASMEMFModel)
					((ASMEMFModel) inputModel)
							.setCheckSameModel(checkSameModel);
				toReturn.put(mName, inputModel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	/**
	 * Get target models of a transformation.
	 * 
	 * @param arg
	 *            the (model_name,metamodel_name) tuples to load
	 * @param path
	 *            the paths of each model
	 * @param modelHandler
	 *            the model handlers
	 * @param atlModelHandler
	 * @param checkSameModel
	 * @param toDispose
	 * @return a map containing loaded ASMModels
	 * @throws CoreException
	 */
	private static Map getTargetModels(Map arg, Map path, Map modelHandler,
			Map atlModelHandler, Map input, boolean checkSameModel,
			Collection toDispose) throws CoreException {
		Map toReturn = new HashMap();
		try {
			for (Iterator i = arg.keySet().iterator(); i.hasNext();) {
				String mName = (String) i.next();
				String mmName = (String) arg.get(mName);

				AtlModelHandler amh = (AtlModelHandler) atlModelHandler
						.get(modelHandler.get(mmName));
				ASMModel mofmm = amh.getMof();
				mofmm.setIsTarget(false);
				ASMModel outputModel;

				if (((String) path.get(mmName)).startsWith("#")) {//$NON-NLS-1$
					if (input.get(mmName) == null)
						toReturn.put(mmName, mofmm);
					outputModel = (ASMModel) toReturn.get(mName);
					if (outputModel == null)
						outputModel = newModel(amh, mName, mofmm, (String) path
								.get(mName), toDispose);
				} else {
					ASMModel outputMetaModel = (ASMModel) input.get(mmName);
					if (outputMetaModel == null)
						outputMetaModel = (ASMModel) toReturn.get(mmName);
					if (outputMetaModel == null) {
						outputMetaModel = loadModel(amh, mmName, mofmm,
								(String) path.get(mmName), toDispose);
						toReturn.put(mmName, outputMetaModel);
					}
					outputMetaModel.setIsTarget(false);
					outputModel = newModel(amh, mName, outputMetaModel,
							(String) path.get(mName), toDispose);
				}
				outputModel.setIsTarget(true);
				if (outputModel instanceof ASMEMFModel)
					((ASMEMFModel) outputModel)
							.setCheckSameModel(checkSameModel);
				toReturn.put(mName, outputModel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	/**
	 * Load a model.
	 * 
	 * @param amh
	 *            the model handler to use
	 * @param mName
	 *            the name of the model
	 * @param metamodel
	 *            the metamodel
	 * @param path
	 *            the path of the model
	 * @param toDispose
	 * @returnn the loaded model
	 * @throws IOException
	 * @throws CoreException
	 * @throws FileNotFoundException
	 */
	private static ASMModel loadModel(AtlModelHandler amh, String mName,
			ASMModel metamodel, String path, Collection toDispose)
			throws IOException, CoreException, FileNotFoundException {
		ASMModel ret = null;

		if (amh instanceof AtlEMFModelHandler) {
			if (path.startsWith("uri:")) { //$NON-NLS-1$
				ret = ((AtlEMFModelHandler) amh).loadModel(mName, metamodel,
						path);
				// this model should not be disposed of because we did not load
				// it
			} else {
				ret = ((AtlEMFModelHandler) amh).loadModel(mName, metamodel,
						URI.createFileURI(path));
				toDispose.add(ret);
			}
		}

		return ret;
	}

	/**
	 * Creates a new model.
	 * 
	 * @param amh
	 *            the model handler to use
	 * @param mName
	 *            the name of the model
	 * @param metamodel
	 *            the metamodel
	 * @param path
	 *            the path of the new model
	 * @param toDispose
	 * @return the new ASMModel
	 * @throws IOException
	 */
	private static ASMModel newModel(AtlModelHandler amh, String mName,
			ASMModel metamodel, String path, Collection toDispose)
			throws IOException {
		ASMModel ret = amh.newModel(mName, URI.createFileURI(path).toString(),
				metamodel);
		toDispose.add(ret);
		return ret;
	}

}