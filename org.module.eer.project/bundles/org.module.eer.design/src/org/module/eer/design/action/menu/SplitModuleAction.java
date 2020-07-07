package org.module.eer.design.action.menu;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;
import org.module.eer.jenetics.action.CreateProjectMEERModule;
import org.module.eer.jenetics.split.impl.HierarchicalModuleEERJenetics;
import org.module.eer.jenetics.split.stats.MEERStatistics;
import org.module.eer.mm.moduleeer.MEERModel;
import org.module.eer.mm.moduleeer.Module;


public class SplitModuleAction implements IExternalJavaAction {

	public SplitModuleAction() {
		// Do Nothing
	}

	@Override
	public void execute(Collection<? extends EObject> selections, Map<String, Object> parameters) {
		Object objectModule = parameters.get("module");
		if (objectModule instanceof Module) {
			Module module = (Module) objectModule;
			Session session = SessionManager.INSTANCE.getSession(module);
			String projectName = getProjectBySession(session);
			//Split Module
			Map<MEERModel, MEERStatistics> mapOfModuleEER = new HierarchicalModuleEERJenetics().splitMEERModelStatistics(module);								
			//Create Modeling Project with new MEERModel Split Diagram
			new CreateProjectMEERModule(mapOfModuleEER, projectName + ".split", 
					module.eResource()).createProject();
		}			
	}	

	private String getProjectBySession(Session session) {
		Resource sessionResource = session.getSessionResource();		
		return sessionResource.getURI().segment(1);
	}

	@Override
	public boolean canExecute(Collection<? extends EObject> selections) {
		return true;
	}
}