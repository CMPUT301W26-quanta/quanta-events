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
  deviceId: string;
  name: string;
  email: string;
  phone: string;
  entrant: Entrant;
  organizer: Organizer;
  admin: Admin;
}

export interface ExternalUser {
  userId: string;
  name: string;
  isAdmin: boolean;
  isOrganizer: boolean;
  isEntrant: boolean;
}
