package es.upm.etsisi.cf4j.recommender.matrixFactorization;



import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.Item;
import es.upm.etsisi.cf4j.data.User;
import es.upm.etsisi.cf4j.process.Parallelizer;
import es.upm.etsisi.cf4j.process.Partible;
import es.upm.etsisi.cf4j.recommender.Recommender;
import es.upm.etsisi.cf4j.utils.Methods;

import java.util.Random;

/**
 * Implements Probabilist Matrix Factorization: Koren, Y., Bell, R., &amp; Volinsky, C. (2009). 
 * Matrix factorization techniques for recommender systems. Computer, (8), 30-37.
 *
 * @author Fernando Ortega
 */
public class Pmf extends Recommender {

	private final static double DEFAULT_GAMMA = 0.01;
	private final static double DEFAULT_LAMBDA = 0.1;

	/**
	 * User factors
	 */
	private double[][] p;

	/**
	 * Item factors
	 */
	private double[][] q;

	/**
	 * Learning rate: 0.01 by default
	 */
	private double gamma;

	/**
	 * Regularization parameter: 0.1 by default
	 */
	private double lambda;

	/**
	 * Number of latent factors
	 */
	private int numFactors;

	/**
	 * Number of iterations
	 */
	private int numIters;

	/**
	 * Model constructor
	 * @param datamodel
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 */
	public Pmf(DataModel datamodel, int numFactors, int numIters)	{
		this(datamodel, numFactors, numIters, DEFAULT_LAMBDA);
	}

	public Pmf(DataModel datamodel, int numFactors, int numIters, long seed)	{
		this(datamodel, numFactors, numIters, DEFAULT_LAMBDA, DEFAULT_GAMMA, seed);
	}

	/**
	 * Model constructor
	 * @param datamodel
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 * @param lambda Regularization parameter
	 */
	public Pmf(DataModel datamodel, int numFactors, int numIters, double lambda) {
		this(datamodel, numFactors, numIters, lambda, DEFAULT_GAMMA, (long) (Math.random() * 1E10));
	}

	public Pmf(DataModel datamodel, int numFactors, int numIters, double lambda, long seed) {
		this(datamodel, numFactors, numIters, lambda, DEFAULT_GAMMA, seed);
	}

	/**
	 * Model constructor
	 * @param datamodel
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 * @param lambda Regularization parameter
	 * @param gamma Learning rate parameter
	 */
	public Pmf(DataModel datamodel, int numFactors, int numIters, double lambda, double gamma, long seed) {
		super(datamodel);

		this.numFactors = numFactors;
		this.numIters = numIters;
		this.lambda = lambda;
		this.gamma = gamma;

		Random rand = new Random(seed);

		// Users initialization
		this.p = new double[datamodel.getNumberOfUsers()][numFactors];
		for (int u = 0; u < datamodel.getNumberOfUsers(); u++) {
			for (int k = 0; k < numFactors; k++) {
				this.p[u][k] = rand.nextDouble() * 2 - 1;
			}
		}

		// Items initialization
		this.q = new double[datamodel.getNumberOfItems()][numFactors];
		for (int i = 0; i < datamodel.getNumberOfItems(); i++) {
			for (int k = 0; k < numFactors; k++) {
				this.q[i][k] = rand.nextDouble() * 2 - 1;
			}
		}
	}

	/**
	 * Get the number of topics of the model
	 * @return Number of topics
	 */
	public int getNumberOfTopics() {
		return this.numFactors;
	}

	/**
	 * Get the regularization parameter of the model
	 * @return Lambda
	 */
	public double getLambda() {
		return this.lambda;
	}

	/**
	 * Get the learning rate parameter of the model
	 * @return Gamma
	 */
	public double getGamma() {
		return this.gamma;
	}

	/**
	 * Estimate the latent model factors
	 */
	public void fit() {

		System.out.println("\nProcessing PMF...");

		for (int iter = 1; iter <= this.numIters; iter++) {

			// ALS: fix q_i and update p_u -> fix p_u and update q_i
			Parallelizer.exec(this.datamodel.getUsers(), new UpdateUsersFactors());
			Parallelizer.exec(this.datamodel.getItems(), new UpdateItemsFactors());

			if ((iter % 10) == 0) System.out.print(".");
			if ((iter % 100) == 0) System.out.println(iter + " iterations");
		}
	}

	/**
	 * Computes a rating prediction
	 * @param userIndex User index
	 * @param itemIndex Item index
	 * @return Prediction
	 */
	public double predict(int userIndex, int itemIndex) {
		return Methods.dotProduct(this.p[userIndex], this.q[itemIndex]);
	}

	/**
	 * Auxiliary inner class to parallelize user factors computation
	 * @author Fernando Ortega
	 */
	private class UpdateUsersFactors implements Partible<User> {

		@Override
		public void beforeRun() { }

		@Override
		public void run(User user) {
			int userIndex = user.getIndex();

			for (int j = 0; j < user.getNumberOfRatings(); j++) {

				int itemIndex = user.getItem(j);

				double error = user.getRatingAt(j) - predict(userIndex, itemIndex);

				for (int k = 0; k < numFactors; k++)	{
					p[userIndex][k] += gamma * (error * q[itemIndex][k] - lambda * p[userIndex][k]);
				}
			}
		}

		@Override
		public void afterRun() { }
	}

	/**
	 * Auxiliary inner class to parallelize item factors computation
	 * @author Fernando Ortega
	 */
	private class UpdateItemsFactors implements Partible<Item> {

		@Override
		public void beforeRun() { }

		@Override
		public void run(Item item) {
			int itemIndex = item.getIndex();

			for (int v = 0; v < item.getNumberOfRatings(); v++) {

				int userIndex = item.getUser(v);

				// Get error
				double error = item.getRating(v) - predict(userIndex, itemIndex);

				for (int k = 0; k < numFactors; k++) {
					q[itemIndex][k] += gamma * (error * p[userIndex][k] - lambda * q[itemIndex][k]);
				}
			}
		}

		@Override
		public void afterRun() { }
	}
}
