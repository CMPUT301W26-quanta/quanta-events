/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import { setGlobalOptions } from "firebase-functions";
import { onCall, HttpsError } from "firebase-functions/https";
import * as logger from "firebase-functions/logger";
import * as z from "zod";
import { v4 as uuidv4 } from 'uuid';

import { initializeApp } from "firebase-admin/app";
// import { getFirestore } from 'firebase-admin/firestore'

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 5 });

initializeApp();

export const health = onCall({ maxInstances: 1 }, async (_request) => {
  const now = Date.now();
  logger.info("Got health", { now });
  return {
    time: now,
  };
});


const createUserInterface = z.object({
  deviceId : z.string(),
  name: z.string().optional(),
  email: z.string().email().optional(),
  phone: z.number().int().optional(),
  receiveNotifications: z.boolean().optional(),
  isEntrant: z.boolean().optional(),
  isOrganizer: z.boolean().optional(),
  isAdmin: z.boolean().optional(),
});

export const createUser = onCall({ maxInstances: 1 }, async (_request) => {
  const result = createUserInterface.safeParse(_request.data);
  if (!result.success) {
    throw new HttpsError('invalid-argument', 'Missing Required Fields');
  }

  const { deviceId, name, email, phone, receiveNotifications, isEntrant, isOrganizer, isAdmin } = result.data;

  const userId = uuidv4();

  const db = getFirestore();
  await db.collection('users').doc(userId).set({
    deviceId,        
    ...(name && { name }),
    ...(email && { email }),
    ...(phone && { phone }),
    entrant: isEntrant ? { enteredEvents: [], receiveNotifications: receiveNotifications ?? false } : null,
    organizer: isOrganizer ? { createdEvents: [], sentNotifications: [] } : null,
    admin: isAdmin ? {} : null,
  });

  logger.info('Created user', { userId });

  return {
    userId
  };
});


const getUserSchema = z.object({
  userId: z.string().uuid(),
});

export const getUser = onCall({ maxInstances: 1}, async (_request) => {
  const result = getUserSchema.safeParse(_request.data);
  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const {userId} = result.data;
  const db = getFirestore();
  const userDoc = await db.collection('users').doc(userId).get();

  if (!userDoc.exists) {
      throw new HttpsError('not-found', 'User not found');
  }

  logger.info('User found', { userId });
  return userDoc.data();
});
