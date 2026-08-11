export const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function shortenUuid(uuid: string): string {
    return `${uuid.slice(0, 8)}…${uuid.slice(-4)}`;
}
