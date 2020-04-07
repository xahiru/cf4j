package com.github.ferortega.cf4j.recommender.matrixFactorization;

import com.github.ferortega.cf4j.util.Parallelizer;
import com.github.ferortega.cf4j.util.Partible;
import com.github.ferortega.cf4j.data.DataModel;
import com.github.ferortega.cf4j.data.Item;
import com.github.ferortega.cf4j.data.User;
import com.github.ferortega.cf4j.recommender.Recommender;
import com.github.ferortega.cf4j.util.Maths;

import java.util.Random;

/**
 * Implements Koren, Y., Bell, R., &amp; Volinsky, C. (2009). Matrix factorization techniques for recommender systems.
 * Computer, (8), 30-37.
 */
public class BiasedMF extends Recommender {

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
	 * User bias
	 */
	private double[] bu;

	/**
	 * Item bias
	 */
	private double[] bi;

	/**
	 * Learning rate
	 */
	private double gamma;

	/**
	 * Regularization parameter
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
	 * @param datamodel DataModel instance
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 */
	public BiasedMF(DataModel datamodel, int numFactors, int numIters)	{
		this(datamodel, numFactors, numIters, DEFAULT_LAMBDA);
	}

	/**
	 * Model constructor
	 * @param datamodel DataModel instance
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 * @param seed Seed for random numbers generation
	 */
	public BiasedMF(DataModel datamodel, int numFactors, int numIters, long seed)	{
		this(datamodel, numFactors, numIters, DEFAULT_LAMBDA, DEFAULT_GAMMA, seed);
	}

	/**
	 * Model constructor
	 * @param datamodel DataModel instance
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 * @param lambda Regularization parameter
	 */
	public BiasedMF(DataModel datamodel, int numFactors, int numIters, double lambda) {
		this(datamodel, numFactors, numIters, lambda, DEFAULT_GAMMA, System.currentTimeMillis());
	}

	/**
	 * Model constructor
	 * @param datamodel DataModel instance
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 * @param lambda Regularization parameter
	 * @param seed Seed for random numbers generation
	 */
	public BiasedMF(DataModel datamodel, int numFactors, int numIters, double lambda, long seed) {
		this(datamodel, numFactors, numIters, lambda, DEFAULT_GAMMA, seed);
	}

	/**
	 * Model constructor
	 * @param datamodel DataModel instance
	 * @param numFactors Number of factors
	 * @param numIters Number of iterations
	 * @param lambda Regularization parameter
	 * @param gamma Learning rate parameter
	 * @param seed Seed for random numbers generation
	 */
	public BiasedMF(DataModel datamodel, int numFactors, int numIters, double lambda, double gamma, long seed) {
		super(datamodel);

		this.numFactors = numFactors;
		this.numIters = numIters;
		this.lambda = lambda;
		this.gamma = gamma;

		Random rand = new Random(seed);

		// Users initialization
		this.p = new double[datamodel.getNumberOfUsers()][numFactors];
		this.bu = new double[datamodel.getNumberOfUsers()];
		for (int u = 0; u < datamodel.getNumberOfUsers(); u++) {
			this.bu[u] = rand.nextDouble() * 2 - 1;
			for (int k = 0; k < numFactors; k++) {
				this.p[u][k] = rand.nextDouble() * 2 - 1;
			}
		}

		// Items initialization
		this.q = new double[datamodel.getNumberOfItems()][numFactors];
		this.bi = new double[datamodel.getNumberOfItems()];
		for (int i = 0; i < datamodel.getNumberOfItems(); i++) {
			this.bi[i] = rand.nextDouble() * 2 - 1;
			for (int k = 0; k < numFactors; k++) {
				this.q[i][k] = rand.nextDouble() * 2 - 1;
			}
		}
	}

	/**
	 * Get the number of factors of the model
	 * @return Number of factors
	 */
	public int getNumFactors() {
		return this.numFactors;
	}

	/**
	 * Get the number of iterations
	 * @return Number of iterations
	 */
	public int getNumIters() {
		return this.numIters;
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

	@Override
	public void fit() {

		System.out.println("\nFitting " + this.toString());

		for (int iter = 1; iter <= this.numIters; iter++) {

			Parallelizer.exec(super.datamodel.getUsers(), new UpdateUsersFactors());
			Parallelizer.exec(super.datamodel.getItems(), new UpdateItemsFactors());

			if ((iter % 10) == 0) System.out.print(".");
			if ((iter % 100) == 0) System.out.println(iter + " iterations");
		}
	}

	@Override
	public double predict(int userIndex, int itemIndex) {
		double[] pu = this.p[userIndex];
		double[] qi = this.q[itemIndex];
		return datamodel.getRatingAverage() + this.bu[userIndex] + this.bi[itemIndex] + Maths.dotProduct(pu, qi);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("BiasedMF(")
				.append("numFactors=").append(this.numFactors)
				.append("; ")
				.append("numIters=").append(this.numIters)
				.append("; ")
				.append("gamma=").append(this.gamma)
				.append("; ")
				.append("lambda=").append(this.lambda)
				.append(")");
		return str.toString();
	}

	/**
	 * Auxiliary inner class to parallelize user factors computation
	 */
	private class UpdateUsersFactors implements Partible<User> {

		@Override
		public void beforeRun() { }

		@Override
		public void run(User user) {
			int userIndex = user.getUserIndex();
			for (int pos = 0; pos < user.getNumberOfRatings(); pos++) {
				int itemIndex = user.getItemAt(pos);
				double error = user.getRatingAt(pos) - predict(userIndex, itemIndex);

				bu[userIndex] += gamma * (error - lambda * bu[userIndex]);

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
	 */
	private class UpdateItemsFactors implements Partible<Item> {

		@Override
		public void beforeRun() { }

		@Override
		public void run(Item item) {
			int itemIndex = item.getItemIndex();
			for (int pos = 0; pos < item.getNumberOfRatings(); pos++) {
				int userIndex = item.getUserAt(pos);
				double error = item.getRatingAt(pos) - predict(userIndex, itemIndex);

				bi[itemIndex] += gamma * (error - lambda * bi[itemIndex]);

				for (int k = 0; k < numFactors; k++) {
					q[itemIndex][k] += gamma * (error * p[userIndex][k] - lambda * q[itemIndex][k]);
				}
			}
		}

		@Override
		public void afterRun() { }
	}
}