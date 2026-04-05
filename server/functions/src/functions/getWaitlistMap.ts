import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";

interface WaitlistEntry {
    userId: string;
    name: string | null;
    email: string | null;
    phone: string | null;
    latitude: number;
    longitude: number;
    accuracyM: number | null;
    joinedAt: FirebaseFirestore.Timestamp;
}

const getWaitlistMapInterface = util.standardForm(
    z.object({
        eventId: z.uuid(),
    })
);

export async function getWaitlistMap(
    request: CallableRequest
): Promise<{ entries: WaitlistEntry[] }> {
    const { userId, deviceId, data } = util.parseInterface(
        getWaitlistMapInterface,
        request
    );
    const { eventId } = data;

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

    const entriesSnapshot = await db
        .collection("events")
        .doc(eventId)
        .collection("waitlistEntries")
        .get();

    if (entriesSnapshot.empty) {
        logger.info("No waitlist entries", { eventId });
        return { entries: [] };
    }

    const userRefs = entriesSnapshot.docs.map((doc) =>
        db.collection("users").doc(doc.data().userId)
    );
    const userDocs = await db.getAll(...userRefs);
    const userMap = new Map(
        userDocs.filter((d) => d.exists).map((d) => [d.id, d.data() as any])
    );

    const entries: WaitlistEntry[] = entriesSnapshot.docs.map((doc) => {
        const d = doc.data();
        const user = userMap.get(d.userId);
        return {
            userId: d.userId,
            name: user?.name ?? null,
            email: user?.email ?? null,
            phone: user?.phone ?? null,
            latitude: d.latitude,
            longitude: d.longitude,
            accuracyM: d.accuracyM ?? null,
            joinedAt: d.joinedAt,
        };
    });

    logger.info("Got waitlist map entries", { eventId, count: entries.length });
    return { entries };
}
