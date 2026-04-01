import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import z from "zod";
import * as util from "../util";
import { QueryDocumentSnapshot, DocumentData } from "firebase-admin/firestore";

const getUsersInterface = util.standardForm(
    z.object({
        userIds: z.array(z.uuid()),
    })
);

export async function getUsers(
    request: CallableRequest,
): Promise<ExternalUser[]> {
    const { userId, deviceId, data } = util.parseInterface(getUsersInterface, request);

    const userData = await util.verifyUser(userId, deviceId);
    util.requireRole(userData, "organizer");

    const { userIds } = data;

    if (userIds.length === 0) {
        return [];
    }

    const db = getFirestore();

    const userDocs = await Promise.all(
        userIds.map(uid => db.collection("users").doc(uid).get())
    );

    logger.info("Got users", { count: userDocs.length });

    return userDocs
        .filter(doc => doc.exists)
        .map(doc => util.userDocToExternalUser(doc as QueryDocumentSnapshot<DocumentData>));
}
