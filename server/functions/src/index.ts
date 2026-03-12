/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import { setGlobalOptions } from "firebase-functions";
import { onCall } from "firebase-functions/https";
import * as functions from "./functions";

import { initializeApp } from "firebase-admin/app";

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

export const createImage = onCall({ maxInstances: 1 }, functions.createImage);

export const getImage = onCall({ maxInstances: 1 }, functions.getImage);

export const deleteUser = onCall({ maxInstances: 1 }, functions.deleteUser);

export const updateUser = onCall({ maxInstances: 1 }, functions.updateUser);

export const createNotification = onCall(
  { maxInstances: 1 },
  functions.createNotification
);

export const getNotification = onCall(
  { maxInstances: 1 },
  functions.getNotification
);

export const getEvents = onCall({ maxInstances: 1 }, functions.getEvents);

export const setToken = onCall({ maxInstances: 1 }, functions.setToken);
