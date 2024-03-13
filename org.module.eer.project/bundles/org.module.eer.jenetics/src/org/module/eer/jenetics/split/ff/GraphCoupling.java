package org.module.eer.jenetics.split.ff;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.module.eer.mm.moduleeer.ModularizableElement;

import io.jenetics.BitChromosome;
import io.jenetics.Genotype;
import io.jenetics.PermutationChromosome;

@SuppressWarnings("rawtypes")
public class GraphCoupling implements Function<Genotype, Double> {

	private EList<ModularizableElement> nodes;
	private boolean[][] edges;

	public GraphCoupling(EList<ModularizableElement> nodes, boolean[][] edges) {
		this.nodes = nodes;
		this.edges = edges;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Double apply(Genotype genotype) {
		final PermutationChromosome<Integer> pc = (PermutationChromosome<Integer>) genotype.get(0);
		final BitChromosome bc = (BitChromosome) genotype.get(1);
		Map<Integer, Integer> moduleMap = new HashMap<>();
		int moduleNumber = 0;
		for (int i = 0; i < bc.length(); i++) {
			Integer index = pc.get(i).alleleIndex();
			moduleMap.put(index, moduleNumber);
			if (bc.get(i).bit() == true) {
				// new module
				moduleNumber++;
			}
		}
		// Add to the last module, the last element
		Integer index = pc.get(pc.length() - 1).alleleIndex();
		moduleMap.put(index, moduleNumber);
		return calculateCoupling(moduleMap);
	}

	private Double calculateCoupling(Map<Integer, Integer> moduleMap) {
		int totalEdges = 0;
		int interEdges = 0;
		for (int i = 0; i < edges.length; i++) {
			for (int j = i + 1; j < edges.length; j++) {
				if(edges[i][j]) {
					totalEdges++;
					if(!moduleMap.get(i).equals(moduleMap.get(j))) {
						interEdges++;
					}
				}
			}
		}
		return (double) interEdges / totalEdges;
	}
}
