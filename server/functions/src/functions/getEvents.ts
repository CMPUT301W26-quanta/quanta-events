import { CallableRequest } from "firebase-functions/https";
import * as util from "../util";
import * as z from "zod";
import {
	CollectionReference,
	getFirestore,
	Query,
	Timestamp,
} from "firebase-admin/firestore";
import Fuse from "fuse.js";
import { logger } from "firebase-functions";

const getEventsInterface = util.standardForm(
	z.object({
		max: z.number().positive(),
		startFrom: z.uuid().nullable(),
		filter: z.object({
			fetch: z.enum(["all", "created", "available", "in", "history"]),
			startDate: z.iso.datetime({ offset: true }).nullable(),
			endDate: z.iso.datetime({ offset: true }).nullable(),
			search: z.string().nullable(),
		}),
		sortBy: z
			.enum(["registrationEnd", "registrationStart", "name"])
			.default("registrationEnd"),
	}),
);

export async function getEvents(
	request: CallableRequest,
): Promise<util.ConvertAllTimestamps<EventDocument>[]> {
	logger.debug("Ran");
	const { userId, deviceId, data } = util.parseInterface(
		getEventsInterface,
		request,
	);

	logger.debug(userId, deviceId, data);

	const { max, startFrom, filter, sortBy } = data;

	const user = await util.verifyUser(userId, deviceId);

	const db = getFirestore();
	const ref = db.collection("events") as CollectionReference<
		EventDocument,
		EventDocument
	>;

	let query: Query<EventDocument, EventDocument>;
	if (filter.fetch === "all") {
		util.requireRole(user, "admin");
		query = ref;
	} else if (filter.fetch === "created") {
		const organizer = util.requireRole(user, "organizer");
		query = ref.where(
			"eventId",
			"in",
			(organizer.organizer.createdEvents || []).concat(["NOTHING"]),
		);
	} else {
		const entrant = util.requireRole(user, "entrant");
		if (filter.fetch === "in") {
			query = ref.where(
				"eventId",
				"in",
				(entrant.entrant.enteredEvents || []).concat(["NOTHING"]),
			);
		} else if (filter.fetch === "history") {
			query = ref.where(
				"eventId",
				"in",
				(entrant.entrant.history || []).concat(["NOTHING"]),
			);
		} else {
			query = ref
				.where(
					"eventId",
					"not-in",
					(entrant.entrant.enteredEvents || []).concat(["NOTHING"]),
				)
				.where("registrationEndTime", ">=", Timestamp.now());
		}
	}

	if (filter.startDate !== null) {
		query = query.where(
			"registrationEndTime",
			">=",
			util.toTimestamp(filter.startDate),
		);
	}

	if (filter.endDate !== null) {
		query = query.where(
			"registrationEndTime",
			"<=",
			util.toTimestamp(filter.endDate),
		);
	}

	const queryResult = await query.get();

	if (queryResult.empty) {
		return [];
	}

	const docs: EventDocument[] = queryResult.docs.map((doc) => doc.data());

	let docResults: EventDocument[];
	if (filter.search !== null && filter.search.trim().length > 0) {
		docResults = new Fuse(docs, {
			keys: [
				{ name: "eventName", getFn: (data) => data.eventName },
				{
					name: "eventDescription",
					getFn: (data) => data.eventDescription,
				},
			],
		})
			.search(filter.search)
			.map((result) => result.item);
	} else {
		docResults = docs;
		switch (sortBy) {
			case "registrationEnd":
				docResults.sort((a, b) =>
					sort(a.registrationEndTime, b.registrationEndTime),
				);
				break;
			case "registrationStart":
				docResults.sort((a, b) =>
					sort(a.registrationStartTime, b.registrationStartTime),
				);
				break;
			case "name":
				docResults.sort((a, b) => sort(a.eventName, b.eventName));
				break;
		}
	}

	docResults = docResults
		.slice(docResults.findIndex((data) => data.eventId === startFrom) + 1)
		.slice(0, max);

	logger.debug("Got result");

	return docResults.map((data) =>
		Object.assign(data, {
			registrationStartTime: util.fromTimestamp(data.registrationStartTime),
			registrationEndTime: util.fromTimestamp(data.registrationEndTime),
			eventTime: util.fromTimestamp(data.eventTime),
		}),
	);
}

function sort<T extends string | Timestamp>(a: T, b: T): number {
	if (a < b) {
		return -1;
	} else if (a > b) {
		return 1;
	} else {
		return 0;
	}
}
