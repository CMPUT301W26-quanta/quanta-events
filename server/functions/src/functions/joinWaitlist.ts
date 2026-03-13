import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { EventDocument } from "../schema";

const joinWaitlistInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
  })
);

export async function joinWaitlist(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    joinWaitlistInterface,
    request
  );

  const { eventId } = data;

  const userData = await util.verifyUser(userId, deviceId);

  util.requireRole(userData, "entrant");

  const db = getFirestore();

  const eventDoc = await db.collection("events").doc(eventId).get();

  if (!eventDoc.exists) {
    throw new HttpsError("not-found", "Event not found");
  }

  const event = eventDoc.data() as EventDocument;

  const now = new Date();
  const registrationStart = event.registrationStartTime.toDate();
  const registrationEnd = event.registrationEndTime.toDate();

  if (now < registrationStart) {
    throw new HttpsError("failed-precondition", "Registration has not started yet");
  }

  if (now > registrationEnd) {
    throw new HttpsError("failed-precondition", "Registration has closed");
  }

  const alreadyInWaitlist = event.waitList?.includes(userId);
  const alreadyInFinalList = event.finalList?.includes(userId);

  if (alreadyInWaitlist || alreadyInFinalList) {
    throw new HttpsError("already-exists", "User is already registered for this event");
  }

  if (
    event.registrationLimit !== null &&
    event.registrationLimit !== 0 &&
    (event.waitList?.length ?? 0) >= event.registrationLimit
  ) {
    throw new HttpsError("resource-exhausted", "Waitlist is full");
  }

  await db.collection("events").doc(eventId).update({
    waitList: FieldValue.arrayUnion(userId),
  });

  await db.collection("users").doc(userId).update({
    "entrant.enteredEvents": FieldValue.arrayUnion(eventId),
  });

  logger.info("User joined waitlist", { userId, eventId });
  return {};
}