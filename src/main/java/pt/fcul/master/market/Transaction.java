package pt.fcul.master.market;

import java.io.Serializable;

import lombok.Data;

@Data
public class Transaction implements Serializable{

	private static final long serialVersionUID = 1L;

	private MarketAction type;

	private double shares;
	
	private double openPrice;
	private int openIndex;
	
	private double closePrice = -1;
	private int closeIndex = -1;
	
	private double penalization;
	private double transactionFee;
	
	
	public Transaction(MarketAction type, double shares, double openPrice, int openIndex, double transactionFee) {
		this.type = type;
		this.shares = shares;
		this.openPrice = openPrice;
		this.openIndex = openIndex;
		this.transactionFee = transactionFee;
	}

	
	public boolean isOpen() {
		return closeIndex < 0 || closePrice < 0;
	}



	public boolean isClose() {
		return !isOpen();
	}
	
	
	
	
	public double unRealizedProfit(double currentPrice, double penalization) {
		if(isClose())
			throw new IllegalStateException("This transaction is closed, so invalid call on unrealizedProfit");
		
		return calculateProfit(currentPrice,penalization);
	}
	
	
	
	
	private double calculateProfit(double currentPrice, double penalization) {
		double profit = type == MarketAction.BUY ?  shares * currentPrice : (2 * getInitialMoney() - shares * currentPrice);
		
		return profit * (1D - transactionFee) - penalization - getInitialMoney();
	}

	
	

	public double realizedProfit() {
		if(isOpen())
			throw new IllegalStateException("This transaction is open, so invalid call on realizedProfit");
		
		return calculateProfit(closePrice, penalization);
	}

	
	

	public void close(int index, double closePrice, double penalization) {
		this.closeIndex = index;
		this.closePrice = closePrice;
		this.penalization = penalization;
	}


	public double getInitialMoney() {
		return openPrice *  shares;
	}


	public String toFileString() {
		return type +","+ getInitialMoney() +","+ realizedProfit() +","+ shares +","+ openPrice +","+ openIndex +","+ closePrice +","+ closeIndex +","+ penalization +","+ transactionFee;
	}


	public static String fileColumns() {
		return "Type,Intial Money,Realized Profit,Shares,Open Price,Open Index,Close Price,Close Index,Penalization,Transaction Fee";
	}
}
