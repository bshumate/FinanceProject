package queryTypes;

import java.util.Date;
import java.util.HashMap;

import utilities.Utilities;

/**
 * @author Ben_Shumate
 * 
 */
public class FundDailyQuote {

	private String fundDailyQuoteSecurityName;
	private int year;
	private int month;
	private int day;
	private float cashAmount;
	private HashMap<String, Float> numSharesPerSecurity; // Keeps track of the number of shares owned by this fund (or
															// dollar amount if the security is a fund)
	private HashMap<String, Float> shareHolders; // Keeps track of the percent stake each share holder has in the fund

	/**
	 * Creates a new FundDailyQuote object according to the given parameters. By default, there are no shareholders or
	 * securities held, and net worth is 0
	 * 
	 * @param name
	 * @param year
	 * @param month
	 * @param day
	 */
	public FundDailyQuote(String name, int year, int month, int day) {
		this.fundDailyQuoteSecurityName = name;
		cashAmount = 0;
		numSharesPerSecurity = new HashMap<String, Float>();
		shareHolders = new HashMap<String, Float>();
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
	 *            The {@code FundDailyQuote} object to be copied
	 * @param year
	 * @param month
	 * @param day
	 */
	public FundDailyQuote(FundDailyQuote oldQuote, int year, int month, int day) {
		this.fundDailyQuoteSecurityName = oldQuote.fundDailyQuoteSecurityName;
		this.cashAmount = oldQuote.cashAmount;

		// Deep copy of numSharesPerSecurity
		this.numSharesPerSecurity = new HashMap<String, Float>();
		for (String s : oldQuote.numSharesPerSecurity.keySet()) {
			float shareValue = oldQuote.numSharesPerSecurity.get(s);
			this.numSharesPerSecurity.put(s, shareValue);
		}
		// Deep copy of stakeHolders
		this.shareHolders = new HashMap<String, Float>();
		for (String s : oldQuote.shareHolders.keySet()) {
			float initialDollarAmount = oldQuote.shareHolders.get(s);
			this.shareHolders.put(s, initialDollarAmount);
		}

		this.year = year;
		this.month = month;
		this.day = day;
	}

	/**
	 * @return The net worth of the fund on this day, which is cash worth + investment worth
	 */
	public float getNetWorth() {
		return this.cashAmount + getInvestmentAmount();
	}

	/**
	 * @return The cash worth of the fund
	 */
	public float getCashAmount() {
		return this.cashAmount;
	}

	/**
	 * @return The investment worth of the fund
	 */
	public float getInvestmentAmount() {
		float investmentAmount = 0;
		for (String s : numSharesPerSecurity.keySet()) {
			// If the security is a company, s_val will be a number of shares owned.
			// If the security is a fund, s_val will be a dollar amount.
			float s_val = numSharesPerSecurity.get(s);
			if (Utilities.isCompany(s)) {
				float quote = CompanyQuery.getQuoteOfCompany(s, this.year, this.month, this.day);
				investmentAmount += (quote * s_val);
			} else {
				float percentReturn = FundWorth.calcFundPercentReturn(this.fundDailyQuoteSecurityName, s,
						Utilities.getDateObject(year, month, day));
				investmentAmount += (s_val + (s_val * percentReturn));
			}
		}
		return investmentAmount;
	}

	/**
	 * @param cashAdded
	 *            Amount of cash to add to the fund
	 */
	public void addCash(float cashAdded) {
		float oldNetWorth = this.getNetWorth();
		cashAmount += cashAdded;
		float newNetWorth = this.getNetWorth();

		// The stake of each shareholder will have changed since the net worth of the fund changed
		updateCurrentShareholdersStake(oldNetWorth, newNetWorth);
	}

	/**
	 * @param name
	 *            Name of the security to buy
	 * @param dollarAmount
	 *            Amount of the security to purchase
	 */
	public void fundBuy(String name, Float dollarAmount) {
		float shares = dollarAmount;

		// Check to see if the fund is a company. If so, lookup how many shares to buy
		if (Utilities.isCompany(name)) {
			float quote = CompanyQuery.getQuoteOfCompany(name, this.year, this.month, this.day);
			shares = (dollarAmount / quote);
			numSharesPerSecurity.put(name, shares);
		}
		// If the fund is a portfolio, then just store the dollar amount invested. That porfolio will keep track of
		// investment stake
		else {
			numSharesPerSecurity.put(name, dollarAmount);
		}
		cashAmount -= dollarAmount;
	}

	/**
	 * @param name
	 *            Name of the security to sell
	 * @param originalPurchaseDate
	 *            Date that this security was originally bought. This is needed to calculate percent return
	 * @return The amount of cash raised from this sale
	 */
	public float fundSell(String name, Date originalPurchaseDate) {
		float originalInvestment = numSharesPerSecurity.get(name);
		float sellAmount;
		// If the security is a company, look up a current quote to calculate sell price
		if (Utilities.isCompany(name)) {
			float quote = CompanyQuery.getQuoteOfCompany(name, year, month, day);
			sellAmount = quote * numSharesPerSecurity.get(name);
		}
		// If the security is a fund, calculate that fund's return over time
		else {
			float percentReturn = FundWorth.calcFundPercentReturn(this.fundDailyQuoteSecurityName, name,
					originalPurchaseDate, Utilities.getDateObject(year, month, day));
			sellAmount = originalInvestment + (originalInvestment * percentReturn);
		}
		cashAmount += sellAmount;
		numSharesPerSecurity.remove(name);
		return sellAmount;
	}

	/**
	 * A shareholder is buying this fund. The fund should distribute the dollar amount added according to its current
	 * allocation. So if the fund is 40% in cash, 25% in stock 1, and 35% in stock 2, it will buy a proportionate amount
	 * of each
	 * 
	 * @param shareHolderName
	 *            The name of the shareholder
	 * @param dollarAmount
	 */
	public void shareHolderBuy(String shareHolderName, Float dollarAmount) {
		// Someone is investing in this fund. Split up their investment amount according to the current allocation. This
		// will involve getting the most recent price quotes for each stock owned. Additional stock purchases will be
		// treated as if an entirely new set of shares is bought for a single price on this day.
		float oldNetWorth = this.getNetWorth();
		float newCashAmount = this.cashAmount + (dollarAmount * (this.cashAmount / oldNetWorth));
		for (String s : numSharesPerSecurity.keySet()) {
			float quote = CompanyQuery.getQuoteOfCompany(s, year, month, day);
			float dollarAmountForS = quote * numSharesPerSecurity.get(s);
			float additionalDollarAmountForS = dollarAmount * (dollarAmountForS / oldNetWorth);
			// this.fundSell(s, FundWorth.getDayFundBought(this.fundDailyQuoteSecurityName, s));
			this.fundBuy(s, (dollarAmountForS + additionalDollarAmountForS));
			FundWorth.updateDayFundBought(this.fundDailyQuoteSecurityName, s, Utilities.getDateObject(day, month, day));
		}
		this.cashAmount = newCashAmount; // Reset the cash
		float newNetWorth = this.getNetWorth();
		updateCurrentShareholdersStake(oldNetWorth, newNetWorth);
		this.shareHolders.put(shareHolderName, (dollarAmount / newNetWorth));
	}

	/**
	 * A shareholder is selling this fund. The fund should sell the dollar amount added according to its current
	 * allocation. So if the fund is 40% in cash, 25% in stock 1, and 35% in stock 2, it will sell a proportionate
	 * amount of each
	 * 
	 * @param shareHolderName
	 * @return The amount of cash removed from the fund
	 */
	public float shareHolderSell(String shareHolderName) {
		// Someone is selling out of this fund. Sell out of each currently owned fund by a percentage equivalent to the
		// amount removed from the fund.
		float oldNetWorth = this.getNetWorth();
		float amountSoldFromFund = this.shareHolders.get(shareHolderName) * oldNetWorth;
		float newCashAmount = this.cashAmount - (amountSoldFromFund * (this.cashAmount / oldNetWorth));
		// For each security, overwrite the old buy and buy back the current number of shares/amount in the fund
		for (String s : numSharesPerSecurity.keySet()) {
			float quote = CompanyQuery.getQuoteOfCompany(s, year, month, day);
			float dollarAmountForS = quote * numSharesPerSecurity.get(s);
			float lessDollarAmountForS = amountSoldFromFund * (dollarAmountForS / oldNetWorth);
			this.fundBuy(s, (dollarAmountForS - lessDollarAmountForS));
			FundWorth
					.updateDayFundBought(this.fundDailyQuoteSecurityName, s, Utilities.getDateObject(year, month, day));
		}
		this.cashAmount = newCashAmount;
		float newNetWorth = this.getNetWorth();
		this.shareHolders.remove(shareHolderName);
		updateCurrentShareholdersStake(oldNetWorth, newNetWorth);
		return amountSoldFromFund;
	}

	/**
	 * Given an old net worth and a new net worth for the fund, update the percentage stake that each shareholder has
	 * 
	 * @param oldNetWorth
	 * @param newNetWorth
	 */
	public void updateCurrentShareholdersStake(float oldNetWorth, float newNetWorth) {
		for (String s : this.shareHolders.keySet()) {
			Float oldPercentage = this.shareHolders.get(s);
			Float newPercentage = oldPercentage * (oldNetWorth / newNetWorth);
			this.shareHolders.put(s, newPercentage);
			// System.out.println("For fund " + this.fundDailyQuoteSecurityName + ": " + s + " now has a " +
			// newPercentage
			// * 100 + "% stake.");
		}
	}

	/**
	 * Find the shareholder that has the greatest percent stake in this fund
	 * 
	 * @return shareholder with the greatest stake
	 */
	public String getMajorityParticipant() {
		String majority = "";
		float maxShare = 0;
		for (String s : this.shareHolders.keySet()) {
			Float shareForS = this.shareHolders.get(s);
			if (shareForS != null && shareForS > maxShare) {
				maxShare = shareForS;
				majority = s;
			}
		}
		return majority;
	}

	/**
	 * Adds up all of the shareholder stakes
	 * 
	 * @return The total percentage of this fund owned by its outside shareholders
	 */
	public float getTotalShareHolderStake() {
		float totalStake = 0;
		for (Float f : shareHolders.values()) {
			totalStake += f;
		}
		return totalStake;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Cash: " + cashAmount + ". Investments: " + getInvestmentAmount() + ". Shares per security: "
				+ numSharesPerSecurity + ". Stakeholders: " + shareHolders;
	}
}
