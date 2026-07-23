import { useEditor, EditorContent, EditorContext } from '@tiptap/react';
import { BubbleMenu } from '@tiptap/react/menus';
import { useMemo } from 'react';
import { Menu } from './components/Menu';
import StarterKit from '@tiptap/starter-kit';
import Highlight from '@tiptap/extension-highlight';
import Placeholder from '@tiptap/extension-placeholder';
import './EditorInput.css';

interface Props {
    value: string;
    onChange: (content: string) => void;
    onBlur: () => void;
    placeholder?: string;
}

export function EditorInput({ onBlur, placeholder, value, onChange }: Props) {
    const editor = useEditor({
        extensions: [
            StarterKit,
            Highlight.configure({ multicolor: true }),
            Placeholder.configure({
                placeholder: placeholder,
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
            <EditorContent value={value} editor={editor} />
            <BubbleMenu editor={editor}>
                <Menu editor={editor} />
            </BubbleMenu>
        </EditorContext.Provider>
    );
}
