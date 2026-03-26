import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import {
  DocumentReference,
  DocumentSnapshot,
  getFirestore,
  Timestamp,
} from "firebase-admin/firestore";

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
  const eventRef = db.collection("events").doc(eventId) as DocumentReference<
    EventDocument,
    EventDocument
  >;
  try {
    const affectedUsers = await db.runTransaction(async (transaction) => {
      const eventDoc = await transaction.get(eventRef);

      if (!eventDoc.exists) {
        throw new HttpsError("not-found", "The event does not exist");
      }

      const event = eventDoc.data() as EventDocument;

      // PRE: Require event owned by organizer
      if (event.organizer !== userId) {
        throw new HttpsError(
          "failed-precondition",
          "Event is not owned by this user"
        );
      }

      // PRE: Event has not yet had a lottery drawn
      if (event.drawn || false) {
        throw new HttpsError(
          "failed-precondition",
          "Event has already drawn lottery"
        );
      }

      // PRE: Passed its registration end date
      const now = Timestamp.now();
      if (event.registrationEndTime > now) {
        throw new HttpsError(
          "failed-precondition",
          "Event is still open for registration"
        );
      }

      // POST: Lottery marked as being drawn
      transaction.update(eventRef, { drawn: true });

      // POST: Users are selected and moved to the selected list all other users are moved to the rejected list
      // POST: Copy uuids to selected/rejected list.

      const waitlist = event.waitList;

      // Randomize order
      for (let i = waitlist.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [waitlist[i], waitlist[j]] = [waitlist[j], waitlist[i]];
      }

      // Slice winners
      const selected = waitlist.slice(0, event.eventCapacity);
    });

    logger.info("Successfully ran drawLottery transaction");

    // POST: All users on waitlist have this event added to the history
  } catch (err) {
    logger.error("Failed to run drawLottery transaction", err);
    if (err instanceof HttpsError) {
      throw err;
    } else {
      throw new HttpsError("internal", "Transaction failure");
    }
  }
}
