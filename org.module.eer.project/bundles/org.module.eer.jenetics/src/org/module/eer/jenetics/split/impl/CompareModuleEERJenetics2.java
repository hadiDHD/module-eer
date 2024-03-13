package org.module.eer.jenetics.split.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.math3.genetics.Population;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.module.eer.jenetics.config.utils.ModularizableElementUtils;
import org.module.eer.jenetics.split.ISplitModulEER;
import org.module.eer.jenetics.split.constraint.ModularizableDependenciesConstraint;
import org.module.eer.jenetics.split.ff.GraphCohesionCouplingFF;
import org.module.eer.json.Result;
import org.module.eer.mm.moduleeer.MEERModel;
import org.module.eer.mm.moduleeer.ModularizableElement;
import org.module.eer.mm.moduleeer.Module;
import org.module.eer.mm.moduleeer.impl.ModuleeerFactoryImpl;

import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStream;
import io.jenetics.util.ISeq;
import io.jenetics.Genotype;
import io.jenetics.BitChromosome;
import io.jenetics.MultiPointCrossover;
import io.jenetics.Mutator;
import io.jenetics.PartialAlterer;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Constraint;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.ext.moea.NSGA2Selector;
import io.jenetics.ext.moea.Vec;

public class CompareModuleEERJenetics2 implements ISplitModulEER {

	public ArrayList<double[]> newStats;
	private Random rand = new Random(System.currentTimeMillis());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ISeq<Phenotype> performParetoSet(Module splittingModule,
			boolean[][] edges) {
		final int size = splittingModule.getModularizableElements().size();
		final Constraint modulEERConstraint = new ModularizableDependenciesConstraint(ModularizableElementUtils
				.dependenciesOfAllModularizableElements(splittingModule.getModularizableElements()), 0);

		@SuppressWarnings("unchecked")
		final Engine engine = Engine
				.builder(new GraphCohesionCouplingFF(splittingModule.getModularizableElements(), edges),
						modulEERConstraint.constrain(encoding(size)))
				.constraint(modulEERConstraint).alterers(
						// The `PartiallyMatchedCrossover` is used on chromosome with index 0.
						PartialAlterer.of(new PartiallyMatchedCrossover(0.9), 0),
						// The `PartiallyMatchedCrossover` is used on chromosome with index 1.
						PartialAlterer.of(new MultiPointCrossover(0.9), 1),
						// The SwapMutator is used on chromosome with index 0.
						PartialAlterer.of(new SwapMutator(0.1), 0),
						// The `Mutator` is used on chromosome with index 1.
						PartialAlterer.of(new Mutator(0.1), 1))
				.survivorsSelector(NSGA2Selector.ofVec()).populationSize(size).build();
		return (ISeq<Phenotype>) engine.stream().limit(size * 2).collect(MOEA.toParetoSet());
	}

	@Override
	public EList<MEERModel> splitModules(Module splittingModule) {
		// TODO Auto-generated method stub
		return null;
	}

	public int bound(int min, int value, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public List<List<Result>> splitModules2(Module splittingModule, boolean[][] edges) {
		int numberOfThreads = 30;

		System.out.println("Model Size: " + splittingModule.getModularizableElements().size());
		
		long time = System.currentTimeMillis();
		
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<ISeq<Phenotype>>> futures = new ArrayList<>();

		for (int i = 0; i < numberOfThreads; i++) {
			Callable<ISeq<Phenotype>> task = new Callable<ISeq<Phenotype>>() {
				@Override
				public ISeq<Phenotype> call() {
					// Replace this with the method you want to execute
					// For demonstration purposes, let's say the method returns the thread's name
					return performParetoSet(splittingModule, edges);
				}
			};
			Future<ISeq<Phenotype>> future = executor.submit(task);
			futures.add(future);
		}

		executor.shutdown();


		EList<MEERModel> resultOfSplittingModels = new BasicEList<MEERModel>();
		List<List<Result>> listOfResults = new ArrayList<>();
		// Collect results from futures
		newStats = new ArrayList<>();
		futures.stream().forEach(it -> {
			Set<Integer> hashSet = new HashSet<>();
			List<Result> results = new ArrayList<>();
			try {
				ISeq<Phenotype> seq = it.get();
				seq.stream().forEach(phenotype -> {
					int hash = Arrays.hashCode((double[]) ((Vec) phenotype.fitness()).data());
					if (!hashSet.contains(hash)) {
						hashSet.add(hash);
						MEERModel model = convertPhenotypeToModulEER(phenotype, splittingModule);
						resultOfSplittingModels.add(model);
						double[] stat = getNewStats(phenotype.fitness());
						newStats.add(stat);
//						System.out.println((newStats.size() - 1) + ": Cohesion = " + stat[0] + ", Coupling = " + stat[1]);
						results.add(new Result(stat[0], stat[1], model));
					}
				});
				listOfResults.add(results);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		System.out.println("Minutes: " + ((System.currentTimeMillis() - time) / 60000.0));
		return listOfResults;
	}
	
	
	public EList<MEERModel> splitModules(Module splittingModule, boolean[][] edges) {
//		int numberOfThreads = bound(10, splittingModule.getModularizableElements().size(), 50);
		int numberOfThreads = 30;

		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<ISeq<Phenotype>>> futures = new ArrayList<>();

		for (int i = 0; i < numberOfThreads; i++) {
			Callable<ISeq<Phenotype>> task = new Callable<ISeq<Phenotype>>() {
				@Override
				public ISeq<Phenotype> call() {
					// Replace this with the method you want to execute
					// For demonstration purposes, let's say the method returns the thread's name
					return performParetoSet(splittingModule, edges);
				}
			};
			Future<ISeq<Phenotype>> future = executor.submit(task);
			futures.add(future);
		}

		executor.shutdown();


		EList<MEERModel> resultOfSplittingModels = new BasicEList<MEERModel>();
		List<List<Result>> listOfResults = new ArrayList<>();
		// Collect results from futures
		newStats = new ArrayList<>();
		futures.stream().forEach(it -> {
			Set<Integer> hashSet = new HashSet<>();
			List<Result> results = new ArrayList<>();
			try {
				it.get().stream().forEach(phenotype -> {
					int hash = Arrays.hashCode((double[]) ((Vec) phenotype.fitness()).data());
					if (!hashSet.contains(hash)) {
						hashSet.add(hash);
						MEERModel model = convertPhenotypeToModulEER(phenotype, splittingModule);
						resultOfSplittingModels.add(model);
						double[] stat = getNewStats(phenotype.fitness());
						newStats.add(stat);
						System.out.println((newStats.size() - 1) + ": Cohesion = " + stat[0] + ", Coupling = " + stat[1]);
						results.add(new Result(stat[0], stat[1], model));
					}
				});
				listOfResults.add(results);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return resultOfSplittingModels;
	}

	public HashMap<Integer, Phenotype> checkParetoSet(HashMap<Integer, Phenotype> map) {
		HashMap<Integer, Phenotype> paretoSet = new HashMap<>();
		for (Map.Entry<Integer, Phenotype> p1 : map.entrySet()) {
			boolean isDominated = false;
			List<Map.Entry<Integer, Phenotype>> toRemove = new ArrayList();

			for (Map.Entry<Integer, Phenotype> p2 : map.entrySet()) {
				if (dominates(p1.getValue(), p2.getValue())) {
					toRemove.add(p2);
				} else if (dominates(p2.getValue(), p1.getValue())) {
					isDominated = true;
					break;
				}
			}

			if (!isDominated) {
				paretoSet.put(p1.getKey(), p1.getValue());
				toRemove.stream().forEach(it -> paretoSet.remove(it.getKey(), it.getValue()));
			}
		}
		return paretoSet;
	}

	private boolean dominates(Phenotype p1, Phenotype p2) {
		double[] d1 = (double[]) ((Vec) p1.fitness()).data();
		double[] d2 = (double[]) ((Vec) p2.fitness()).data();
		return d1[0] > d2[0] && d1[1] <= d2[1] || d1[0] >= d2[0] && d1[1] < d2[1];
	}

	@SuppressWarnings("rawtypes")
	private MEERModel convertPhenotypeToModulEER(Phenotype phenotype, Module splittingModule) {
		MEERModel splittedMEERModel = ModuleeerFactoryImpl.eINSTANCE.createMEERModel();
		splittedMEERModel.setName("Splitted " + splittingModule.getName());
		Copier copier = new Copier();
		EList<Module> listOfModules = new BasicEList<Module>();
		Module currentModule = ModuleeerFactoryImpl.eINSTANCE.createModule();
		listOfModules.add(currentModule);
		final PermutationChromosome pc = (PermutationChromosome) phenotype.genotype().get(0);
		final BitChromosome bc = (BitChromosome) phenotype.genotype().get(1);
		for (int i = 0; i < bc.length(); i++) {
			currentModule.getModularizableElements().add((ModularizableElement) copier
					.copy(splittingModule.getModularizableElements().get((Integer) pc.get(i).allele())));
			if (bc.get(i).bit() == true) {
				currentModule = ModuleeerFactoryImpl.eINSTANCE.createModule();
				listOfModules.add(currentModule);
			}
		}
		currentModule.getModularizableElements().add((ModularizableElement) copier
				.copy(splittingModule.getModularizableElements().get((Integer) pc.get(bc.length()).allele())));
		copier.copyReferences();
		splittedMEERModel.getModules().addAll(listOfModules);
		return splittedMEERModel;
	}

	private double[] getNewStats(Comparable c) {
		return (double[]) ((Vec) c).data();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Genotype encoding(int size) {
		return Genotype.of((Chromosome) PermutationChromosome.ofInteger(size), (Chromosome) BitChromosome.of(size - 1));
	}

}