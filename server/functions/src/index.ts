/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import { setGlobalOptions } from "firebase-functions";
import { onRequest, onCall } from "firebase-functions/https";
import type { Request, Response } from "express";
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
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 5 });

initializeApp();

export const health = onRequest({ maxInstances: 1 }, async (_request: Request, _response: Response) => {
  const now = Date.now();
  logger.info("Got health", { now });
  _response.status(200).json({
    time: now
  });
});


const authInterface = z.object({
  userId: z.string().uuid,
  deviceId: z.string().uuid,
});

type Role = "entrant" | "admin" |"organizer";

async function verifyUser(userId: string, deviceId: string) {
  const db = getFirestore();

  const userDoc = await db.collection("users").doc(userId).get();

  if (!userDoc.exists) {
    throw {code:404, message: "User does not exist"};
  }

  const data = userDoc.data()!;

  if (data.deviceId !== deviceId) {
    throw {code:402, message:"Device ID do not match"};
  }

  return data;
}

async function requireRole(userData: any, role: Role) {
  if (!userData[role] || userData[role] === null) {
    throw {code:403, message: `User is not an ${role}`};
  }
}

function handleError(_response:any, error:any) {
  if (error.code && error.message) {
    _response.status(error.code).json({error:error.message});
  } else {
    logger.error("Unhandled Error", error);
    _response.status(500).json({error: "Internal Server Error"});
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

export const createUser = onRequest({ maxInstances: 1 }, async (_request, _response) => {
  try {
    const result = createUserInterface.safeParse(_request.body);
    if (!result.success) {
      _response.status(400).json({error: "Missing Required Fields"});
      return;
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
    _response.status(201).json({userId});
  } catch (error) {
    handleError(_response, error);
  }
});

const getUserInterface = z.object({
    userId: z.string().uuid(),
    deviceId: z.string().uuid(),
});

export const getUser = onRequest({ maxInstances: 1}, async (_request, _response) => {
  try {
    const result = getUserInterface.safeParse(_request.body);
    if (!result.success) {
      _response.status(400).json({error: "Missing Required Fields"});
      return;
    }

    const {userId, deviceId} = result.data;
    const userData = await verifyUser(userId, deviceId);

    logger.info('User found', { userId });
    _response.status(200).json(userData);
  } catch (error) {
    handleError(_response, error);
  }
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

export const createEvent = onRequest({ maxInstances: 1}, async (_request,_response) => {
  try{
    const result = createEventInterface.safeParse(_request.body);
    if (!result.success) {
      _response.status(400).json({ error: "Missing Required Fields"});
      return;
    }
  
    const {userId, deviceId, data} = result.data;
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
    _response.status(201).json({
      eventId,
    });
  } catch(error) {
    handleError(_response, error);
  }
});

const getEventInterface = z.object({
    userId: z.string().uuid(),
    deviceId: z.string().uuid(),
    data: z.object({
      eventId: z.string().uuid(),
    }),
});

export const getEvent = onRequest({ maxInstances: 1 }, async (_request, _response) => {
  try{
    const result = getEventInterface.safeParse(_request.body);
    if (!result.success) {
        _response.status(400).json({ error: "Missing Required Fields"});
        return;
    }

    const { userId, deviceId, data } = result.data;
    const { eventId } = data;
    
    await verifyUser(userId, deviceId);

    const db = getFirestore();
    const eventDoc = await db.collection('events').doc(eventId).get();

    if (!eventDoc.exists) {
        _response.status(400).json({error: "Event not found"});
    }

    logger.info('Event found', { eventId });
    _response.status(200).json(eventDoc.data());

  } catch(error) {
    handleError(_response, error);
  }

});

const createImageInterface = z.object({
  userId: z.string().uuid(),
  deviceId: z.string().uuid(),
  data: z.object({
    imageData: z.base64(),
  }),
});

export const createImage = onRequest({ maxInstances: 1 }, async (_request, _response) => {
  try{

    const result = createImageInterface.safeParse(_request.body);
    if (!result.success) {
      _response.status(400).json({error:"Missing Required Fields"});
      return;
    }

    const {userId, deviceId, data} = result.data;
    const {imageData} = data;

    await verifyUser(userId, deviceId);

    const imageId = uuidv4();
    const db = getFirestore();
    await db.collection('images').doc(imageId).set({
      imageData,
    })

    logger.info("Image Created", {imageId});

    _response.status(201).json({ imageId });

  } catch(error) {
    handleError(_response, error);
  }

});

const getImageInterface = z.object({
  userId: z.string().uuid(),
  deviceId: z.string().uuid(),
  data: z.object({
    imageId: z.string().uuid(),
  }),
});

export const getImage = onRequest({ maxInstances: 1}, async (_request, _response) => {
  try{
    const result = getImageInterface.safeParse(_request.body);
    if (!result.success) {
      _request.status(400).json({error: "Missing Required Fields"});
      return;
    }
    
    const {userId, deviceId, data} = result.data;
    const {imageId} = data;

    await verifyUser(userId, deviceId);

    const db = getFirestore();

    const imageDoc = await db.collection("images").doc(imageId).get();

    if (!imageDoc.exists) {
      _response.status(404).json({error:"Image not found"});
      return;
    }
  
    logger.info("Found image", {imageId});
    _response.status(200).json(imageDoc.data());

  } catch(error) {
    handleError(_response, error);
  }

});


