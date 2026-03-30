import { DocumentSnapshot, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const getEventInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
  })
);

export async function getEvent(
  request: CallableRequest
): Promise<util.ConvertAllTimestamps<EventDocument>> {
  const { userId, deviceId, data } = util.parseInterface(
    getEventInterface,
    request
  );

  const { eventId } = data;

  await util.verifyUser(userId, deviceId);

  const db = getFirestore();

  const eventDoc = (await db
    .collection("events")
    .doc(eventId)
    .get()) as DocumentSnapshot<EventDocument, EventDocument>;

  if (!eventDoc.exists) {
    throw new HttpsError("not-found", "Event not found");
  }

  logger.info("Event found", { eventId });

  const eventData = eventDoc.data() as EventDocument;

  const remappedData = Object.assign(eventData, {
    registrationStartTime: util.fromTimestamp(eventData.registrationStartTime),
    registrationEndTime: util.fromTimestamp(eventData.registrationEndTime),
    eventTime: util.fromTimestamp(eventData.eventTime),
  });

  return remappedData;
}
