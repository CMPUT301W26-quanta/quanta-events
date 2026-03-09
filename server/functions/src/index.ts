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
import { getFirestore, FieldValue } from 'firebase-admin/firestore'

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

export const health = onCall({ maxInstances: 1 }, async (_request) => {
  const now = Date.now();
  logger.info("Got health", { now });
  return {
    time: now,
  };
});

type Role = "entrant" | "admin" |"organizer";

async function verifyUser(userId: string, deviceId: string) {
  const db = getFirestore();

  const userDoc = await db.collection("users").doc(userId).get();

  if (!userDoc.exists) {
    throw new HttpsError("not-found", "User does not exist");
  }

  const data = userDoc.data()!;

  if (data.deviceId !== deviceId) {
    throw new HttpsError("unauthenticated", "Device ID does not match");
  }
  return data;
}

async function requireRole(userData: any, role: Role) {
  if (!userData[role] || userData[role] === null) {
    throw new HttpsError("permission-denied", `User is not an ${role}`);
  }
}

const createUserInterface = z.object({
  deviceId : z.string().uuid(),
  name: z.string().optional(),
  email: z.string().email().optional(),
  phone: z.number().int().optional(),
  receiveNotifications: z.boolean().optional(),
  isEntrant: z.boolean().optional(),
  isOrganizer: z.boolean().optional(),
  isAdmin: z.boolean().optional(),
});

export const createUser = onCall({ maxInstances: 1 }, async (request) => {
  const result = createUserInterface.safeParse(request.data);

  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
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
  return { userId };

});

const getUserInterface = z.object({
    userId: z.string().uuid(),
    deviceId: z.string().uuid(),
});

export const getUser = onCall({ maxInstances: 1 }, async (request) => {

  const result = getUserInterface.safeParse(request.data);

  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const { userId, deviceId } = result.data;
  const userData = await verifyUser(userId, deviceId);

  logger.info('User found', { userId });
  return userData;

});


const createEventInterface = z.object({
    userId: z.string().uuid(),
    deviceId: z.string().uuid(),
    data: z.object({
        registrationStartTime: z.iso.datetime({ offset: true }),
        registrationEndTime: z.iso.datetime({ offset: true }),
        eventName: z.string(),
        eventDescription: z.string(),
        location: z.string(),
        registrationLimit: z.number().int().optional(),
        imageId: z.string().uuid().optional(),
    }),
});

export const createEvent = onCall({ maxInstances: 1 }, async (request) => {

  const result = createEventInterface.safeParse(request.data);
  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const { userId, deviceId, data } = result.data;

  const { registrationStartTime, registrationEndTime, eventName, eventDescription, location, registrationLimit, imageId } = data;

  const userData = await verifyUser(userId, deviceId);

  await requireRole(userData, "organizer");

  const eventId = uuidv4();

  const db = getFirestore();

  await db.collection('events').doc(eventId).set({
    organizer: userId,
    waitList: [],
    cancelledList: [],
    finalList: [],
    registrationStartTime,
    registrationEndTime,
    eventName,
    eventDescription,
    location,
    registrationLimit: registrationLimit || null,
    imageId: imageId || null,

  });

  await db.collection('users').doc(userId).update({
    "organizer.createdEvents": FieldValue.arrayUnion(eventId),
  });

  logger.info('Created event', { eventId });
  return { eventId };
});


const getEventInterface = z.object({
    userId: z.string().uuid(),
    deviceId: z.string().uuid(),
    data: z.object({
      eventId: z.string().uuid(),
    }),
});

export const getEvent = onCall({ maxInstances: 1 }, async (request) => {
  const result = getEventInterface.safeParse(request.data);
  if (!result.success) {
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  const { userId, deviceId, data } = result.data;

  const { eventId } = data;

  await verifyUser(userId, deviceId);

  const db = getFirestore();

  const eventDoc = await db.collection('events').doc(eventId).get();

  if (!eventDoc.exists) {
    throw new HttpsError("not-found", "Event not found");
  }

  logger.info('Event found', { eventId });
  return eventDoc.data();
});

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

  await db.collection('images').doc(imageId).set({ imageData });

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


