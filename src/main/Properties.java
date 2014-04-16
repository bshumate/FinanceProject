package main;

public class Properties {

	protected static final String METHOD_COMPANY_QUERY = "companyQuery";
	protected static final String METHOD_UPDATE_QUOTES = "updateQuotes";
	
	protected static String stockList[] = { "MMM", "ABT", "ACE", "ACN", "ACT", "ADBE", "AES", "AET", "AFL", "A", "GAS",
		"APD", "ARG", "AKAM", "AA", "ALXN", "ATI", "AGN", "ADS", "ALL", "ALTR", "MO", "AMZN", "AEE", "AEP", "AXP",
		"AIG", "AMT", "AMP", "ABC", "AME", "AMGN", "APH", "APC", "ADI", "AON", "APA", "AIV", "AAPL", "AMAT", "ADM",
		"AIZ", "T", "ADSK", "ADP", "AN", "AZO", "AVB", "AVY", "AVP", "BHI", "BLL", "BAC", "BK", "BCR", "BAX",
		"BBT", "BEAM", "BDX", "BBBY", "BMS", "BBY", "BIIB", "BLK", "HRB", "BA", "BWA", "BXP", "BSX", "BMY", "BRCM",
		"CHRW", "CA", "CVC", "COG", "CAM", "CPB", "COF", "CAH", "KMX", "CCL", "CAT", "CBG", "CBS", "CELG", "CNP",
		"CTL", "CERN", "CF", "SCHW", "CHK", "CVX", "CMG", "CB", "CI", "CINF", "CTAS", "CSCO", "C", "CTXS", "CLF",
		"CLX", "CME", "CMS", "COH", "KO", "CCE", "CTSH", "CL", "CMCSA", "CMA", "CSC", "CAG", "COP", "CNX", "ED",
		"STZ", "GLW", "COST", "COV", "CCI", "CSX", "CMI", "CVS", "DHI", "DHR", "DRI", "DVA", "DE", "DAL", "DNR",
		"XRAY", "DVN", "DO", "DTV", "DFS", "DISCA", "DLTR", "D", "DOV", "DOW", "DTE", "DD", "DUK", "DNB", "ETFC",
		"EMN", "ETN", "EBAY", "ECL", "EIX", "EW", "EA", "EMC", "EMR", "ESV", "ETR", "EOG", "EQT", "EFX", "EQR",
		"EL", "EXC", "EXPE", "EXPD", "ESRX", "XOM", "FFIV", "FDO", "FAST", "FDX", "FIS", "FITB", "FSLR", "FE",
		"FISV", "FLIR", "FLS", "FLR", "FMC", "FTI", "F", "FRX", "FOSL", "BEN", "FCX", "FTR", "GME", "GCI", "GPS",
		"GRMN", "GD", "GE", "GIS", "GPC", "GNW", "GILD", "GS", "GT", "GOOGL", "GWW", "HAL", "HOG", "HAR", "HRS",
		"HIG", "HAS", "HCP", "HCN", "HP", "HES", "HPQ", "HD", "HON", "HRL", "HSP", "HST", "HCBK", "HUM", "HBAN",
		"ITW", "IR", "TEG", "INTC", "ICE", "IBM", "IGT", "IP", "IPG", "IFF", "INTU", "ISRG", "IVZ", "IRM", "JBL",
		"JEC", "JNJ", "JCI", "JOY", "JPM", "JNPR", "KSU", "K", "KEY", "KMB", "KLAC", "KSS", "KR", "LB", "LLL",
		"LH", "LRCX", "LM", "LEG", "LEN", "LUK", "LLY", "LNC", "LLTC", "LMT", "L", "LOW", "LSI", "MTB", "MAC", "M",
		"MRO", "MAR", "MMC", "MAS", "MA", "MAT", "MKC", "MCD", "MHFI", "MCK", "MWV", "MDT", "MRK", "MET", "MCHP",
		"MU", "MSFT", "MHK", "TAP", "MDLZ", "MON", "MNST", "MCO", "MS", "MOS", "MSI", "MUR", "MYL", "NBR", "NDAQ",
		"NOV", "NTAP", "NFLX", "NWL", "NFX", "NEM", "NEE", "NKE", "NI", "NE", "NBL", "JWN", "NSC", "NTRS", "NOC",
		"NU", "NRG", "NUE", "NVDA", "ORLY", "OXY", "OMC", "OKE", "ORCL", "OI", "PCG", "PCAR", "PLL", "PH", "PDCO",
		"PAYX", "BTU", "PNR", "PBCT", "POM", "PEP", "PKI", "PRGO", "PETM", "PFE", "PNW", "PXD", "PBI", "PCL",
		"PNC", "RL", "PPG", "PPL", "PX", "PCP", "PCLN", "PFG", "PG", "PGR", "PLD", "PRU", "PSA", "PHM", "PVH",
		"PWR", "QCOM", "DGX", "RRC", "RTN", "RHT", "REGN", "RF", "RSG", "RAI", "RHI", "ROK", "COL", "ROP", "ROST",
		"RDC", "R", "SWY", "CRM", "SNDK", "SCG", "SLB", "STX", "SEE", "SRE", "SHW", "SIAL", "SPG", "SLM", "SJM",
		"SNA", "SO", "LUV", "SWN", "SE", "STJ", "SWK", "SPLS", "SBUX", "HOT", "STT", "SRCL", "SYK", "STI", "SYMC",
		"SYY", "TROW", "TGT", "TEL", "TE", "THC", "TDC", "TSO", "TXN", "TXT", "HSY", "TRV", "TMO", "TIF", "TWX",
		"TWC", "TJX", "TMK", "TSS", "TSCO", "RIG", "FOXA", "TSN", "TYC", "USB", "UNP", "UNH", "UPS", "X", "UTX",
		"UNM", "URBN", "VFC", "VLO", "VAR", "VTR", "VRSN", "VZ", "VRTX", "VIAB", "VNO", "VMC", "WMT", "WAG", "DIS",
		"GHC", "WM", "WAT", "WLP", "WFC", "WDC", "WU", "WY", "WHR", "WFM", "WMB", "WIN", "WEC", "WYN", "WYNN",
		"XEL", "XRX", "XLNX", "XL", "YHOO", "YUM", "ZMH", "ZION" };
}
