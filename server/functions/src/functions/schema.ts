// the actual underlying forms of each of the documents

export interface Admin {}

export interface Organizer {
	createdEvents: [];
	sentNotifications: [];
}

export interface Entrant {
	enteredEvents: [];
	history: [];
	receiveNotifications: boolean;
}

export interface User {
	deviceID: string;
	email: string;
	organizer: Organizer;
	admin: Admin;
	entrant: Entrant;
	name: string;
	phone: string;
}
