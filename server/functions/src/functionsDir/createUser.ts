import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";
import * as util from "../util";
import { v4 as uuidv4 } from "uuid";
import { getFirestore } from "firebase-admin/firestore";

const createUserInterface = z.object({
  deviceId: z.uuid(),
  name: z.string().optional(),
  email: z.email().optional(),
  phone: z.string().optional(),
  receiveNotifications: z.boolean().optional(),
  isEntrant: z.boolean().optional(),
  isOrganizer: z.boolean().optional(),
  isAdmin: z.boolean().optional(),
});

export async function createUser(request: CallableRequest) {
  const {
    deviceId,
    name,
    email,
    phone,
    receiveNotifications,
    isEntrant,
    isOrganizer,
    isAdmin,
  } = util.parseInterface(createUserInterface, request);

  const userId = uuidv4();

  const db = getFirestore();
  try {
    await db
      .collection("users")
      .doc(userId)
      .create({
        deviceId,
        ...(name && { name }),
        ...(email && { email }),
        ...(phone && { phone }),
        entrant: isEntrant
          ? {
              enteredEvents: [],
              receiveNotifications: receiveNotifications ?? false,
            }
          : null,
        organizer: isOrganizer
          ? { createdEvents: [], sentNotifications: [] }
          : null,
        admin: isAdmin ? {} : null,
      });
  } catch (_) {
    throw new HttpsError("already-exists", "User already exists");
  }

  logger.info("Created user", { userId });
  return { userId };
}
