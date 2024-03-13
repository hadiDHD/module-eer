package org.module.eer.json;

import org.module.eer.mm.moduleeer.MEERModel;

public class Result {
	private double cohesion;
	private double coupling;
	private MEERModel model;

	public Result(double cohesion, double coupling, MEERModel model) {
		super();
		this.cohesion = cohesion;
		this.coupling = coupling;
		this.model = model;
	}

	public double getCohesion() {
		return cohesion;
	}

	public double getCoupling() {
		return coupling;
	}

	public MEERModel getModel() {
		return model;
	}
	
	
}
