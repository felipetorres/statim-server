package optimizer.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import optimizer.Coordenada;
import optimizer.Cromossomo;
import optimizer.Gene;
import optimizer.crossover.CrossoverContext;
import optimizer.crossover.PMX;
import optimizer.fitness.FitnessCalculator;
import optimizer.selection.SelectionContext;
import optimizer.selection.Wheel;
import app.model.GoogleDistanceMatrixObject;

import com.google.gson.Gson;

public class BenchmarkEngine {
	
	private GoogleDistanceMatrixObject matrix = (new Gson()).fromJson(Example.matrix, GoogleDistanceMatrixObject.class);
	private List<Cromossomo> populacao;
	private SelectionContext fittnessContext = new SelectionContext(new Wheel());
	private CrossoverContext crossoverContext = new CrossoverContext(new PMX());
	
	public BenchmarkEngine(String otimo) {
		System.out.println("Benchmark");
		System.out.println("Wheel PMX " + otimo);
	}

	public List<Cromossomo> minimizeRoute(List<Coordenada> coordenadas, int popInicial, int geracoes, int fitness_amount) {
		System.out.println(popInicial + " " + geracoes + " " + fitness_amount);
		long start = System.currentTimeMillis();
		
		populacao = geraPopulacaoInicial(popInicial);
		for(int i=0;i<geracoes;i++) {
			evaluateFitness();
			List<Cromossomo> fittest = selectsTheFittestUsing(fittnessContext, fitness_amount);
			populacao = crossover(crossoverContext, fittest);
		}
		System.out.println(System.currentTimeMillis() - start);
		evaluateFitness();
		Collections.sort(populacao, Collections.reverseOrder());
		System.out.println(1/populacao.get(0).getFitness());
		
		return populacao;
	}

	private List<Cromossomo> geraPopulacaoInicial(int n) {
		int dimension = matrix.getDimension();
		List<Gene> genes = geneListWithSize(dimension);

		List<Cromossomo> populacao = new ArrayList<Cromossomo>();
		
		for (int i=0; i<n; i++) {
			List<Gene> copy = new ArrayList<Gene>(genes);
			Collections.shuffle(copy);
			populacao.add(new Cromossomo(copy));
		}
		return populacao;
	}
	
	private void evaluateFitness() {
		for (Cromossomo cromossomo : populacao) {
			new FitnessCalculator(cromossomo, matrix).calculateTotalDistance();
		}
	}
	
	private List<Cromossomo> selectsTheFittestUsing(SelectionContext context, int amount) {
		return context.select(amount, populacao);
	}
	
	private List<Cromossomo> crossover(CrossoverContext context, List<Cromossomo> from) {
		Collections.shuffle(from);
		List<Cromossomo> born = new ArrayList<Cromossomo>();
		
		for(int i=0,j=from.size()-1; i<j; i++, j--) {
			Cromossomo dad = from.get(i);
			Cromossomo mom = from.get(j);
			List<Cromossomo> children = context.cross(dad, mom);
			born.add(children.get(0));
			born.add(children.get(1));
		}
		from.addAll(born);
		return from;
	}
	
	private List<Gene> geneListWithSize(int dimension) {
		List<Gene> genes = new ArrayList<Gene>();
		for(int i=0; i<dimension; i++) {
			Gene gene = new Gene(i, false); //verificar se o gene inicia uma rota.
			genes.add(gene);
		}
		return genes;
	}
}
