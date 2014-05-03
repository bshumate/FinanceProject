package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Contains Utility methods for other classes to use
 * 
 * @author Ben_Shumate
 * 
 */
public class Utilities {

	/**
	 * The earliest allowable date in the system
	 */
	public static final int EARLIEST_YEAR = 2005;
	/**
	 * The earliest allowable date in the system
	 */
	public static final int EARLIEST_MONTH = 1;
	/**
	 * The earliest allowable date in the system
	 */
	public static final int EARLIEST_DAY = 3;

	/**
	 * Comparator for Date objects. Compares only year, month and day - if all 3 of these are equal, the Date objects
	 * compare as equal
	 */
	public static Comparator<Date> s = new Comparator<Date>() {
		@Override
		public int compare(Date date1, Date date2) {
			if (getYear(date1) < getYear(date2)) {
				return -1;
			} else if (getYear(date1) > getYear(date2)) {
				return 1;
			} else {
				if (getMonth(date1) < getMonth(date2)) {
					return -1;
				} else if (getMonth(date1) > getMonth(date2)) {
					return 1;
				} else {
					if (getDay(date1) < getDay(date2)) {
						return -1;
					} else if (getDay(date1) > getDay(date2)) {
						return 1;
					}
				}
			}
			return 0;
		}

	};

	/**
	 * A list of all processable S&P500 stocks
	 */
	public static final String stockList[] = { "MMM", "ABT", "ACE", "ACN", "ACT", "ADBE", "AES", "AET", "AFL", "A",
			"GAS", "APD", "ARG", "AKAM", "AA", "ALXN", "ATI", "AGN", "ADS", "ALL", "ALTR", "MO", "AMZN", "AEE", "AEP",
			"AXP", "AIG", "AMT", "AMP", "ABC", "AME", "AMGN", "APH", "APC", "ADI", "AON", "APA", "AIV", "AAPL", "AMAT",
			"ADM", "AIZ", "T", "ADSK", "ADP", "AN", "AZO", "AVB", "AVY", "AVP", "BHI", "BLL", "BAC", "BK", "BCR",
			"BAX", "BBT", "BEAM", "BDX", "BBBY", "BMS", "BBY", "BIIB", "BLK", "HRB", "BA", "BWA", "BXP", "BSX", "BMY",
			"BRCM", "CHRW", "CA", "CVC", "COG", "CAM", "CPB", "COF", "CAH", "KMX", "CCL", "CAT", "CBG", "CBS", "CELG",
			"CNP", "CTL", "CERN", "CF", "SCHW", "CHK", "CVX", "CMG", "CB", "CI", "CINF", "CTAS", "CSCO", "C", "CTXS",
			"CLF", "CLX", "CME", "CMS", "COH", "KO", "CCE", "CTSH", "CL", "CMCSA", "CMA", "CSC", "CAG", "COP", "CNX",
			"ED", "STZ", "GLW", "COST", "COV", "CCI", "CSX", "CMI", "CVS", "DHI", "DHR", "DRI", "DVA", "DE", "DAL",
			"DNR", "XRAY", "DVN", "DO", "DTV", "DFS", "DISCA", "DLTR", "D", "DOV", "DOW", "DTE", "DD", "DUK", "DNB",
			"ETFC", "EMN", "ETN", "EBAY", "ECL", "EIX", "EW", "EA", "EMC", "EMR", "ESV", "ETR", "EOG", "EQT", "EFX",
			"EQR", "EL", "EXC", "EXPE", "EXPD", "ESRX", "XOM", "FFIV", "FDO", "FAST", "FDX", "FIS", "FITB", "FSLR",
			"FE", "FISV", "FLIR", "FLS", "FLR", "FMC", "FTI", "F", "FRX", "FOSL", "BEN", "FCX", "FTR", "GME", "GCI",
			"GPS", "GRMN", "GD", "GE", "GIS", "GPC", "GNW", "GILD", "GS", "GT", "GOOGL", "GWW", "HAL", "HOG", "HAR",
			"HRS", "HIG", "HAS", "HCP", "HCN", "HP", "HES", "HPQ", "HD", "HON", "HRL", "HSP", "HST", "HCBK", "HUM",
			"HBAN", "ITW", "IR", "TEG", "INTC", "ICE", "IBM", "IGT", "IP", "IPG", "IFF", "INTU", "ISRG", "IVZ", "IRM",
			"JBL", "JEC", "JNJ", "JCI", "JOY", "JPM", "JNPR", "KSU", "K", "KEY", "KMB", "KLAC", "KSS", "KR", "LB",
			"LLL", "LH", "LRCX", "LM", "LEG", "LEN", "LUK", "LLY", "LNC", "LLTC", "LMT", "L", "LOW", "LSI", "MTB",
			"MAC", "M", "MRO", "MAR", "MMC", "MAS", "MA", "MAT", "MKC", "MCD", "MHFI", "MCK", "MWV", "MDT", "MRK",
			"MET", "MCHP", "MU", "MSFT", "MHK", "TAP", "MDLZ", "MON", "MNST", "MCO", "MS", "MOS", "MSI", "MUR", "MYL",
			"NBR", "NDAQ", "NOV", "NTAP", "NFLX", "NWL", "NFX", "NEM", "NEE", "NKE", "NI", "NE", "NBL", "JWN", "NSC",
			"NTRS", "NOC", "NU", "NRG", "NUE", "NVDA", "ORLY", "OXY", "OMC", "OKE", "ORCL", "OI", "PCG", "PCAR", "PLL",
			"PH", "PDCO", "PAYX", "BTU", "PNR", "PBCT", "POM", "PEP", "PKI", "PRGO", "PETM", "PFE", "PNW", "PXD",
			"PBI", "PCL", "PNC", "RL", "PPG", "PPL", "PX", "PCP", "PCLN", "PFG", "PG", "PGR", "PLD", "PRU", "PSA",
			"PHM", "PVH", "PWR", "QCOM", "DGX", "RRC", "RTN", "RHT", "REGN", "RF", "RSG", "RAI", "RHI", "ROK", "COL",
			"ROP", "ROST", "RDC", "R", "SWY", "CRM", "SNDK", "SCG", "SLB", "STX", "SEE", "SRE", "SHW", "SIAL", "SPG",
			"SLM", "SJM", "SNA", "SO", "LUV", "SWN", "SE", "STJ", "SWK", "SPLS", "SBUX", "HOT", "STT", "SRCL", "SYK",
			"STI", "SYMC", "SYY", "TROW", "TGT", "TEL", "TE", "THC", "TDC", "TSO", "TXN", "TXT", "HSY", "TRV", "TMO",
			"TIF", "TWX", "TWC", "TJX", "TMK", "TSS", "TSCO", "RIG", "FOXA", "TSN", "TYC", "USB", "UNP", "UNH", "UPS",
			"X", "UTX", "UNM", "URBN", "VFC", "VLO", "VAR", "VTR", "VRSN", "VZ", "VRTX", "VIAB", "VNO", "VMC", "WMT",
			"WAG", "DIS", "GHC", "WM", "WAT", "WLP", "WFC", "WDC", "WU", "WY", "WHR", "WFM", "WMB", "WIN", "WEC",
			"WYN", "WYNN", "XEL", "XRX", "XLNX", "XL", "YHOO", "YUM", "ZMH", "ZION" };

	/**
	 * @param c
	 *            Name of a company
	 * @return True if {@code:c} is present in {@link #stockList stockList}, false otherwise
	 */
	public static boolean isCompany(String c) {
		ArrayList<String> companyList = new ArrayList<String>(Arrays.asList(stockList));
		return companyList.contains(c);
	}

	/**
	 * @param month
	 *            Integer 1-12 representing the month
	 * @return The number of days in the given month
	 */
	public static int maxDaysInMonth(int month) {
		int days = 31; // January, March, May, July, August, October, December
		if (month == 2) {
			days = 29; // February (has 29 days on leap years)
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			days = 30; // April, June, September, November
		}

		return days;
	}

	/**
	 * @param fromDay
	 * @param fromMonth
	 * @param fromYear
	 * @param toDay
	 * @param toMonth
	 * @param toYear
	 * @return The number of days in between the two given dates
	 */
	public static int calcTotalDaysHeld(int fromDay, int fromMonth, int fromYear, int toDay, int toMonth, int toYear) {
		Calendar fromDate = Calendar.getInstance();
		fromDate.set(fromYear, fromMonth - 1, fromDay);
		Calendar toDate = Calendar.getInstance();
		toDate.set(toYear, toMonth - 1, toDay);

		long millisecondsPerDay = 1000 * 60 * 60 * 24;
		return (int) ((toDate.getTimeInMillis() - fromDate.getTimeInMillis()) / millisecondsPerDay);
	}

	/**
	 * @param startPrice
	 * @param endPrice
	 * @param totalDaysHeld
	 * @return The total return rate
	 */
	public static float calcAnnualReturnRate(float startPrice, float endPrice, int totalDaysHeld) {
		float totalReturnRate = (startPrice == -1.0 || endPrice == -1.0) ? -1 : (100 * (endPrice - startPrice))
				/ startPrice;
		float returnRate = (float) Math.pow((1.0 + (totalReturnRate / 100.0)), (1.0 / (totalDaysHeld / 365.0)));
		returnRate = (returnRate - 1) * 100;
		return returnRate;
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 * @return A Date object representing the given year, month, and day
	 */
	public static Date getDateObject(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month - 1, day, 0, 0, 0);
		return c.getTime();
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 * @return A Date object representing the given year, month, and day
	 */
	public static Date getDateObject(String year, String month, String day) {
		return getDateObject(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
	}

	/**
	 * @param date
	 * @return The year of the given Date object
	 */
	public static int getYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.YEAR);
	}

	/**
	 * @param date
	 * @return The month of the given Date object
	 */
	public static int getMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return (c.get(Calendar.MONTH) + 1);
	}

	/**
	 * @param date
	 * @return The day of the given Date object
	 */
	public static int getDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 * @return True if the input represents a valid date, false otherwise
	 */
	public static boolean isValidDate(int year, int month, int day) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		String dateAsString = (String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day));
		try {
			dateFormat.parse(dateAsString);
			return true;
		} catch (ParseException e) {
			return false;
		} // throws an exception; invalid date
	}

	/**
	 * Compares 2 Date objects based only on year, month, and day
	 * 
	 * @param date1
	 * @param date2
	 * @return -1 if date1 < date2, 1 if date1 > date2, 0 if equal
	 */
	public static int compareDates(Date date1, Date date2) {
		if (getYear(date1) < getYear(date2)) {
			return -1;
		} else if (getYear(date1) > getYear(date2)) {
			return 1;
		} else {
			if (getMonth(date1) < getMonth(date2)) {
				return -1;
			} else if (getMonth(date1) > getMonth(date2)) {
				return 1;
			} else {
				if (getDay(date1) < getDay(date2)) {
					return -1;
				} else if (getDay(date1) > getDay(date2)) {
					return 1;
				}
			}
		}
		return 0;
	}

}
