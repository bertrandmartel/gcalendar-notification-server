package fr.bmartel.protocol.google.api.calendar;

/**
 * Calendar event object
 * 
 * https://developers.google.com/google-apps/calendar/v3/reference/
 * 
 * 
 * @author Bertrand Martel
 *
 */
public class CalendarEvents {
	
	private String eventId = "";

	private String status = "";

	private String dateCreated = "";

	private String dateUpdated = "";

	private String summary = "";

	private String creatorEmail = "";

	private String creatorDisplayName = "";

	private boolean creatorSelf = false;

	private String organizerEmail = "";

	private String organizerDisplayName = "";

	private boolean organizerSelf = false;

	private String dateStart = "";

	private String dateEnd = "";

	private String dateTimeStart = "";

	private String dateTimeEnd = "";

	private boolean subscribed = false;

	public CalendarEvents(String eventId, String status, String dateCreated, String dateUpdated, String summary, String creatorEmail,
			String creatorDisplayName, boolean creatorSelf, String organizerEmail, String organizerDisplayName, boolean organizerSelf, String dateStart,
			String dateEnd, String dateTimeStart, String dateTimeEnd, boolean subscribed) {

		this.eventId = eventId;
		this.status = status;
		this.dateCreated = dateCreated;
		this.summary = summary;
		this.creatorEmail = creatorEmail;
		this.creatorDisplayName = creatorDisplayName;
		this.creatorSelf = creatorSelf;
		this.organizerEmail = organizerEmail;
		this.organizerDisplayName = organizerDisplayName;
		this.organizerSelf = organizerSelf;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		this.dateTimeStart = dateTimeStart;
		this.dateTimeEnd = dateTimeEnd;
		this.subscribed = subscribed;
	}

	public String getEventId() {
		return eventId;
	}

	public String getStatus() {
		return status;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public String getDateUpdated() {
		return dateUpdated;
	}

	public String getSummary() {
		return summary;
	}

	public String getCreatorEmail() {
		return creatorEmail;
	}

	public String getCreatorDisplayName() {
		return creatorDisplayName;
	}

	public boolean isCreatorSelf() {
		return creatorSelf;
	}

	public String getOrganizerEmail() {
		return organizerEmail;
	}

	public String getOrganizerDisplayName() {
		return organizerDisplayName;
	}

	public boolean isOrganizerSelf() {
		return organizerSelf;
	}

	public String getDateStart() {
		return dateStart;
	}

	public String getDateEnd() {
		return dateEnd;
	}

	public String getDateTimeStart() {
		return dateTimeStart;
	}

	public String getDateTimeEnd() {
		return dateTimeEnd;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

}
