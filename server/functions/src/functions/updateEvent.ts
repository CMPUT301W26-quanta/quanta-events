import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { EventDocument } from "../schema";

const updateEventInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
    registrationStartTime: z.iso.datetime({ offset: true }),
    registrationEndTime: z.iso.datetime({ offset: true }),
    eventTime: z.iso.datetime({ offset: true }),
    eventName: z.string(),
    eventDescription: z.string(),
    eventCategory: z.string().nullable(),
    eventGuidelines: z.string().nullable(),
    geolocation: z.boolean(),
    eventCapacity: z.int(),
    location: z.string(),
    registrationLimit: z.number().int().nullable(),
    imageId: z.uuid().nullable(),
  })
);

export async function updateEvent(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    updateEventInterface,
    request
  );

  const {
    eventId,
    registrationStartTime,
    registrationEndTime,
    eventTime,
    eventName,
    eventDescription,
    eventCategory,
    eventGuidelines,
    geolocation,
    eventCapacity,
    location,
    registrationLimit,
    imageId,
  } = data;

  const userData = await util.verifyUser(userId, deviceId);

  util.requireRole(userData, "organizer");

  const db = getFirestore();

  const eventDoc = await db.collection("events").doc(eventId).get();

  if (!eventDoc.exists) {
    throw new HttpsError("not-found", "Event not found");
  }

  const event = eventDoc.data() as EventDocument;

  if (event.organizer !== userId) {
    throw new HttpsError("permission-denied", "User is not the organizer of this event");
  }

  const updates: Record<string, any> = {
    registrationStartTime: util.toTimestamp(registrationStartTime),
    registrationEndTime: util.toTimestamp(registrationEndTime),
    eventTime: util.toTimestamp(eventTime),
    eventName,
    eventDescription,
    eventCategory,
    eventGuidelines,
    geolocation,
    eventCapacity,
    location,
    registrationLimit,
    imageId,
  };

  await db.collection("events").doc(eventId).update(updates);

  logger.info("Updated event", { eventId });
  return {};
}