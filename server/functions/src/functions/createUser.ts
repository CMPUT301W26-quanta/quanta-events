import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";

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

export async function createUser(request: CallableRequest<any>) {
  const result = createUserInterface.safeParse(request.data);

  if (!result.success) {
    logger.error(result.error);
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const {
    deviceId,
    name,
    email,
    phone,
    receiveNotifications,
    isEntrant,
    isOrganizer,
    isAdmin,
  } = result.data;

  const userId = uuidv4();

  const db = getFirestore();
  await db
    .collection("users")
    .doc(userId)
    .set({
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

  logger.info("Created user", { userId });
  return { userId };
}
