import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { getFirestore } from "firebase-admin/firestore";

const createNotificationInterface = util.standardForm(
  z.object({
    senderId: z.uuid(),
    recipients: z.enum(["wonLottery", "lostLottery", "waiting", "cancelled", "selectedFromWaitlist"]).nullable(),
    message: z.string().optional(),
    title: z.string().optional(),
  })
);

export async function createNotification(request: CallableRequest) {

  // TODO Find a way to send the notification
  const { userId, deviceId, data } = util.parseInterface(
      createNotificationInterface,
      request
    );
  
    const { senderId, recipients, message, title } = data;
  
    const userData = await util.verifyUser(userId, deviceId);
    await util.requireRole(userData, "organizer");
  
    const notificationId = uuidv4();
  
    const db = getFirestore();
  
    try {
      await db
      .collection("notifications")
      .doc(notificationId)
      .create({
        senderId,
        recipients,
        message,
        title,
      });
    } catch (_) {
      throw new HttpsError("already-exists", "Notification already exists");
    }
  
    logger.info("Notification Created", { notificationId });
    return { notificationId };
}