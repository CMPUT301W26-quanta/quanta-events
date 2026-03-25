import { logger } from "firebase-functions";
import { CallableRequest, HttpsError } from "firebase-functions/https";
import * as z from "zod";

export function parseInterface<Z extends z.ZodObject>(
  zodInterface: Z,
  source: CallableRequest,
): z.output<Z> {
  const result = zodInterface.safeParse(source.data);

  if (!result.success) {
    logger.error(result.error);
    throw new HttpsError("invalid-argument", "Missing Required Fields");
  }

  return result.data;
}
