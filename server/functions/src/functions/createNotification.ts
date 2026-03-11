import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { getFirestore } from "firebase-admin/firestore";

const createNotificationInterface = z.object({
  senderId: z.uuid(),
  recipientIds: z.array(z.uuid()).optional(),
  message: z.string().optional(),
});

export async function createNotification(request: CallableRequest) {
  const {
    senderId,
    recipientIds,
    message,
  } = util.parseInterface(createNotificationInterface, request);

  const notificationId = uuidv4();

  const db = getFirestore();
  try {
    await db
      .collection("notifications")
      .doc(notificationId)
      .create({
        senderId,
        ...(recipientIds && { recipientIds }),
        ...(message && { message }),
      });
  } catch (_) {
    throw new HttpsError("already-exists", "Notification already exists");
  }

  logger.info("Created notification", { notificationId });
  return { notificationId };
}