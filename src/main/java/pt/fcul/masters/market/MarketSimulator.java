package pt.fcul.masters.market;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.Data;
import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.table.Table;
import pt.fcul.masters.utils.Pair;
import pt.fcul.masters.vgp.util.ComplexVector;
import pt.fcul.masters.vgp.util.Vector;

/**
 * 
 * @author Owner
 *
 * @param <T> T is the type of data that the agent receives.
 */
@Data
public class MarketSimulator<T> {
	
	@lombok.ToString.Exclude
	@lombok.EqualsAndHashCode.Exclude
	private final Table<T> table;

	private double slidingWindowPercentage;
	private double intialInvestment;
	private double transactionFee;
	private double leverage;
	private double penalizerRate;
	
	private boolean compoundMode = false;
	
	
	@lombok.ToString.Exclude
	@lombok.EqualsAndHashCode.Exclude
	private List<Transaction> transactions = new ArrayList<>();
	
	
	private double money = intialInvestment;
	private double timewithoutaction = 0;
	private double currentPrice = 0;
	
	private Transaction currentTransaction;
	private MarketAction currentAction = MarketAction.NOOP;
	
	private Pair<Integer, Integer> trainSlice;
	
	private MarketSimulator(Table<T> table) {
		this.table = table;
	}

	/**
	 * 
	 * @param agent a function that pass the agent arguments and expects a MarketAction
	 * @param useTrainData Flag to know if it should be used test or validation data
	 * @param interceptor intercepter that receives the agent money as parameter 
	 * @return returns the money that this agent could make trading in the market
	 */
	public double simulateMarket(Function<T[] , MarketAction> agent, boolean useTrainData, Consumer<MarketSimulator<T>> interceptor) {
		Pair<Integer, Integer> data = useTrainData ? 
				trainSlice == null ? table.randomTrainSet((int)(table.getTrainSet().value() * slidingWindowPercentage))  : trainSlice
				: table.getValidationSet();
		
		money = intialInvestment;
		
		for(int i = data.key() ; i < data.value() ; i ++) {
			List<T> row = getTable().getRow(i); //current values

			currentPrice = getCurrentPrice(row);
			currentAction = agent.apply(getArgs(row, 0)); // action that the agent want to perform at iteration i
			
//			System.out.println(currentAction);
			
			if((currentTransaction == null || currentTransaction.isClose() || currentTransaction.getType() != currentAction) && currentAction != MarketAction.NOOP) { // Place new order
				if(currentTransaction != null && currentTransaction.isOpen())
					money = closeTransaction(currentTransaction, i);
				
				if(currentTransaction == null)
					money = money - timewithoutaction * penalizerRate;
				
				currentTransaction = openTransaction(i, currentAction);
			//	System.out.println((i-data.key())+" "+currentAction+" Money: "+money+" Price: "+currentPrice+" Shares: "+currentTransaction.getShares());
			}else
				timewithoutaction ++;
			
			if(currentTransaction != null && 
				((currentTransaction.isOpen() && currentTransaction.getInitialMoney() - currentTransaction.unRealizedProfit(currentPrice, timewithoutaction * penalizerRate) <= 0)
					|| money <= 0)) {// verify if it lost all money
				
				closeTransaction(currentTransaction, i);
				money = 0;
				
				if(interceptor != null) 
					interceptor.accept(this);
				
				return money;
			}
			
			if(interceptor != null) 
				interceptor.accept(this);
		}
		
		if(currentTransaction != null && currentTransaction.isOpen()) {
			currentPrice = getCurrentPrice(table.getRow(data.value()-1));
			money = closeTransaction(currentTransaction, data.value());
		}
		
		if(interceptor != null) 
			interceptor.accept(this);
		if(Double.isNaN((currentTransaction != null ? money : money - timewithoutaction * penalizerRate) ))
			System.out.println();
			
		return(currentTransaction != null ? money : money - timewithoutaction * penalizerRate)  ;// (double)(data.value()-data.key());
	}
	
	
	
	
	public double moneyRate() {
		if(transactions.size() < 10)
			return -1;
		
		double profit = 0;
		for (Transaction t : transactions)
				profit += t.realizedProfit();
		return transactions.isEmpty() ? 0 : profit / (double)transactions.size();
	}	
	
	
	
	public double winRate() {
		if(transactions.size() < 5)
			return -1;
		
		double win = 0;
		for (Transaction t : transactions)
			if(t.realizedProfit()> 0)
				win++;
		return transactions.isEmpty() ? 0 : win / (double)transactions.size();
	}
	
	
	
	
	private double closeTransaction(Transaction lastTransaction, int index) {
		lastTransaction.close(index,currentPrice, timewithoutaction * penalizerRate);
		double realizedProfit = lastTransaction.realizedProfit();
		return money + realizedProfit ;
	}
	
	
	
	private Transaction openTransaction(int index,MarketAction type) {
		timewithoutaction = 0;
		Transaction transaction = new Transaction(type, (compoundMode ? money : intialInvestment) / currentPrice, currentPrice , index, transactionFee);
		transactions.add(transaction);
		return transaction;
	}

	
	
	
	@SuppressWarnings("unchecked")
	private T[] getArgs(List<T> row, double moneyPrecentage) {
		T element = row.get(0);
		T[] args = row.toArray((T[]) Array.newInstance(element.getClass(), row.size()+1));
		
//		if(element instanceof Vector )
//			args[args.length -1] = (T) Vector.of(position);
//		else if(element instanceof Number)
//			args[args.length -1] = (T)(Object) position;
//		else if(element instanceof StvgpType)
//			args[args.length -1] = (T) StvgpType.of(Vector.of(position));
//		else
//			throw new IllegalArgumentException("I don't know how to extract currentPrice from type: "+element.getClass());
		
		return args;
	}




	public Double getCurrentMoney() {
		if(currentTransaction != null && currentTransaction.isOpen())
			return money + currentTransaction.unRealizedProfit(currentPrice	, timewithoutaction * penalizerRate);
		return money;
	}
	
	


	private double getCurrentPrice(List<T> row) {
		T closeValue = row.get(table.columnIndexOf("close"));
		
		if(closeValue instanceof Vector v)
			return v.last();
		else if(closeValue instanceof StvgpType st)
			return st.getAsVectorType().last();
		else if(closeValue instanceof Double d)
			return d;
		else if(closeValue instanceof ComplexVector cv)
			return cv.last().getReal();
		else
			throw new IllegalArgumentException("I don't know how to extract currentPrice from type: "+closeValue.getClass());
	}


	public static <T> MarketSimulatorBuilder<T> builder(Table<T> table){
		return new MarketSimulatorBuilder<>(table);
	}


	/*
	 * 
	 *	Static methods
	 *
	 */
	
	
	
	
	
	
	/*
	 * 
	 *Builder
	 * 
	 */
	
	
	public static class MarketSimulatorBuilder<T>{
		
		private final Table<T> table;
		private double slidingWindowPercentage = 0.05;
		private double intialInvestment = 10_000;
		private double transactionFee = 0.001;
		private double leverage = 1;
		private double penalizerRate = 0;
		private boolean compoundMode = false;
		private Pair<Integer, Integer> trainSlice;
		
		
		private MarketSimulatorBuilder(Table<T> table) {
			this.table = table;
		}
		
		public MarketSimulatorBuilder<T> slidingWindowPercentage(double slidingWindowPercentage) {
			this.slidingWindowPercentage = slidingWindowPercentage;
			return this;
		}


		public MarketSimulatorBuilder<T> intialInvestment(double intialInvestment) {
			this.intialInvestment = intialInvestment;
			return this;
		}


		public MarketSimulatorBuilder<T> transactionFee(double transactionFee) {
			this.transactionFee = transactionFee;
			return this;
		}


		public MarketSimulatorBuilder<T> leverage(double leverage) {
			this.leverage = leverage;
			return this;
		}


		public MarketSimulatorBuilder<T> penalizerRate(double penalizerRate) {
			this.penalizerRate = penalizerRate;
			return this;
		}


		public MarketSimulatorBuilder<T> compoundMode(boolean compoundMode) {
			this.compoundMode = compoundMode;
			return this;
		}
		
		public MarketSimulatorBuilder<T> trainSlice(Pair<Integer, Integer> trainSlice) {
			this.trainSlice = trainSlice;
			return this;
		}
		
		public MarketSimulator<T> build(){
			MarketSimulator<T> market = new MarketSimulator<>(table);
			market.setCompoundMode(compoundMode);
			market.setPenalizerRate(penalizerRate);
			market.setLeverage(leverage);
			market.setTransactionFee(transactionFee);
			market.setIntialInvestment(intialInvestment);
			market.setSlidingWindowPercentage(slidingWindowPercentage);
			market.setTrainSlice(trainSlice);
			return market;
		}
	}
}
