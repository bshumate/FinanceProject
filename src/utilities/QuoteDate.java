package utilities;

/**
 * QuoteDate can be used as a key in a map that caches stock quotes. It allows the user to store the combination of a
 * ticker an a date.
 * 
 * @author Ben_Shumate
 * 
 */
public class QuoteDate {

	public String ticker;
	public int year;
	public int month;
	public int day;

	/**
	 * @param ticker
	 * @param year
	 * @param month
	 * @param day
	 */
	public QuoteDate(String ticker, int year, int month, int day) {
		this.ticker = ticker;
		this.year = year;
		this.month = month;
		this.day = day;
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 */
	public void setNew(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	/**
	 * Compares two quote dates. Their ticker, year, month, and day must be the same to be equal.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		// System.out.println("Calling equals for QuoteDate");
		if (!(other instanceof QuoteDate)) {
			return false;
		}
		QuoteDate d2 = (QuoteDate) other;
		return (this.ticker.equals(d2.ticker) && this.year == d2.year && this.month == d2.month && this.day == d2.day);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// System.out.println("Calling hashCode for QuoteDate");
		return (((year << 27) | (month << 22) | (day << 17)) | ticker.hashCode());
	}

}
