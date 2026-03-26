// the actual underlying forms of each of the documents

import { Timestamp } from "firebase-admin/firestore";

declare interface EntrantMap {
  enteredEvents: string[];
  history: string[];
  receiveNotifications: boolean;
}

declare interface OrganizerMap {
  createdEvents: string[];
  sentNotifications: string[];
}

declare interface AdminMap {}

declare interface UserDocument {
  deviceId: string;
  name: string | null;
  email: string | null;
  phone: string | null;
  entrant: EntrantMap | null;
  organizer: OrganizerMap | null;
  admin: AdminMap | null;
  notifToken: string | null;
}

declare interface ExternalUser {
  userId: string;
  name: string | null;
  isAdmin: boolean;
  isOrganizer: boolean;
  isEntrant: boolean;
}

declare type UserRole = "entrant" | "admin" | "organizer";

type NotNull<V> = V extends null ? never : V;

declare type WithRole<U extends UserDocument, R extends UserRole> = U & {
  [K in R]: NotNull<U[K]>;
};

declare interface EventDocument {
  eventId: string;
  organizer: string;
  waitList: string[];
  cancelledList: string[];
  finalList: string[];
  registrationStartTime: Timestamp;
  registrationEndTime: Timestamp;
  eventTime: Timestamp;
  eventName: string;
  eventDescription: string;
  location: string;
  registrationLimit: number | null;
  imageId: string | null;
}
