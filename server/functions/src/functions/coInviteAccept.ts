import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";

const coInviteAcceptInterface = util.standardForm(
    z.object({ eventId: z.uuid() }),
);

export async function coInviteAccept(request: CallableRequest): Promise<{}> {
    const { userId, deviceId, data } = util.parseInterface(
        coInviteAcceptInterface,
        request,
    );
    await util.verifyUser(userId, deviceId);

    logger.info(`Accepting ${userId}'s invitation to co-organize ${data.eventId}.`);

    const db = getFirestore();

    const eventCollection = db.collection("events") as EventDocCollection;
    const eventRef = eventCollection.doc(data.eventId);
    const event = await eventRef.get();

    if (!event.exists) {
        throw new HttpsError("not-found", "The event does not exist.");
    }

    const userCollection = db.collection("users") as UserDocCollection;
    const userRef = userCollection.doc(userId);
    const user = await userRef.get();

    // Check for existence of entrant
    if (user.data()?.entrant === null) {
		throw new HttpsError("not-found", "The user is not an entrant.");
	}
    else {

        await db
        .collection("users")
        .doc(userId)
        .update({
          "entrant.coOrganizedEvents": FieldValue.arrayUnion(data.eventId),
        });

        logger.info("User became co-organizer successfully");

    }

    return {};
}