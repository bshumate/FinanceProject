package queryTypes;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import utilities.Utilities;

public class FundWorth {

	private String name;
	private float initialFundWorth;
	private TreeMap<Date, FundDailyQuote> fundDailyQuotes;

	/**
	 * Create a new FundWorth object. This will be used to track the worth of the fund over time
	 * 
	 * @param name
	 *            - The name of the new fund
	 * @param date
	 *            - The date the fund was created
	 * @param initialCashAmount
	 *            - The initial cash amount for this fund
	 */
	public FundWorth(String name, Date date, float initialCashAmount) {
		this.name = name;
		this.initialFundWorth = initialCashAmount;
		this.fundDailyQuotes = new TreeMap<Date, FundDailyQuote>();

		// Get the year, month, and day from the date
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		// Create a quote for the creation day, and add the initial cash amount
		FundDailyQuote initialQuote = new FundDailyQuote(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
				c.get(Calendar.DAY_OF_MONTH));
		initialQuote.addCash(initialCashAmount);
		fundDailyQuotes.put(date, initialQuote);
	}

	public String getName() {
		return this.name;
	}

	public Float getInitialFundWorth() {
		return this.initialFundWorth;
	}
	
	public FundDailyQuote getFundQuoteForDay(int year, int month, int day) {
		Date d = Utilities.getDateObject(year, month, day);
		FundDailyQuote quote = fundDailyQuotes.floorEntry(d).getValue();
		if (quote == null) { // The date we want is before the first element in the tree.
			// Get the first element
			quote = fundDailyQuotes.firstEntry().getValue();
		}
		return quote;
	}

	public void fundBuyFund(String name, Float dollarAmount, Date date) {
		System.out.println(this.name + " buying " + name);
		FundDailyQuote quote = fundDailyQuotes.get(date);

		// Get the year, month, and day from the date
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (quote == null) {
			Date priorEntry = fundDailyQuotes.lowerKey(date);
			if (priorEntry == null) {
				// Create a quote for the creation day, and add the initial cash amount
				quote = new FundDailyQuote(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
						c.get(Calendar.DAY_OF_MONTH));
				fundDailyQuotes.put(date, quote);
			} else {
				quote = new FundDailyQuote(fundDailyQuotes.get(priorEntry), c.get(Calendar.YEAR),
						c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
				fundDailyQuotes.put(date, quote);
			}
		}
		quote.fundBuy(name, dollarAmount);
	}

	public void fundSellFund(String name, Date date) {

	}

	// public void fundBuyCompany(String name, Float dollarAmount, Date date) {
	// // Look up how
	// }
	//
	// public void fundSellCompany(String name, Date date) {
	//
	// }

	public void stakeHolderBuy(String stakeHolderName, Float dollarAmount, Date date) {

	}

	public void stakeHolderSell(String stakeHolderName, Date date) {

	}

	public FundDailyQuote getFirstQuote() {
		return fundDailyQuotes.firstEntry().getValue();
	}
	
	public FundDailyQuote getLastQuote() {
		return fundDailyQuotes.lastEntry().getValue();
	}
}
