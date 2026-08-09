import type { ScheduleType } from '@/shared/types/Notification.type';

export const NOTIFICATION_TYPE_SELECT_DATA = [
    { text: 'Ежедневно', value: 'DAILY' },
    { text: 'Еженедельно', value: 'WEEKLY' },
    { text: 'Ежемесячно', value: 'MONTHLY' },
];

export function getScheduleTypeByValue(scheduleType: ScheduleType) {
    return NOTIFICATION_TYPE_SELECT_DATA.find((st) => st.value === scheduleType);
}

export const DAYS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
export type Day = (typeof DAYS)[number];

export const DAYS_VALUE: Record<Day, number> = {
    Пн: 0b00000001,
    Вт: 0b00000010,
    Ср: 0b00000100,
    Чт: 0b00001000,
    Пт: 0b00010000,
    Сб: 0b00100000,
    Вс: 0b01000000,
};

export const VALUE_DAYS = Object.fromEntries(Object.entries(DAYS_VALUE).map(([day, value]) => [value, day])) as Record<
    number,
    Day
>;

export function formatHM(value: string): string {
    const [hh, mm] = value.split(':').map(Number);

    const date = new Date();
    date.setHours(hh, mm, 0, 0);
    return `${date.getHours()}:${date.getMinutes() > 9 ? date.getMinutes() : '0' + date.getMinutes()}`;
}

export function getBinNumber(n: number): string {
    return n.toString(2).padStart(8, '0');
}

export function getDaysFromNumber(n: number): Day[] {
    if (n > 127) return [];
    const binNum = getBinNumber(n);
    const result: Day[] = [];
    for (let i = 1; i < binNum.length; i++) {
        if (binNum[i] === '0') continue;
        const value = parseInt('0'.repeat(i) + '1' + '0'.repeat(7 - i), 2);
        result.push(VALUE_DAYS[value]);
    }

    return result;
}
