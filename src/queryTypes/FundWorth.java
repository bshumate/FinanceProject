package queryTypes;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import main.FinanceServlet;
import utilities.Utilities;

public class FundWorth {

	private String name;
	private float initialFundWorth;
	private float totalAdditionalCashAdded;
	private TreeMap<Date, FundDailyQuote> fundDailyQuotes;
	private HashMap<String, Date> dayFundBought;

	private static HashMap<String, FundWorth> fundWorthObjects = new HashMap<String, FundWorth>();

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
		this.totalAdditionalCashAdded = 0;
		this.fundDailyQuotes = new TreeMap<Date, FundDailyQuote>(Utilities.s);
		this.dayFundBought = new HashMap<String, Date>();

		// Get the year, month, and day from the date
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		// Create a quote for the creation day, and add the initial cash amount
		FundDailyQuote initialQuote = new FundDailyQuote(this.name, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
				c.get(Calendar.DAY_OF_MONTH));
		initialQuote.addCash(initialCashAmount);
		fundDailyQuotes.put(date, initialQuote);

		// Place this FundWorth Object in the list of all FundWorth objects
		fundWorthObjects.put(name, this);
	}

	public String getName() {
		return this.name;
	}

	public Float getInitialFundWorth() {
		return this.initialFundWorth;
	}

	public FundDailyQuote getFundQuoteForDay(int year, int month, int day) {
		Date d = Utilities.getDateObject(year, month, day);
		Entry<Date, FundDailyQuote> e = fundDailyQuotes.floorEntry(d);
		FundDailyQuote existingQuote = (e == null ? null : e.getValue());
		FundDailyQuote quoteToReturn = null;
		if (existingQuote == null) { // The date we want is before the first element in the tree.
			// Get the first element
			quoteToReturn = fundDailyQuotes.firstEntry().getValue();
		} else {
			quoteToReturn = new FundDailyQuote(existingQuote, year, month, day);
		}
		return quoteToReturn;
	}

	public void addCash(Float dollarAmount, Date date) {
		FundDailyQuote quote = fundDailyQuotes.get(date);
		if (quote == null) {
			quote = addFundDailyQuote(date);
		}
		quote.addCash(dollarAmount);
		this.totalAdditionalCashAdded += dollarAmount;
	}

	public void fundBuyFund(String purchasedName, Float dollarAmount, Date date) {
		//System.out.println("FundBuyFund: " + this.name + " buying " + purchasedName);
		FundDailyQuote quote = fundDailyQuotes.get(date);
		if (quote == null) {
			quote = addFundDailyQuote(date);
		}
		quote.fundBuy(purchasedName, dollarAmount);
		dayFundBought.put(purchasedName, date);
	}

	public float fundSellFund(String soldName, Date date) {
		//System.out.println("FundSellFund: " + this.name + " selling " + soldName);
		FundDailyQuote quote = fundDailyQuotes.get(date);
		if (quote == null) {
			quote = addFundDailyQuote(date);
		}
		return quote.fundSell(soldName, dayFundBought.get(soldName));
		// dayFundBought.remove(soldName);
	}

	public void shareHolderBuy(String shareHolderName, Float dollarAmount, Date date) {
		//System.out.println("ShareHolderBuy: " + shareHolderName + " buying " + this.name);
		FundDailyQuote quote = fundDailyQuotes.get(date);
		if (quote == null) {
			quote = addFundDailyQuote(date);
		}
		quote.shareHolderBuy(shareHolderName, dollarAmount);
	}

	public float shareHolderSell(String shareHolderName, Date date) {
		//System.out.println("ShareHolderSell: " + shareHolderName + " selling " + this.name);
		FundDailyQuote quote = fundDailyQuotes.get(date);
		if (quote == null) {
			quote = addFundDailyQuote(date);
		}
		return quote.shareHolderSell(shareHolderName);
	}

	public FundDailyQuote getFirstQuote() {
		return fundDailyQuotes.firstEntry().getValue();
	}

	public FundDailyQuote getLastQuote() {
		return fundDailyQuotes.lastEntry().getValue();
	}

	public String getMajorityParticipant() {
		return this.getLastQuote().getMajorityParticipant();
	}
	
	private FundDailyQuote addFundDailyQuote(Date date) {
		FundDailyQuote quote;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		Date priorEntry = fundDailyQuotes.lowerKey(date);
		if (priorEntry == null) {
			// Create a quote for the creation day, and add the initial cash amount
			quote = new FundDailyQuote(this.name, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
					c.get(Calendar.DAY_OF_MONTH));
			fundDailyQuotes.put(date, quote);
		} else {
			quote = new FundDailyQuote(fundDailyQuotes.get(priorEntry), c.get(Calendar.YEAR),
					c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
			fundDailyQuotes.put(date, quote);
		}
		return quote;
	}

	public static void updateDayFundBought(String myName, String targetFundName, Date newDate) {
		FundWorth.fundWorthObjects.get(myName).dayFundBought.put(targetFundName, newDate);
	}

	public static Date getDayFundBought(String myName, String targetFundName) {
		return FundWorth.fundWorthObjects.get(myName).dayFundBought.get(targetFundName);
	}

	public static float calcFundPercentReturn(String myName, String targetFundName, Date toDate) {
		// Assume the From Date is the purchase date of the target fund
		return calcFundPercentReturn(myName, targetFundName,
				FundWorth.fundWorthObjects.get(myName).dayFundBought.get(targetFundName), toDate);
	}

	public static float calcFundPercentReturn(String myName, String targetFundName, Date fromDate, Date toDate) {
		float startingWorth = 0;
		float endingWorth = 0;

		FundWorth fw = FundWorth.fundWorthObjects.get(targetFundName);

		// Find the starting worth of the fund for this time interval
		FundDailyQuote fromQuote = fw.getFundQuoteForDay(Utilities.getYear(fromDate), Utilities.getMonth(fromDate),
				Utilities.getDay(fromDate));
		startingWorth = (fromQuote.getNetWorth() * (1 - fromQuote.getTotalShareHolderStake()));

		// Find the ending worth of the fund for this time interval
		FundDailyQuote toQuote = fw.getFundQuoteForDay(Utilities.getYear(toDate), Utilities.getMonth(toDate),
				Utilities.getDay(toDate));
		endingWorth = (toQuote.getNetWorth() * (1 - toQuote.getTotalShareHolderStake()));
		endingWorth -= fw.totalAdditionalCashAdded; // The fund adding cash to itself shouldn't affect rate of return

		return ((endingWorth - startingWorth) / startingWorth);
	}
	
	public static float calcTotalFundPercentReturn(FundWorth fw, FundDailyQuote toQuote, float startNetWorth, float endNetWorth) {
		float adjustedEndNetWorth = (endNetWorth * (1 - toQuote.getTotalShareHolderStake())) - fw.totalAdditionalCashAdded;
		return ((adjustedEndNetWorth - startNetWorth) / startNetWorth);
	}

	public static void clearFundWorthSet() {
		fundWorthObjects.clear();
	}

	public String toString() {
		String s = "FundWorth for " + this.name + ". Initial Value: " + this.initialFundWorth + ".\n";
		for (Date d : this.fundDailyQuotes.keySet()) {
			FundDailyQuote q = this.fundDailyQuotes.get(d);
			s += d.toString() + ". " + q.toString() + "\n";
		}
		return s;
	}
}
