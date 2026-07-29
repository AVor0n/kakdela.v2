interface Props {
    className?: string;
    html: string;
}

export function HTMLRender({ className, html }: Props) {
    return <div className={className} dangerouslySetInnerHTML={{ __html: html }} />;
}
