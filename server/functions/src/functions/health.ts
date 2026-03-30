import { logger } from "firebase-functions";
import { CallableRequest } from "firebase-functions/https";

export async function health(
	_request: CallableRequest<Record<any, never>>,
): Promise<{ time: number }> {
	const now = Date.now();
	logger.info("Got health", { now });
	return {
		time: now,
	};
}
