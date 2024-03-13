package org.module.eer.jenetics.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.swing.JFileChooser;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.module.eer.jenetics.split.impl.HierarchicalModuleEERJenetics;
import org.module.eer.mm.moduleeer.MEERModel;
import org.module.eer.mm.moduleeer.ModuleeerPackage;

public class BatchExecute {
	public static void main(String[] args) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("moduleeer", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		// Register the XMI resource factory
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		// Register the moduleeer package
		EPackage moduleeerPackage = ModuleeerPackage.eINSTANCE;
		resourceSet.getPackageRegistry().put(moduleeerPackage.getNsURI(), moduleeerPackage);

		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.showOpenDialog(null);
		File[] files = chooser.getSelectedFiles();
		HierarchicalModuleEERJenetics jen = new HierarchicalModuleEERJenetics();
		for (File file : files) {
			
				if (file.isFile() && file.getName().endsWith(".moduleeer")) {
					// Read the XMI file

					Resource resource = resourceSet.getResource(URI.createFileURI(file.getAbsolutePath()), true);
					// Get the root element of the XMI model
					EObject root = resource.getContents().get(0);

					// Cast the root element to the generated Java class for the Ecore model
					MEERModel meer = (MEERModel) root;
					EList<MEERModel> result = jen.splitModules(meer.getModules().get(0));
					File directory = new File(
							file.getParentFile().getAbsolutePath() + "/" + file.getName() + "_result/");
					System.out.println(directory.getAbsolutePath());
					for (int i = 0; i < result.size(); i++) {
						MEERModel model = result.get(i);
						URI resourceURI = URI.createFileURI(
								directory.getAbsolutePath() + "/" + file.getName() + i + "." + ModuleeerPackage.eNAME);

						Resource newResource = resourceSet.createResource(resourceURI);
						newResource.getContents().add(model);
						try {
							newResource.save(Collections.EMPTY_MAP);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			
		}
	}
}
