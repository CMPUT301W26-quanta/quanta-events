// the actual underlying forms of each of the documents

import { Timestamp } from "firebase-admin/firestore";

type NotNull<V> = V extends null ? never : V;

declare global {
	interface UserDocument {
		deviceId: string;
		name: string | null;
		email: string | null;
		phone: string | null;
		entrant: EntrantMap | null;
		organizer: OrganizerMap | null;
		admin: AdminMap | null;
		notifToken: string | null;
	}

	interface EntrantMap {
		enteredEvents: string[];
		history: string[];
		receiveNotifications: boolean;
	}

	interface OrganizerMap {
		createdEvents: string[];
		sentNotifications: string[];
	}

	interface AdminMap {}

	type UserRole = "entrant" | "admin" | "organizer";

	type WithRole<U extends UserDocument, R extends UserRole> = U & {
		[K in R]: NotNull<U[K]>;
	};

	interface EventDocument {
		// Identification props
		/** The UUID of the event (same as the document id) */
		eventId: string;

		/** The UUID of the organizer */
		organizer: string;

		// Registration props
		/** List of the UUIDs of entrants enrolled in this event */
		waitList: string[];
		/** List of the UUIDs of entrants selected for this event */
		selectedList: string[];
		/** List of the UUIDs of entrants rejected from this event */
		rejectedList: string[];
		/** List of the UUIDs of entrants cancelled in this event */
		cancelledList: string[];
		/** List of the UUIDs of entrants accepted in this event */
		finalList: string[];
		/** Timestamp marking the start of the registration timeframe */
		registrationStartTime: Timestamp;
		/** Timestamp marking the end of the registration timeframe */
		registrationEndTime: Timestamp;

		// Event detail props
		/** Timestamp marking the time of the event's occurence */
		eventTime: Timestamp;
		/** The name of the event */
		eventName: string;
		/** The description of the event */
		eventDescription: string;
		/** The guidelines of the event, if any */
		eventGuidelines: string | null;
		/** The location of the event */
		location: string;
		/** The category of the event, if applicable */
		eventCategory: string | null;
		/** If there is a geolocation requirement for this event */
		geolocation: boolean;
		/** The capacity of the event */
		eventCapacity: number;
		/** The registration limit of the event */
		registrationLimit: number | null;
		/** The id of the image of the event, if applicable */
		imageId: string | null;

		// Extra
		/** Whether or not the event has been drawn */
		drawn?: boolean;
		/** Whether this event is private */
		isPrivate?: boolean;
	}

	interface NotificationDocument {
		/** The UUID of the source event */
		eventId: string;

		/** The UUIDs of the target users */
		targetUsers: string[];

		title: string;
		message: string;

		kind: NotificationAnyKind;
	}

	type NotificationAnyKind =
		| NotificationMessageKind
		| NotificationLotteryKind
		| NotificationInviteKind;

	interface NotificationMessageKind {
		kind: "MESSAGE";

		waited: boolean;
		selected: boolean;
		cancelled: boolean;
		final: boolean;
	}

	interface NotificationLotteryKind {
		kind: "LOTTERY";

		selected: boolean;
	}

	interface NotificationInviteKind {
		kind: "INVITE";
	}

	/** The form comments are stored as in the database. */
	interface CommentDocument {
		senderId: string;
		message: string;
	}

	/** The form images are stored as in the database. */
	interface ImageDocument {
		imageData: string;
	}

	// *** External

	/** The user as sent to the frontend */
	interface ExternalUser {
		userId: string;
		name: string | null;
		phone: string | null;
		email: string | null;
		isAdmin: boolean;
		isOrganizer: boolean;
		isEntrant: boolean;
	}

	/** The form sent as comments to the front end. */
	interface ExternalComment {
		commentId: string;

		senderId: string;
		message: string;
	}
}
