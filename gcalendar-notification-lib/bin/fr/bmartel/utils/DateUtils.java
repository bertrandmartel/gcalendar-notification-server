package fr.bmartel.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date parsing/converting functions
 * 
 * @author Bertrand Martel
 *
 */
public class DateUtils {

	/**
	 * 
	 * @param yearIn
	 *            Calendar.YEAR
	 * @param monthIn
	 *            Calendar.MONTH (begin from 0 )
	 * @param dayIn
	 *            Calendar.DAY_OF_MONTH
	 * @param hourIn
	 *            Calendar.HOUR_OF_DAY
	 * @param minIn
	 *            Calendar.MINUTE
	 * @param secIn
	 *            Calendar.SECOND
	 * @return built date for now
	 */
	public static String buildDate(long dateMillis) {

		Date dateObj = new Date(dateMillis);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateObj);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;

		String monthString = "";
		if (month < 10) {
			monthString = "0" + month;
		}
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String date = year + "-" + monthString + "-" + day + "T" + hour + ":" + min + ":" + sec;

		return date;
	}

	/**
	 * 2015-08-06T06:12:24-00:00
	 * 
	 * @param date
	 * @return
	 */
	public static long convertTimestampDateToSeconds(String dateStr) {

		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateStr);
			long ts = date.getTime();

			Date now = new Date();
			long tsNow = now.getTime();

			if (ts > tsNow) {
				return ((ts - tsNow) / 1000);
			} else {
				System.err.println("Error event timestamp is anterior to now");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}
}
