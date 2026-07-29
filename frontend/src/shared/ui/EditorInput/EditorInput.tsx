import { useEditor, EditorContent, EditorContext } from '@tiptap/react';
import { BubbleMenu } from '@tiptap/react/menus';
import { useMemo } from 'react';
import { Menu } from './components/Menu/Menu';
import StarterKit from '@tiptap/starter-kit';
import Highlight from '@tiptap/extension-highlight';
import Placeholder from '@tiptap/extension-placeholder';
import { Color, TextStyle } from '@tiptap/extension-text-style';
import Heading from '@tiptap/extension-heading';
import style from './EditorInput.module.css';
interface Props {
    value: string;
    onChange: (_content: string) => void;
    onBlur: () => void;
    placeholder?: string;
    isHeading?: boolean;
    isTextColor?: boolean;
    isMarkColor?: boolean;
    className?: string;
}

export function EditorInput({
    onBlur,
    placeholder,
    value,
    onChange,
    isHeading,
    isTextColor,
    isMarkColor,
    className,
}: Props) {
    const editor = useEditor({
        extensions: [
            StarterKit,
            Highlight.configure({ multicolor: true }),
            Placeholder.configure({
                placeholder: placeholder,
            }),
            TextStyle,
            Color,
            Heading.configure({
                levels: [1, 2, 3],
            }),
        ],
        content: value,
        onUpdate: ({ editor }) => onChange(editor.getHTML()),
        onBlur,
    });

    const providerValue = useMemo(
        () => ({
            editor,
        }),
        [editor],
    );

    return (
        <EditorContext.Provider value={providerValue}>
            <EditorContent className={className ?? style.tiptap} value={value} editor={editor} />
            <BubbleMenu editor={editor}>
                <Menu editor={editor} isHeading={isHeading} isMarkColor={isMarkColor} isTextColor={isTextColor} />
            </BubbleMenu>
        </EditorContext.Provider>
    );
}
