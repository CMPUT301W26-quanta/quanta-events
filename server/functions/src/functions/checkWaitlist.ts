import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { EventDocument } from "../schema";

const checkWaitlistInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
  })
);

export async function checkWaitlist(
  request: CallableRequest
): Promise<{ inWaitlist: boolean }> {
  const { userId, deviceId, data } = util.parseInterface(
    checkWaitlistInterface,
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

  const inWaitlist = event.waitList?.includes(userId) ?? false;

  logger.info("Checked waitlist status", { userId, eventId, inWaitlist });
  return { inWaitlist };
}