import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { getFirestore, CollectionReference } from "firebase-admin/firestore";

const updateRolesInterface = util.standardForm(
    z.object({
        targetUserId: z.uuid(),
        isEntrant: z.boolean(),
        isOrganizer: z.boolean(),
        isAdmin: z.boolean(),
    }),
);

export async function updateRoles(request: CallableRequest) {
    const { userId, deviceId, data } = util.parseInterface(
        updateRolesInterface,
        request,
    );

    const { targetUserId, isEntrant, isOrganizer, isAdmin } = data;

    const userData =await util.verifyUser(userId, deviceId);
    util.requireRole(userData, "admin");

    const db = getFirestore();

    const userDocuments = db.collection("users") as CollectionReference<
            UserDocument,
            UserDocument
        >;
    const userDoc = (await userDocuments.doc(targetUserId).get()).data();

    // Check if the user currently has these roles
    let currentEntrant = userDoc?.entrant;
    let currentOrganizer = userDoc?.organizer;
    let currentAdmin = userDoc?.admin;

    // Granted entrant permission
    if (isEntrant && currentEntrant == null) {
        userDoc!.entrant = {enteredEvents: [], history: [], undismissedNotifications: [], receiveNotifications: true, coOrganizedEvents: []};  // Reset and make true as default
    }
    // Banned from being entrant
    else if (!isEntrant && currentEntrant !== null) {
        userDoc!.entrant = null;
    }

    if (isOrganizer && currentOrganizer == null) {
        userDoc!.organizer = { createdEvents: [], sentNotifications: [] };
    }
    else if (!isOrganizer && currentOrganizer !== null) {
        userDoc!.organizer = null;
    }

    if (isAdmin && currentAdmin == null) {
        userDoc!.admin = {};
    }
    else if (!isAdmin && currentAdmin !== null) {
        userDoc!.admin = null;
    }

    currentEntrant = userDoc?.entrant;
    currentOrganizer = userDoc?.organizer;
    currentAdmin = userDoc?.admin;

    const updates: Record<string, any> = {
        entrant: currentEntrant,
        organizer: currentOrganizer,
        admin: currentAdmin,
    };

    await db.collection("users").doc(targetUserId).update(updates);
    logger.info("Updated roles for user", { targetUserId });
}