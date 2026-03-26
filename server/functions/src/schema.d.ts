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

	interface ExternalUser {
		userId: string;
		name: string | null;
		isAdmin: boolean;
		isOrganizer: boolean;
		isEntrant: boolean;
	}
}
