import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { v4 as uuidv4 } from "uuid";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

const createEventInterface = util.standardForm(
  z.object({
    registrationStartTime: z.iso.datetime({ offset: true }),
    registrationEndTime: z.iso.datetime({ offset: true }),
    eventName: z.string(),
    eventDescription: z.string(),
    location: z.string(),
    registrationLimit: z.number().int().optional(),
    imageId: z.uuid().optional(),
  })
);

export async function createEvent(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    createEventInterface,
    request
  );

  const {
    registrationStartTime,
    registrationEndTime,
    eventName,
    eventDescription,
    location,
    registrationLimit,
    imageId,
  } = data;

  const userData = await util.verifyUser(userId, deviceId);

  await util.requireRole(userData, "organizer");

  const eventId = uuidv4();

  const db = getFirestore();

  try {
    await db
      .collection("events")
      .doc(eventId)
      .create({
        organizer: userId,
        waitList: [],
        cancelledList: [],
        finalList: [],
        registrationStartTime,
        registrationEndTime,
        eventName,
        eventDescription,
        location,
        registrationLimit: registrationLimit || null,
        imageId: imageId || null,
      });
  } catch (_) {
    throw new HttpsError("already-exists", "Event already exists");
  }

  await db
    .collection("users")
    .doc(userId)
    .update({
      "organizer.createdEvents": FieldValue.arrayUnion(eventId),
    });

  logger.info("Created event", { eventId });
  return { eventId };
}
