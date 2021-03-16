package org.module.eer.jenetics.linked.list.chromosome.mutator;

import io.jenetics.Alterer;
import io.jenetics.AltererResult;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.ext.moea.Vec;
import io.jenetics.util.Seq;

public class GraftingMutator implements Alterer<IntegerGene, Vec<double[]>>{
	
	private double probability;
	
	public GraftingMutator(final double probability) {
		this.probability = probability;
	}

	@Override
	public AltererResult<IntegerGene, Vec<double[]>> alter(Seq<Phenotype<IntegerGene, Vec<double[]>>> population, long generation) {
		// TODO Auto-generated method stub
		return null;
	}

}