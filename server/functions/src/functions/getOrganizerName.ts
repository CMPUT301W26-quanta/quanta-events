import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { EventDocument, UserDocument } from "../schema";

const getOrganizerNameInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
  })
);

export async function getOrganizerName(
  request: CallableRequest
): Promise<{ name: string | null }> {
  const { userId, deviceId, data } = util.parseInterface(
    getOrganizerNameInterface,
    request
  );

  const { eventId } = data;

  await util.verifyUser(userId, deviceId);

  const db = getFirestore();

  const eventDoc = await db.collection("events").doc(eventId).get();

  if (!eventDoc.exists) {
    throw new HttpsError("not-found", "Event not found");
  }

  const event = eventDoc.data() as EventDocument;

  const organizerDoc = await db.collection("users").doc(event.organizer).get();

  if (!organizerDoc.exists) {
    throw new HttpsError("not-found", "Organizer not found");
  }

  const organizer = organizerDoc.data() as UserDocument;

  logger.info("Got organizer name", { eventId, organizerId: event.organizer });
  return { name: organizer.name };
}