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
import * as functions from "./functions";
import * as util from "./util";
import { v4 as uuidv4 } from "uuid";

import { initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onCall({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 5 });

initializeApp();

export const health = onCall({ maxInstances: 1 }, functions.health);

export const createUser = onCall({ maxInstances: 1 }, functions.createUser);

export const getUser = onCall({ maxInstances: 1 }, functions.getUser);

export const createEvent = onCall({ maxInstances: 1 }, functions.createEvent);

export const getEvent = onCall({ maxInstances: 1 }, functions.getEvent);

const createImageInterface = z.object({
  userId: z.string().uuid(),
  deviceId: z.string().uuid(),
  data: z.object({
    imageData: z.base64(),
  }),
});

export const createImage = onCall({ maxInstances: 1 }, async (request) => {
  const result = createImageInterface.safeParse(request.data);
  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const { userId, deviceId, data } = result.data;

  const { imageData } = data;

  await verifyUser(userId, deviceId);

  const imageId = uuidv4();

  const db = getFirestore();

  await db.collection("images").doc(imageId).set({ imageData });

  logger.info("Image Created", { imageId });
  return { imageId };
});

const getImageInterface = z.object({
  userId: z.string().uuid(),
  deviceId: z.string().uuid(),
  data: z.object({
    imageId: z.string().uuid(),
  }),
});

export const getImage = onCall({ maxInstances: 1 }, async (request) => {
  const result = getImageInterface.safeParse(request.data);

  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const { userId, deviceId, data } = result.data;
  const { imageId } = data;

  await verifyUser(userId, deviceId);

  const db = getFirestore();

  const imageDoc = await db.collection("images").doc(imageId).get();

  if (!imageDoc.exists) {
    throw new HttpsError("not-found", "Image not found");
  }

  logger.info("Found image", { imageId });
  return imageDoc.data();
});
