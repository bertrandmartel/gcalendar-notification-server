package fr.bmartel.protocol.google.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import fr.bmartel.protocol.google.api.calendar.IEventListener;
import fr.bmartel.protocol.google.oauth.device.CalendarNotifManager;

/**
 * Notification manager used to manager calendar event notification to api
 * client
 * 
 * @author Bertrand Martel
 *
 */
public class NotificationManager {

	/**
	 * Scheduled thread pool executor used to scheduled event notification task
	 */
	private ScheduledExecutorService scheduledThreadPool = null;

	/**
	 * oauth authentication manager
	 */
	private CalendarNotifManager authManager = null;

	/**
	 * List of notification object used to scheduled event task
	 */
	private List<NotificationObject> notificationList = new ArrayList<NotificationObject>();

	/**
	 * Build notification manager
	 * 
	 * @param authManager
	 */
	public NotificationManager(CalendarNotifManager authManager) {
		this.authManager = authManager;
		this.scheduledThreadPool = Executors.newScheduledThreadPool(1);
	}

	/**
	 * to know if eventId is already subscrived or not
	 * 
	 * @param eventId
	 * @return
	 */
	public boolean isSubscribed(String eventId) {

		for (int i = 0; i < notificationList.size(); i++) {
			if (notificationList.get(i).getEventId().equals(eventId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Subscribe to event with id eventId with specific timing for aboutToStart
	 * event
	 * 
	 * @param eventId
	 * @param aboutToStartSeconds
	 * @param eventListener
	 */
	public void subscribeEvent(String eventId, int aboutToStartSeconds, final IEventListener eventListener) {
		if (!isInNotificationList(eventId)) {
			NotificationObject notificationObject = new NotificationObject(eventId, authManager.getCalendarManager().getEventSummary(eventId), eventListener,
					aboutToStartSeconds, this);
			notificationObject.start();
			notificationList.add(notificationObject);
		} else {
			System.err.println("Error evetnId has already a task in notfiicaiton list");
		}
	}

	/**
	 * Unsubscribe from event
	 * 
	 * @param eventId
	 */
	public void unsubscribeEvent(String eventId) {
		for (int i = notificationList.size() - 1; i >= 0; i--) {
			if (notificationList.get(i).getEventId().equals(eventId)) {
				notificationList.get(i).cancel();
				notificationList.remove(i);
			}
		}
	}

	/**
	 * Retrieve scheduled thread pool executor
	 * 
	 * @return
	 */
	public ScheduledExecutorService getScheduledThreadPool() {
		return scheduledThreadPool;
	}

	/**
	 * to know if eventId is in notification list or not
	 * 
	 * @param eventId
	 * @return
	 */
	public boolean isInNotificationList(String eventId) {
		for (int i = 0; i < notificationList.size(); i++) {
			if (notificationList.get(i).getEventId().equals(eventId)) {
				return true;
			}
		}
		return false;
	}

	public CalendarNotifManager getAuthenticationManager() {
		return authManager;
	}

}
