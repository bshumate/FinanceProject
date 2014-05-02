package utilities;

public class QuoteDate {

	public String ticker;
	public int year;
	public int month;
	public int day;
	
	public QuoteDate(String ticker, int year, int month, int day) {
		this.ticker = ticker;
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public void setNew(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	
	@Override
	public boolean equals(Object other) {
		//System.out.println("Calling equals for QuoteDate");
		if (!(other instanceof QuoteDate)) {
			return false;
		}
		QuoteDate d2 = (QuoteDate) other;
		return (this.ticker.equals(d2.ticker) && this.year == d2.year && this.month == d2.month && this.day == d2.day);
	}
	
	@Override
	public int hashCode() {
		//System.out.println("Calling hashCode for QuoteDate");
		return (((year << 24) | (month << 16) | (day << 8)) | ticker.hashCode());
	}
	
}
