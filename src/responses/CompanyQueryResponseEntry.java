package responses;


public class CompanyQueryResponseEntry {

	public float startPrice = -1;
	public float endPrice = -1;
	public float returnRate = -1;
	public float high = -1;
	public float low = -1;
	public float risk = -1;
	
	public static int START_PRICE = 0;
	public static int END_PRICE = 1;
	public static int RETURN_RATE = 2;
	public static int HIGH = 3;
	public static int LOW = 4;
	public static int RISK = 5;

	public CompanyQueryResponseEntry() {
		// Default constructor
	}
	
	public float getAttribute(int id) {
		if(id == START_PRICE)
			return startPrice;
		else if(id == END_PRICE)
			return endPrice;
		else if(id == RETURN_RATE)
			return returnRate;
		else if(id == HIGH)
			return high;
		else if(id == LOW)
			return low;
		else if(id == RISK)
			return risk;
		else
			return -1;
	}
	
	public void setAttribute(int id, float attr) {
		if(id == START_PRICE)
			startPrice = attr;
		else if(id == END_PRICE)
			endPrice = attr;
		else if(id == RETURN_RATE)
			returnRate = attr;
		else if(id == HIGH)
			high = attr;
		else if(id == LOW)
			low = attr;
		else if(id == RISK)
			risk = attr;
	}
	
}
