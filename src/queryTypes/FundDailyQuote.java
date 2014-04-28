package queryTypes;

import java.util.Date;
import java.util.HashMap;

import utilities.Utilities;

public class FundDailyQuote {

	private String fundDailyQuoteSecurityName;
	private int year;
	private int month;
	private int day;
	private float cashAmount;
	private HashMap<String, Float> numSharesPerSecurity; // Keeps track of the number of shares owned by this fund (or
															// dollar amount if the security is a fund)
	private HashMap<String, Float> shareHolders; // Keeps track of the percent stake each share holder has in the fund

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
	 *            - The quote to be copied
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

	public float getNetWorth() {
		return this.cashAmount + getInvestmentAmount();
	}

	public float getCashAmount() {
		return this.cashAmount;
	}

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

	public void addCash(float cashAdded) {
		cashAmount += cashAdded;
	}

	public void fundBuy(String name, Float dollarAmount) {
		float shares = dollarAmount;
		// float oldNetWorth = cashAmount + investmentAmount;

		// Check to see if the fund is a company. If so, lookup how many shares to buy
		if (Utilities.isCompany(name)) {
			float quote = CompanyQuery.getQuoteOfCompany(name, this.year, this.month, this.day);
			shares = (dollarAmount / quote);
			numSharesPerSecurity.put(name, shares);
			System.out.println("Buy shares: " + shares + " shares. Quote: " + quote + ". Amount: " + dollarAmount);
		}
		// If the fund is a portfolio, then just store the dollar amount invested. That porfolio will keep track of
		// investment stake
		else {
			numSharesPerSecurity.put(name, dollarAmount);
		}
		cashAmount -= dollarAmount;
	}

	public void fundSell(String name, Date originalPurchaseDate) {
		float originalInvestment = numSharesPerSecurity.get(name);
		float sellAmount;
		if (Utilities.isCompany(name)) {
			float quote = CompanyQuery.getQuoteOfCompany(name, year, month, day);
			sellAmount = quote * numSharesPerSecurity.get(name);
			System.out.println("OriginalInvestment: " + originalInvestment + " shares. Quote: " + quote
					+ ". SellAmount: " + sellAmount);
		} else {
			float percentReturn = FundWorth.calcFundPercentReturn(this.fundDailyQuoteSecurityName, name,
					originalPurchaseDate, Utilities.getDateObject(year, month, day));
			sellAmount = originalInvestment + (originalInvestment * percentReturn);
		}
		cashAmount += sellAmount;
		numSharesPerSecurity.remove(name);
	}

	public void shareHolderBuy(String shareHolderName, Float dollarAmount) {
		// Someone is investing in this fund. Split up their investment amount according to the current allocation. This
		// will involve getting the most recent price quotes for each stock owned. Additional stock purchases will be
		// treated as if an entirely new set of shares is bought for a single price on this day.
		float netWorth = this.getNetWorth();
		float newCashAmount = this.cashAmount + (dollarAmount * (this.cashAmount / netWorth));
		for (String s : numSharesPerSecurity.keySet()) {
			float quote = CompanyQuery.getQuoteOfCompany(s, year, month, day);
			float dollarAmountForS = quote * numSharesPerSecurity.get(s);
			float additionalDollarAmountForS = dollarAmount * (dollarAmountForS / netWorth);
			//this.fundSell(s, FundWorth.getDayFundBought(this.fundDailyQuoteSecurityName, s));
			this.fundBuy(s, (dollarAmountForS + additionalDollarAmountForS));
			FundWorth.updateDayFundBought(this.fundDailyQuoteSecurityName, s, Utilities.getDateObject(day, month, day));
		}
		this.cashAmount = newCashAmount; // Reset the cash
		float newNetWorth = this.getNetWorth();
		updateCurrentShareholdersStake(netWorth, newNetWorth);
		this.shareHolders.put(shareHolderName, (dollarAmount/newNetWorth));
	}

	public void shareHolderSell(String shareHolderName) {
		// Someone is selling out of this fund. Sell out of each currently owned fund by a percentage equivalent to the
		// amount removed from the fund.
		float netWorth = this.getNetWorth();
		float amountSoldFromFund = this.shareHolders.get(shareHolderName) * netWorth;
		float newCashAmount = this.cashAmount - (amountSoldFromFund * (this.cashAmount / netWorth));
		for (String s : numSharesPerSecurity.keySet()) {
			float quote = CompanyQuery.getQuoteOfCompany(s, year, month, day);
			float dollarAmountForS = quote * numSharesPerSecurity.get(s);
			float lessDollarAmountForS = amountSoldFromFund * (dollarAmountForS / netWorth);
			//this.fundSell(s, FundWorth.getDayFundBought(this.fundDailyQuoteSecurityName, s));
			this.fundBuy(s, (dollarAmountForS - lessDollarAmountForS));
			FundWorth.updateDayFundBought(this.fundDailyQuoteSecurityName, s, Utilities.getDateObject(year, month, day));
		}
		this.cashAmount = newCashAmount;
		float newNetWorth = this.getNetWorth();
		this.shareHolders.remove(shareHolderName);
		updateCurrentShareholdersStake(netWorth, newNetWorth);
		
	}

	public void updateCurrentShareholdersStake(float oldNetWorth, float newNetWorth) {
		for (String s : this.shareHolders.keySet()) {
			Float oldPercentage = this.shareHolders.get(s);
			Float newPercentage = oldPercentage * (oldNetWorth / newNetWorth);
			this.shareHolders.put(s, newPercentage);
			System.out.println("For fund " + this.fundDailyQuoteSecurityName + ": " + s + " now has a " + newPercentage
					* 100 + "% stake.");
		}
	}

	public float getTotalShareHolderStake() {
		float totalStake = 0;
		for (Float f : shareHolders.values()) {
			totalStake += f;
		}
		return totalStake;
	}

	// public void addCash2(float cashAdded) {
	// float oldNetWorth = cashAmount + investmentAmount;
	// cashAmount += cashAdded;
	// float newNetWorth = cashAmount + investmentAmount;
	//
	// // Re-calculate investment percentage of each stock owned
	// for (String s : numSharesPerSecurity.keySet()) {
	// float oldPercentage = numSharesPerSecurity.get(s);
	// numSharesPerSecurity.put(s, ((oldPercentage * oldNetWorth) / newNetWorth));
	// }
	// }

	public String toString() {
		return "Cash: " + cashAmount + ". Investments: " + getInvestmentAmount() + ". Shares per security: "
				+ numSharesPerSecurity + ". Stakeholders: " + shareHolders;
	}
}
