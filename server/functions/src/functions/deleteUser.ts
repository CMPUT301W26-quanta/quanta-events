import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { FieldValue, getFirestore } from "firebase-admin/firestore";

const deleteUserInterface = z.object({
  userId: z.uuid(),
  deviceId: z.uuid(),
});

export async function deleteUser(request: CallableRequest) {
  const { userId, deviceId } = util.parseInterface(deleteUserInterface, request);

  await util.verifyUser(userId, deviceId);

  const db = getFirestore();

  const eventsSnapshot = await db.collection("events").get();

  const eventUpdates = eventsSnapshot.docs.map(async (eventDoc) => {
    const event = eventDoc.data();

    if (event.organizer === userId) {
      return eventDoc.ref.delete();
    }

    const isInAnyList =
      event.waitList?.includes(userId) ||
      event.finalList?.includes(userId) ||
      event.cancelledList?.includes(userId);

    if (!isInAnyList) return;

    return eventDoc.ref.update({
      waitList: FieldValue.arrayRemove(userId),
      finalList: FieldValue.arrayRemove(userId),
      cancelledList: FieldValue.arrayRemove(userId),
    });
  });

  await Promise.all(eventUpdates);

  await db.collection("users").doc(userId).delete();

  logger.info("Deleted user and all references", { userId });
  return {};
}
