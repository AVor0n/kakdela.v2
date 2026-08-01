import { Select } from '@hh.ru/magritte-ui';
import { useStaticDataProvider, type StaticDataFetcherItem } from '@hh.ru/magritte-common-data-provider';
import { Editor, useEditorState } from '@tiptap/react';
interface HeadingOption extends StaticDataFetcherItem {
    level: 0 | 1 | 2 | 3; // 0 — обычный текст (paragraph)
}

const HEADING_OPTIONS: HeadingOption[] = [
    { value: 'paragraph', text: 'Текст', level: 0 },
    { value: 'h1', text: 'Заголовок 1', level: 1 },
    { value: 'h2', text: 'Заголовок 2', level: 2 },
    { value: 'h3', text: 'Заголовок 3', level: 3 },
];

export function HeadingSelect({ editor }: { editor: Editor }) {
    const dataProvider = useStaticDataProvider(HEADING_OPTIONS);

    const { current } = useEditorState({
        editor,
        selector: (ctx) => {
            const found = HEADING_OPTIONS.find(
                (opt) => opt.level !== 0 && ctx.editor.isActive('heading', { level: opt.level }),
            );
            return { current: found ?? HEADING_OPTIONS[0] };
        },
    });

    return (
        <Select
            type='radio'
            dataProvider={dataProvider}
            value={current}
            widthEqualToActivator={false}
            dropWidth={195}
            onChange={(value) => {
                if (value.level === 0) {
                    editor.chain().focus().setParagraph().run();
                } else {
                    editor.chain().focus().toggleHeading({ level: value.level }).run();
                }
            }}
        />
    );
}
