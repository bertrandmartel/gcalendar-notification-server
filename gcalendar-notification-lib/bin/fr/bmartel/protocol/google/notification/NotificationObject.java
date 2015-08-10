package fr.bmartel.protocol.google.notification;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.bmartel.protocol.google.api.calendar.CalendarUtils;
import fr.bmartel.protocol.google.api.calendar.IEventListener;
import fr.bmartel.utils.DateUtils;

/**
 * Feature a single notification scheduled containing 2 task at most (about to
 * start task and started task)
 * 
 * 
 * @author Bertrand Martel
 *
 */
public class NotificationObject {

	/**
	 * scheduled task triggered when event has started
	 */
	private ScheduledFuture<?> taskEvent = null;

	/**
	 * scheduled task triggered when event is about to start
	 */
	private ScheduledFuture<?> taskBeforeEvent = null;

	/**
	 * event listener (notify api client)
	 */
	private IEventListener eventListener = null;

	/**
	 * google calendar event id
	 */
	private String eventId = "";

	/**
	 * google calendar evetn summary
	 */
	private String summary = "";

	/**
	 * time in seconds defining task "about to start"
	 */
	private int timeBeforeStartSeconds = 0;

	/**
	 * notification manager used to retrieve scheduled thread pool executor
	 * service
	 */
	private NotificationManager manager = null;

	boolean isStarted = false;
	boolean isCancelled = false;

	private long timeBeforeEvent = 0;

	/**
	 * Build notification object
	 * 
	 * @param eventId
	 * @param summary
	 * @param eventListener
	 * @param timeBeforeStartSeconds
	 * @param manager
	 */
	public NotificationObject(String eventId, String summary, IEventListener eventListener, int timeBeforeStartSeconds, NotificationManager manager) {
		this.eventListener = eventListener;
		this.eventId = eventId;
		this.summary = summary;
		this.timeBeforeStartSeconds = timeBeforeStartSeconds;
		this.manager = manager;

		String date = CalendarUtils.getEventDateStart(manager.getAuthenticationManager().getCalendarManager().getCalendarEventList(), eventId);

		if (date.equals("")) {
			System.err.println("Error eventId was not found in event list");
		} else {
			timeBeforeEvent = DateUtils.convertTimestampDateToSeconds(date);
		}
	}

	/**
	 * schedule "start" task and "about to start" task if necessary
	 */
	public void start() {
		if (!isStarted) {
			if (timeBeforeEvent > 0) {

				System.out.println("Scheduling notification in " + timeBeforeEvent + " seconds");

				taskEvent = manager.getScheduledThreadPool().schedule(new Runnable() {

					@Override
					public void run() {
						eventListener.onEventStart(eventId, summary);
					}
				}, timeBeforeEvent, TimeUnit.SECONDS);

				if (timeBeforeStartSeconds > 0 && (timeBeforeEvent - timeBeforeStartSeconds) > 0) {

					System.out.println("Scheduling notification in " + (timeBeforeEvent - timeBeforeStartSeconds) + " seconds");

					taskBeforeEvent = manager.getScheduledThreadPool().schedule(new Runnable() {

						@Override
						public void run() {
							eventListener.onEventAboutToStart(eventId, summary);
						}
					}, timeBeforeEvent - timeBeforeStartSeconds, TimeUnit.SECONDS);
				}
			} else {
				System.err.println("Error the event may not be registered");
			}
		} else {
			System.err.println("Task " + eventId + " is already started");
		}
	}

	/**
	 * cancel all task refering to this notification object
	 */
	public void cancel() {
		if (taskEvent != null)
			taskEvent.cancel(true);
		if (taskBeforeEvent != null)
			taskBeforeEvent.cancel(true);
	}

	public String getEventId() {
		return eventId;
	}
}
