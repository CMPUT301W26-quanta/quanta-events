import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { CollectionReference, getFirestore } from "firebase-admin/firestore";
import { EventDocument } from "../schema";

const createNotificationInterface = util.standardForm(
  z.object({
    senderId: z.uuid(),
    message: z.string().optional(),
    title: z.string().optional(),
    eventId: z.uuid(),
    waited: z.boolean(),
    cancelled: z.boolean(),
    selected: z.boolean(),
  })
);

export async function createNotification(request: CallableRequest) {

  // TODO Find a way to send the notification
  const { userId, deviceId, data } = util.parseInterface(
      createNotificationInterface,
      request
    );
  
    const { senderId, message, title, eventId, waited, cancelled, selected } = data;
  
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
        message,
        title,
        eventId,
        waited,
        cancelled,
        selected,
      });
    } catch (_) {
      throw new HttpsError("already-exists", "Notification already exists");
    }

    const eventDocuments = db.collection("events") as CollectionReference<EventDocument, EventDocument>;
    const events = (await eventDocuments.doc(eventId).get()).data();

    // Store all the recipients in some collection
    const recipients: string[] = [];
    if (waited) {
      const waitedList = events?.waitList as string[];
      for (const entrantId of waitedList) {
        if (!recipients.includes(entrantId)) {
          recipients.push(entrantId);
        }
      }
    }
    if (cancelled) {
      const cancelledList = events?.cancelledList as string[];
      for (const entrantId of cancelledList) {
        if (!recipients.includes(entrantId)) {
          recipients.push(entrantId);
        }
      }
    }
    if (selected) {
      const finalList = events?.finalList as string[];
      for (const entrantId of finalList) {
        if (!recipients.includes(entrantId)) {
          recipients.push(entrantId);
        }
      }
    }

    // TODO get the FCM codes by querying the user objects in the recipient list
    // TODO Send notification to the recipients

    logger.info("Notification Created and sent succesfully", { notificationId });
    return { notificationId };
}