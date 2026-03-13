import { HttpsError } from "firebase-functions/https";
import { UserDocument, WithRole } from "../schema";
import { Messaging } from "firebase-admin/messaging";

// TODO finish this checking function
export type ReceiveNotifications = "yes" | "no";

export async function checkReceiveNotifications(
  entrantData: WithRole<UserDocument, "entrant">,
  receiveNotifications: ReceiveNotifications
) {
  if (entrantData.entrant.receiveNotifications) {
  }
}
