import { Editor, useEditorState } from '@tiptap/react';
import { ColorPicker } from '../ColorPicker/ColorPicker';
import style from './MarkColor.module.css';

const HIGHLIGHT_COLORS = [
    { value: '#ffc078', className: style.orange },
    { value: '#8ce99a', className: style.green },
    { value: '#74c0fc', className: style.blue },
    { value: '#b197fc', className: style.purple },
    { value: 'red', className: style.red },
    { value: '#ffa8a8', className: style.rose },
];

interface Props {
    editor: Editor;
}

export function MarkColor({ editor }: Props) {
    const state = useEditorState({
        editor,
        selector: (ctx) => ({
            isHighlight: ctx.editor.isActive('highlight'),
            activeColors: HIGHLIGHT_COLORS.map(({ value }) => ctx.editor.isActive('highlight', { color: value })),
        }),
    });

    return (
        <ColorPicker
            buttonLabel='M'
            colors={HIGHLIGHT_COLORS.map((c, i) => ({ ...c, isActive: state.activeColors[i] }))}
            onSet={(color) => editor.chain().focus().toggleHighlight({ color }).run()}
            onReset={() => editor.chain().focus().unsetHighlight().run()}
            isResetDisabled={!state.isHighlight}
        />
    );
}
