import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { getFirestore } from "firebase-admin/firestore";

const createNotificationInterface = util.standardForm(
  z.object({
    senderId: z.uuid(),
    recipientIds: z.array(z.uuid()).optional(),
    message: z.string().optional(),
    title: z.string().optional(),
  })
);

export async function createNotification(request: CallableRequest) {

  const { userId, deviceId, data } = util.parseInterface(
      createNotificationInterface,
      request
    );
  
    const { senderId, recipientIds, message, title } = data;
  
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
        recipientIds,
        message,
        title,
      });
    } catch (_) {
      throw new HttpsError("already-exists", "Notification already exists");
    }
  
    logger.info("Notification Created", { notificationId });
    return { notificationId };
}