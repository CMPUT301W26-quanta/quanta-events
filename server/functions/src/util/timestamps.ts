import { Timestamp } from "firebase-admin/firestore";

export function toTimestamp(value: string): Timestamp {
  return Timestamp.fromDate(new Date(value));
}

export function fromTimestamp(timestamp: Timestamp): string {
  return timestamp.toDate().toISOString();
}

export type ConvertTimestamp<T> = T extends Timestamp ? string : T;

export type ConvertAllTimestamps<O extends object> = {
  [K in keyof O]: ConvertTimestamp<O[K]>;
};
