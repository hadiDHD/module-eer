package org.module.eer.jenetics.split.constraint;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.module.eer.jenetics.config.utils.ModularizableElementUtils;

import io.jenetics.BitChromosome;
import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.PermutationChromosome;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.jenetics.util.MSeq;

@SuppressWarnings("rawtypes")
public class ModularizableDependenciesConstraint implements Constraint{
	
	private List<BasicConstraint> listOfConstraints; 
	private int minNumberOfModules;
	private int maxNumberOfModules;
	
	public ModularizableDependenciesConstraint(List<BasicConstraint> listOfConstraints, int optimalNumberOfModules) {
		this.listOfConstraints = listOfConstraints;
		int half = optimalNumberOfModules/2;
		this.minNumberOfModules = 1;
		this.maxNumberOfModules = Integer.MAX_VALUE;
//		this.minNumberOfModules = optimalNumberOfModules - half <= 1? 2 :optimalNumberOfModules-half;
//		this.maxNumberOfModules = optimalNumberOfModules + half;
	}	
		
	@Override
	public boolean test(Phenotype individual) {		
		return false;
	}
	
	private boolean validNumberOfModules(StringBuilder strBuilder) {
		int numberOfClusters = StringUtils.countMatches(strBuilder.toString(), "1") + 1;
		if (numberOfClusters >= this.minNumberOfModules && numberOfClusters <= this.maxNumberOfModules)
			return true;
		return false;
	}	
	
	private void repairNumberOfModules(StringBuilder strBuilder) {
		int numberOfClusters = StringUtils.countMatches(strBuilder.toString(), "1") + 1;
		if (!(numberOfClusters >= this.minNumberOfModules && numberOfClusters <= this.maxNumberOfModules)) {		
			if (numberOfClusters > this.maxNumberOfModules) { // Remove some clusters
				int remove = numberOfClusters - this.maxNumberOfModules;
				List<Integer> ones = ones(strBuilder);						
				Collections.shuffle(ones);
				for (int i = 0; i < remove; i++) {
					strBuilder.replace(ones.get(i), ones.get(i) + 1, "0");
				}			
			} else { // Add some clusters
				int add = this.minNumberOfModules - numberOfClusters;
				int addedCluster = 0;
				Random random = new Random();
				while (add != addedCluster) {
					int randomPosition = random.nextInt(strBuilder.length());
					if (strBuilder.charAt(randomPosition) == '0') {
						strBuilder.replace(randomPosition, randomPosition + 1, "1");
						addedCluster++;
					}
				}
			}
		}
	}
	
	private List<Integer> ones(StringBuilder strBuilder) {
		return IntStream
		          .iterate(strBuilder.indexOf("1"), index -> index >= 0, index -> strBuilder.indexOf("1", index + 1))
		          .boxed()
		          .collect(Collectors.toList());
	}

	private void repairBasicConstraint(MSeq<EnumGene<Integer>> repairPc, StringBuilder strBuilder) {
		for (BasicConstraint basicConstraint : this.listOfConstraints) {
			int chromosomePosition = getPositionInPChromosome(repairPc, basicConstraint.getElementId());
			boolean search = false;
			for (int relatedElementId : basicConstraint.getElements()) {
				search = ModularizableElementUtils.belongsToTheSameModule(relatedElementId, chromosomePosition, 
						repairPc, strBuilder.toString());
				if (search == true)
					break;
			}			
			if (search == false) {				
				try {
					int toPosition = prioritizeModuleLessElements(basicConstraint.getElements(), strBuilder, repairPc);
					updateModuleOf(chromosomePosition, toPosition, repairPc, strBuilder);					
				} catch (Exception e) {					
					e.printStackTrace();
				} 				
			}			
		}
	}
	
	private int getPositionInPChromosome(MSeq<EnumGene<Integer>> repairPc, int elementId) {
		for (int i = 0; i < repairPc.length(); i++) {
			if (repairPc.get(i).allele() == elementId)
				return i;
		}
		throw new IllegalArgumentException("The Chromosome is invalid");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Phenotype repair(Phenotype individual, long generation) {
		MSeq<EnumGene<Integer>> repairPc = MSeq.of((PermutationChromosome<Integer>) individual.genotype().get(0));
		BitChromosome bitChromosome = ((BitChromosome) individual.genotype().get(1));
		StringBuilder strBuilder = new StringBuilder(bitChromosome.toCanonicalString());
		do {
			repairNumberOfModules(strBuilder);			
			repairBasicConstraint(repairPc, strBuilder);			
		} 
		while (validNumberOfModules(strBuilder) == false);
		
		return newPhenotype(repairPc, strBuilder.toString(), bitChromosome.oneProbability(), generation);
	}	

	//Select the module with less elements to insert the relationshipType instance
	//If all the elements has the same number of elements, then the module is selected randomly
	private int prioritizeModuleLessElements(List<Integer> listOfelements, StringBuilder strBuilder, 
			MSeq<EnumGene<Integer>> enumChromosome) throws Exception {
		//Map<Position of the Element, Number of Elements in that Module>
		TreeMap<Integer, Integer> elementsNumberOfModule = new TreeMap<Integer, Integer>();
		for (Integer element : listOfelements) {
			int positionElement = getPositionInPChromosome(enumChromosome, element); 
			int numberOfElementsInModule = ModularizableElementUtils.numberOfElementsInModule(positionElement, strBuilder);
			if (elementsNumberOfModule.size() == 0)
				elementsNumberOfModule.put(positionElement, numberOfElementsInModule);
			else {
				if (elementsNumberOfModule.firstEntry().getValue() > numberOfElementsInModule) {
					elementsNumberOfModule.clear();
					elementsNumberOfModule.put(positionElement,	numberOfElementsInModule);
				} else if (elementsNumberOfModule.firstEntry().getValue() == numberOfElementsInModule){
					elementsNumberOfModule.put(positionElement,	numberOfElementsInModule);
				}
			}
		}		
		if (elementsNumberOfModule.size() == 1) 
			return elementsNumberOfModule.firstEntry().getKey();
		else {
			int randomId = new Random().nextInt(elementsNumberOfModule.size());
			Iterator<Integer> itElementsPositions = elementsNumberOfModule.keySet().iterator();
			int pos = 0;
			while (itElementsPositions.hasNext()) {
				Integer mapPosition = (Integer) itElementsPositions.next();
				if (pos == randomId)
					return mapPosition;
				else
					pos++;
			}			
		}
		throw new Exception("It could not be found a priority Module");		
	}
	
	
	private void updateModuleOf(int position, int toPosition, MSeq<EnumGene<Integer>> repairPc, StringBuilder strBuilder) {
		removeSeparator(position, repairPc, strBuilder);
		// Swap elements positions until toPosition
		if (position > toPosition) {
			swapElementsBackwards(toPosition, position, repairPc);
			strBuilder.insert(toPosition, '0');
		} else {
			swapElementsForWards(position, toPosition, repairPc);
			strBuilder.insert(toPosition - 1, '0');
		}		
	}
	
	private void swapElementsBackwards(int startPosition, int endPosition, MSeq<EnumGene<Integer>> repairPc) {
		for (int i = endPosition; i > startPosition; i--) {
			repairPc.swap(i-1, i);
		}		
	}

	private void swapElementsForWards(int startPosition, int endPosition, MSeq<EnumGene<Integer>> repairPc) {
		for (int i = startPosition; i < endPosition - 1; i++) {
			repairPc.swap(i, i+1);
		}
	}
	
	private void removeSeparator(int position, MSeq<EnumGene<Integer>> repairPc, StringBuilder strBuilder) {
		if (position == 0)
			strBuilder.deleteCharAt(0);
		else if (position == (repairPc.length() - 1))
			strBuilder.deleteCharAt(strBuilder.length() - 1);
		else {
			char nextSeparator = strBuilder.charAt(position);
			char previousSeparator = strBuilder.charAt(position - 1);
			strBuilder.delete(position-1, position + 1);
			char newBit = '1';
			if (previousSeparator == nextSeparator && previousSeparator == '0') {
				newBit = '0';
			} 
			if (position >= strBuilder.length()) {
				strBuilder.append(newBit);
			} else {
				strBuilder.insert(position, newBit);
			}						
		}		
	}
	
	@SuppressWarnings("unchecked")
	private Phenotype newPhenotype(MSeq<EnumGene<Integer>> repairPc, String stringBitChromosome, 
			double probabilityOnes, long generation) {
		PermutationChromosome<Integer> pc = new PermutationChromosome<Integer>(repairPc.toISeq());
		BitChromosome bc = BitChromosome.of(stringBitChromosome, stringBitChromosome.length(), probabilityOnes);				
		final Genotype genoType = Genotype.of(
				(Chromosome) pc,
				(Chromosome) bc 
				);		
		return Phenotype.of(genoType, generation);	
	}	
}
