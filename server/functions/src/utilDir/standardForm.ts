import * as z from "zod";

export function standardForm<Z extends z.ZodObject>(zodInterface: Z) {
  return z.object({
    userId: z.uuid(),
    deviceId: z.uuid(),
    data: zodInterface,
  });
}
