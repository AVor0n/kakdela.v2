const LOGIN_MAX_LENGTH = 32;
const EMAIL_MAX_LENGTH = 254;
const PASSWORD_MIN_LENGTH = 8;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_LETTER_REGEX = /[a-zA-Zа-яА-Я]/;
const PASSWORD_DIGIT_REGEX = /[0-9]/;
const PASSWORD_SPECIAL_CHARACTER_REGEX = /[!@#&()–[{}\]:;',?/*~$^+=<>]/;

const PASSWORD_REGEX =
    /^(?=.*[0-9])(?=.*[a-zA-Zа-яА-Я])(?=.*[!@#&()–[{}\]:;',?/*~$^+=<>]).{8,}$/;

export function validateLogin(value: string): string {
    if (!value) {
        return 'Введите логин';
    }

    if (value.length > LOGIN_MAX_LENGTH) {
        return 'Логин не должен быть длиннее 32 символов';
    }

    return '';
}

export function validateEmail(value: string): string {
    if (!value) {
        return 'Введите email';
    }

    if (!EMAIL_REGEX.test(value)) {
        return 'Введите корректный email';
    }

    if (value.length > EMAIL_MAX_LENGTH) {
        return 'Email не должен быть длиннее 254 символов';
    }

    return '';
}

export function validatePassword(value: string): string {
    if (!value) {
        return 'Введите пароль';
    }

    if (value.length < PASSWORD_MIN_LENGTH) {
        return 'Пароль должен быть не короче 8 символов';
    }

    if (!PASSWORD_LETTER_REGEX.test(value)) {
        return 'Пароль должен содержать хотя бы одну букву';
    }

    if (!PASSWORD_DIGIT_REGEX.test(value)) {
        return 'Пароль должен содержать хотя бы одну цифру';
    }

    if (!PASSWORD_SPECIAL_CHARACTER_REGEX.test(value)) {
        return 'Пароль должен содержать хотя бы один спецсимвол';
    }

    if (!PASSWORD_REGEX.test(value)) {
        return 'Пароль должен содержать хотя бы один спецсимвол';
    }

    return '';
}

export function validatePasswordConfirmation(value: string, password: string): string {
    if (!value) {
        return 'Повторите пароль';
    }

    if (value !== password) {
        return 'Пароли не совпадают';
    }

    return '';
}
