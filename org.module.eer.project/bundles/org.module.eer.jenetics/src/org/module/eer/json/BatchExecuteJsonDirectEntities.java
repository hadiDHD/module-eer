package org.module.eer.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.module.eer.jenetics.split.impl.CompareModuleEERJenetics2;
import org.module.eer.mm.moduleeer.EntityType;
import org.module.eer.mm.moduleeer.MEERModel;
import org.module.eer.mm.moduleeer.ModularizableElement;
import org.module.eer.mm.moduleeer.ModuleeerPackage;
import org.module.eer.mm.moduleeer.Module;
import org.module.eer.mm.moduleeer.ModuleeerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class BatchExecuteJsonDirectEntities {

	private static File csvFile;
	private static FileWriter csvOut;
	private static CSVPrinter printer;
	private static CompareModuleEERJenetics2 nsga;
	private static Gson gson = new Gson();

	public static void main(String[] args) {

		int start = 0;

		new Thread(new Runnable() {

			@Override
			public void run() {
				Scanner in = new Scanner(System.in);
				while (true) {
					String read = in.nextLine();
					if (read.equals("T")) {
						try {
							printer.flush();
							printer.close();
							System.out.println("terminated sucessfully!!!");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println("terminated with error!!!");
						} finally {
							System.exit(0);
						}
					}
				}
			}
		}).start();

		try {
			csvFile = new File("C:\\Users\\hadid\\OneDrive\\Desktop\\University\\02 February\\statsJson.csv");
			csvFile.createNewFile();
			System.out.println(csvFile.getAbsolutePath());
			csvOut = new FileWriter(csvFile);
			printer = new CSVPrinter(csvOut, CSVFormat.DEFAULT.withDelimiter(',').withHeader("Model Index", "Name", "Pareto",
					"Instance", "Number of Modules", "Cohesion", "Coupling", "String"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		nsga = new CompareModuleEERJenetics2();

		// Input and output directories
		String inputDirectory = "C:\\Users\\hadid\\Downloads\\ModelsJson\\Ecore";

		// Initialize the EMF package
		EPackage.Registry.INSTANCE.put(ModuleeerPackage.eNS_URI, ModuleeerPackage.eINSTANCE);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("moduleeer", new XMIResourceFactoryImpl());

		// Process each Ecore file in the input directory
		List<File> jsonFiles = processModuleEERfiles(inputDirectory);

		for (int i = start; i < jsonFiles.size(); i++) {
			System.out.println("Progress " + i + " / " + jsonFiles.size());
			loadJson(jsonFiles.get(i), i);
		}

		try {
			printer.flush();
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<File> processModuleEERfiles(String inputDirectoryPath) {

		File inputDirectory = new File(inputDirectoryPath);
		File[] files = inputDirectory.listFiles();
		List<File> jsonFiles = new ArrayList<>();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					jsonFiles.addAll(processModuleEERfiles(file.getAbsolutePath()));
				} else if (file.isFile() && file.getName().toLowerCase().endsWith(".json")) {
					jsonFiles.add(file);
				}
			}
		}
		return jsonFiles;
	}

	private static void loadJson(File file, int modelIndex) {
		System.out.println(file.getName());
		JsonER json = null;
		try {
			json = gson.fromJson(new JsonReader(new FileReader(file)), JsonER.class);
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean[][] edges = new boolean[json.getEntities().size()][json.getEntities().size()];
		Module module = readModuleeerModel(json, edges);
		List<ModularizableElement> elements = module.getModularizableElements();
		List<List<Result>> models = nsga.splitModules2(module, edges);
		writeStats(modelIndex, models, file.getName().replace(".json", ""));
	}

	private static Module readModuleeerModel(JsonER json, boolean[][] edges) {
		ModuleeerFactory fac = ModuleeerFactory.eINSTANCE;
		Module module = fac.createModule();
		Map<String, Integer> entityMap = new HashMap<>();
		for (Entity e : json.getEntities()) {
			EntityType entity = fac.createEntityType();
			entity.setName(e.getName());
			entityMap.put(entity.getName(), module.getModularizableElements().size());
			module.getModularizableElements().add(entity);
		}
		for (Generalization g : json.getGeneralizations()) {
			int child = entityMap.get(g.getChild().getName());
			int parent = entityMap.get(g.getParent().getName());
			edges[child][parent] = true;
			edges[parent][child] = true;
		}
		for (Relation r : json.getRelations()) {
			int a = entityMap.get(r.getA().getName());
			int b = entityMap.get(r.getB().getName());
			edges[a][b] = true;
			edges[b][a] = true;
		}
		return module;
	}

	private static void writeStats(int modelIndex, List<List<Result>> models, String modelName) {
		// Create CSV Summary of the Stats
		try {
			if (models.size() != 30) {
				throw new RuntimeException("Wrong size");
			}
//			System.out.println(models.size());
			int sum = 0;
			for (int i = 0; i < models.size(); i++) {
				List<Result> results = models.get(i);
//				System.out.println(results.size());
				sum += results.size();
				for (int j = 0; j < results.size(); j++) {
					Result result = results.get(j);
					MEERModel model = result.getModel();
					printer.printRecord(modelIndex, modelName, i + 1, j + 1, model.getModules().size(), result.getCohesion(),
							result.getCoupling(), moduleMap(model));
				}
			}

			System.out.println("Average pareto size: " + (sum / 30.0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String moduleMap(MEERModel model) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int sum = 0;
		for (int i = 0; i < model.getModules().size(); i++) {
			Module m = model.getModules().get(i);
			for (ModularizableElement it : m.getModularizableElements()) {
				sb.append("'" + it.getName() + "': " + i + ", ");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}

}
