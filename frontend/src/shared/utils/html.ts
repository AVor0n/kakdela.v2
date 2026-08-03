export function htmlToText(html: string) {
    return new DOMParser().parseFromString(html, 'text/html').body.textContent ?? '';
}

export function hasHtmlText(html: string) {
    return htmlToText(html).trim().length > 0;
}
