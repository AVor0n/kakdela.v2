interface Props {
    className?: string;
    html: string;
}

export function HTMLRender({ className, html }: Props) {
    // Для заполнения пустых тегов, что бы они считывлись как \n
    const processedHtml = html.replace(/<(\w+)([^>]*)>\s*<\/\1>/g, '<$1$2>&nbsp;</$1>');
    return <div className={className} dangerouslySetInnerHTML={{ __html: processedHtml }} />;
}
