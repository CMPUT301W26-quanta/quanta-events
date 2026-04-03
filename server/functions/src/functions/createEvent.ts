import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import { v4 as uuidv4 } from "uuid";
import * as z from "zod";
import * as util from "../util";

const createEventInterface = util.standardForm(
	z.object({
		registrationStartTime: z.iso.datetime({ offset: true }),
		registrationEndTime: z.iso.datetime({ offset: true }),
		eventTime: z.iso.datetime({ offset: true }),
		eventName: z.string(),
		eventDescription: z.string(),
		eventCategory: z.string().nullable(),
		eventGuidelines: z.string().nullable(),
		geolocation: z.boolean(),
		eventCapacity: z.int().positive(),
		location: z.string(),
		registrationLimit: z.int().positive().nullable(),
		imageId: z.uuid().nullable(),
		isPrivate: z.boolean(),
	}),
);

export async function createEvent(request: CallableRequest) {
	const { userId, deviceId, data } = util.parseInterface(
		createEventInterface,
		request,
	);

	const {
		registrationStartTime,
		registrationEndTime,
		eventTime,
		eventName,
		eventDescription,
		eventGuidelines,
		eventCategory,
		eventCapacity,
		geolocation,
		location,
		registrationLimit,
		imageId,
		isPrivate,
	} = data;

	const userData = await util.verifyUser(userId, deviceId);

	await util.requireRole(userData, "organizer");

	const eventId = uuidv4();

	const db = getFirestore();

	try {
		await db
			.collection("events")
			.doc(eventId)
			.create(
				util.enforceFull<EventDocument>({
					eventId,
					organizer: userId,
					waitList: [],
					selectedList: [],
					rejectedList: [],
					cancelledList: [],
					finalList: [],
					registrationStartTime: util.toTimestamp(registrationStartTime),
					registrationEndTime: util.toTimestamp(registrationEndTime),
					eventTime: util.toTimestamp(eventTime),
					eventName,
					eventDescription,
					eventGuidelines,
					location,
					eventCategory,
					geolocation,
					eventCapacity,
					registrationLimit,
					imageId,
					isPrivate,
				}),
			);
	} catch (_) {
		throw new HttpsError("already-exists", "Event already exists");
	}

	await db
		.collection("users")
		.doc(userId)
		.update({
			"organizer.createdEvents": FieldValue.arrayUnion(eventId),
		});

	logger.info("Created event", { eventId });
	return { eventId };
}
