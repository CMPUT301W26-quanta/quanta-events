import * as z from "zod";
import * as util from "../util.js";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const getEventInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
  }),
);

export async function getEvent(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    getEventInterface,
    request,
  );

  const { eventId } = data;

  await util.verifyUser(userId, deviceId);

  const db = getFirestore();

  const eventDoc = await db.collection("events").doc(eventId).get();

  if (!eventDoc.exists) {
    throw new HttpsError("not-found", "Event not found");
  }

  logger.info("Event found", { eventId });
  return eventDoc.data();
}
