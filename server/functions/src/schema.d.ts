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
		eventId: string;
		organizer: string;
		waitList: string[];
		selectedList: string[];
		rejectedList: string[];
		cancelledList: string[];
		finalList: string[];
		registrationStartTime: Timestamp;
		registrationEndTime: Timestamp;
		eventTime: Timestamp;
		eventName: string;
		eventDescription: string;
		eventGuidelines: string | null;
		location: string;
		eventCategory: string | null;
		geolocation: boolean;
		eventCapacity: number;
		registrationLimit: number | null;
		imageId: string | null;
		drawn?: boolean;
	}

	interface NotificationDocument {
		eventId: string;

		title: string;
		message: string;

		waited: boolean;
		selected: boolean;
		cancelled: boolean;
	}

	/** The form comments are stored as in the database. */
	interface CommentDocument {
		senderId: string;
		postTime: string;
		message: string;
	}

	/** The form images are stored as in the database. */
	interface ImageDocument {
		imageData: string;
	}

	// *** External

	/** The form sent as comments to the front end. */
	interface ExternalComment {
		commentId: string;

		senderName: string;
		senderId: string;
		postTime: string;
		message: string;
	}

	interface ExternalUser {
		userId: string;

		name: string | null;
		isAdmin: boolean;
		isOrganizer: boolean;
		isEntrant: boolean;
	}
}
