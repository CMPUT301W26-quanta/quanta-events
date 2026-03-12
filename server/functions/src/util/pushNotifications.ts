import { HttpsError } from "firebase-functions/https";

export type ReceiveNotifications = "yes" | "no";

export async function pushNotifications(entrantData: any, receiveNotifications: ReceiveNotifications) {
  if (!entrantData[receiveNotifications]) {
    throw new HttpsError("failed-precondition", `User does not want to receive notifications`);
  }
}