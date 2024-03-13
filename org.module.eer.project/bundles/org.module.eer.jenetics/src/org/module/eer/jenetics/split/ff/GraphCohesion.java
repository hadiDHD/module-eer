package org.module.eer.jenetics.split.ff;

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.module.eer.mm.moduleeer.ModularizableElement;

import io.jenetics.BitChromosome;
import io.jenetics.Genotype;
import io.jenetics.PermutationChromosome;

@SuppressWarnings("rawtypes")
public class GraphCohesion implements Function<Genotype, Double> {

	private EList<ModularizableElement> nodes;
	private boolean[][] edges;

	public GraphCohesion(EList<ModularizableElement> nodes, boolean[][] edges) {
		this.nodes = nodes;
		this.edges = edges;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Double apply(Genotype genotype) {
		final PermutationChromosome<Integer> pc = (PermutationChromosome<Integer>) genotype.get(0);
		final BitChromosome bc = (BitChromosome) genotype.get(1);
		List<List<Integer>> modules = new BasicEList<List<Integer>>();
		List<Integer> currentModule = new BasicEList<Integer>();
		modules.add(currentModule);
		for (int i = 0; i < bc.length(); i++) {
			Integer index = pc.get(i).alleleIndex();
			currentModule.add(index);
			if (bc.get(i).bit() == true) {
				// new module
				currentModule = new BasicEList<Integer>();
				modules.add(currentModule);
			}
		}
		// Add to the last module, the last element
		Integer index = pc.get(pc.length() - 1).alleleIndex();
		currentModule.add(index);
		return calculateCohesion(modules);
	}

	private Double calculateCohesion(List<List<Integer>> modules) {
		if (modules.size() == 0) {
			return 0.0;
		}
		double sum = 0;
		for (List<Integer> currentSet : modules) {
			int possibleConnections = currentSet.size() * (currentSet.size() - 1) / 2;
			if (possibleConnections == 0) {
				continue;
			}
			int actualConnections = calculateConnections(currentSet);
			double moduleCohesion = (double) actualConnections / possibleConnections;
			sum += moduleCohesion;
		}
		double totalCohesion = sum / modules.size();
		return totalCohesion;
	}

	private int calculateConnections(List<Integer> indices) {
		int connections = 0;
		for (int i = 0; i < indices.size(); i++) {
			for (int j = i + 1; j < indices.size(); j++) {
				if(edges[indices.get(i)][indices.get(j)]) {
					connections++;
				}
			}
		}
		return connections;
	}
}