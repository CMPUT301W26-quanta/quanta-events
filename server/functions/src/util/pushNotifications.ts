import { HttpsError } from "firebase-functions/https";
import { UserDocument, WithRole } from "../schema";
import { Messaging } from "firebase-admin/messaging";

export type ReceiveNotifications = "yes" | "no";

export async function pushNotifications(
  entrantData: WithRole<UserDocument, "entrant">,
  receiveNotifications: ReceiveNotifications
) {
  if (entrantData.entrant.receiveNotifications) {
  }
}
