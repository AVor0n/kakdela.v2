import { Editor, useEditorState } from '@tiptap/react';
import { ColorPicker } from '../ColorPicker/ColorPicker';
import style from './TextColor.module.css';

const TEXT_COLORS = [
    { value: '#958DF1', className: style.textPurple, letter: 'A' },
    { value: '#F98181', className: style.textRed, letter: 'A' },
    { value: '#FBBC88', className: style.textOrange, letter: 'A' },
    { value: '#000000', className: '', letter: 'A' },
    { value: '#70CFF8', className: style.textBlue, letter: 'A' },
    { value: '#94FADB', className: style.textTeal, letter: 'A' },
    { value: '#B9F18D', className: style.textGreen, letter: 'A' },
];

interface Props {
    editor: Editor;
}

export function TextColor({ editor }: Props) {
    const state = useEditorState({
        editor,
        selector: (ctx) => ({
            color: ctx.editor.getAttributes('textStyle').color,
            activeColors: TEXT_COLORS.map(({ value }) => ctx.editor.isActive('textStyle', { color: value })),
        }),
    });

    return (
        <ColorPicker
            buttonLabel='A'
            colors={TEXT_COLORS.map((c, i) => ({ ...c, isActive: state.activeColors[i] }))}
            onSet={(color) => editor.chain().focus().setColor(color).run()}
            onReset={() => editor.chain().focus().unsetColor().run()}
        >
            <input
                type='color'
                onInput={(e) => editor.chain().focus().setColor(e.currentTarget.value).run()}
                value={state.color ?? '#000000'}
                className={style.colorInput}
            />
        </ColorPicker>
    );
}
