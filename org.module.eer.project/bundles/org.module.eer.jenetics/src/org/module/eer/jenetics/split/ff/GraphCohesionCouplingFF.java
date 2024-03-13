package org.module.eer.jenetics.split.ff;

import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.ext.moea.Vec;
import io.jenetics.ext.moea.VecFactory;

import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.module.eer.jenetics.utils.Cohesive;
import org.module.eer.mm.moduleeer.ModularizableElement;

@SuppressWarnings("rawtypes")
public class GraphCohesionCouplingFF implements Function<Genotype, Vec<double[]>>{
	
	private EList<ModularizableElement> nodes;
	private boolean[][] edges;
	
	public GraphCohesionCouplingFF(EList<ModularizableElement> nodes, boolean[][] edges) {
		this.nodes = nodes;
		this.edges = edges;
	}
	
	private static final VecFactory<double[]> VEC_FACTORY =
			VecFactory.ofDoubleVec(
				Optimize.MAXIMUM,//Cohesion	
				Optimize.MINIMUM//Coupling	
	);	
	
	@Override
	public Vec<double[]> apply(Genotype genotype) {	
		return VEC_FACTORY.newVec(new double[] {
				new GraphCohesion(this.nodes, this.edges).apply(genotype),
				new GraphCoupling(this.nodes, this.edges).apply(genotype)
			});
	}
}
