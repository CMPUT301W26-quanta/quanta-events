import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { FieldValue, getFirestore } from "firebase-admin/firestore";

const deleteUserInterface = util.standardForm(
  z.object({
    target: z.uuid(),
  }),
);

export async function deleteUser(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    deleteUserInterface,
    request,
  );

  const userData = await util.verifyUser(userId, deviceId);

  const { target } = data;

  const db = getFirestore();

  const targetDoc = await db.collection("users").doc(target).get();
  logger.log(targetDoc);

  if (!targetDoc.exists) {
    throw new HttpsError("not-found", "Target user does not exist");
  }

  if (target !== userId) {
    await util.requireRole(userData, "admin");
  }

  const eventsSnapshot = await db.collection("events").get();

  const eventUpdates = eventsSnapshot.docs.map(async (eventDoc) => {
    const event = eventDoc.data();

    if (event.organizer === target) {
      return eventDoc.ref.delete();
    }

    const isInAnyList =
      event.waitList?.includes(target) ||
      event.finalList?.includes(target) ||
      event.cancelledList?.includes(target);

    if (!isInAnyList) return;

    return eventDoc.ref.update({
      waitList: FieldValue.arrayRemove(target),
      finalList: FieldValue.arrayRemove(target),
      cancelledList: FieldValue.arrayRemove(target),
    });
  });

  await Promise.all(eventUpdates);

  await db.collection("users").doc(target).delete();

  logger.info("Deleted user and all references", { target });
  return {};
}
