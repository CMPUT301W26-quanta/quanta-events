import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { DocumentSnapshot, getFirestore } from "firebase-admin/firestore";

const drawLotteryInterface = util.standardForm(
  z.object({
    eventId: z.uuid(),
  })
);

export async function drawLottery(request: CallableRequest) {
  const { userId, deviceId, data } = util.parseInterface(
    drawLotteryInterface,
    request
  );

  const userData = await util.verifyUser(userId, deviceId);

  // PRE: Require organizer
  const organizer = util.requireRole(userData, "organizer");

  const { eventId } = data;

  const db = getFirestore();

  // PRE: Require event owned by organizer
  const event = (await db
    .collection("events")
    .doc(eventId)
    .get()) as DocumentSnapshot<EventDocument, EventDocument>;

  if (event.exists) {
  }

  // PRE: Event has not yet had a lottery drawn
  // PRE: Passed its registration end date
  // POST: Lottery marked as being drawn
  // POST: All users on waitlist have this event added to the history
  // POST: Users are selected and moved to the selected list all other users are moved to the rejected list
  // POST: Copy uuids to selected/rejected list.
}
