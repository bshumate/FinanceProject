package queryTypes;

import java.util.HashMap;

import utilities.Utilities;

public class FundDailyQuote {

	private int year;
	private int month;
	private int day;
	private float cashAmount;
	private float investmentAmount;
	private HashMap<String, Float> numSharesPerSecurity; // Keeps track of the number of shares owned by this fund (or
															// dollar amount if the security is a fund)
	private HashMap<String, Float> stakeHolders; // Keeps track of the percentage stake each investor has in this fund

	public FundDailyQuote(int year, int month, int day) {
		cashAmount = 0;
		investmentAmount = 0;
		numSharesPerSecurity = new HashMap<String, Float>();
		stakeHolders = new HashMap<String, Float>();
		this.year = year;
		this.month = month;
		this.day = day;
	}

	/**
	 * Initialize this FundDailyQuote object according to another FundDailyQuote object. This will be done when
	 * inserting new quotes - it is convenient to be able to copy over the most recent quote before making changes
	 * according to current transactions
	 * 
	 * @param oldQuote
	 *            - The quote to be copied
	 */
	public FundDailyQuote(FundDailyQuote oldQuote, int year, int month, int day) {
		cashAmount = oldQuote.cashAmount;
		investmentAmount = oldQuote.investmentAmount;
		// Deep copy of numSharesPerSecurity
		numSharesPerSecurity = new HashMap<String, Float>();
		for (String s : oldQuote.numSharesPerSecurity.keySet()) {
			float shareValue = oldQuote.numSharesPerSecurity.get(s);
			numSharesPerSecurity.put(s, shareValue);
		}
		// Deep copy of stakeHolders
		stakeHolders = new HashMap<String, Float>();
		for (String s : oldQuote.stakeHolders.keySet()) {
			float percentStake = oldQuote.numSharesPerSecurity.get(s);
			stakeHolders.put(s, percentStake);
		}
		
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public float getNetWorth() {
		return this.cashAmount + this.investmentAmount;
	}

	public float getCashAmount() {
		return this.cashAmount;
	}

	public float getInvestmentAmount() {
		return this.investmentAmount;
	}

	public void addCash(float cashAdded) {
		cashAmount += cashAdded;
	}

	public void fundBuy(String name, Float dollarAmount) {
		float shares = dollarAmount;
		float oldNetWorth = cashAmount + investmentAmount;
		
		// Check to see if the fund is a company. If so, lookup how many shares to buy
		if (Utilities.isCompany(name)) {
			float quote = CompanyQuery.getQuoteOfCompany(name, this.year, this.month, this.day);
			shares = (dollarAmount / quote);
		}
		investmentAmount += dollarAmount;
		cashAmount -= dollarAmount;
	}

	public void fundSell(String name) {

	}

	public void stakeHolderBuy(String stakeHolderName, Float dollarAmount) {

	}

	public void stakeHolderSell(String stakeHolderName) {

	}

	public void addCash2(float cashAdded) {
		float oldNetWorth = cashAmount + investmentAmount;
		cashAmount += cashAdded;
		float newNetWorth = cashAmount + investmentAmount;

		// Re-calculate investment percentage of each stock owned
		for (String s : numSharesPerSecurity.keySet()) {
			float oldPercentage = numSharesPerSecurity.get(s);
			numSharesPerSecurity.put(s, ((oldPercentage * oldNetWorth) / newNetWorth));
		}
	}

	public String toString() {
		return "Cash: " + cashAmount + ". Investments: " + investmentAmount + ". Shares per security: "
				+ numSharesPerSecurity + ". Stakeholders: " + stakeHolders;
	}
}
